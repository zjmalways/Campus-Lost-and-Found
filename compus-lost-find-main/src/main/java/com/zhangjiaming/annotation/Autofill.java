package com.zhangjiaming.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 公共字段自动填充注解，标注在 Mapper 层方法上，配合 {@code AutoFillAspect} 使用，
 * 自动为实体填充 createTime / updateTime。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Autofill {

    /**
     * 操作类型：INSERT 填充 createTime + updateTime，UPDATE 仅填充 updateTime
     */
    OperationType value();

    /**
     * 操作类型枚举
     */
    enum OperationType {
        /** 插入操作：填充 createTime + updateTime */
        INSERT,
        /** 更新操作：仅填充 updateTime */
        UPDATE
    }
}
