-- Add birth_date column to users table
-- Note: Column already exists from previous migration attempt, so only ensuring constraints

-- Update any NULL values
UPDATE users SET birth_date = '2000-01-01' WHERE birth_date IS NULL;

-- Make the column NOT NULL
ALTER TABLE users MODIFY COLUMN birth_date DATE NOT NULL;
