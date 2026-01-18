-- Add birth_date column to users table

-- Step 1: Add birth_date column as nullable with a default value
ALTER TABLE users ADD COLUMN birth_date DATE DEFAULT '2000-01-01';

-- Step 2: Update any NULL values (shouldn't be any, but just in case)
UPDATE users SET birth_date = '2000-01-01' WHERE birth_date IS NULL;

-- Step 3: Make the column NOT NULL and remove default
ALTER TABLE users MODIFY COLUMN birth_date DATE NOT NULL;
