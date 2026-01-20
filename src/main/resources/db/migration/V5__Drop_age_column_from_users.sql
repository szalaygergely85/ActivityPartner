-- Drop redundant age column (age is calculated from birth_date)
ALTER TABLE users DROP COLUMN IF EXISTS age;
