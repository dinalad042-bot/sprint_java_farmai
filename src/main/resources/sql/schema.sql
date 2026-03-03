-- Schéma base de données : Gestion Achat et Stocks - FarmIA Desk
-- Exécuter ce script dans MySQL pour créer les tables

CREATE DATABASE IF NOT EXISTS farmia_desk CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE farmia_desk;

-- Table Service
CREATE TABLE IF NOT EXISTS service (
    id_service INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description TEXT,
    prix DOUBLE NOT NULL DEFAULT 0,
    stock INT NOT NULL DEFAULT 0,
    seuil_critique INT NOT NULL DEFAULT 0
);

-- Table Achat
CREATE TABLE IF NOT EXISTS achat (
    id_achat INT AUTO_INCREMENT PRIMARY KEY,
    date_achat DATE NOT NULL,
    total DOUBLE NOT NULL DEFAULT 0
);

-- Table associative Liste_Achat (N:M entre Achat et Service)
CREATE TABLE IF NOT EXISTS liste_achat (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_achat INT NOT NULL,
    id_service INT NOT NULL,
    quantite INT NOT NULL,
    prix_unitaire DOUBLE NOT NULL,
    CONSTRAINT fk_liste_achat_achat FOREIGN KEY (id_achat) REFERENCES achat(id_achat) ON DELETE CASCADE,
    CONSTRAINT fk_liste_achat_service FOREIGN KEY (id_service) REFERENCES service(id_service) ON DELETE RESTRICT,
    UNIQUE KEY uk_liste_achat (id_achat, id_service)
);

CREATE INDEX idx_service_stock ON service(stock);
CREATE INDEX idx_achat_date ON achat(date_achat);
