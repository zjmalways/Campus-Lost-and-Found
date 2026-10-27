package com.zhangjiaming.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客服会话实体，记录会话所属用户及会话名称、创建和更新时间。
 */
@Data
@TableName("chat_sessions")
public class ChatSession {

    /** 会话主键。 */
    @TableId(type = IdType.AUTO)
    private Long sessionId;

    /** 会话所属用户 ID。 */
    private Long userId;

    /** 会话标题。 */
    private String title;

    /** 会话创建时间。 */
    private LocalDateTime createdAt;

    /** 会话最近更新时间。 */
    private LocalDateTime updatedAt;
}
