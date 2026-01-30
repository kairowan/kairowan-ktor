-- V3__Add_dept_ancestors_and_post_remark.sql
-- 添加部门表的 ancestors 字段和岗位表的 remark 字段

-- 为部门表添加 ancestors 字段（祖级列表）
ALTER TABLE sys_dept ADD COLUMN ancestors VARCHAR(500) DEFAULT '0' COMMENT '祖级列表' AFTER parent_id;

-- 为岗位表添加 remark 字段（备注）
ALTER TABLE sys_post ADD COLUMN remark VARCHAR(500) DEFAULT '' COMMENT '备注' AFTER status;
