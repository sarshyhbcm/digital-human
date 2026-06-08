package cn.edu.nuc.digitalhuman.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ChatResponseDto {

    private Long conversationId;
    private String userMessage;
    private String assistantMessage;
}
