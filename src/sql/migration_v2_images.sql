-- 校园失物招领平台 - 数据库迁移 v2：扩展图片字段长度
-- 说明：OSS 图片 URL 较长，原 VARCHAR(200/1000) 可能不够用

USE campus_lost_found;

-- 1. 用户表：avatar 字段扩展到 500
ALTER TABLE users MODIFY avatar VARCHAR(500) COMMENT '头像URL';

-- 2. 物品表：images 字段扩展到 TEXT（多个逗号分隔的OSS URL可能很长）
ALTER TABLE items MODIFY images TEXT COMMENT '图片URL（多个图片用逗号分隔）';

-- 3. 物品表：新增 image_list 字段，以 JSON 数组存储图片URL（更规范）
ALTER TABLE items ADD COLUMN image_list JSON DEFAULT NULL COMMENT '图片URL列表（JSON数组格式）' AFTER images;

COMMIT;
