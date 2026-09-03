package com.zhangjiaming.util;

import com.zhangjiaming.context.ErrorContext;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码工具类：SHA-256 + 随机盐，存储格式为 "salt:hash"
 */
public class PasswordUtil {

    private static final int SALT_LENGTH = 16;

    /**
     * 生成随机盐值（Base64 编码的 16 字节随机串）
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 使用指定盐对密码做 SHA-256 哈希（UTF-8，保证跨平台一致）
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(ErrorContext.CRYPTO_ERROR, e);
        }
    }

    /**
     * 生成完整密码存储串 "salt:hash"
     */
    public static String encodePassword(String password) {
        String salt = generateSalt();
        return salt + ":" + hashPassword(password, salt);
    }

    /**
     * 校验密码是否匹配
     */
    public static boolean checkPassword(String password, String encodedPassword) {
        if (encodedPassword == null) {
            return false;
        }
        String[] parts = encodedPassword.split(":");
        if (parts.length != 2) {
            return false;
        }
        String salt = parts[0];
        String storedHash = parts[1];
        return hashPassword(password, salt).equals(storedHash);
    }
}
