-- ============================================
-- Kairowan Ktor Enterprise 数据库初始化脚本
-- 执行前请确保数据库 kairowan_ktor 已创建
-- ============================================

-- 扩展用户表 (添加新字段)
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS password VARCHAR(200) DEFAULT '';
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS status CHAR(1) DEFAULT '0';
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS email VARCHAR(50) DEFAULT '';
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS phone VARCHAR(20) DEFAULT '';
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS dept_id INT DEFAULT NULL;
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS create_time DATETIME DEFAULT CURRENT_TIMESTAMP;

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    role_id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_key VARCHAR(100) NOT NULL COMMENT '角色标识',
    role_sort INT DEFAULT 0 COMMENT '显示顺序',
    status CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 菜单/权限表
CREATE TABLE IF NOT EXISTS sys_menu (
    menu_id INT PRIMARY KEY AUTO_INCREMENT,
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    parent_id INT DEFAULT 0 COMMENT '父菜单ID',
    order_num INT DEFAULT 0 COMMENT '显示顺序',
    path VARCHAR(200) DEFAULT '' COMMENT '路由地址',
    component VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
    menu_type CHAR(1) DEFAULT '' COMMENT '菜单类型(M目录 C菜单 F按钮)',
    perms VARCHAR(100) DEFAULT '' COMMENT '权限标识',
    icon VARCHAR(100) DEFAULT '#' COMMENT '菜单图标',
    status CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
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

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_oper_log (
    oper_id BIGINT PRIMARY KEY AUTO_INCREMENT,
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
    oper_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 登录日志表
CREATE TABLE IF NOT EXISTS sys_login_log (
    info_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_name VARCHAR(50) DEFAULT '' COMMENT '用户账号',
    ipaddr VARCHAR(128) DEFAULT '' COMMENT '登录IP地址',
    login_location VARCHAR(255) DEFAULT '' COMMENT '登录地点',
    browser VARCHAR(50) DEFAULT '' COMMENT '浏览器类型',
    os VARCHAR(50) DEFAULT '' COMMENT '操作系统',
    status CHAR(1) DEFAULT '0' COMMENT '登录状态(0成功 1失败)',
    msg VARCHAR(255) DEFAULT '' COMMENT '提示消息',
    login_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- ============================================
-- 初始化数据
-- ============================================

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

-- 初始菜单/权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, perms, icon) VALUES
(1, '系统管理', 0, 1, 'system', NULL, 'M', '', 'system'),
(100, '用户管理', 1, 1, 'user', 'system/user/index', 'C', 'system:user:list', 'user'),
(101, '角色管理', 1, 2, 'role', 'system/role/index', 'C', 'system:role:list', 'peoples'),
(102, '菜单管理', 1, 3, 'menu', 'system/menu/index', 'C', 'system:menu:list', 'tree-table'),
(103, '配置管理', 1, 4, 'config', 'system/config/index', 'C', 'system:config:list', 'edit'),
(104, '字典管理', 1, 5, 'dict', 'system/dict/index', 'C', 'system:dict:list', 'dict'),
(1000, '用户查询', 100, 1, '', '', 'F', 'system:user:query', '#'),
(1001, '用户新增', 100, 2, '', '', 'F', 'system:user:add', '#'),
(1002, '用户修改', 100, 3, '', '', 'F', 'system:user:edit', '#'),
(1003, '用户删除', 100, 4, '', '', 'F', 'system:user:remove', '#'),
(1004, '用户导出', 100, 5, '', '', 'F', 'system:user:export', '#'),
(1010, '角色查询', 101, 1, '', '', 'F', 'system:role:query', '#'),
(1011, '角色新增', 101, 2, '', '', 'F', 'system:role:add', '#'),
(1012, '角色修改', 101, 3, '', '', 'F', 'system:role:edit', '#'),
(1013, '角色删除', 101, 4, '', '', 'F', 'system:role:remove', '#'),
(1020, '菜单查询', 102, 1, '', '', 'F', 'system:menu:query', '#'),
(1021, '菜单新增', 102, 2, '', '', 'F', 'system:menu:add', '#'),
(1022, '菜单修改', 102, 3, '', '', 'F', 'system:menu:edit', '#'),
(1023, '菜单删除', 102, 4, '', '', 'F', 'system:menu:remove', '#'),
(1030, '配置查询', 103, 1, '', '', 'F', 'system:config:query', '#'),
(1031, '配置新增', 103, 2, '', '', 'F', 'system:config:add', '#'),
(1032, '配置删除', 103, 3, '', '', 'F', 'system:config:remove', '#'),
(1040, '字典查询', 104, 1, '', '', 'F', 'system:dict:query', '#'),
(1041, '字典新增', 104, 2, '', '', 'F', 'system:dict:add', '#'),
(1042, '字典删除', 104, 3, '', '', 'F', 'system:dict:remove', '#')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 角色-菜单关联 (admin 拥有所有菜单权限)
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1), (1, 100), (1, 101), (1, 102), (1, 103), (1, 104),
(1, 1000), (1, 1001), (1, 1002), (1, 1003), (1, 1004),
(1, 1010), (1, 1011), (1, 1012), (1, 1013),
(1, 1020), (1, 1021), (1, 1022), (1, 1023),
(1, 1030), (1, 1031), (1, 1032),
(1, 1040), (1, 1041), (1, 1042);

-- 普通角色只有查询权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(2, 1), (2, 100), (2, 1000);

-- ============================================
-- 系统配置表
-- ============================================
CREATE TABLE IF NOT EXISTS sys_config (
    config_id INT PRIMARY KEY AUTO_INCREMENT,
    config_name VARCHAR(100) DEFAULT '' COMMENT '参数名称',
    config_key VARCHAR(100) DEFAULT '' COMMENT '参数键名',
    config_value VARCHAR(500) DEFAULT '' COMMENT '参数键值',
    config_type CHAR(1) DEFAULT 'N' COMMENT '系统内置(Y是 N否)',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 初始配置
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type) VALUES
(1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y'),
(2, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y'),
(3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y')
ON DUPLICATE KEY UPDATE config_name = VALUES(config_name);

-- ============================================
-- 字典类型表
-- ============================================
CREATE TABLE IF NOT EXISTS sys_dict_type (
    dict_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dict_name VARCHAR(100) DEFAULT '' COMMENT '字典名称',
    dict_type VARCHAR(100) DEFAULT '' COMMENT '字典类型',
    status CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

-- 字典数据表
CREATE TABLE IF NOT EXISTS sys_dict_data (
    dict_code BIGINT PRIMARY KEY AUTO_INCREMENT,
    dict_sort INT DEFAULT 0 COMMENT '字典排序',
    dict_label VARCHAR(100) DEFAULT '' COMMENT '字典标签',
    dict_value VARCHAR(100) DEFAULT '' COMMENT '字典键值',
    dict_type VARCHAR(100) DEFAULT '' COMMENT '字典类型',
    css_class VARCHAR(100) DEFAULT '' COMMENT '样式属性',
    list_class VARCHAR(100) DEFAULT '' COMMENT '表格回显样式',
    is_default CHAR(1) DEFAULT 'N' COMMENT '是否默认(Y是 N否)',
    status CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

-- 初始字典
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status) VALUES
(1, '用户性别', 'sys_user_sex', '0'),
(2, '系统开关', 'sys_normal_disable', '0'),
(3, '系统是否', 'sys_yes_no', '0')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default) VALUES
(1, 1, '男', '0', 'sys_user_sex', '', '', 'Y'),
(2, 2, '女', '1', 'sys_user_sex', '', '', 'N'),
(3, 3, '未知', '2', 'sys_user_sex', '', '', 'N'),
(4, 1, '正常', '0', 'sys_normal_disable', '', 'primary', 'Y'),
(5, 2, '停用', '1', 'sys_normal_disable', '', 'danger', 'N'),
(6, 1, '是', 'Y', 'sys_yes_no', '', 'primary', 'Y'),
(7, 2, '否', 'N', 'sys_yes_no', '', 'danger', 'N')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- ============================================
-- 部门表
-- ============================================
CREATE TABLE IF NOT EXISTS sys_dept (
    dept_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    ancestors VARCHAR(500) DEFAULT '' COMMENT '祖级列表',
    dept_name VARCHAR(50) DEFAULT '' COMMENT '部门名称',
    order_num INT DEFAULT 0 COMMENT '显示顺序',
    leader VARCHAR(50) DEFAULT '' COMMENT '负责人',
    phone VARCHAR(20) DEFAULT '' COMMENT '联系电话',
    email VARCHAR(50) DEFAULT '' COMMENT '邮箱',
    status CHAR(1) DEFAULT '0' COMMENT '部门状态(0正常 1停用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 初始部门
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader) VALUES
(100, 0, '0', 'Kairowan科技', 0, 'admin'),
(101, 100, '0,100', '研发部门', 1, ''),
(102, 100, '0,100', '市场部门', 2, '')
ON DUPLICATE KEY UPDATE dept_name = VALUES(dept_name);

-- ============================================
-- 岗位表
-- ============================================
CREATE TABLE IF NOT EXISTS sys_post (
    post_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_code VARCHAR(100) DEFAULT '' COMMENT '岗位编码',
    post_name VARCHAR(100) DEFAULT '' COMMENT '岗位名称',
    post_sort INT DEFAULT 0 COMMENT '显示顺序',
    status CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位表';

-- 初始岗位
INSERT INTO sys_post (post_id, post_code, post_name, post_sort) VALUES
(1, 'ceo', '董事长', 1),
(2, 'se', '项目经理', 2),
(3, 'hr', '人力资源', 3),
(4, 'user', '普通员工', 4)
ON DUPLICATE KEY UPDATE post_name = VALUES(post_name);

-- ============================================
-- 文件管理表
-- ============================================
CREATE TABLE IF NOT EXISTS sys_file (
    file_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_name VARCHAR(255) DEFAULT '' COMMENT '原始文件名',
    file_key VARCHAR(500) DEFAULT '' COMMENT '存储路径/Key',
    file_url VARCHAR(1000) DEFAULT '' COMMENT '访问URL',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    file_md5 VARCHAR(64) DEFAULT '' COMMENT '文件MD5',
    file_type VARCHAR(50) DEFAULT '' COMMENT '文件类型(image/video/document/other)',
    mime_type VARCHAR(100) DEFAULT '' COMMENT 'MIME类型',
    storage_type VARCHAR(20) DEFAULT 'local' COMMENT '存储类型(local/minio/oss/cos)',
    bucket VARCHAR(100) DEFAULT '' COMMENT '存储桶',
    create_by VARCHAR(64) DEFAULT '' COMMENT '上传者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件管理表';

-- ============================================
-- 定时任务表
-- ============================================
CREATE TABLE IF NOT EXISTS sys_job (
    job_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_name VARCHAR(100) DEFAULT '' COMMENT '任务名称',
    job_group VARCHAR(100) DEFAULT 'DEFAULT' COMMENT '任务组名',
    invoke_target VARCHAR(500) DEFAULT '' COMMENT '调用目标(类全名)',
    cron_expression VARCHAR(100) DEFAULT '' COMMENT 'CRON表达式',
    misfire_policy VARCHAR(20) DEFAULT '1' COMMENT '计划策略(1立即执行 2执行一次 3放弃)',
    concurrent CHAR(1) DEFAULT '1' COMMENT '是否并发(0允许 1禁止)',
    status CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1暂停)',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务表';

-- 定时任务日志表
CREATE TABLE IF NOT EXISTS sys_job_log (
    job_log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_name VARCHAR(100) DEFAULT '' COMMENT '任务名称',
    job_group VARCHAR(100) DEFAULT '' COMMENT '任务组名',
    invoke_target VARCHAR(500) DEFAULT '' COMMENT '调用目标',
    job_message VARCHAR(1000) DEFAULT '' COMMENT '日志信息',
    status CHAR(1) DEFAULT '0' COMMENT '执行状态(0正常 1失败)',
    exception_info VARCHAR(2000) DEFAULT '' COMMENT '异常信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务日志表';

-- 初始定时任务示例
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, status, remark) VALUES
(1, '系统健康检查', 'DEFAULT', 'com.kairowan.ktor.framework.task.HealthCheckJob', '0 0 * * * ?', '1', '每小时执行一次健康检查')
ON DUPLICATE KEY UPDATE job_name = VALUES(job_name);

SELECT 'Database initialization completed!' AS result;

