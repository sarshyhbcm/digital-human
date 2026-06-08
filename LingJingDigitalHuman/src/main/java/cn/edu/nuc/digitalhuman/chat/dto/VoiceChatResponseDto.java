package cn.edu.nuc.digitalhuman.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 语音对话响应 DTO
 */
@Data
@Builder
@AllArgsConstructor
public class VoiceChatResponseDto {

    private Long conversationId;
    /** ASR 识别的用户语音文本 */
    private String userText;
    /** LLM 回复文本 */
    private String assistantText;
    /** TTS 合成音频的访问 URL */
    private String audioUrl;
    /** 图片识别场景：用户上传的图片访问 URL */
    private String imageUrl;
}
