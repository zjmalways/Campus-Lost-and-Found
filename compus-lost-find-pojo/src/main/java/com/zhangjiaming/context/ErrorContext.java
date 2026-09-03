package com.zhangjiaming.context;

/**
 * 异常/错误信息常量（所有异常场景的错误提示集中在此定义）
 */
public final class ErrorContext {

    private ErrorContext() {
    }

    // ===== 通用 =====
    public static final String SERVER_ERROR = "服务器内部错误";
    public static final String CRYPTO_ERROR = "SHA-256 算法不可用";
    public static final String BAD_REQUEST_BODY = "请求体格式错误";
    public static final String PARAM_ERROR = "参数错误";

    // ===== 认证 / 鉴权 =====
    public static final String NOT_LOGIN = "未登录，请先登录";
    public static final String TOKEN_INVALID = "Token 无效或已过期，请重新登录";

    // ===== 用户 =====
    public static final String LOGIN_FAILED = "用户名或密码错误";
    public static final String LOGIN_TOO_MANY_ATTEMPTS = "登录失败次数过多，请稍后再试";
    public static final String ACCOUNT_DISABLED = "账号已被禁用，请联系管理员";
    public static final String USERNAME_EXISTS = "用户名已存在";
    public static final String USER_NOT_EXISTS = "用户不存在";
    public static final String OLD_PASSWORD_ERROR = "旧密码错误";
    public static final String NEW_PASSWORD_BLANK = "新密码不能为空";

    // ===== 文件 / OSS =====
    public static final String FILE_EMPTY = "上传文件不能为空";
    public static final String ONLY_IMAGE_ALLOWED = "只能上传图片文件";
    public static final String OSS_NOT_CONFIGURED = "OSS 未配置，请设置 aliyun.oss.access-key-id/secret 或环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID/SECRET";
    public static final String FILE_UPLOAD_FAILED = "文件上传失败";

    // ===== 物品 =====
    public static final String ITEM_NOT_EXISTS = "物品不存在";
    public static final String NO_PERMISSION_UPDATE = "无权修改此物品";
    public static final String NO_PERMISSION_DELETE = "无权删除此物品";
    public static final String NO_PERMISSION_STATUS = "无权修改此物品状态";
    public static final String IMAGE_URL_EMPTY = "请提供要删除的图片URL";

    // ===== 评论 =====
    public static final String COMMENT_NOT_EXISTS = "评论不存在";
    public static final String NO_PERMISSION_DELETE_COMMENT = "无权删除此评论";
    public static final String COMMENT_CONTENT_BLANK = "评论内容不能为空";

    // ===== 公告 =====
    public static final String ANNOUNCEMENT_NOT_EXISTS = "公告不存在";
    public static final String NOT_ADMIN_CREATE = "仅管理员可发布公告";
    public static final String NOT_ADMIN_UPDATE = "仅管理员可修改公告";
    public static final String NOT_ADMIN_DELETE = "仅管理员可删除公告";
}
