-- ============================================
-- FarmAI Database Setup Script
-- Creates the user table for user management
-- ============================================

-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS farmai;

USE farmai;

-- Drop table if exists (for clean setup)
-- DROP TABLE IF EXISTS user;

-- Create user table
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

-- Insert a default admin user (password: admin123)
-- The password is hashed using SHA-256 with salt: YWRtaW4xMjM=$/hX...
-- For testing, use password: admin123
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

-- Insert sample users for each role (for testing)
INSERT INTO user (nom, prenom, email, password, cin, role, telephone, adresse)
VALUES 
    ('Expert', 'Test', 'expert@farmai.tn', 'dO6VnlGvKxY0AAAA$+Y0XqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqx=', '11111111', 'EXPERT', '+216 71 000 000', 'Sfax, Tunisie'),
    ('Agricole', 'Test', 'agricole@farmai.tn', 'dO6VnlGvKxY0AAAA$+Y0XqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqx=', '22222222', 'AGRICOLE', '+216 72 000 000', 'Sousse, Tunisie'),
    ('Fournisseur', 'Test', 'fournisseur@farmai.tn', 'dO6VnlGvKxY0AAAA$+Y0XqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqx=', '33333333', 'FOURNISSEUR', '+216 73 000 000', 'Bizerte, Tunisie')
ON DUPLICATE KEY UPDATE email = email;

-- Show created table structure
DESCRIBE user;

-- Show inserted users
SELECT id_user, nom, prenom, email, cin, role, telephone FROM user;
