package com.zhangjiaming.common;

import com.zhangjiaming.context.MessageContext;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 统一响应结果封装
 */
@Data
@Schema(description = "统一响应结果")
public class Result<T> {

    @Schema(description = "状态码：200-成功,400-参数错误,401-未登录,403-无权限,500-服务器错误")
    private Integer code;

    @Schema(description = "提示信息")
    private String message;

    @Schema(description = "数据")
    private T data;

    private Result() {
    }

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = MessageContext.SUCCESS;
        r.data = data;
        return r;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }

    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    public static <T> Result<T> badRequest(String message) {
        return error(400, message);
    }

    public static <T> Result<T> unauthorized(String message) {
        return error(401, message);
    }

    public static <T> Result<T> forbidden(String message) {
        return error(403, message);
    }
}
