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

-- 插入示例数据
INSERT INTO sys_file (file_name, file_type, file_size, file_url, thumbnail_url, uploader_id, uploader_name) VALUES
('项目文档.pdf', 'document', 2621440, 'https://example.com/files/doc1.pdf', 'https://example.com/thumbnails/doc1.jpg', 1, '管理员'),
('系统架构图.png', 'image', 524288, 'https://example.com/files/img1.png', 'https://example.com/thumbnails/img1.jpg', 1, '管理员'),
('需求说明.docx', 'document', 1048576, 'https://example.com/files/doc2.docx', 'https://example.com/thumbnails/doc2.jpg', 1, '管理员'),
('演示视频.mp4', 'video', 10485760, 'https://example.com/files/video1.mp4', 'https://example.com/thumbnails/video1.jpg', 1, '管理员'),
('用户手册.pdf', 'document', 3145728, 'https://example.com/files/doc3.pdf', 'https://example.com/thumbnails/doc3.jpg', 1, '管理员');
