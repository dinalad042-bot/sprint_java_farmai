-- ============================================================
-- Migration Script: Update Column Names for Java Application
-- This script updates the existing database to use Symfony-compatible column names
-- Run this AFTER creating the unified schema if you have existing data to preserve
-- ============================================================

USE `farmia_new`;

-- Backup existing data before migration (optional but recommended)
-- CREATE TABLE analyse_backup AS SELECT * FROM analyse;

-- Update analyse table column names to match Symfony conventions
-- Note: This assumes you're migrating from the old Java schema to the unified schema

-- If the table already has the old column names, rename them
-- ALTER TABLE analyse 
-- CHANGE COLUMN id_technicien id_technicien_id INT(11) DEFAULT NULL,
-- CHANGE COLUMN id_ferme id_ferme_id INT(11) NOT NULL;

-- Add new columns if they don't exist
ALTER TABLE analyse 
ADD COLUMN IF NOT EXISTS weather_data JSON DEFAULT NULL,
ADD COLUMN IF NOT EXISTS ai_diagnosis TEXT DEFAULT NULL,
ADD COLUMN IF NOT EXISTS confidence_score DECIMAL(5,2) DEFAULT NULL;

-- Update ferme table to add coordinates if they don't exist
ALTER TABLE ferme 
ADD COLUMN IF NOT EXISTS latitude DECIMAL(10,8) DEFAULT NULL,
ADD COLUMN IF NOT EXISTS longitude DECIMAL(11,8) DEFAULT NULL;

-- Update user table to add coordinates if they don't exist
ALTER TABLE user 
ADD COLUMN IF NOT EXISTS latitude DECIMAL(10,8) DEFAULT NULL,
ADD COLUMN IF NOT EXISTS longitude DECIMAL(11,8) DEFAULT NULL;

-- Ensure animal table exists (rename from animaux if needed)
-- CREATE TABLE IF NOT EXISTS animal LIKE animaux;
-- INSERT IGNORE INTO animal SELECT * FROM animaux;

-- Ensure plante table exists (rename from plantes if needed)  
-- CREATE TABLE IF NOT EXISTS plante LIKE plantes;
-- INSERT IGNORE INTO plante SELECT * FROM plantes;

-- Update foreign key constraints for analyse table
-- ALTER TABLE analyse DROP FOREIGN KEY IF EXISTS analyse_ibfk_1;
-- ALTER TABLE analyse DROP FOREIGN KEY IF EXISTS analyse_ibfk_2;

-- ALTER TABLE analyse 
-- ADD CONSTRAINT FK_351B0C7EAD6DA333 FOREIGN KEY (id_technicien_id) REFERENCES user (id_user) ON DELETE SET NULL,
-- ADD CONSTRAINT FK_351B0C7E4843FDA7 FOREIGN KEY (id_ferme_id) REFERENCES ferme (id_ferme) ON DELETE CASCADE;

COMMIT;