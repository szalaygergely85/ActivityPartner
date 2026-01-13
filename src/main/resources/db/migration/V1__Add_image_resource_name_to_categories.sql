-- Add image_resource_name column to categories table
-- Check if column exists before adding
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'categories'
    AND COLUMN_NAME = 'image_resource_name'
);

SET @sql = IF(@column_exists = 0,
    'ALTER TABLE categories ADD COLUMN image_resource_name VARCHAR(100)',
    'SELECT ''Column already exists'' AS msg'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
