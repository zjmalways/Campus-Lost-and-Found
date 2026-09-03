package com.zhangjiaming.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于 ThreadLocal 的请求级用户上下文（由拦截器写入、清理）
 */
public class ThreadLocalUtil {
    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = new ThreadLocal<>();

    public static void set(String key, Object value) {
        Map<String, Object> map = THREAD_LOCAL.get();
        if (map == null) {
            map = new HashMap<>();
            THREAD_LOCAL.set(map);
        }
        map.put(key, value);
    }

    public static Object get(String key) {
        Map<String, Object> map = THREAD_LOCAL.get();
        return map == null ? null : map.get(key);
    }

    public static Long getCurrentUserId() {
        Object obj = get("userId");
        return obj != null ? (Long) obj : null;
    }

    public static String getCurrentUsername() {
        Object obj = get("username");
        return obj != null ? (String) obj : null;
    }

    public static Integer getCurrentRole() {
        Object obj = get("role");
        return obj != null ? (Integer) obj : null;
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }
}
