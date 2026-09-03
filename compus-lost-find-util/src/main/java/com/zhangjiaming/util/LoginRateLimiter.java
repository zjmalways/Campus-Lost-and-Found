package com.zhangjiaming.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录限流组件：使用 JUC 的 {@link ConcurrentHashMap} 原子地记录登录失败次数，防止暴力破解。
 * 高并发下同一用户名的多次失败尝试会被串行化计数，达到阈值后暂时锁定。
 */
@Component
public class LoginRateLimiter {

    /** 允许的最大连续失败次数 */
    private static final int MAX_FAILED_ATTEMPTS = 5;

    /** 锁定时长（毫秒），15 分钟 */
    private static final long LOCK_DURATION_MS = 15 * 60 * 1000L;

    /** 用户名 -> 失败记录 */
    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    /**
     * 判断用户当前是否被暂时锁定
     *
     * @param username 用户名
     * @return true 表示已被锁定，应拒绝登录
     */
    public boolean isBlocked(String username) {
        Attempt attempt = attempts.get(username);
        if (attempt == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        // 达到阈值且仍在锁定时长内，视为锁定
        return attempt.count >= MAX_FAILED_ATTEMPTS
                && now - attempt.lastAttemptTime <= LOCK_DURATION_MS;
    }

    /**
     * 记录一次登录失败（原子操作，高并发下不会丢失计数）
     *
     * @param username 用户名
     */
    public void recordFailure(String username) {
        attempts.compute(username, (k, v) -> {
            long now = System.currentTimeMillis();
            // 首次失败，或已超过锁定时长则重新计数
            if (v == null || now - v.lastAttemptTime > LOCK_DURATION_MS) {
                return new Attempt(1, now);
            }
            return new Attempt(v.count + 1, now);
        });
    }

    /**
     * 登录成功，清除失败记录
     *
     * @param username 用户名
     */
    public void recordSuccess(String username) {
        attempts.remove(username);
    }

    /**
     * 失败记录（不可变，保证线程安全）
     */
    private static final class Attempt {
        final int count;
        final long lastAttemptTime;

        Attempt(int count, long lastAttemptTime) {
            this.count = count;
            this.lastAttemptTime = lastAttemptTime;
        }
    }
}
