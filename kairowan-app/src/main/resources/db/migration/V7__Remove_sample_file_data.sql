-- V7__Remove_sample_file_data.sql
-- Remove sample file records inserted by earlier migrations

DELETE FROM sys_file
WHERE file_url LIKE 'https://example.com/%';
