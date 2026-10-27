package com.zhangjiaming.service;

import com.zhangjiaming.entity.ChatMessage;
import com.zhangjiaming.entity.ChatSession;

import java.util.List;

/**
 * 定义客服会话和消息历史的管理操作。
 */
public interface ChatHistoryService {

    /** 为指定用户创建会话。 */
    ChatSession createSession(Long userId, String title);

    /** 查询指定用户最近更新的会话。 */
    List<ChatSession> listSessions(Long userId);

    /** 查询会话消息，并验证会话属于当前用户。 */
    List<ChatMessage> listMessages(Long userId, Long sessionId);

    /** 校验指定会话是否属于当前用户。 */
    void assertSessionOwner(Long userId, Long sessionId);

    /** 加载用于构造 AI 对话上下文的已完成消息。 */
    List<ChatMessage> loadContext(Long userId, Long sessionId);

    /** 保存一条会话消息并更新会话的最近活动时间。 */
    void saveMessage(Long userId, Long sessionId, String role, String content, String status);
}
