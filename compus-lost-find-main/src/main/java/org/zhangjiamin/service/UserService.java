package org.zhangjiamin.service;

import org.zhangjiamin.dto.LoginRequest;
import org.zhangjiamin.dto.RegisterRequest;
import org.zhangjiamin.entity.User;

import java.util.Map;

public interface UserService {
    Map<String, Object> login(LoginRequest loginRequest);
    User register(RegisterRequest registerRequest);
    User getUserInfo(Long userId);
    User updateUserInfo(User user);
    boolean updatePassword(Long userId, String oldPassword, String newPassword);
}
