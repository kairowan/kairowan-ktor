-- V5__Add_user_fields.sql
-- 为用户表添加性别、头像、备注字段

ALTER TABLE sys_user ADD COLUMN gender VARCHAR(1) DEFAULT '2' COMMENT '性别(0男 1女 2未知)' AFTER phone;
ALTER TABLE sys_user ADD COLUMN avatar VARCHAR(500) DEFAULT '' COMMENT '头像URL' AFTER gender;
ALTER TABLE sys_user ADD COLUMN remark VARCHAR(500) DEFAULT '' COMMENT '备注' AFTER avatar;
