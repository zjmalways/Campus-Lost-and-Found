package com.zhangjiaming.dto;

import com.zhangjiaming.context.MessageContext;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "登录请求")
public class LoginRequest {

    @Schema(description = "用户名")
    @NotBlank(message = MessageContext.USERNAME_BLANK)
    private String username;

    @Schema(description = "密码")
    @NotBlank(message = MessageContext.PASSWORD_BLANK)
    private String password;
}
