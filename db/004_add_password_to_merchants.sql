-- Add password field to merchants table
-- This migration adds password support to the merchants table for authentication

BEGIN;

-- Add password column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'merchants' AND column_name = 'password'
    ) THEN
        ALTER TABLE merchants ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT '';
    END IF;
END
$$;

COMMIT;
