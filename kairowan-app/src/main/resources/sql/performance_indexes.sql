-- =====================================================
-- Kairowan-Ktor 性能优化 - 数据库索引
-- 执行此脚本可显著提升查询性能
-- =====================================================

USE kairowan_ktor;

-- =====================================================
-- 1. 用户表索引
-- =====================================================
-- 用户名唯一索引（登录查询）
CREATE UNIQUE INDEX IF NOT EXISTS idx_sys_user_user_name ON sys_user(user_name);

-- 状态索引（列表查询）
CREATE INDEX IF NOT EXISTS idx_sys_user_status ON sys_user(status);

-- 部门ID索引（部门用户查询）
CREATE INDEX IF NOT EXISTS idx_sys_user_dept_id ON sys_user(dept_id);

-- 创建时间索引（排序查询）
CREATE INDEX IF NOT EXISTS idx_sys_user_create_time ON sys_user(create_time DESC);

-- 邮箱索引（查重）
CREATE INDEX IF NOT EXISTS idx_sys_user_email ON sys_user(email);

-- 手机号索引（查重）
CREATE INDEX IF NOT EXISTS idx_sys_user_phone ON sys_user(phone);

-- =====================================================
-- 2. 角色表索引
-- =====================================================
-- 角色标识索引（权限查询）
CREATE INDEX IF NOT EXISTS idx_sys_role_role_key ON sys_role(role_key);

-- 状态索引（列表查询）
CREATE INDEX IF NOT EXISTS idx_sys_role_status ON sys_role(status);

-- 排序索引
CREATE INDEX IF NOT EXISTS idx_sys_role_role_sort ON sys_role(role_sort);

-- =====================================================
-- 3. 菜单表索引
-- =====================================================
-- 父级ID索引（树形查询）
CREATE INDEX IF NOT EXISTS idx_sys_menu_parent_id ON sys_menu(parent_id);

-- 菜单类型索引
CREATE INDEX IF NOT EXISTS idx_sys_menu_menu_type ON sys_menu(menu_type);

-- 状态索引
CREATE INDEX IF NOT EXISTS idx_sys_menu_status ON sys_menu(status);

-- 排序索引
CREATE INDEX IF NOT EXISTS idx_sys_menu_order_num ON sys_menu(order_num);

-- 复合索引（常用查询组合）
CREATE INDEX IF NOT EXISTS idx_sys_menu_type_status ON sys_menu(menu_type, status);

-- =====================================================
-- 4. 用户角色关联表索引
-- =====================================================
-- 用户ID索引（查询用户角色）
CREATE INDEX IF NOT EXISTS idx_sys_user_role_user_id ON sys_user_role(user_id);

-- 角色ID索引（查询角色用户）
CREATE INDEX IF NOT EXISTS idx_sys_user_role_role_id ON sys_user_role(role_id);

-- 复合索引（权限查询优化）
CREATE INDEX IF NOT EXISTS idx_sys_user_role_user_role ON sys_user_role(user_id, role_id);

-- =====================================================
-- 5. 角色菜单关联表索引
-- =====================================================
-- 角色ID索引（查询角色菜单）
CREATE INDEX IF NOT EXISTS idx_sys_role_menu_role_id ON sys_role_menu(role_id);

-- 菜单ID索引（查询菜单角色）
CREATE INDEX IF NOT EXISTS idx_sys_role_menu_menu_id ON sys_role_menu(menu_id);

-- 复合索引（权限查询优化）
CREATE INDEX IF NOT EXISTS idx_sys_role_menu_role_menu ON sys_role_menu(role_id, menu_id);

-- =====================================================
-- 6. 操作日志表索引
-- =====================================================
-- 操作时间索引（时间范围查询）
CREATE INDEX IF NOT EXISTS idx_sys_oper_log_oper_time ON sys_oper_log(oper_time DESC);

-- 操作人索引（按用户查询）
CREATE INDEX IF NOT EXISTS idx_sys_oper_log_oper_name ON sys_oper_log(oper_name);

