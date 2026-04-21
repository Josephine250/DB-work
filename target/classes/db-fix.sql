-- Fix legacy 'password' column on students table.
-- The column existed from an old schema but is not used (auth is via the users table).
-- Make it nullable so new student inserts don't fail.
ALTER TABLE students MODIFY COLUMN password VARCHAR(255) NULL DEFAULT NULL;
