-- Add is_guest flag to users table for guest mode support
ALTER TABLE users
    ADD COLUMN is_guest BOOLEAN NOT NULL DEFAULT FALSE;

-- Create index for guest user queries
CREATE INDEX idx_users_is_guest ON users(is_guest);
