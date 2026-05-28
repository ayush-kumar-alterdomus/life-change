-- Ascend Platform Database Initialization Script
-- This script runs automatically on first container startup

-- Create database if not exists (handled by POSTGRES_DB env var, but explicit for clarity)
SELECT 'CREATE DATABASE ascend_dev'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'ascend_dev')\gexec

-- Enable UUID generation extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
