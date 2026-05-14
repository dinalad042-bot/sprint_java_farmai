-- ============================================================
-- Complete Migration Script: Add Missing Columns for FarmAI Application
-- This script adds all missing columns needed for the Java application
-- ============================================================

-- Add missing columns to analyse table for farmer request workflow
ALTER TABLE `analyse`
    ADD COLUMN IF NOT EXISTS `statut` VARCHAR(20) NOT NULL DEFAULT 'en_attente' AFTER `image_url`,
    ADD COLUMN IF NOT EXISTS `id_demandeur` INT(11) DEFAULT NULL AFTER `statut`,
    ADD COLUMN IF NOT EXISTS `description_demande` TEXT DEFAULT NULL AFTER `id_demandeur`,
    ADD COLUMN IF NOT EXISTS `id_animal_cible` INT(11) DEFAULT NULL AFTER `description_demande`,
    ADD COLUMN IF NOT EXISTS `id_plante_cible` INT(11) DEFAULT NULL AFTER `id_animal_cible`,
    ADD COLUMN IF NOT EXISTS `ai_diagnosis_result` TEXT DEFAULT NULL AFTER `id_plante_cible`,
    ADD COLUMN IF NOT EXISTS `ai_diagnosis_date` DATETIME DEFAULT NULL AFTER `ai_diagnosis_result`,
    ADD COLUMN IF NOT EXISTS `ai_confidence_score` VARCHAR(20) DEFAULT NULL AFTER `ai_diagnosis_date`,
    ADD COLUMN IF NOT EXISTS `diagnosis_mode` VARCHAR(20) DEFAULT NULL AFTER `ai_confidence_score`;

-- Update existing rows to have valid demandeur (set to id_technicien for backward compatibility)
UPDATE `analyse` SET `id_demandeur` = `id_technicien` WHERE `id_demandeur` IS NULL OR `id_demandeur` = 0;

-- Add indexes for better performance
CREATE INDEX IF NOT EXISTS `idx_analyse_statut` ON `analyse` (`statut`);
CREATE INDEX IF NOT EXISTS `idx_analyse_demandeur` ON `analyse` (`id_demandeur`);
CREATE INDEX IF NOT EXISTS `idx_analyse_ferme` ON `analyse` (`id_ferme`);
CREATE INDEX IF NOT EXISTS `idx_analyse_technicien` ON `analyse` (`id_technicien`);

-- Add foreign key constraints if they don't exist
-- Note: Uncomment these if you want to enforce referential integrity
-- ALTER TABLE `analyse` 
-- ADD CONSTRAINT `fk_analyse_technicien` FOREIGN KEY (`id_technicien`) REFERENCES `user` (`id_user`) ON DELETE SET NULL,
-- ADD CONSTRAINT `fk_analyse_ferme` FOREIGN KEY (`id_ferme`) REFERENCES `ferme` (`id_ferme`) ON DELETE CASCADE,
-- ADD CONSTRAINT `fk_analyse_demandeur` FOREIGN KEY (`id_demandeur`) REFERENCES `user` (`id_user`) ON DELETE SET NULL,
-- ADD CONSTRAINT `fk_analyse_animal` FOREIGN KEY (`id_animal_cible`) REFERENCES `animaux` (`id_animal`) ON DELETE SET NULL,
-- ADD CONSTRAINT `fk_analyse_plante` FOREIGN KEY (`id_plante_cible`) REFERENCES `plantes` (`id_plante`) ON DELETE SET NULL;

SELECT 'Migration completed: All missing columns added to analyse table' AS status;