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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 校验拦截器：从 Cookie（token）或 Authorization 头读取 Token，校验通过后将用户信息写入 ThreadLocal。
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final String TOKEN_COOKIE_NAME = "token";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 预检请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String token = resolveToken(request);
        if (token == null || token.isEmpty()) {
            return reject(response, ErrorContext.NOT_LOGIN);
        }
        if (!jwtUtil.validateToken(token)) {
            return reject(response, ErrorContext.TOKEN_INVALID);
        }

        // 解析 Token，将用户信息写入 ThreadLocal
        Claims claims = jwtUtil.parseToken(token);
        ThreadLocalUtil.set("userId", Long.parseLong(claims.getSubject()));
        ThreadLocalUtil.set("username", claims.get("username", String.class));
        ThreadLocalUtil.set("role", claims.get("role", Integer.class));

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

    private boolean reject(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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
