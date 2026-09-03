package com.zhangjiaming.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhangjiaming.util.LoginRateLimiter;
import com.zhangjiaming.context.ErrorContext;
import com.zhangjiaming.dto.LoginRequest;
import com.zhangjiaming.dto.RegisterRequest;
import com.zhangjiaming.entity.User;
import com.zhangjiaming.mapper.UserMapper;
import com.zhangjiaming.service.UserService;
import com.zhangjiaming.util.JwtUtil;
import com.zhangjiaming.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private LoginRateLimiter loginRateLimiter;

    /**
     * 用户登录：校验用户名密码与账号状态，通过后生成 JWT token
     *
     * @param loginRequest 登录请求（用户名、密码）
     * @return 登录结果（token、用户信息）
     */
    @Override
    public Map<String, Object> login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();

        // 高并发防暴力破解：检查该用户名是否已被暂时锁定
        if (loginRateLimiter.isBlocked(username)) {
            throw new RuntimeException(ErrorContext.LOGIN_TOO_MANY_ATTEMPTS);
        }

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            loginRateLimiter.recordFailure(username);
            throw new RuntimeException(ErrorContext.LOGIN_FAILED);
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new RuntimeException(ErrorContext.ACCOUNT_DISABLED);
        }
        if (!PasswordUtil.checkPassword(loginRequest.getPassword(), user.getPassword())) {
            loginRateLimiter.recordFailure(username);
            throw new RuntimeException(ErrorContext.LOGIN_FAILED);
        }

        // 登录成功，清除失败计数
        loginRateLimiter.recordSuccess(username);

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

    /**
     * 用户注册：校验用户名唯一性后写入数据库
     *
     * @param registerRequest 注册请求（用户名、密码、昵称、联系方式）
     * @return 注册后的用户信息（不含密码）
     */
    @Override
    public User register(RegisterRequest registerRequest) {
        User existing = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, registerRequest.getUsername()));
        if (existing != null) {
            throw new RuntimeException(ErrorContext.USERNAME_EXISTS);
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(PasswordUtil.encodePassword(registerRequest.getPassword()));
        user.setNickname(registerRequest.getNickname());
        user.setContact(registerRequest.getContact());
        user.setRole(0);
        user.setStatus(0);

        userMapper.insert(user);
        user.setPassword(null);
        return user;
    }

    /**
     * 根据用户ID查询用户信息（不含密码）
     *
     * @param userId 用户ID
     * @return 用户信息（不含密码）
     */
    @Override
    public User getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException(ErrorContext.USER_NOT_EXISTS);
        }
        user.setPassword(null);
        return user;
    }

    /**
     * 更新用户资料（仅昵称/联系方式/头像）
     *
     * @param user 待更新的用户信息
     * @return 更新后的用户信息（不含密码）
     */
    @Override
    public User updateUserInfo(User user) {
        User existing = userMapper.selectById(user.getUserId());
        if (existing == null) {
            throw new RuntimeException(ErrorContext.USER_NOT_EXISTS);
        }

        // 仅允许更新昵称/联系方式/头像，避免越权修改 role/status/username/password
        User updateUser = new User();
        updateUser.setUserId(user.getUserId());
        updateUser.setNickname(user.getNickname());
        updateUser.setContact(user.getContact());
        updateUser.setAvatar(user.getAvatar());

        if (updateUser.getNickname() == null && updateUser.getContact() == null && updateUser.getAvatar() == null) {
            existing.setPassword(null);
            return existing;
        }

        userMapper.updateById(updateUser);
        User updated = userMapper.selectById(user.getUserId());
        updated.setPassword(null);
        return updated;
    }

    /**
     * 修改密码：校验旧密码后更新新密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    @Override
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException(ErrorContext.NEW_PASSWORD_BLANK);
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException(ErrorContext.USER_NOT_EXISTS);
        }
        if (!PasswordUtil.checkPassword(oldPassword, user.getPassword())) {
            throw new RuntimeException(ErrorContext.OLD_PASSWORD_ERROR);
        }

        User updateUser = new User();
        updateUser.setUserId(userId);
        updateUser.setPassword(PasswordUtil.encodePassword(newPassword));
        userMapper.updateById(updateUser);
        return true;
    }
}
