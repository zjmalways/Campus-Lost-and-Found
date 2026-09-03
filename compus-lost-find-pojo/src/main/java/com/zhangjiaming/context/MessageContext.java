package com.zhangjiaming.context;

/**
 * 提示信息常量（成功提示、参数校验等用户提示信息集中在此定义）
 */
public final class MessageContext {

    private MessageContext() {
    }

    // ===== 成功提示 =====
    public static final String SUCCESS = "success";

    // ===== 参数校验提示 =====
    public static final String USERNAME_BLANK = "用户名不能为空";
    public static final String PASSWORD_BLANK = "密码不能为空";
    public static final String NICKNAME_BLANK = "昵称不能为空";
}
