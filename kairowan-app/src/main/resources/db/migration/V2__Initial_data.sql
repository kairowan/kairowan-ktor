-- V2__Initial_data.sql
-- Flyway 初始化数据脚本

-- 初始管理员用户 (密码: admin123，BCrypt 加密)
INSERT INTO sys_user (user_id, user_name, nick_name, password, status, email, phone) VALUES
(1, 'admin', '超级管理员', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', 'admin@kairowan.com', '15888888888')
ON DUPLICATE KEY UPDATE password = VALUES(password);

-- 初始角色
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, status) VALUES
(1, '超级管理员', 'admin', 1, '0'),
(2, '普通角色', 'common', 2, '0')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

-- 用户-角色关联 (admin 拥有 admin 角色)
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 初始菜单数据
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, perms, icon, status) VALUES
(1, '系统管理', 0, 1, 'system', NULL, 'M', '', 'system', '0'),
(2, '用户管理', 1, 1, 'user', 'system/user/index', 'C', 'system:user:list', 'user', '0'),
(3, '角色管理', 1, 2, 'role', 'system/role/index', 'C', 'system:role:list', 'peoples', '0'),
(4, '菜单管理', 1, 3, 'menu', 'system/menu/index', 'C', 'system:menu:list', 'tree-table', '0'),
(5, '部门管理', 1, 4, 'dept', 'system/dept/index', 'C', 'system:dept:list', 'tree', '0')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 角色-菜单关联 (超级管理员拥有所有菜单权限)
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5);

-- 初始部门
INSERT INTO sys_dept (dept_id, parent_id, dept_name, order_num, leader, status) VALUES
(1, 0, '凯若湾科技', 0, 'admin', '0'),
(2, 1, '研发部门', 1, 'admin', '0'),
(3, 1, '市场部门', 2, 'admin', '0')
ON DUPLICATE KEY UPDATE dept_name = VALUES(dept_name);

-- 初始配置
INSERT INTO sys_config (config_name, config_key, config_value, config_type) VALUES
('主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y'),
('用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y'),
('主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);

-- 初始字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status) VALUES
('用户性别', 'sys_user_sex', '0'),
('菜单状态', 'sys_show_hide', '0'),
('系统开关', 'sys_normal_disable', '0'),
('任务状态', 'sys_job_status', '0')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 初始字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status) VALUES
(1, '男', '0', 'sys_user_sex', '0'),
(2, '女', '1', 'sys_user_sex', '0'),
(1, '显示', '0', 'sys_show_hide', '0'),
(2, '隐藏', '1', 'sys_show_hide', '0'),
(1, '正常', '0', 'sys_normal_disable', '0'),
(2, '停用', '1', 'sys_normal_disable', '0'),
(1, '正常', '0', 'sys_job_status', '0'),
(2, '暂停', '1', 'sys_job_status', '0')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);
