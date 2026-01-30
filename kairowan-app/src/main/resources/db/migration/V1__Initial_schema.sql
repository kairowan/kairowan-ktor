-- V1__Initial_schema.sql
-- Flyway 初始化脚本 - 创建所有表

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    user_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    user_name VARCHAR(50) NOT NULL UNIQUE COMMENT '用户账号',
    nick_name VARCHAR(50) DEFAULT '' COMMENT '用户昵称',
    password VARCHAR(200) DEFAULT '' COMMENT '密码',
    status CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
    email VARCHAR(50) DEFAULT '' COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT '' COMMENT '手机号',
    dept_id INT DEFAULT NULL COMMENT '部门ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

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

-- 部门表
CREATE TABLE IF NOT EXISTS sys_dept (
    dept_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '部门ID',
    parent_id INT DEFAULT 0 COMMENT '父部门ID',
    dept_name VARCHAR(50) NOT NULL COMMENT '部门名称',
    order_num INT DEFAULT 0 COMMENT '显示顺序',
    leader VARCHAR(50) DEFAULT '' COMMENT '负责人',
    phone VARCHAR(20) DEFAULT '' COMMENT '联系电话',
    email VARCHAR(50) DEFAULT '' COMMENT '邮箱',
    status CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 岗位表
CREATE TABLE IF NOT EXISTS sys_post (
    post_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '岗位ID',
    post_code VARCHAR(64) NOT NULL COMMENT '岗位编码',
    post_name VARCHAR(50) NOT NULL COMMENT '岗位名称',
    post_sort INT DEFAULT 0 COMMENT '显示顺序',
    status CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位表';

-- 字典类型表
CREATE TABLE IF NOT EXISTS sys_dict_type (
    dict_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '字典主键',
    dict_name VARCHAR(100) DEFAULT '' COMMENT '字典名称',
    dict_type VARCHAR(100) DEFAULT '' COMMENT '字典类型',
    status CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

-- 字典数据表
CREATE TABLE IF NOT EXISTS sys_dict_data (
    dict_code INT PRIMARY KEY AUTO_INCREMENT COMMENT '字典编码',
    dict_sort INT DEFAULT 0 COMMENT '字典排序',
    dict_label VARCHAR(100) DEFAULT '' COMMENT '字典标签',
    dict_value VARCHAR(100) DEFAULT '' COMMENT '字典键值',
    dict_type VARCHAR(100) DEFAULT '' COMMENT '字典类型',
    status CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

-- 参数配置表
CREATE TABLE IF NOT EXISTS sys_config (
    config_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '参数主键',
    config_name VARCHAR(100) DEFAULT '' COMMENT '参数名称',
    config_key VARCHAR(100) DEFAULT '' COMMENT '参数键名',
    config_value VARCHAR(500) DEFAULT '' COMMENT '参数键值',
    config_type CHAR(1) DEFAULT 'N' COMMENT '系统内置(Y是 N否)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参数配置表';

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

-- 定时任务表
CREATE TABLE IF NOT EXISTS sys_job (
    job_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    job_name VARCHAR(64) NOT NULL COMMENT '任务名称',
    job_group VARCHAR(64) NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
    invoke_target VARCHAR(500) NOT NULL COMMENT '调用目标字符串',
    cron_expression VARCHAR(255) DEFAULT '' COMMENT 'cron执行表达式',
    status CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1暂停)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务表';
