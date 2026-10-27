-- Run against an existing campus_lost_found database to add AI chat history.
USE campus_lost_found;

CREATE TABLE IF NOT EXISTS chat_sessions (
    session_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT NOT NULL,
    title      VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_chat_sessions_user_updated (user_id, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI客服会话表';

CREATE TABLE IF NOT EXISTS chat_messages (
    message_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    role       VARCHAR(20) NOT NULL COMMENT 'USER或ASSISTANT',
    content    MEDIUMTEXT NOT NULL,
    status     VARCHAR(20) NOT NULL DEFAULT 'COMPLETED' COMMENT 'COMPLETED、FAILED或CANCELLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_chat_messages_session_message (session_id, message_id),
    INDEX idx_chat_messages_user_session (user_id, session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI客服消息历史表';
