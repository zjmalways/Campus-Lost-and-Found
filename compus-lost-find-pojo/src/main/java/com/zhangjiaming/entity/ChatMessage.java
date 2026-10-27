package com.zhangjiaming.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客服消息实体，持久化用户与 AI 在指定会话中的消息及处理状态。
 */
@Data
@TableName("chat_messages")
public class ChatMessage {

    /** 消息主键。 */
    @TableId(type = IdType.AUTO)
    private Long messageId;

    /** 消息所属会话 ID。 */
    private Long sessionId;

    /** 消息所属用户 ID。 */
    private Long userId;

    /** 消息角色，取值为 USER 或 ASSISTANT。 */
    private String role;

    /** 消息文本内容。 */
    private String content;

    /** 消息处理状态，取值为 COMPLETED、FAILED 或 CANCELLED。 */
    private String status;

    /** 消息创建时间。 */
    private LocalDateTime createdAt;
}
