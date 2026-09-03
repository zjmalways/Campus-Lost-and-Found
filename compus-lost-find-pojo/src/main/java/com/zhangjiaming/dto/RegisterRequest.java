package com.zhangjiaming.dto;

import com.zhangjiaming.context.MessageContext;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "注册请求")
public class RegisterRequest {

    @Schema(description = "用户名")
    @NotBlank(message = MessageContext.USERNAME_BLANK)
    private String username;

    @Schema(description = "密码")
    @NotBlank(message = MessageContext.PASSWORD_BLANK)
    private String password;

    @Schema(description = "昵称")
    @NotBlank(message = MessageContext.NICKNAME_BLANK)
    private String nickname;

    @Schema(description = "联系方式")
    private String contact;
}
