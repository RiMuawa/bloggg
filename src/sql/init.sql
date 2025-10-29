-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS bloggg
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE bloggg;

-- =========================================================
-- 2. bookmarks 表：博客导航与收藏
-- =========================================================
DROP TABLE IF EXISTS bookmarks;

CREATE TABLE bookmarks (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
                           title VARCHAR(255) DEFAULT NULL COMMENT '标题',
                           url VARCHAR(1000) NOT NULL COMMENT '博客网址',
                           description VARCHAR(1000) DEFAULT NULL COMMENT '描述',
                           has_update BOOLEAN DEFAULT FALSE COMMENT '是否检测到更新',
                           last_fetched_at DATETIME DEFAULT NULL COMMENT '上次抓取时间',
                           last_content_hash VARCHAR(64) DEFAULT NULL COMMENT '上次内容哈希值',
                           created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='博客收藏与导航';

-- 常用索引
CREATE INDEX idx_bookmarks_url ON bookmarks(url(255));
CREATE INDEX idx_bookmarks_update ON bookmarks(has_update);

-- =========================================================
-- 3. subscriptions 表：博客订阅配置
-- =========================================================
DROP TABLE IF EXISTS subscriptions;

CREATE TABLE subscriptions (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
                               bookmark_id BIGINT DEFAULT NULL COMMENT '关联的书签 ID',
                               url VARCHAR(1000) NOT NULL COMMENT '订阅博客地址',
                               notify_email VARCHAR(255) NOT NULL COMMENT '通知邮箱',
                               period_hours INT DEFAULT 24 COMMENT '抓取周期（小时）',
                               enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用订阅',
                               last_checked_at DATETIME DEFAULT NULL COMMENT '上次检查时间',
                               last_content_hash VARCHAR(64) DEFAULT NULL COMMENT '上次内容哈希值',
                               created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               CONSTRAINT fk_subscription_bookmark FOREIGN KEY (bookmark_id)
                                   REFERENCES bookmarks(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='博客订阅信息';

-- 索引
CREATE INDEX idx_subscriptions_url ON subscriptions(url(255));
CREATE INDEX idx_subscriptions_email ON subscriptions(notify_email);
CREATE INDEX idx_subscriptions_enabled ON subscriptions(enabled);