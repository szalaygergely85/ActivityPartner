-- Drop redundant age column (age is calculated from birth_date)
-- Note: MySQL doesn't support IF EXISTS for columns, so this will fail if column doesn't exist
ALTER TABLE users DROP COLUMN age;
