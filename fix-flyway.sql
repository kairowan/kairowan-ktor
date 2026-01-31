-- 修复 Flyway 迁移失败的问题

-- 1. 检查当前字段是否存在
SELECT
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'kairowan_ktor'
  AND TABLE_NAME = 'sys_dept'
  AND COLUMN_NAME = 'ancestors';

SELECT
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'kairowan_ktor'
  AND TABLE_NAME = 'sys_post'
  AND COLUMN_NAME = 'remark';

-- 2. 如果上面的查询返回空，说明字段不存在，执行以下语句添加字段
-- 如果字段已存在，跳过这两条语句
ALTER TABLE sys_dept ADD COLUMN ancestors VARCHAR(500) DEFAULT '0' COMMENT '祖级列表' AFTER parent_id;
ALTER TABLE sys_post ADD COLUMN remark VARCHAR(500) DEFAULT '' COMMENT '备注' AFTER status;

-- 3. 删除失败的 Flyway 迁移记录
DELETE FROM flyway_schema_history WHERE version = '3' AND success = 0;

-- 4. 查看 Flyway 历史记录
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
