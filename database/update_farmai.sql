-- ============================================
-- Mise à jour de la base farmai existante
-- Ajout des tables analyse et conseil
-- ============================================

USE farmai;

-- ============================================
-- Create the analyse table
-- ============================================
CREATE TABLE IF NOT EXISTS analyse (
    id_analyse INT(11) NOT NULL AUTO_INCREMENT,
    date_analyse TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resultat_technique TEXT DEFAULT NULL,
    id_technicien INT(11) NOT NULL,
    id_ferme INT(11) NOT NULL,
    image_url VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id_analyse),
    FOREIGN KEY (id_technicien) REFERENCES user(id_user) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Create the conseil table (1:N with analyse)
-- ============================================
CREATE TABLE IF NOT EXISTS conseil (
    id_conseil INT(11) NOT NULL AUTO_INCREMENT,
    description_conseil TEXT NOT NULL,
    priorite ENUM('HAUTE', 'MOYENNE', 'BASSE') DEFAULT 'MOYENNE',
    id_analyse INT(11) NOT NULL,
    PRIMARY KEY (id_conseil),
    FOREIGN KEY (id_analyse) REFERENCES analyse(id_analyse) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Update completed successfully
-- Tables created/updated: analyse, conseil
-- ============================================

-- Verification queries (run manually if needed):
-- DESCRIBE analyse;
-- DESCRIBE conseil;
