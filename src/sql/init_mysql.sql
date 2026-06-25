-- 校园失物招领平台 - MySQL数据库初始化脚本

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS campus_lost_found DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_lost_found;

-- 2. 创建用户表
CREATE TABLE users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    contact VARCHAR(100),
    avatar VARCHAR(200),
    role TINYINT DEFAULT 0 COMMENT '用户角色：0-普通用户，1-管理员',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status TINYINT DEFAULT 0 COMMENT '用户状态：0-正常，1-禁用'
) COMMENT='用户表';

-- 3. 创建物品表
CREATE TABLE items (
    item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    item_type TINYINT NOT NULL COMMENT '物品类型：1-证件, 2-钥匙, 3-电子设备, 4-衣物, 5-钱包, 6-其他',
    publish_type TINYINT NOT NULL COMMENT '发布类型：0-丢失, 1-捡到',
    title VARCHAR(200) NOT NULL,
    description TEXT,
    features VARCHAR(500),
    images VARCHAR(1000) COMMENT '图片URL（多个图片用逗号分隔）',
    location VARCHAR(200) NOT NULL,
    event_time TIMESTAMP NOT NULL,
    publisher_id BIGINT NOT NULL,
    publisher_name VARCHAR(50) NOT NULL,
    contact VARCHAR(100),
    storage_location VARCHAR(200),
    status TINYINT DEFAULT 0 COMMENT '物品状态：0-未找回/未归还, 1-已找回/已归还',
    view_count INT DEFAULT 0,
    collect_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_items_publisher FOREIGN KEY (publisher_id) REFERENCES users(user_id)
) COMMENT='物品信息表';

-- 4. 创建评论表
CREATE TABLE comments (
    comment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    item_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    user_nickname VARCHAR(50) NOT NULL,
    user_avatar VARCHAR(200),
    content TEXT NOT NULL,
    parent_id BIGINT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    CONSTRAINT fk_comments_item FOREIGN KEY (item_id) REFERENCES items(item_id),
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) COMMENT='评论表';

-- 5. 创建公告表
CREATE TABLE announcements (
    announcement_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    publisher_id BIGINT NOT NULL,
    publisher_name VARCHAR(50) NOT NULL,
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶：0-不置顶，1-置顶',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_announcements_publisher FOREIGN KEY (publisher_id) REFERENCES users(user_id)
) COMMENT='公告表';

-- 6. 创建索引
-- 用户表索引
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_status ON users(status);

-- 物品表索引
CREATE INDEX idx_items_publish_type ON items(publish_type);
CREATE INDEX idx_items_item_type ON items(item_type);
CREATE INDEX idx_items_status ON items(status);
CREATE INDEX idx_items_publisher_id ON items(publisher_id);
CREATE INDEX idx_items_create_time ON items(create_time);

-- 评论表索引
CREATE INDEX idx_comments_item_id ON comments(item_id);
CREATE INDEX idx_comments_user_id ON comments(user_id);
CREATE INDEX idx_comments_create_time ON comments(create_time);

-- 公告表索引
CREATE INDEX idx_announcements_is_top ON announcements(is_top);
CREATE INDEX idx_announcements_create_time ON announcements(create_time);

-- 7. 插入默认管理员用户（密码：admin123，建议首次登录后修改）
INSERT INTO users (username, password, nickname, contact, role, status)
VALUES ('admin', 'admin123', '系统管理员', 'admin@campus.edu', 1, 0);

-- 8. 插入示例公告
INSERT INTO announcements (title, content, publisher_id, publisher_name, is_top)
VALUES ('欢迎使用校园失物招领平台',
        '本平台旨在帮助同学们找回丢失的物品，请大家文明使用，如实发布信息。',
        1, '系统管理员', 1);

INSERT INTO announcements (title, content, publisher_id, publisher_name, is_top)
VALUES ('失物招领注意事项',
        '1. 发布信息请如实描述物品特征\n2. 贵重物品请及时联系保卫处\n3. 请勿发布虚假信息\n4. 物品找回后请及时更新状态',
        1, '系统管理员', 0);

COMMIT;
