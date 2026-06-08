package cn.edu.nuc.digitalhuman.chat.controller;

import cn.edu.nuc.digitalhuman.chat.asr.AsrService;
import cn.edu.nuc.digitalhuman.chat.dto.ChatResponseDto;
import cn.edu.nuc.digitalhuman.chat.dto.VoiceChatResponseDto;
import cn.edu.nuc.digitalhuman.chat.entity.Conversation;
import cn.edu.nuc.digitalhuman.chat.mapper.ConversationMapper;
import cn.edu.nuc.digitalhuman.chat.service.ChatService;
import cn.edu.nuc.digitalhuman.chat.tts.TtsService;
import cn.edu.nuc.digitalhuman.common.result.R;
import cn.edu.nuc.digitalhuman.common.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class VoiceController {

    private final ChatService chatService;
    private final AsrService asrService;
    private final TtsService ttsService;
    private final ConversationMapper conversationMapper;

    /**
     * 语音发送：上传音频 → ASR 识别 → LLM 回复 → TTS 合成
     */
    @PostMapping("/send-voice")
    public R<VoiceChatResponseDto> sendVoice(
            @RequestParam("audio") MultipartFile audio,
            @RequestParam(required = false) Long conversationId,
            @RequestParam(defaultValue = "false") boolean muted,
            @RequestParam(defaultValue = "1.0") double ttsSpeed,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        // 1. ASR 语音识别
        log.info("收到语音消息, fileName={}, size={}, muted={}", audio.getOriginalFilename(), audio.getSize(), muted);
        String userText = asrService.transcribe(audio);
        if (userText == null || userText.isBlank()) {
            return R.error(500, "语音识别失败，请重试");
        }
        log.info("ASR 识别结果: {}", userText);

        // 2. 发送给 LLM 获取回复
        ChatResponseDto chatResult = chatService.sendMessage(userId, conversationId, userText);

        // 3. TTS 语音合成（静音模式下跳过）
        String audioUrl = null;
        if (!muted) {
            audioUrl = ttsService.synthesize(chatResult.getAssistantMessage(), ttsSpeed);
        }

        VoiceChatResponseDto result = VoiceChatResponseDto.builder()
                .conversationId(chatResult.getConversationId())
                .userText(userText)
                .assistantText(chatResult.getAssistantMessage())
                .audioUrl(audioUrl)
                .build();

        return R.success(result);
    }

    /**
     * 为指定对话的最后一条AI消息合成语音
     */
    @GetMapping("/tts/{conversationId}")
    public R<String> synthesizeMessageAudio(@PathVariable Long conversationId,
                                            @RequestParam(defaultValue = "1.0") double speed,
                                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        // 检查对话所有权
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            return R.error(404, "对话不存在");
        }
        var messages = chatService.getMessages(conversationId);
        if (messages.isEmpty()) {
            return R.error(404, "对话不存在");
        }
        // 取最后一条 assistant 消息
        var msg = messages.stream()
                .filter(m -> "assistant".equals(m.getRole()))
                .reduce((first, second) -> second)
                .orElse(null);
        if (msg == null) {
            return R.error(404, "没有找到AI回复");
        }
        String audioUrl = ttsService.synthesize(msg.getContent(), speed);
        if (audioUrl == null) {
            return R.error(500, "语音合成失败");
        }
        return R.success(audioUrl);
    }

    /**
     * 为任意文本合成语音（供前端朗读按钮使用）
     */
    @PostMapping("/tts")
    public R<String> synthesizeText(@RequestBody Map<String, Object> body) {
        String content = (String) body.get("content");
        if (content == null || content.isBlank()) {
            return R.error(400, "内容不能为空");
        }
        double speed = body.containsKey("speed") ? ((Number) body.get("speed")).doubleValue() : 1.0;
        String audioUrl = ttsService.synthesize(content, speed);
        if (audioUrl == null) {
            return R.error(500, "语音合成失败");
        }
        return R.success(audioUrl);
    }
}
