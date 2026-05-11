-- Migration: Add farmer request workflow columns to analyse table
-- Run this script to enable the expert/agricole request workflow

-- Add missing columns for farmer request workflow
ALTER TABLE `analyse`
    ADD COLUMN IF NOT EXISTS `statut` VARCHAR(20) NOT NULL DEFAULT 'en_attente' AFTER `image_url`,
    ADD COLUMN IF NOT EXISTS `id_demandeur` INT(11) NOT NULL DEFAULT 0 AFTER `statut`,
    ADD COLUMN IF NOT EXISTS `description_demande` TEXT DEFAULT NULL AFTER `id_demandeur`,
    ADD COLUMN IF NOT EXISTS `id_animal_cible` INT(11) DEFAULT NULL AFTER `description_demande`,
    ADD COLUMN IF NOT EXISTS `id_plante_cible` INT(11) DEFAULT NULL AFTER `id_animal_cible`,
    ADD COLUMN IF NOT EXISTS `ai_diagnosis_result` TEXT DEFAULT NULL AFTER `id_plante_cible`,
    ADD COLUMN IF NOT EXISTS `ai_diagnosis_date` DATETIME DEFAULT NULL AFTER `ai_diagnosis_result`,
    ADD COLUMN IF NOT EXISTS `ai_confidence_score` VARCHAR(20) DEFAULT NULL AFTER `ai_diagnosis_date`,
    ADD COLUMN IF NOT EXISTS `diagnosis_mode` VARCHAR(20) DEFAULT NULL AFTER `ai_confidence_score`;

-- Update existing rows to have valid demandeur (set to id_technicien for backward compatibility)
UPDATE `analyse` SET `id_demandeur` = `id_technicien` WHERE `id_demandeur` = 0;

-- Rename old columns to new naming convention (if they exist with old names)
-- This handles the case where columns were added with _id suffix
-- ALTER TABLE `analyse` CHANGE `id_technicien` `id_technicien_id` INT(11) NOT NULL;
-- ALTER TABLE `analyse` CHANGE `id_ferme` `id_ferme_id` INT(11) NOT NULL;

SELECT 'Migration completed: Farmer request workflow columns added' AS status;