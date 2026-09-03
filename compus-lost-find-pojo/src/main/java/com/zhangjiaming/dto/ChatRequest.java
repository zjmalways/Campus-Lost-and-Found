package com.zhangjiaming.dto;

import com.zhangjiaming.context.MessageContext;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "智能客服问答请求")
public class ChatRequest {

    @Schema(description = "用户问题")
    @NotBlank(message = MessageContext.QUESTION_BLANK)
    private String question;
}
