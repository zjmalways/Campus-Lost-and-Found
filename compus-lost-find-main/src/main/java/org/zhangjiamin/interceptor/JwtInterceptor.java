package org.zhangjiamin.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.zhangjiamin.common.Result;
import org.zhangjiamin.util.JwtUtil;
import org.zhangjiamin.util.ThreadLocalUtil;

import io.jsonwebtoken.Claims;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行OPTIONS预检请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");

        if (token == null || token.isEmpty()) {
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(OBJECT_MAPPER.writeValueAsString(Result.unauthorized("未登录，请先登录")));
            return false;
        }

        // 去掉 "Bearer " 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (!jwtUtil.validateToken(token)) {
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(OBJECT_MAPPER.writeValueAsString(Result.unauthorized("Token无效或已过期，请重新登录")));
            return false;
        }

        // 解析Token，将用户信息存入ThreadLocal
        Claims claims = jwtUtil.parseToken(token);
        ThreadLocalUtil.set("userId", Long.parseLong(claims.getSubject()));
        ThreadLocalUtil.set("username", claims.get("username", String.class));
        ThreadLocalUtil.set("role", claims.get("role", Integer.class));

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求完成后清除ThreadLocal，避免内存泄漏
        ThreadLocalUtil.remove();
    }
}
