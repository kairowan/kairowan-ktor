-- V6__Create_file_table.sql
-- 创建文件表

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
    INDEX idx_uploader_id (uploader_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表';
