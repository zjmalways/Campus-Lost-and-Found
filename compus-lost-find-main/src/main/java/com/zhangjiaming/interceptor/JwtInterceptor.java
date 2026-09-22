package com.zhangjiaming.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangjiaming.common.Result;
import com.zhangjiaming.context.ErrorContext;
import com.zhangjiaming.util.JwtUtil;
import com.zhangjiaming.util.ThreadLocalUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 校验拦截器：从 Cookie（token）或 Authorization 头读取 Token，校验通过后将用户信息写入 ThreadLocal。
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final String TOKEN_COOKIE_NAME = "token";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 打印所有cookie
        Cookie[] cookies = request.getCookies();
        log.info("====进入JWT拦截器====");
        if(cookies == null){
            log.info("Cookie数组为null！");
        }else{
            for (Cookie cookie : cookies) {
                log.info("cookie name={}, value={}", cookie.getName(), cookie.getValue());
            }
        }


        // 放行 OPTIONS 预检请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String token = resolveToken(request);
        if (token == null || token.isEmpty()) {
            log.info("token为空，返回401 NOT_LOGIN");
//            return reject(response, ErrorContext.NOT_LOGIN);
            return reject(request,response, ErrorContext.NOT_LOGIN);
        }
        if (!jwtUtil.validateToken(token)) {
//            return reject(response, ErrorContext.TOKEN_INVALID);
            return reject(request,response, ErrorContext.TOKEN_INVALID);
        }

        // 解析 Token，将用户信息写入 ThreadLocal
        // 解析 Token，同时存入ThreadLocal 和 request attribute
        Claims claims = jwtUtil.parseToken(token);
        Long userId = Long.parseLong(claims.getSubject());
        String username = claims.get("username", String.class);
        Integer role = claims.get("role", Integer.class);

// 普通MVC接口继续用ThreadLocal
        ThreadLocalUtil.set("userId", userId);
        ThreadLocalUtil.set("username", username);
        ThreadLocalUtil.set("role", role);

// SSE流式接口使用request属性
        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("role", role);

        log.info("token校验成功，userId={}", userId);
        return true;
    }

    /**
     * 依次从 Cookie、Authorization 头解析 Token
     */
    private String resolveToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

//    private boolean reject(HttpServletResponse response, String message) throws Exception {
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        response.setContentType("application/json;charset=utf-8");
//        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(Result.unauthorized(message)));
//        return false;
//    }

    private boolean reject(HttpServletRequest request, HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 判断当前请求是不是SSE流式接口
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("text/event-stream")) {
            // SSE场景：只设置401状态码，不写任何body
            return false;
        }

        // 普通接口：正常返回JSON
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(Result.unauthorized(message)));
        return false;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求完成后清除 ThreadLocal，避免内存泄漏
        ThreadLocalUtil.remove();
    }
}
