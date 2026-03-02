-- ============================================================
-- Migration: Add Notification System for Agri Users
-- Purpose: Create notification table to alert fermiers when
--          experts add analyses for their farms
-- ============================================================

-- Create notification table
CREATE TABLE IF NOT EXISTS notification (
    id_notification INT AUTO_INCREMENT PRIMARY KEY,
    id_user INT NOT NULL COMMENT 'Recipient user (fermier)',
    titre VARCHAR(255) NOT NULL COMMENT 'Notification title',
    message TEXT NOT NULL COMMENT 'Notification message',
    type VARCHAR(50) NOT NULL COMMENT 'Type: ANALYSE, CONSEIL, SYSTEM',
    id_reference INT NULL COMMENT 'ID of related entity (analysis or conseil)',
    is_read BOOLEAN DEFAULT FALSE COMMENT 'Read status',
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
    
    -- Foreign key constraint
    CONSTRAINT fk_notification_user 
        FOREIGN KEY (id_user) REFERENCES user(id_user) 
        ON DELETE CASCADE,
    
    -- Indexes for performance
    INDEX idx_user_read (id_user, is_read),
    INDEX idx_date (date_creation),
    INDEX idx_type_reference (type, id_reference)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Notifications for users when experts add analyses/conseils';

-- Add sample notifications for testing (optional)
-- These will be created dynamically when experts add analyses
-- Uncomment below to add test notifications for existing data

/*
-- Add notifications for existing analyses
INSERT INTO notification (id_user, titre, message, type, id_reference, is_read, date_creation)
SELECT 
    f.id_fermier,
    'Nouvelle analyse disponible',
    CONCAT('Une nouvelle analyse a été ajoutée pour votre ferme: ', f.nom_ferme),
    'ANALYSE',
    a.id_analyse,
    FALSE,
    a.date_analyse
FROM analyse a
JOIN ferme f ON a.id_ferme = f.id_ferme
WHERE f.id_fermier IS NOT NULL
AND f.id_fermier > 0;
*/

-- Verify table creation
SELECT 
    'Notification table created successfully' AS status,
    COUNT(*) AS total_notifications
FROM notification;

-- Show table structure
DESCRIBE notification;