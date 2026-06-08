package cn.edu.nuc.digitalhuman.chat.service.impl;

import cn.edu.nuc.digitalhuman.attraction.entity.Attraction;
import cn.edu.nuc.digitalhuman.attraction.mapper.AttractionMapper;
import cn.edu.nuc.digitalhuman.chat.dto.ChatResponseDto;
import cn.edu.nuc.digitalhuman.chat.entity.Conversation;
import cn.edu.nuc.digitalhuman.chat.entity.Message;
import cn.edu.nuc.digitalhuman.chat.mapper.ConversationMapper;
import cn.edu.nuc.digitalhuman.chat.mapper.MessageMapper;
import cn.edu.nuc.digitalhuman.chat.service.ChatService;
import cn.edu.nuc.digitalhuman.common.exception.ServiceException;
import cn.edu.nuc.digitalhuman.digitalhuman.dto.DigitalHumanConfigDto;
import cn.edu.nuc.digitalhuman.digitalhuman.service.DigitalHumanConfigService;
import cn.edu.nuc.digitalhuman.interaction.entity.InteractionLog;
import cn.edu.nuc.digitalhuman.interaction.mapper.InteractionLogMapper;
import cn.edu.nuc.digitalhuman.interaction.util.SentimentAnalyzer;
import cn.edu.nuc.digitalhuman.kb.service.KbSearchService;
import cn.edu.nuc.digitalhuman.llm.LlmService;
import cn.edu.nuc.digitalhuman.llm.VisionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ChatServiceImpl implements ChatService {

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final LlmService llmService;
    private final VisionService visionService;
    private final AttractionMapper attractionMapper;
    private final DigitalHumanConfigService digitalHumanConfigService;
    private final KbSearchService kbSearchService;
    private final InteractionLogMapper interactionLogMapper;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            你是灵境智游的AI导游"%s"，一个%s的景区智能导游。

            ## 你的身份
            - 你是无锡灵山胜境和拈花湾禅意小镇的官方AI导游
            - 你的名字叫"%s"，是灵山胜境的AI导游
            - 你对灵山胜境和拈花湾的所有景点、文化、历史了如指掌

            ## 语言风格
            - 你的整体风格是：%s
            - 用这种风格与游客交流，保持一致的语感和氛围

            ## 回答规范
            - 回答要简洁明了，重点突出
            - 适当使用表情符号增加亲和力
            - 不知道的信息不要编造，可以引导游客咨询景区服务中心
            - 涉及佛教文化的内容要尊重，用词得体
            - 推荐路线时考虑实际游览时间和体力消耗
            - **使用纯文本回复，不要使用任何Markdown格式（如##标题、**加粗**、-列表、*斜体*、```代码块等）**
            """;

    @Override
    public Conversation createConversation(Long userId, String title) {
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setTitle(title != null ? title : "新对话");
        conversation.setStatus(1);
        conversationMapper.insert(conversation);
        return conversation;
    }

    @Override
    public List<Conversation> getUserConversations(Long userId) {
        return conversationMapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getUserId, userId)
                        .eq(Conversation::getStatus, 1)
                        .orderByDesc(Conversation::getUpdatedAt));
    }

    @Override
    public ChatResponseDto sendMessage(Long userId, Long conversationId, String content) {
        Conversation conversation;
        boolean isNew = false;

        if (conversationId == null) {
            conversation = createConversation(userId, content.length() > 20 ? content.substring(0, 20) + "..." : content);
            isNew = true;
        } else {
            conversation = conversationMapper.selectById(conversationId);
            if (conversation == null || !conversation.getUserId().equals(userId)) {
                throw new ServiceException(404, "对话不存在");
            }
        }

        Message userMsg = new Message();
        userMsg.setConversationId(conversation.getId());
        userMsg.setRole("user");
        userMsg.setContent(content);
        userMsg.setContentType("text");
        messageMapper.insert(userMsg);
        logInteraction(userId, conversation.getId(), "chat", content);

        if (isNew) {
            conversation.setTitle(content.length() > 30 ? content.substring(0, 30) + "..." : content);
            conversationMapper.updateById(conversation);
        }

        List<Message> history = messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversation.getId())
                        .orderByAsc(Message::getCreatedAt));

        List<Map<String, String>> llmMessages = history.stream()
                .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
                .collect(Collectors.toList());

        String attractionContext = buildAttractionContext();
        String kbContext = buildKbContext(content);
        DigitalHumanConfigDto config = digitalHumanConfigService.getDefaultConfig();
        log.info("数字人配置: name={}, personality={}", config.getName(), config.getPersonality());
        String systemPrompt = buildSystemPrompt(config) + "\n\n## 当前景区信息\n" + attractionContext
                + "\n\n## 知识库参考信息\n" + kbContext;
        log.info("发送给大模型的系统提示词:\n{}", systemPrompt);

        String reply = llmService.chat(systemPrompt, llmMessages);

        Message assistantMsg = new Message();
        assistantMsg.setConversationId(conversation.getId());
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(reply);
        assistantMsg.setContentType("text");
        messageMapper.insert(assistantMsg);

        conversation.setUpdatedAt(null);
        conversationMapper.updateById(conversation);

        return ChatResponseDto.builder()
                .conversationId(conversation.getId())
                .userMessage(content)
                .assistantMessage(reply)
                .build();
    }

    @Override
    public void sendMessageStream(Long userId, Long conversationId, String content, SseEmitter emitter) {
        try {
            Conversation conversation;
            boolean isNew = false;

            if (conversationId == null) {
                conversation = createConversation(userId, content.length() > 20 ? content.substring(0, 20) + "..." : content);
                isNew = true;
            } else {
                conversation = conversationMapper.selectById(conversationId);
                if (conversation == null || !conversation.getUserId().equals(userId)) {
                    emitter.completeWithError(new ServiceException(404, "对话不存在"));
                    return;
                }
            }

            // 保存用户消息
            Message userMsg = new Message();
            userMsg.setConversationId(conversation.getId());
            userMsg.setRole("user");
            userMsg.setContent(content);
            userMsg.setContentType("text");
            messageMapper.insert(userMsg);
            logInteraction(userId, conversation.getId(), "chat", content);

            if (isNew) {
                conversation.setTitle(content.length() > 30 ? content.substring(0, 30) + "..." : content);
                conversationMapper.updateById(conversation);
            }

            // 构建上下文
            List<Message> history = messageMapper.selectList(
                    new LambdaQueryWrapper<Message>()
                            .eq(Message::getConversationId, conversation.getId())
                            .orderByAsc(Message::getCreatedAt));

            List<Map<String, String>> llmMessages = history.stream()
                    .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
                    .collect(Collectors.toList());

            String attractionContext = buildAttractionContext();
            String kbContext = buildKbContext(content);
            DigitalHumanConfigDto config = digitalHumanConfigService.getDefaultConfig();
            String systemPrompt = buildSystemPrompt(config) + "\n\n## 当前景区信息\n" + attractionContext
                    + "\n\n## 知识库参考信息\n" + kbContext;

            StringBuilder fullReply = new StringBuilder();

            llmService.chatStream(
                systemPrompt,
                llmMessages,
                // onChunk: 逐 token 推送
                chunk -> {
                    fullReply.append(chunk);
                    try {
                        emitter.send(SseEmitter.event()
                                .name("token")
                                .data(chunk));
                    } catch (Exception e) {
                        log.error("SSE send token failed", e);
                    }
                },
                // onComplete: 保存消息，发送结束事件
                () -> {
                    try {
                        String reply = fullReply.toString();
                        if (!reply.isEmpty()) {
                            Message assistantMsg = new Message();
                            assistantMsg.setConversationId(conversation.getId());
                            assistantMsg.setRole("assistant");
                            assistantMsg.setContent(reply);
                            assistantMsg.setContentType("text");
                            messageMapper.insert(assistantMsg);
                        }

                        conversation.setUpdatedAt(null);
                        conversationMapper.updateById(conversation);

                        emitter.send(SseEmitter.event()
                                .name("done")
                                .data(Map.of("conversationId", conversation.getId())));
                        emitter.complete();
                    } catch (Exception e) {
                        log.error("SSE complete failed", e);
                        emitter.completeWithError(e);
                    }
                },
                // onError
                errorMsg -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("error")
                                .data(errorMsg));
                        emitter.complete();
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                }
            );
        } catch (Exception e) {
            log.error("sendMessageStream failed", e);
            emitter.completeWithError(e);
        }
    }

    @Override
    public List<Message> getMessages(Long conversationId) {
        return messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversationId)
                        .orderByAsc(Message::getCreatedAt));
    }

    @Override
    public void deleteConversation(Long userId, Long conversationId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new ServiceException(404, "对话不存在");
        }
        messageMapper.delete(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversationId));
        conversationMapper.deleteById(conversationId);
    }

    @Override
    public ChatResponseDto recognizeScenic(Long userId, Long conversationId, byte[] imageBytes, String imageUrl) {
        Conversation conversation;
        boolean isNew = false;

        if (conversationId == null) {
            conversation = createConversation(userId, "拍照识景");
            isNew = true;
        } else {
            conversation = conversationMapper.selectById(conversationId);
            if (conversation == null || !conversation.getUserId().equals(userId)) {
                throw new ServiceException(404, "对话不存在");
            }
        }

        // 1. 调用视觉模型识别景点
        String visionPrompt = "请识别这张照片中是灵山胜境或拈花湾的哪个景点？请只回答景点名称，不要回答其他内容。如果无法识别，请回答\"未识别\"。";
        String scenicName = visionService.recognize(imageBytes, visionPrompt);

        // 检查识别结果是否在景点列表中
        if (scenicName == null || scenicName.isBlank() || scenicName.contains("未识别")) {
            scenicName = "未知景点";
        }

        // 2. 保存用户消息
        String userContent = "📷 " + scenicName;
        Message userMsg = new Message();
        userMsg.setConversationId(conversation.getId());
        userMsg.setRole("user");
        userMsg.setContent(userContent);
        userMsg.setContentType("photo");
        userMsg.setImageUrl(imageUrl);
        messageMapper.insert(userMsg);
        logInteraction(userId, conversation.getId(), scenicName.contains("未知") ? "chat" : "checkin", userContent);

        if (isNew) {
            conversation.setTitle("📷 " + scenicName);
            conversationMapper.updateById(conversation);
        }

        // 3. 构建系统提示并调用 LLM 生成讲解
        List<Message> history = messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversation.getId())
                        .orderByAsc(Message::getCreatedAt));

        List<Map<String, String>> llmMessages = history.stream()
                .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
                .collect(Collectors.toList());

        String attractionContext = buildAttractionContext();
        DigitalHumanConfigDto config = digitalHumanConfigService.getDefaultConfig();
        String systemPrompt = buildSystemPrompt(config) + "\n\n## 当前景区信息\n" + attractionContext;

        // 追加一条引导 LLM 讲解的用户消息
        String guidePrompt = scenicName.contains("未知")
                ? "游客拍摄了一张照片，但我没有识别出具体的景点。请引导游客描述照片内容或提供更多信息。"
                : "游客正在参观【" + scenicName + "】，请为游客详细讲解这个景点的历史背景、文化特色和游览亮点。";
        llmMessages.add(Map.of("role", "user", "content", guidePrompt));

        String reply = llmService.chat(systemPrompt, llmMessages);

        // 4. 保存 assistant 消息
        Message assistantMsg = new Message();
        assistantMsg.setConversationId(conversation.getId());
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(reply);
        assistantMsg.setContentType("text");
        messageMapper.insert(assistantMsg);

        conversation.setUpdatedAt(null);
        conversationMapper.updateById(conversation);

        return ChatResponseDto.builder()
                .conversationId(conversation.getId())
                .userMessage(userContent)
                .assistantMessage(reply)
                .build();
    }

    @Override
    public void recognizeScenicStream(Long userId, Long conversationId, byte[] imageBytes, String imageUrl, SseEmitter emitter) {
        try {
            Conversation conversation;
            boolean isNew = false;

            if (conversationId == null) {
                conversation = createConversation(userId, "拍照识景");
                isNew = true;
            } else {
                conversation = conversationMapper.selectById(conversationId);
                if (conversation == null || !conversation.getUserId().equals(userId)) {
                    emitter.completeWithError(new ServiceException(404, "对话不存在"));
                    return;
                }
            }

            // 1. 视觉识别（同步，必须等）
            String visionPrompt = "请识别这张照片中是灵山胜境或拈花湾的哪个景点？请只回答景点名称，不要回答其他内容。如果无法识别，请回答\"未识别\"。";
            String scenicName = visionService.recognize(imageBytes, visionPrompt);
            if (scenicName == null || scenicName.isBlank() || scenicName.contains("未识别")) {
                scenicName = "未知景点";
            }

            // 2. 保存用户消息
            String userContent = "📷 " + scenicName;
            Message userMsg = new Message();
            userMsg.setConversationId(conversation.getId());
            userMsg.setRole("user");
            userMsg.setContent(userContent);
            userMsg.setContentType("photo");
            userMsg.setImageUrl(imageUrl);
            messageMapper.insert(userMsg);
            logInteraction(userId, conversation.getId(), scenicName.contains("未知") ? "chat" : "checkin", userContent);

            if (isNew) {
                conversation.setTitle("📷 " + scenicName);
                conversationMapper.updateById(conversation);
            }

            // 3. 先发送识别结果事件
            emitter.send(SseEmitter.event()
                    .name("scenic")
                    .data(Map.of("scenicName", scenicName, "imageUrl", imageUrl, "userContent", userContent)));

            // 4. 构建上下文
            List<Message> history = messageMapper.selectList(
                    new LambdaQueryWrapper<Message>()
                            .eq(Message::getConversationId, conversation.getId())
                            .orderByAsc(Message::getCreatedAt));

            List<Map<String, String>> llmMessages = history.stream()
                    .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
                    .collect(Collectors.toList());

            String attractionContext = buildAttractionContext();
            DigitalHumanConfigDto config = digitalHumanConfigService.getDefaultConfig();
            String systemPrompt = buildSystemPrompt(config) + "\n\n## 当前景区信息\n" + attractionContext;

            String guidePrompt = scenicName.contains("未知")
                    ? "游客拍摄了一张照片，但我没有识别出具体的景点。请引导游客描述照片内容或提供更多信息。"
                    : "游客正在参观【" + scenicName + "】，请为游客详细讲解这个景点的历史背景、文化特色和游览亮点。";
            llmMessages.add(Map.of("role", "user", "content", guidePrompt));

            // 5. 流式 LLM 生成讲解
            StringBuilder fullReply = new StringBuilder();

            llmService.chatStream(systemPrompt, llmMessages,
                chunk -> {
                    fullReply.append(chunk);
                    try {
                        emitter.send(SseEmitter.event().name("token").data(chunk));
                    } catch (IOException e) {
                        log.error("SSE send token failed", e);
                    }
                },
                () -> {
                    try {
                        String reply = fullReply.toString();
                        if (!reply.isEmpty()) {
                            Message assistantMsg = new Message();
                            assistantMsg.setConversationId(conversation.getId());
                            assistantMsg.setRole("assistant");
                            assistantMsg.setContent(reply);
                            assistantMsg.setContentType("text");
                            messageMapper.insert(assistantMsg);
                        }
                        conversation.setUpdatedAt(null);
                        conversationMapper.updateById(conversation);

                        emitter.send(SseEmitter.event()
                                .name("done")
                                .data(Map.of("conversationId", conversation.getId())));
                        emitter.complete();
                    } catch (Exception e) {
                        log.error("SSE complete failed", e);
                        emitter.completeWithError(e);
                    }
                },
                errorMsg -> {
                    try {
                        emitter.send(SseEmitter.event().name("error").data(errorMsg));
                        emitter.complete();
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                }
            );
        } catch (Exception e) {
            log.error("recognizeScenicStream failed", e);
            emitter.completeWithError(e);
        }
    }

    private String buildAttractionContext() {
        List<Attraction> attractions = attractionMapper.selectList(
                new LambdaQueryWrapper<Attraction>()
                        .eq(Attraction::getStatus, 1)
                        .orderByAsc(Attraction::getSortOrder));

        if (attractions.isEmpty()) {
            return "暂无景点数据。";
        }

        StringBuilder sb = new StringBuilder("以下是当前景区所有景点信息，供你回答游客问题时参考：\n\n");
        for (Attraction a : attractions) {
            sb.append("- 【").append(a.getName()).append("】（").append(a.getArea()).append("）\n");
            if (a.getDescription() != null) {
                sb.append("  简介：").append(a.getDescription()).append("\n");
            }
            if (a.getOpeningHours() != null) {
                sb.append("  开放时间：").append(a.getOpeningHours()).append("\n");
            }
            if (a.getDuration() != null) {
                sb.append("  建议游览：").append(a.getDuration()).append("\n");
            }
            if (a.getTags() != null) {
                sb.append("  标签：").append(a.getTags()).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String buildSystemPrompt(DigitalHumanConfigDto config) {
        String name = config.getName() != null ? config.getName() : "灵宝";
        String personality = config.getPersonality() != null ? config.getPersonality() : "热情、专业、耐心";
        String style = config.getStyle() != null ? config.getStyle() : "古风典雅";
        return String.format(SYSTEM_PROMPT_TEMPLATE, name, personality, name, style);
    }

    private String buildKbContext(String query) {
        var chunks = kbSearchService.search(query, 5);
        if (chunks.isEmpty()) {
            return "暂无相关知识库信息。";
        }
        StringBuilder sb = new StringBuilder("以下是知识库中与游客问题相关的内容，请优先参考这些信息回答：\n\n");
        for (int i = 0; i < chunks.size(); i++) {
            sb.append("---\n").append(chunks.get(i).getContent()).append("\n\n");
        }
        sb.append("注意：如果知识库信息与景点信息冲突，以知识库信息为准。");
        return sb.toString();
    }

    private void logInteraction(Long userId, Long conversationId, String type, String content) {
        try {
            InteractionLog log = new InteractionLog();
            log.setUserId(userId);
            log.setSessionId(String.valueOf(conversationId));
            log.setInteractionType(type);
            log.setContent(content.length() > 200 ? content.substring(0, 200) : content);
            log.setSentiment(SentimentAnalyzer.analyze(content));
            interactionLogMapper.insert(log);
        } catch (Exception e) {
            log.warn("记录交互日志失败", e);
        }
    }
}
