-- ============================================================
-- migration-v7-equipped-badge.sql
-- 用途：sys_user 表增加 equipped_badge_id 字段，持久化佩戴状态
-- ============================================================

USE lingjing_digital_human;

ALTER TABLE sys_user
    ADD COLUMN equipped_badge_id BIGINT DEFAULT NULL COMMENT '当前佩戴的成就徽章ID' AFTER role;
