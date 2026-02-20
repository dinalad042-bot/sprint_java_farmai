-- ============================================
-- FarmAI Face Data Table
-- Run this once to enable facial recognition login
-- ============================================

USE farmai;

CREATE TABLE IF NOT EXISTS face_data (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT NOT NULL,
    face_model LONGBLOB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user (user_id),
    FOREIGN KEY (user_id) REFERENCES user(id_user) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Verify
DESCRIBE face_data;
