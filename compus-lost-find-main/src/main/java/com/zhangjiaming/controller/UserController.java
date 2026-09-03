package com.zhangjiaming.controller;

import com.zhangjiaming.common.Result;
import com.zhangjiaming.context.ErrorContext;
import com.zhangjiaming.dto.LoginRequest;
import com.zhangjiaming.dto.RegisterRequest;
import com.zhangjiaming.entity.User;
import com.zhangjiaming.service.UserService;
import com.zhangjiaming.util.AliyunOSSUtil;
import com.zhangjiaming.util.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@Tag(name = "用户管理", description = "用户注册、登录、资料管理、头像上传")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AliyunOSSUtil aliyunOSSUtil;

    /**
     * 登录：返回 JWT token（前端保存到 Cookie，后续请求由拦截器从 Cookie 读取校验）
     */
    @Operation(summary = "用户登录", description = "使用用户名密码登录，成功后返回 JWT token（前端保存到 Cookie）")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest) {
        return Result.success(userService.login(loginRequest));
    }

    /**
     * 注册
     */
    @Operation(summary = "用户注册", description = "注册新用户，默认角色为普通用户")
    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return Result.success(userService.register(registerRequest));
    }

    /**
     * 获取当前登录用户信息（用于从 Cookie 校验登录态）
     */
    @Operation(summary = "获取个人信息", description = "获取当前登录用户信息（需登录）")
    @GetMapping("/info")
    public Result<User> getUserInfo() {
        Long userId = ThreadLocalUtil.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized(ErrorContext.NOT_LOGIN);
        }
        return Result.success(userService.getUserInfo(userId));
    }

    /**
     * 更新个人资料（昵称/联系方式/头像）
     */
    @Operation(summary = "更新资料", description = "更新当前用户昵称/联系方式/头像（需登录）")
    @PutMapping("/update")
    public Result<User> updateUserInfo(@RequestBody User user) {
        Long userId = ThreadLocalUtil.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized(ErrorContext.NOT_LOGIN);
        }
        user.setUserId(userId);
        return Result.success(userService.updateUserInfo(user));
    }

    /**
     * 修改密码
     */
    @Operation(summary = "修改密码", description = "校验旧密码后修改为新密码（需登录）")
    @PutMapping("/password")
    public Result<?> updatePassword(@RequestBody Map<String, String> params) {
        Long userId = ThreadLocalUtil.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized(ErrorContext.NOT_LOGIN);
        }
        userService.updatePassword(userId, params.get("oldPassword"), params.get("newPassword"));
        return Result.success();
    }

    /**
     * 上传头像到 OSS
     */
    @Operation(summary = "上传头像", description = "上传头像到阿里云 OSS 并更新用户头像（需登录）")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = ThreadLocalUtil.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized(ErrorContext.NOT_LOGIN);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error(ErrorContext.ONLY_IMAGE_ALLOWED);
        }

        String avatarUrl = aliyunOSSUtil.uploadAvatar(file);
        log.info("头像上传成功 —— userId: {}, url: {}", userId, avatarUrl);

        User user = new User();
        user.setUserId(userId);
        user.setAvatar(avatarUrl);
        userService.updateUserInfo(user);

        return Result.success(avatarUrl);
    }
}
