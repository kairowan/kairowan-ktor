-- ============================================
-- Kairowan Ktor Enterprise 最新数据库初始化脚本
-- 目标：直接初始化到当前代码所需的最新结构（不依赖 Flyway 逐版本迁移）
-- ============================================

SET NAMES utf8mb4;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    user_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    user_name VARCHAR(50) NOT NULL UNIQUE COMMENT '用户账号',
    nick_name VARCHAR(50) DEFAULT '' COMMENT '用户昵称',
    password VARCHAR(200) DEFAULT '' COMMENT '密码',
    status CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
    email VARCHAR(50) DEFAULT '' COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT '' COMMENT '手机号',
    gender VARCHAR(1) DEFAULT '2' COMMENT '性别(0男 1女 2未知)',
    avatar VARCHAR(500) DEFAULT '' COMMENT '头像URL',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    dept_id INT DEFAULT NULL COMMENT '部门ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    role_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_key VARCHAR(100) NOT NULL COMMENT '角色标识',
    role_sort INT DEFAULT 0 COMMENT '显示顺序',
    status CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 菜单/权限表
CREATE TABLE IF NOT EXISTS sys_menu (
    menu_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    parent_id INT DEFAULT 0 COMMENT '父菜单ID',
    order_num INT DEFAULT 0 COMMENT '显示顺序',
    path VARCHAR(200) DEFAULT '' COMMENT '路由地址',
    component VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
    menu_type CHAR(1) DEFAULT '' COMMENT '菜单类型(M目录 C菜单 F按钮)',
    perms VARCHAR(100) DEFAULT '' COMMENT '权限标识',
    icon VARCHAR(100) DEFAULT '#' COMMENT '菜单图标',
    status CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- 用户-角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id INT NOT NULL COMMENT '用户ID',
    role_id INT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色-菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id INT NOT NULL COMMENT '角色ID',
    menu_id INT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 部门表
CREATE TABLE IF NOT EXISTS sys_dept (
    dept_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '部门ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    ancestors VARCHAR(500) DEFAULT '0' COMMENT '祖级列表',
    dept_name VARCHAR(50) NOT NULL COMMENT '部门名称',
    order_num INT DEFAULT 0 COMMENT '显示顺序',
    leader VARCHAR(50) DEFAULT '' COMMENT '负责人',
    phone VARCHAR(20) DEFAULT '' COMMENT '联系电话',
    email VARCHAR(50) DEFAULT '' COMMENT '邮箱',
    status CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 岗位表
CREATE TABLE IF NOT EXISTS sys_post (
    post_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '岗位ID',
    post_code VARCHAR(64) NOT NULL COMMENT '岗位编码',
    post_name VARCHAR(50) NOT NULL COMMENT '岗位名称',
    post_sort INT DEFAULT 0 COMMENT '显示顺序',
    status CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位表';

-- 字典类型表
CREATE TABLE IF NOT EXISTS sys_dict_type (
    dict_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '字典主键',
    dict_name VARCHAR(100) DEFAULT '' COMMENT '字典名称',
    dict_type VARCHAR(100) DEFAULT '' COMMENT '字典类型',
    status CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

-- 字典数据表
CREATE TABLE IF NOT EXISTS sys_dict_data (
    dict_code BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '字典编码',
    dict_sort INT DEFAULT 0 COMMENT '字典排序',
    dict_label VARCHAR(100) DEFAULT '' COMMENT '字典标签',
    dict_value VARCHAR(100) DEFAULT '' COMMENT '字典键值',
    dict_type VARCHAR(100) DEFAULT '' COMMENT '字典类型',
    css_class VARCHAR(100) DEFAULT '' COMMENT '样式属性',
    list_class VARCHAR(100) DEFAULT '' COMMENT '表格回显样式',
    is_default CHAR(1) DEFAULT 'N' COMMENT '是否默认(Y是 N否)',
    status CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

-- 参数配置表
CREATE TABLE IF NOT EXISTS sys_config (
    config_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '参数主键',
    config_name VARCHAR(100) DEFAULT '' COMMENT '参数名称',
    config_key VARCHAR(100) DEFAULT '' COMMENT '参数键名',
    config_value VARCHAR(500) DEFAULT '' COMMENT '参数键值',
    config_type CHAR(1) DEFAULT 'N' COMMENT '系统内置(Y是 N否)',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参数配置表';

-- 系统通知表
CREATE TABLE IF NOT EXISTS sys_notification (
    notification_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
    user_id INT NOT NULL COMMENT '用户ID',
    type VARCHAR(20) NOT NULL COMMENT '通知类型(system/message/todo)',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_notification_user_id (user_id),
    INDEX idx_notification_type (type),
    INDEX idx_notification_is_read (is_read),
    INDEX idx_notification_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';

-- 文件表
CREATE TABLE IF NOT EXISTS sys_file (
    file_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文件ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_type VARCHAR(20) NOT NULL COMMENT '文件类型(image/document/video/other)',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    file_url VARCHAR(500) NOT NULL COMMENT '文件URL',
    thumbnail_url VARCHAR(500) DEFAULT NULL COMMENT '缩略图URL',
    uploader_id INT NOT NULL COMMENT '上传者ID',
    uploader_name VARCHAR(50) NOT NULL COMMENT '上传者名称',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_file_type (file_type),
    INDEX idx_file_uploader_id (uploader_id),
    INDEX idx_file_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表';

-- 定时任务表
CREATE TABLE IF NOT EXISTS sys_job (
    job_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    job_name VARCHAR(64) NOT NULL COMMENT '任务名称',
    job_group VARCHAR(64) NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
    invoke_target VARCHAR(500) NOT NULL COMMENT '调用目标字符串',
    cron_expression VARCHAR(255) DEFAULT '' COMMENT 'cron执行表达式',
    misfire_policy VARCHAR(20) DEFAULT '1' COMMENT '计划策略(1立即执行 2执行一次 3放弃)',
    concurrent CHAR(1) DEFAULT '1' COMMENT '是否并发(0允许 1禁止)',
    status CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1暂停)',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务表';

-- 定时任务日志表
CREATE TABLE IF NOT EXISTS sys_job_log (
    job_log_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务日志ID',
    job_name VARCHAR(64) DEFAULT '' COMMENT '任务名称',
    job_group VARCHAR(64) DEFAULT '' COMMENT '任务组名',
    invoke_target VARCHAR(500) DEFAULT '' COMMENT '调用目标字符串',
    job_message VARCHAR(500) DEFAULT '' COMMENT '日志信息',
    status CHAR(1) DEFAULT '0' COMMENT '执行状态(0正常 1失败)',
    exception_info VARCHAR(2000) DEFAULT '' COMMENT '异常信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务日志表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_oper_log (
    oper_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志主键',
    title VARCHAR(50) DEFAULT '' COMMENT '模块标题',
    business_type INT DEFAULT 0 COMMENT '业务类型(0其它 1新增 2修改 3删除)',
    method VARCHAR(100) DEFAULT '' COMMENT '方法名称',
    request_method VARCHAR(10) DEFAULT '' COMMENT '请求方式',
    oper_name VARCHAR(50) DEFAULT '' COMMENT '操作人员',
    oper_url VARCHAR(255) DEFAULT '' COMMENT '请求URL',
    oper_ip VARCHAR(128) DEFAULT '' COMMENT '主机地址',
    oper_param VARCHAR(2000) DEFAULT '' COMMENT '请求参数',
    json_result VARCHAR(2000) DEFAULT '' COMMENT '返回参数',
    status INT DEFAULT 0 COMMENT '操作状态(0正常 1异常)',
    error_msg VARCHAR(2000) DEFAULT '' COMMENT '错误消息',
    oper_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 登录日志表
CREATE TABLE IF NOT EXISTS sys_login_log (
    info_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '访问ID',
    user_name VARCHAR(50) DEFAULT '' COMMENT '用户账号',
    ipaddr VARCHAR(128) DEFAULT '' COMMENT '登录IP地址',
    login_location VARCHAR(255) DEFAULT '' COMMENT '登录地点',
    browser VARCHAR(50) DEFAULT '' COMMENT '浏览器类型',
    os VARCHAR(50) DEFAULT '' COMMENT '操作系统',
    status CHAR(1) DEFAULT '0' COMMENT '登录状态(0成功 1失败)',
    msg VARCHAR(255) DEFAULT '' COMMENT '提示消息',
    login_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- ============================================
-- 初始化数据
-- ============================================

-- 初始管理员用户 (密码: admin123，BCrypt 加密)
INSERT INTO sys_user (user_id, user_name, nick_name, password, status, email, phone, gender, avatar, remark)
VALUES (1, 'admin', '超级管理员', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', 'admin@kairowan.com', '15888888888', '2', '', '')
ON DUPLICATE KEY UPDATE password = VALUES(password), status = VALUES(status);

-- 初始角色
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, status) VALUES
(1, '超级管理员', 'admin', 1, '0'),
(2, '普通角色', 'common', 2, '0')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name), role_key = VALUES(role_key);

-- 初始菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, perms, icon, status) VALUES
(1, '系统管理', 0, 1, 'system', NULL, 'M', '', 'system', '0'),
(2, '用户管理', 1, 1, 'user', 'system/user/index', 'C', 'system:user:list', 'user', '0'),
(3, '角色管理', 1, 2, 'role', 'system/role/index', 'C', 'system:role:list', 'peoples', '0'),
(4, '菜单管理', 1, 3, 'menu', 'system/menu/index', 'C', 'system:menu:list', 'tree-table', '0'),
(5, '部门管理', 1, 4, 'dept', 'system/dept/index', 'C', 'system:dept:list', 'tree', '0')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), perms = VALUES(perms);

-- 用户-角色关联
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 角色-菜单关联
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5);

-- 初始部门
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status) VALUES
(1, 0, '0', '凯若湾科技', 0, 'admin', '', '', '0'),
(2, 1, '0,1', '研发部门', 1, 'admin', '', '', '0'),
(3, 1, '0,1', '市场部门', 2, 'admin', '', '', '0')
ON DUPLICATE KEY UPDATE dept_name = VALUES(dept_name), ancestors = VALUES(ancestors);

-- 初始岗位
INSERT INTO sys_post (post_id, post_code, post_name, post_sort, status, remark) VALUES
(1, 'ceo', '董事长', 1, '0', ''),
(2, 'se', '项目经理', 2, '0', ''),
(3, 'hr', '人力资源', 3, '0', ''),
(4, 'staff', '普通员工', 4, '0', '')
ON DUPLICATE KEY UPDATE post_name = VALUES(post_name), post_code = VALUES(post_code);

-- 初始配置
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, remark) VALUES
(1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', ''),
(2, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y', ''),
(3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y', '')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), config_name = VALUES(config_name);

-- 初始字典类型
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark) VALUES
(1, '用户性别', 'sys_user_sex', '0', ''),
(2, '菜单状态', 'sys_show_hide', '0', ''),
(3, '系统开关', 'sys_normal_disable', '0', ''),
(4, '任务状态', 'sys_job_status', '0', '')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), dict_type = VALUES(dict_type);

-- 初始字典数据
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, remark) VALUES
(1, 1, '男', '0', 'sys_user_sex', '', '', 'N', '0', ''),
(2, 2, '女', '1', 'sys_user_sex', '', '', 'N', '0', ''),
(3, 3, '未知', '2', 'sys_user_sex', '', '', 'Y', '0', ''),
(4, 1, '显示', '0', 'sys_show_hide', '', 'primary', 'Y', '0', ''),
(5, 2, '隐藏', '1', 'sys_show_hide', '', 'danger', 'N', '0', ''),
(6, 1, '正常', '0', 'sys_normal_disable', '', 'primary', 'Y', '0', ''),
(7, 2, '停用', '1', 'sys_normal_disable', '', 'danger', 'N', '0', ''),
(8, 1, '正常', '0', 'sys_job_status', '', 'primary', 'Y', '0', ''),
(9, 2, '暂停', '1', 'sys_job_status', '', 'danger', 'N', '0', '')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label), dict_value = VALUES(dict_value);

-- 初始通知
INSERT INTO sys_notification (notification_id, user_id, type, title, content, is_read) VALUES
(1, 1, 'system', '系统升级通知', '系统将于今晚 22:00 进行升级维护，预计维护时间 2 小时，请提前保存工作。', false),
(2, 1, 'message', '新消息提醒', '您有一条新的系统消息，请及时查看。', false),
(3, 1, 'todo', '待办事项提醒', '您有 3 个待办事项即将到期，请及时处理。', false)
ON DUPLICATE KEY UPDATE title = VALUES(title), content = VALUES(content), is_read = VALUES(is_read);

