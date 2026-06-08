-- 路线推荐功能：为 tour_route 表添加 tags 字段
-- 如果已经通过 init.sql 重新初始化则可跳过此文件
-- 在数据库客户端中执行以下语句：

ALTER TABLE tour_route
    ADD COLUMN tags VARCHAR(255) COMMENT '标签(逗号分隔)，如"历史文化,人文"'
    AFTER cover_image;

-- 为现有路线数据设置标签
UPDATE tour_route SET tags = '历史文化' WHERE id = 1;
UPDATE tour_route SET tags = '自然风光' WHERE id = 2;
UPDATE tour_route SET tags = '亲子家庭' WHERE id = 3;
