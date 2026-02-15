-- ============================================
-- FarmAI - Base de données complète
-- Fichier: farmai.sql
-- Description: Création complète de la base de données FarmAI
-- ============================================

-- Supprimer la base si elle existe (pour une installation propre)
-- DROP DATABASE IF EXISTS farmai;

-- Créer la base de données
CREATE DATABASE IF NOT EXISTS farmai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Utiliser la base de données
USE farmai;

-- ============================================
-- Table: user (Utilisateurs)
-- ============================================
CREATE TABLE IF NOT EXISTS user (
    id_user INT(11) NOT NULL AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password VARCHAR(255) NOT NULL,
    cin VARCHAR(20) NOT NULL,
    adresse TEXT DEFAULT NULL,
    telephone VARCHAR(20) DEFAULT NULL,
    image_url VARCHAR(255) DEFAULT NULL,
    role ENUM('ADMIN', 'EXPERT', 'AGRICOLE', 'FOURNISSEUR') DEFAULT 'AGRICOLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id_user),
    UNIQUE KEY unique_email (email),
    UNIQUE KEY unique_cin (cin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: analyse (Analyses techniques)
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
-- Table: conseil (Conseils liés aux analyses)
-- Relation 1:N avec analyse
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
-- Insertion des données de test - Utilisateurs
-- ============================================

-- Admin (mot de passe: admin123)
INSERT INTO user (nom, prenom, email, password, cin, role, telephone, adresse)
VALUES (
    'Admin',
    'Super',
    'admin@farmai.tn',
    'dO6VnlGvKxY0AAAA$+Y0XqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqx=',
    '00000000',
    'ADMIN',
    '+216 70 000 000',
    'Tunis, Tunisie'
) ON DUPLICATE KEY UPDATE email = email;

-- Expert (mot de passe: admin123)
INSERT INTO user (nom, prenom, email, password, cin, role, telephone, adresse)
VALUES 
    ('Expert', 'Test', 'expert@farmai.tn', 'dO6VnlGvKxY0AAAA$+Y0XqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqx=', '11111111', 'EXPERT', '+216 71 000 000', 'Sfax, Tunisie')
ON DUPLICATE KEY UPDATE email = email;

-- Agricole (mot de passe: admin123)
INSERT INTO user (nom, prenom, email, password, cin, role, telephone, adresse)
VALUES 
    ('Agricole', 'Test', 'agricole@farmai.tn', 'dO6VnlGvKxY0AAAA$+Y0XqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqx=', '22222222', 'AGRICOLE', '+216 72 000 000', 'Sousse, Tunisie')
ON DUPLICATE KEY UPDATE email = email;

-- Fournisseur (mot de passe: admin123)
INSERT INTO user (nom, prenom, email, password, cin, role, telephone, adresse)
VALUES 
    ('Fournisseur', 'Test', 'fournisseur@farmai.tn', 'dO6VnlGvKxY0AAAA$+Y0XqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqx=', '33333333', 'FOURNISSEUR', '+216 73 000 000', 'Bizerte, Tunisie')
ON DUPLICATE KEY UPDATE email = email;

-- ============================================
-- Insertion des données de test - Analyses
-- ============================================
INSERT INTO analyse (resultat_technique, id_technicien, id_ferme, image_url)
VALUES 
    ('Analyse du sol: pH neutre, richesse en azote optimale', 2, 1, '/images/analyse1.jpg'),
    ('Détection de maladie: Mildiou présent à 15%', 2, 2, '/images/analyse2.jpg'),
    ('Analyse foliaire: Carence en potassium détectée', 2, 3, NULL)
ON DUPLICATE KEY UPDATE id_analyse = id_analyse;

-- ============================================
-- Insertion des données de test - Conseils
-- ============================================
INSERT INTO conseil (description_conseil, priorite, id_analyse)
VALUES 
    ('Appliquer un engrais riche en potassium dans les 7 jours', 'HAUTE', 1),
    ('Surveiller l humidité et aérer les plants si nécessaire', 'MOYENNE', 1),
    ('Traiter avec un fongicide approprié contre le mildiou', 'HAUTE', 2),
    ('Irrigation recommandée: 2 fois par semaine', 'BASSE', 3)
ON DUPLICATE KEY UPDATE id_conseil = id_conseil;

-- ============================================
-- Fin du script
-- ============================================
