-- interview_configs 增加描述字段（幂等）
SET @has := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='session_db' AND TABLE_NAME='interview_configs' AND COLUMN_NAME='description');
SET @s := IF(@has=0, 'ALTER TABLE session_db.interview_configs ADD COLUMN description VARCHAR(255) NULL COMMENT ''配置描述'' AFTER name', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;