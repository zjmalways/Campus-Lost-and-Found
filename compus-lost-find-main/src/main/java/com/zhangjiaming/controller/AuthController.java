package com.zhangjiaming.controller;

import com.zhangjiaming.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证相关接口
 */
@Tag(name = "认证管理", description = "登录态相关接口")
@RestController
public class AuthController {

    /**
     * 登出：清除 Cookie 中的 token
     */
    @Operation(summary = "登出", description = "清除浏览器 Cookie 中的 token，退出登录")
    @PostMapping("/logout")
    public Result<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return Result.success();
    }

    @Value("${DB_PASSWORD:NOT_FOUND}")
    private String dbPwd;
    @Value("${DEEPSEEK_API_KEY:NOT_FOUND}")
    private String deepseekApiKey;
    @Value("${TEST_PWD:NOT_FOUND}")
    private String test;

    @GetMapping("/debug-env")
    public String debug() {
        return "读到的密码 --> [" + dbPwd + "]" + deepseekApiKey + "]" + test;
    }
}
