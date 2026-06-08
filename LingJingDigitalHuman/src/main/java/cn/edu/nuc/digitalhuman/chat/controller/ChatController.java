package cn.edu.nuc.digitalhuman.chat.controller;

import cn.edu.nuc.digitalhuman.chat.dto.ChatRequestDto;
import cn.edu.nuc.digitalhuman.chat.dto.ChatResponseDto;
import cn.edu.nuc.digitalhuman.chat.entity.Conversation;
import cn.edu.nuc.digitalhuman.chat.entity.Message;
import cn.edu.nuc.digitalhuman.chat.service.ChatService;
import cn.edu.nuc.digitalhuman.common.result.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    public R<ChatResponseDto> send(@Valid @RequestBody ChatRequestDto dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        ChatResponseDto result = chatService.sendMessage(userId, dto.getConversationId(), dto.getContent());
        return R.success(result);
    }

    /**
     * 流式聊天：SSE 推送，逐 token 返回
     */
    @PostMapping(value = "/send/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendStream(@Valid @RequestBody ChatRequestDto dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        SseEmitter emitter = new SseEmitter(120_000L);
        chatService.sendMessageStream(userId, dto.getConversationId(), dto.getContent(), emitter);
        return emitter;
    }

    @GetMapping("/conversations")
    public R<List<Conversation>> conversations(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.success(chatService.getUserConversations(userId));
    }

    @PostMapping("/conversations")
    public R<Conversation> createConversation(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.success(chatService.createConversation(userId, "新对话"));
    }

    @GetMapping("/conversations/{id}/messages")
    public R<List<Message>> messages(@PathVariable Long id) {
        return R.success(chatService.getMessages(id));
    }

    @DeleteMapping("/conversations/{id}")
    public R<Void> deleteConversation(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        chatService.deleteConversation(userId, id);
        return R.success();
    }
}
