package org.zhangjiamin.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zhangjiamin.dto.LoginRequest;
import org.zhangjiamin.dto.RegisterRequest;
import org.zhangjiamin.entity.User;
import org.zhangjiamin.mapper.UserMapper;
import org.zhangjiamin.service.UserService;
import org.zhangjiamin.util.JwtUtil;
import org.zhangjiamin.util.PasswordUtil;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Map<String, Object> login(LoginRequest loginRequest) {
        User user = userMapper.findByUsername(loginRequest.getUsername());

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (user.getStatus() == 1) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }

        // 校验密码
        if (!PasswordUtil.checkPassword(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getUserId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("role", user.getRole());
        result.put("avatar", user.getAvatar());

        return result;
    }

    @Override
    public User register(RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        User existingUser = userMapper.findByUsername(registerRequest.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(PasswordUtil.encodePassword(registerRequest.getPassword()));
        user.setNickname(registerRequest.getNickname());
        user.setContact(registerRequest.getContact());
        user.setRole(0); // 默认为普通用户
        user.setStatus(0); // 正常状态

        userMapper.insert(user);
        return user;
    }

    @Override
    public User getUserInfo(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(null); // 不返回密码
        return user;
    }

    @Override
    public User updateUserInfo(User user) {
        User existingUser = userMapper.findById(user.getUserId());
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }

        userMapper.update(user);
        User updatedUser = userMapper.findById(user.getUserId());
        updatedUser.setPassword(null);
        return updatedUser;
    }

    @Override
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证旧密码
        if (!PasswordUtil.checkPassword(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        // 更新新密码
        user.setPassword(PasswordUtil.encodePassword(newPassword));
        userMapper.updatePassword(user);
        return true;
    }
}
