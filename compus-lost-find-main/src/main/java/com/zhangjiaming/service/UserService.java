package com.zhangjiaming.service;

import com.zhangjiaming.dto.LoginRequest;
import com.zhangjiaming.dto.RegisterRequest;
import com.zhangjiaming.entity.User;

import java.util.Map;

public interface UserService {

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求（用户名、密码）
     * @return 登录结果（token、用户信息）
     */
    Map<String, Object> login(LoginRequest loginRequest);

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求（用户名、密码、昵称、联系方式）
     * @return 注册后的用户信息（不含密码）
     */
    User register(RegisterRequest registerRequest);

    /**
     * 根据用户ID查询用户信息
     *
     * @param userId 用户ID
     * @return 用户信息（不含密码）
     */
    User getUserInfo(Long userId);

    /**
     * 更新用户资料（昵称/联系方式/头像）
     *
     * @param user 待更新的用户信息
     * @return 更新后的用户信息
     */
    User updateUserInfo(User user);

    /**
     * 修改密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    boolean updatePassword(Long userId, String oldPassword, String newPassword);
}
