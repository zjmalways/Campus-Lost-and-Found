package org.zhangjiamin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zhangjiamin.common.Result;
import org.zhangjiamin.dto.LoginRequest;
import org.zhangjiamin.dto.RegisterRequest;
import org.zhangjiamin.entity.User;
import org.zhangjiamin.service.UserService;
import org.zhangjiamin.util.ThreadLocalUtil;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        try {
            Map<String, Object> result = userService.login(loginRequest);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<User> register(@RequestBody RegisterRequest registerRequest) {
        try {
            User user = userService.register(registerRequest);
            user.setPassword(null);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/info")
    public Result<User> getUserInfo() {
        try {
            Long userId = ThreadLocalUtil.getCurrentUserId();
            if (userId == null) {
                return Result.unauthorized("未登录");
            }
            User user = userService.getUserInfo(userId);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<User> updateUserInfo(@RequestBody User user) {
        try {
            Long userId = ThreadLocalUtil.getCurrentUserId();
            if (userId == null) {
                return Result.unauthorized("未登录");
            }
            user.setUserId(userId);
            User updatedUser = userService.updateUserInfo(user);
            return Result.success(updatedUser);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/password")
    public Result<?> updatePassword(@RequestBody Map<String, String> params) {
        try {
            Long userId = ThreadLocalUtil.getCurrentUserId();
            if (userId == null) {
                return Result.unauthorized("未登录");
            }
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");
            userService.updatePassword(userId, oldPassword, newPassword);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
