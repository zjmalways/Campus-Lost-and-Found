package com.zhangjiaming.config;

import com.zhangjiaming.common.Result;
import com.zhangjiaming.context.ErrorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 参数校验失败（@Valid） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage())
                .orElse(ErrorContext.PARAM_ERROR);
        return Result.badRequest(message);
    }

    /** 请求体缺失 / 格式错误 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handleNotReadable(HttpMessageNotReadableException e) {
        return Result.badRequest(ErrorContext.BAD_REQUEST_BODY);
    }

    /** 业务异常（Service 层抛出的 RuntimeException，携带友好提示） */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        return Result.error(e.getMessage());
    }

    /** 兜底异常 */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("服务器内部错误", e);
        return Result.error(ErrorContext.SERVER_ERROR);
    }
}
