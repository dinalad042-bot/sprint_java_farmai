-- ============================================
-- Mise à jour des tables existantes
-- Ajout de la colonne image_url manquante
-- ============================================

USE farmai;

-- Ajouter la colonne image_url si elle n'existe pas (MySQL 8.0.16+)
ALTER TABLE analyse
ADD COLUMN IF NOT EXISTS image_url VARCHAR(255) DEFAULT NULL;

-- Pour les versions MySQL < 8.0.16, utiliser cette procédure stockée conditionnelle
DELIMITER $$
CREATE PROCEDURE AddColumnIfNotExists()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME = 'analyse'
        AND COLUMN_NAME = 'image_url'
        AND TABLE_SCHEMA = 'farmai'
    ) THEN
        ALTER TABLE analyse ADD COLUMN image_url VARCHAR(255) DEFAULT NULL;
    END IF;
END$$
DELIMITER ;

CALL AddColumnIfNotExists();
DROP PROCEDURE IF EXISTS AddColumnIfNotExists();