-- 状态索引（按状态查询）
CREATE INDEX IF NOT EXISTS idx_sys_oper_log_status ON sys_oper_log(status);

-- 业务类型索引
CREATE INDEX IF NOT EXISTS idx_sys_oper_log_business_type ON sys_oper_log(business_type);

-- 复合索引（常用查询组合）
CREATE INDEX IF NOT EXISTS idx_sys_oper_log_time_status ON sys_oper_log(oper_time DESC, status);

-- =====================================================
-- 7. 登录日志表索引
-- =====================================================
-- 登录时间索引
CREATE INDEX IF NOT EXISTS idx_sys_login_log_login_time ON sys_login_log(login_time DESC);

-- 用户名索引
CREATE INDEX IF NOT EXISTS idx_sys_login_log_user_name ON sys_login_log(user_name);

-- 状态索引
CREATE INDEX IF NOT EXISTS idx_sys_login_log_status ON sys_login_log(status);

-- IP地址索引（安全分析）
CREATE INDEX IF NOT EXISTS idx_sys_login_log_ipaddr ON sys_login_log(ipaddr);

-- =====================================================
-- 8. 部门表索引
-- =====================================================
-- 父级ID索引（树形查询）
CREATE INDEX IF NOT EXISTS idx_sys_dept_parent_id ON sys_dept(parent_id);

-- 状态索引
CREATE INDEX IF NOT EXISTS idx_sys_dept_status ON sys_dept(status);

-- 排序索引
CREATE INDEX IF NOT EXISTS idx_sys_dept_order_num ON sys_dept(order_num);

-- =====================================================
-- 9. 岗位表索引
-- =====================================================
-- 状态索引
CREATE INDEX IF NOT EXISTS idx_sys_post_status ON sys_post(status);

-- 岗位编码索引
CREATE INDEX IF NOT EXISTS idx_sys_post_post_code ON sys_post(post_code);

-- =====================================================
-- 10. 系统配置表索引
-- =====================================================
-- 配置键唯一索引
CREATE UNIQUE INDEX IF NOT EXISTS idx_sys_config_config_key ON sys_config(config_key);

-- =====================================================
-- 11. 数据字典表索引
-- =====================================================
-- 字典类型索引
CREATE INDEX IF NOT EXISTS idx_sys_dict_type_dict_type ON sys_dict_type(dict_type);

-- 字典数据类型索引
CREATE INDEX IF NOT EXISTS idx_sys_dict_data_dict_type ON sys_dict_data(dict_type);

-- 字典数据状态索引
CREATE INDEX IF NOT EXISTS idx_sys_dict_data_status ON sys_dict_data(status);

-- 复合索引（常用查询）
CREATE INDEX IF NOT EXISTS idx_sys_dict_data_type_status ON sys_dict_data(dict_type, status);

-- =====================================================
-- 12. 定时任务表索引
-- =====================================================
-- 任务名称索引
CREATE INDEX IF NOT EXISTS idx_sys_job_job_name ON sys_job(job_name);

-- 任务组索引
CREATE INDEX IF NOT EXISTS idx_sys_job_job_group ON sys_job(job_group);

-- 状态索引
CREATE INDEX IF NOT EXISTS idx_sys_job_status ON sys_job(status);

-- =====================================================
-- 验证索引创建
-- =====================================================
-- 查看所有索引
SELECT
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX,
    INDEX_TYPE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'kairowan_ktor'
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- 分析表统计信息
ANALYZE TABLE sys_user, sys_role, sys_menu, sys_user_role, sys_role_menu,
             sys_oper_log, sys_login_log, sys_dept, sys_post,
             sys_config, sys_dict_type, sys_dict_data, sys_job;

-- =====================================================
-- 性能优化建议
-- =====================================================
-- 1. 定期清理日志表（保留最近90天）
-- 2. 考虑日志表分区（按月分区）
-- 3. 定期执行 OPTIMIZE TABLE 优化表
-- 4. 监控慢查询日志
-- 5. 使用 EXPLAIN 分析查询计划
