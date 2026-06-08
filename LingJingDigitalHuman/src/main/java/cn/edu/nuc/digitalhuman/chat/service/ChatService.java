package cn.edu.nuc.digitalhuman.chat.service;

import cn.edu.nuc.digitalhuman.chat.dto.ChatResponseDto;
import cn.edu.nuc.digitalhuman.chat.entity.Conversation;
import cn.edu.nuc.digitalhuman.chat.entity.Message;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface ChatService {

    Conversation createConversation(Long userId, String title);

    List<Conversation> getUserConversations(Long userId);

    ChatResponseDto sendMessage(Long userId, Long conversationId, String content);

    /**
     * 拍照识景：上传图片 → 识别景点 → LLM 生成讲解
     */
    ChatResponseDto recognizeScenic(Long userId, Long conversationId, byte[] imageBytes, String imageUrl);

    /**
     * 流式拍照识景：视觉识别后 LLM 讲解流式推送
     */
    void recognizeScenicStream(Long userId, Long conversationId, byte[] imageBytes, String imageUrl, SseEmitter emitter);

    /**
     * 流式聊天：SSE 推送，逐 token 返回
     */
    void sendMessageStream(Long userId, Long conversationId, String content, SseEmitter emitter);

    List<Message> getMessages(Long conversationId);

    void deleteConversation(Long userId, Long conversationId);
}
