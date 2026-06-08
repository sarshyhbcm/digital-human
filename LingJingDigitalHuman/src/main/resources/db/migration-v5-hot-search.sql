-- 热搜管理表（v5）
-- 管理端配置热搜条目，游客端展示
CREATE TABLE IF NOT EXISTS hot_search (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question VARCHAR(255) NOT NULL COMMENT '热搜问题',
    heat_label VARCHAR(10) NOT NULL DEFAULT 'wen' COMMENT '热度标签：bao/re/huo/wen',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序（越小越前）',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='热搜管理';

-- 初始示例数据
INSERT INTO hot_search (question, heat_label, sort_order, enabled) VALUES
('灵山大佛有多高？', 'bao', 1, 1),
('拈花湾门票多少钱？', 're', 2, 1),
('梵宫有什么看点？', 're', 3, 1),
('景区开放时间是？', 'huo', 4, 1),
('怎么去灵山胜境？', 'huo', 5, 1),
('有没有推荐路线？', 'wen', 6, 1),
('附近有什么美食？', 'wen', 7, 1),
('可以带宠物吗？', 'wen', 8, 1);
