-- 校园失物招领平台 - MySQL 数据库初始化脚本
-- 说明：包含用户表、物品表、评论表、公告表

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS campus_lost_found DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_lost_found;

-- 2. 创建用户表
CREATE TABLE IF NOT EXISTS users (
    user_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    password    VARCHAR(200) NOT NULL COMMENT '密码（格式 salt:hash，SHA-256+盐）',
    nickname    VARCHAR(50)  NOT NULL COMMENT '昵称',
    contact     VARCHAR(100) COMMENT '联系方式',
    avatar      VARCHAR(500) COMMENT '头像 URL',
    role        TINYINT DEFAULT 0 COMMENT '角色：0-普通用户，1-管理员',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status      TINYINT DEFAULT 0 COMMENT '状态：0-正常，1-禁用'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 3. 索引
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_status ON users(status);

-- 4. 默认管理员账号（用户名 admin，密码 admin123，已按 SHA-256+盐 存储）
INSERT IGNORE INTO users (username, password, nickname, contact, role, status)
VALUES ('admin',
        'CampusLostFound2024:3114ESqt77z7RprsHzAHxL0J0xgoS2j1P14IbvKgMzw=',
        '系统管理员',
        'admin@campus.edu',
        1,
        0);

-- 5. 创建物品表
CREATE TABLE IF NOT EXISTS items (
    item_id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    item_type        TINYINT NOT NULL COMMENT '物品类型：1-证件, 2-钥匙, 3-电子设备, 4-衣物, 5-钱包, 6-其他',
    publish_type     TINYINT NOT NULL COMMENT '发布类型：0-丢失, 1-捡到',
    title            VARCHAR(200) NOT NULL,
    description      TEXT,
    features         VARCHAR(500),
    images           TEXT COMMENT '物品图片URL（逗号分隔）',
    location         VARCHAR(200) NOT NULL,
    event_time       DATETIME COMMENT '事件发生时间',
    publisher_id     BIGINT NOT NULL,
    publisher_name   VARCHAR(50) NOT NULL,
    contact          VARCHAR(100),
    storage_location VARCHAR(200),
    status           TINYINT DEFAULT 0 COMMENT '状态：0-未找回/未归还, 1-已找回/已归还',
    view_count       INT DEFAULT 0,
    collect_count    INT DEFAULT 0,
    comment_count    INT DEFAULT 0,
    create_time      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品信息表';

CREATE INDEX idx_items_publish_type ON items(publish_type);
CREATE INDEX idx_items_item_type ON items(item_type);
CREATE INDEX idx_items_status ON items(status);
CREATE INDEX idx_items_publisher_id ON items(publisher_id);
CREATE INDEX idx_items_create_time ON items(create_time);

-- 6. 创建评论表
CREATE TABLE IF NOT EXISTS comments (
    comment_id    BIGINT PRIMARY KEY AUTO_INCREMENT,
    item_id       BIGINT NOT NULL,
    user_id       BIGINT NOT NULL,
    user_nickname VARCHAR(50) NOT NULL,
    user_avatar   VARCHAR(500),
    content       TEXT NOT NULL,
    parent_id     BIGINT DEFAULT 0,
    create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read       TINYINT DEFAULT 0 COMMENT '是否已读：0-未读，1-已读'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

CREATE INDEX idx_comments_item_id ON comments(item_id);
CREATE INDEX idx_comments_user_id ON comments(user_id);
CREATE INDEX idx_comments_create_time ON comments(create_time);

-- 7. 创建公告表
CREATE TABLE IF NOT EXISTS announcements (
    announcement_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title           VARCHAR(200) NOT NULL,
    content         TEXT NOT NULL,
    publisher_id    BIGINT NOT NULL,
    publisher_name  VARCHAR(50) NOT NULL,
    is_top          TINYINT DEFAULT 0 COMMENT '是否置顶：0-不置顶，1-置顶',
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

CREATE INDEX idx_announcements_is_top ON announcements(is_top);
CREATE INDEX idx_announcements_create_time ON announcements(create_time);

COMMIT;
