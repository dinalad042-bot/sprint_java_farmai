-- ============================================
-- FarmAI - Création de la table ferme
-- Fichier: create_ferme_table.sql
-- Description: Ajoute la table ferme avec liaison vers user (fermier)
-- ============================================

USE farmai;

-- ============================================
-- Table: ferme (Exploitations agricoles)
-- ============================================
CREATE TABLE IF NOT EXISTS ferme (
    id_ferme INT(11) NOT NULL AUTO_INCREMENT,
    nom_ferme VARCHAR(100) NOT NULL,
    lieu VARCHAR(255) NOT NULL,
    surface DOUBLE DEFAULT 0,
    id_fermier INT(11) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id_ferme),
    UNIQUE KEY unique_fermier (id_fermier),
    FOREIGN KEY (id_fermier) REFERENCES user(id_user) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Ajouter la contrainte FK sur analyse.id_ferme
-- ============================================
-- Note: Décommenter si la FK n'existe pas déjà
-- ALTER TABLE analyse ADD CONSTRAINT fk_analyse_ferme 
--     FOREIGN KEY (id_ferme) REFERENCES ferme(id_ferme) ON DELETE CASCADE;

-- ============================================
-- Insertion des données de test - Fermes
-- ============================================

-- Ferme pour le fermier ID 22 (ala ferme - fermier1@farmai.com)
INSERT INTO ferme (nom_ferme, lieu, surface, id_fermier)
VALUES ('Ferme de Sousse', 'Sousse, Tunisie', 50.5, 22)
ON DUPLICATE KEY UPDATE nom_ferme = VALUES(nom_ferme);

-- Ferme pour l'utilisateur AGRICOLE de test (ID 3)
INSERT INTO ferme (nom_ferme, lieu, surface, id_fermier)
VALUES ('Ferme Test Agricole', 'Sousse, Tunisie', 25.0, 3)
ON DUPLICATE KEY UPDATE nom_ferme = VALUES(nom_ferme);

-- Ferme pour l'utilisateur Admin (ID 1) - pour les tests
INSERT INTO ferme (nom_ferme, lieu, surface, id_fermier)
VALUES ('Ferme Pilote Admin', 'Tunis, Tunisie', 100.0, 1)
ON DUPLICATE KEY UPDATE nom_ferme = VALUES(nom_ferme);

-- Ferme pour l'Expert (ID 2) - pour les tests
INSERT INTO ferme (nom_ferme, lieu, surface, id_fermier)
VALUES ('Ferme Experte Sfax', 'Sfax, Tunisie', 75.0, 2)
ON DUPLICATE KEY UPDATE nom_ferme = VALUES(nom_ferme);

-- Ferme pour le Fournisseur (ID 4) - pour les tests
INSERT INTO ferme (nom_ferme, lieu, surface, id_fermier)
VALUES ('Ferme Bizerte', 'Bizerte, Tunisie', 60.0, 4)
ON DUPLICATE KEY UPDATE nom_ferme = VALUES(nom_ferme);

-- Ferme pour l'Expert ala (ID 5)
INSERT INTO ferme (nom_ferme, lieu, surface, id_fermier)
VALUES ('Ferme Ala Expert', 'vvd, Tunisie', 30.0, 5)
ON DUPLICATE KEY UPDATE nom_ferme = VALUES(nom_ferme);

-- ============================================
-- Mise à jour des analyses existantes avec les bons id_ferme
-- ============================================
-- Les analyses existantes ont id_ferme = 1, 2, 3, etc.
-- Nous devons nous assurer qu'elles pointent vers des fermes valides

-- Vérification
SELECT f.id_ferme, f.nom_ferme, f.lieu, u.nom, u.prenom, u.email 
FROM ferme f 
JOIN user u ON f.id_fermier = u.id_user;

-- ============================================
-- Fin du script
-- ============================================
