package cn.edu.nuc.digitalhuman.chat.controller;

import cn.edu.nuc.digitalhuman.chat.dto.ChatResponseDto;
import cn.edu.nuc.digitalhuman.chat.dto.VoiceChatResponseDto;
import cn.edu.nuc.digitalhuman.chat.service.ChatService;
import cn.edu.nuc.digitalhuman.chat.tts.TtsService;
import cn.edu.nuc.digitalhuman.common.result.R;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ScenicRecognitionController {

    private final ChatService chatService;
    private final TtsService ttsService;
    private final String uploadPath;

    public ScenicRecognitionController(
            ChatService chatService,
            TtsService ttsService,
            @Value("${upload.path}") String uploadPath) {
        this.chatService = chatService;
        this.ttsService = ttsService;
        this.uploadPath = uploadPath;
    }

    /**
     * 拍照识景（流式）：视觉识别后 LLM 讲解逐 token 推送
     */
    @PostMapping(value = "/recognize-scenic/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter recognizeScenicStream(
            @RequestParam("image") MultipartFile image,
            @RequestParam(required = false) Long conversationId,
            @RequestParam(defaultValue = "1.0") double ttsSpeed,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        byte[] imageBytes;
        try {
            imageBytes = image.getBytes();
        } catch (IOException e) {
            log.error("读取图片失败", e);
            SseEmitter err = new SseEmitter();
            err.completeWithError(e);
            return err;
        }

        // 保存图片
        String imageFileName = "scenic_" + UUID.randomUUID().toString().replace("-", "") + ".jpg";
        String imageUrl;
        try {
            Path scenicDir = Paths.get(uploadPath, "scenic");
            Files.createDirectories(scenicDir);
            Files.write(scenicDir.resolve(imageFileName), imageBytes);
            imageUrl = "/uploads/scenic/" + imageFileName;
        } catch (IOException e) {
            log.error("保存图片失败", e);
            SseEmitter err = new SseEmitter();
            err.completeWithError(e);
            return err;
        }

        SseEmitter emitter = new SseEmitter(180_000L);
        chatService.recognizeScenicStream(userId, conversationId, imageBytes, imageUrl, emitter);
        return emitter;
    }

    /**
     * 拍照识景：上传图片 → 识别景点 → LLM 讲解 → TTS 合成
     */
    @PostMapping("/recognize-scenic")
    public R<VoiceChatResponseDto> recognizeScenic(
            @RequestParam("image") MultipartFile image,
            @RequestParam(required = false) Long conversationId,
            @RequestParam(defaultValue = "false") boolean muted,
            @RequestParam(defaultValue = "1.0") double ttsSpeed,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        // 1. 读取图片字节
        byte[] imageBytes;
        try {
            imageBytes = image.getBytes();
        } catch (IOException e) {
            log.error("读取图片失败", e);
            return R.error(500, "图片读取失败");
        }

        log.info("收到拍照识景请求, fileName={}, size={}, muted={}", image.getOriginalFilename(), imageBytes.length, muted);

        // 2. 保存图片到服务器
        String imageFileName = "scenic_" + UUID.randomUUID().toString().replace("-", "") + ".jpg";
        String imageUrl;
        try {
            Path scenicDir = Paths.get(uploadPath, "scenic");
            Files.createDirectories(scenicDir);
            Files.write(scenicDir.resolve(imageFileName), imageBytes);
            imageUrl = "/uploads/scenic/" + imageFileName;
            log.info("图片已保存: {}", imageUrl);
        } catch (IOException e) {
            log.error("保存图片失败", e);
            return R.error(500, "图片保存失败");
        }

        // 3. 调用服务识别景点并生成讲解
        ChatResponseDto chatResult = chatService.recognizeScenic(userId, conversationId, imageBytes, imageUrl);

        // 4. TTS 语音合成（静音模式下跳过）
        String audioUrl = null;
        if (!muted) {
            audioUrl = ttsService.synthesize(chatResult.getAssistantMessage(), ttsSpeed);
        }

        VoiceChatResponseDto result = VoiceChatResponseDto.builder()
                .conversationId(chatResult.getConversationId())
                .userText(chatResult.getUserMessage())
                .assistantText(chatResult.getAssistantMessage())
                .audioUrl(audioUrl)
                .imageUrl(imageUrl)
                .build();

        return R.success(result);
    }
}
