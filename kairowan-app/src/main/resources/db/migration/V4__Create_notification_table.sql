-- V4__Create_notification_table.sql
-- 创建通知表

CREATE TABLE IF NOT EXISTS sys_notification (
    notification_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
    user_id INT NOT NULL COMMENT '用户ID',
    type VARCHAR(20) NOT NULL COMMENT '通知类型(system/message/todo)',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_is_read (is_read),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';

-- 插入示例数据
INSERT INTO sys_notification (user_id, type, title, content, is_read) VALUES
(1, 'system', '系统升级通知', '系统将于今晚 22:00 进行升级维护，预计维护时间 2 小时，请提前保存工作。', false),
(1, 'message', '新消息提醒', '您有一条新的系统消息，请及时查看。', false),
(1, 'todo', '待办事项提醒', '您有 3 个待办事项即将到期，请及时处理。', false),
(1, 'system', '功能更新通知', '系统新增了数据分析功能，欢迎体验！', true),
(1, 'message', '评论回复', '您的评论收到了新的回复。', true);
