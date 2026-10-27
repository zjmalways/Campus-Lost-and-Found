package com.zhangjiaming.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangjiaming.entity.ChatMessage;
import com.zhangjiaming.entity.ChatSession;
import com.zhangjiaming.mapper.ChatMessageMapper;
import com.zhangjiaming.mapper.ChatSessionMapper;
import com.zhangjiaming.service.ChatHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 实现客服会话的持久化、用户隔离及上下文历史加载。
 */
@Service
@RequiredArgsConstructor
public class ChatHistoryServiceImpl implements ChatHistoryService {

    private static final int MAX_SESSIONS = 50;
    private static final int MAX_HISTORY_MESSAGES = 100;
    private static final int MAX_CONTEXT_MESSAGES = 20;
    private static final int MAX_TITLE_LENGTH = 100;

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;

    /** 创建归属当前用户的客服会话。 */
    @Override
    @Transactional
    public ChatSession createSession(Long userId, String title) {
        requireUserId(userId);
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle(normalizeTitle(title));
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(session.getCreatedAt());
        chatSessionMapper.insert(session);
        return session;
    }

    /** 按最近更新时间倒序返回用户的有限数量会话。 */
    @Override
    public List<ChatSession> listSessions(Long userId) {
        requireUserId(userId);
        Page<ChatSession> page = chatSessionMapper.selectPage(
                new Page<>(1, MAX_SESSIONS),
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getUserId, userId)
                        .orderByDesc(ChatSession::getUpdatedAt));
        return page.getRecords();
    }

    /** 返回会话最近消息，并恢复为时间正序。 */
    @Override
    public List<ChatMessage> listMessages(Long userId, Long sessionId) {
        assertSessionOwner(userId, sessionId);
        Page<ChatMessage> page = chatMessageMapper.selectPage(
                new Page<>(1, MAX_HISTORY_MESSAGES),
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getUserId, userId)
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByDesc(ChatMessage::getMessageId));
        List<ChatMessage> messages = page.getRecords();
        Collections.reverse(messages);
        return messages;
    }

    /** 防止用户读取或操作其他用户的会话。 */
    @Override
    public void assertSessionOwner(Long userId, Long sessionId) {
        requireUserId(userId);
        if (sessionId == null || sessionId < 1) {
            throw new IllegalArgumentException("会话ID必须为正整数");
        }
        Long count = chatSessionMapper.selectCount(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getSessionId, sessionId)
                .eq(ChatSession::getUserId, userId));
        if (count == null || count == 0) {
            throw new IllegalArgumentException("会话不存在或无权访问");
        }
    }

    /** 仅向模型上下文提供最近已完成的对话消息。 */
    @Override
    public List<ChatMessage> loadContext(Long userId, Long sessionId) {
        assertSessionOwner(userId, sessionId);
        Page<ChatMessage> page = chatMessageMapper.selectPage(
                new Page<>(1, MAX_CONTEXT_MESSAGES),
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getUserId, userId)
                        .eq(ChatMessage::getSessionId, sessionId)
                        .eq(ChatMessage::getStatus, "COMPLETED")
                        .in(ChatMessage::getRole, "USER", "ASSISTANT")
                        .orderByDesc(ChatMessage::getMessageId));
        List<ChatMessage> messages = page.getRecords();
        Collections.reverse(messages);
        return messages;
    }

    /** 持久化消息，并同步更新会话标题和活动时间。 */
    @Override
    @Transactional
    public void saveMessage(Long userId, Long sessionId, String role, String content, String status) {
        assertSessionOwner(userId, sessionId);
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setUserId(userId);
        message.setRole(role);
        message.setContent(content);
        message.setStatus(status);
        message.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insert(message);

        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session.getTitle() == null && "USER".equals(role)) {
            session.setTitle(normalizeTitle(content));
        }
        session.setUpdatedAt(message.getCreatedAt());
        chatSessionMapper.updateById(session);
    }

    private static void requireUserId(Long userId) {
        if (userId == null) {
            throw new IllegalStateException("请先登录后再访问会话历史");
        }
    }

    private static String normalizeTitle(String title) {
        if (!StringUtils.hasText(title)) {
            return null;
        }
        String normalized = title.strip();
        return normalized.length() <= MAX_TITLE_LENGTH
                ? normalized
                : normalized.substring(0, MAX_TITLE_LENGTH);
    }
}
