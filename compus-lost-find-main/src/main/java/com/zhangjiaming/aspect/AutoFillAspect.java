package com.zhangjiaming.aspect;

import com.zhangjiaming.annotation.Autofill;
import com.zhangjiaming.annotation.Autofill.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

/**
 * 公共字段自动填充切面：拦截标注了 {@link Autofill} 的 Mapper 方法，
 * 通过反射为实体自动填充 createTime / updateTime。
 */
@Slf4j
@Aspect
@Component
public class AutoFillAspect {

    /**
     * 拦截标注了 @Autofill 的方法
     */
    @Around("@annotation(autofill)")
    public Object autoFill(ProceedingJoinPoint joinPoint, Autofill autofill) throws Throwable {
        OperationType operationType = autofill.value();
        Object[] args = joinPoint.getArgs();
        LocalDateTime now = LocalDateTime.now();

        // 遍历参数，为拥有 createTime/updateTime 字段的实体对象填充
        for (Object arg : args) {
            if (arg != null) {
                fillEntity(arg, operationType, now);
            }
        }

        return joinPoint.proceed(args);
    }

    /**
     * 为单个实体填充公共字段
     */
    private void fillEntity(Object entity, OperationType operationType, LocalDateTime now) {
        Field createTimeField = findField(entity.getClass(), "createTime");
        Field updateTimeField = findField(entity.getClass(), "updateTime");

        if (createTimeField == null && updateTimeField == null) {
            return;
        }
        // 插入时填充 createTime，插入/更新都填充 updateTime
        if (operationType == OperationType.INSERT && createTimeField != null) {
            setField(entity, createTimeField, now);
        }
        if (updateTimeField != null) {
            setField(entity, updateTimeField, now);
        }
    }

    /**
     * 递归向上查找字段（含父类）
     */
    private Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    /**
     * 通过反射设置字段值
     */
    private void setField(Object entity, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(entity, value);
        } catch (Exception e) {
            log.warn("自动填充字段 {} 失败: {}", field.getName(), e.getMessage());
        }
    }
}
