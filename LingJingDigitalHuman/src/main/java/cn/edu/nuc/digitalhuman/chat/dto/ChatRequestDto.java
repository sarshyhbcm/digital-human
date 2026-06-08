package cn.edu.nuc.digitalhuman.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequestDto {

    private Long conversationId;

    @NotBlank(message = "消息内容不能为空")
    private String content;
}
