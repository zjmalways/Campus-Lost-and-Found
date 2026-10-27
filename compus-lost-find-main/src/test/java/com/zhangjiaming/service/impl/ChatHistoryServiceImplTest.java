package com.zhangjiaming.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangjiaming.entity.ChatSession;
import com.zhangjiaming.mapper.ChatMessageMapper;
import com.zhangjiaming.mapper.ChatSessionMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatHistoryServiceImplTest {

    @Test
    void createsSessionForAuthenticatedUser() {
        ChatSessionMapper sessionMapper = mock(ChatSessionMapper.class);
        ChatHistoryServiceImpl service = new ChatHistoryServiceImpl(
                sessionMapper, mock(ChatMessageMapper.class));
        when(sessionMapper.insert(any(ChatSession.class))).thenAnswer(invocation -> {
            ChatSession session = invocation.getArgument(0);
            session.setSessionId(12L);
            return 1;
        });

        ChatSession session = service.createSession(42L, "  Lost keys  ");

        assertEquals(12L, session.getSessionId());
        assertEquals(42L, session.getUserId());
        assertEquals("Lost keys", session.getTitle());
        assertNotNull(session.getCreatedAt());
    }

    @Test
    void rejectsAccessToSessionOwnedByAnotherUser() {
        ChatSessionMapper sessionMapper = mock(ChatSessionMapper.class);
        ChatMessageMapper messageMapper = mock(ChatMessageMapper.class);
        ChatHistoryServiceImpl service = new ChatHistoryServiceImpl(sessionMapper, messageMapper);
        when(sessionMapper.selectCount(any(Wrapper.class))).thenReturn(0L);

        assertThrows(IllegalArgumentException.class, () -> service.listMessages(99L, 12L));

        verify(messageMapper, never()).selectPage(any(Page.class), any(Wrapper.class));
    }
}
