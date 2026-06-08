-- 用户自定义路线表
-- 如果已经通过 init.sql 重新初始化则可跳过此文件
-- 在数据库客户端中执行以下语句：

CREATE TABLE IF NOT EXISTS user_route (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(100) NOT NULL COMMENT '路线名称',
    attraction_ids JSON COMMENT '排序后的景点ID列表',
    total_distance_meters INT DEFAULT 0 COMMENT '总路程(米)',
    estimated_minutes INT DEFAULT 0 COMMENT '预计耗时(分钟)',
    attraction_count INT DEFAULT 0 COMMENT '景点数量',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户自定义路线表';
