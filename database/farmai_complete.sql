-- ============================================
-- FarmAI Complete Database Schema
-- Merged from: expertise, security, ferme branches
-- Date: 2026-02-28
-- ============================================

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

-- --------------------------------------------------------
-- Database: farmai
-- --------------------------------------------------------
CREATE DATABASE IF NOT EXISTS farmai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE farmai;

-- --------------------------------------------------------
-- Table: user
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
    `id_user` int(11) NOT NULL AUTO_INCREMENT,
    `nom` varchar(100) NOT NULL,
    `prenom` varchar(100) NOT NULL,
    `email` varchar(150) NOT NULL,
    `password` varchar(255) NOT NULL,
    `cin` varchar(20) NOT NULL,
    `adresse` text DEFAULT NULL,
    `telephone` varchar(20) DEFAULT NULL,
    `image_url` varchar(255) DEFAULT NULL,
    `role` enum('ADMIN','EXPERT','AGRICOLE','FOURNISSEUR') DEFAULT 'AGRICOLE',
    `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
    `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
    PRIMARY KEY (`id_user`),
    UNIQUE KEY `unique_email` (`email`),
    UNIQUE KEY `unique_cin` (`cin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table: ferme
-- NOTE: Removed UNIQUE constraint on id_fermier to allow multiple farms per user
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `ferme` (
    `id_ferme` int(11) NOT NULL AUTO_INCREMENT,
    `nom_ferme` varchar(100) NOT NULL,
    `lieu` varchar(255) NOT NULL,
    `surface` double DEFAULT 0,
    `id_fermier` int(11) NOT NULL,
    `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
    `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
    PRIMARY KEY (`id_ferme`),
    KEY `id_fermier` (`id_fermier`),
    FOREIGN KEY (`id_fermier`) REFERENCES `user` (`id_user`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table: analyse
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `analyse` (
    `id_analyse` int(11) NOT NULL AUTO_INCREMENT,
    `date_analyse` timestamp NOT NULL DEFAULT current_timestamp(),
    `resultat_technique` text DEFAULT NULL,
    `id_technicien` int(11) NOT NULL,
    `id_ferme` int(11) NOT NULL,
    `image_url` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id_analyse`),
    KEY `id_technicien` (`id_technicien`),
    KEY `id_ferme` (`id_ferme`),
    FOREIGN KEY (`id_technicien`) REFERENCES `user` (`id_user`) ON DELETE CASCADE,
    FOREIGN KEY (`id_ferme`) REFERENCES `ferme` (`id_ferme`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table: conseil
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `conseil` (
    `id_conseil` int(11) NOT NULL AUTO_INCREMENT,
    `description_conseil` text NOT NULL,
    `priorite` enum('HAUTE','MOYENNE','BASSE') DEFAULT 'MOYENNE',
    `id_analyse` int(11) NOT NULL,
    PRIMARY KEY (`id_conseil`),
    KEY `id_analyse` (`id_analyse`),
    FOREIGN KEY (`id_analyse`) REFERENCES `analyse` (`id_analyse`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table: animaux (from ferme branch)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `animaux` (
    `id_animal` int(11) NOT NULL AUTO_INCREMENT,
    `espece` varchar(100) NOT NULL,
    `etat_sante` varchar(50) DEFAULT 'Bon',
    `date_naissance` date DEFAULT NULL,
    `id_ferme` int(11) NOT NULL,
    `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
    `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
    PRIMARY KEY (`id_animal`),
    KEY `id_ferme` (`id_ferme`),
    FOREIGN KEY (`id_ferme`) REFERENCES `ferme` (`id_ferme`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table: plantes (from ferme branch)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `plantes` (
    `id_plante` int(11) NOT NULL AUTO_INCREMENT,
    `nom_espece` varchar(100) NOT NULL,
    `cycle_vie` varchar(50) DEFAULT NULL,
    `id_ferme` int(11) NOT NULL,
    `quantite` double DEFAULT 0,
    `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
    `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
    PRIMARY KEY (`id_plante`),
    KEY `id_ferme` (`id_ferme`),
    FOREIGN KEY (`id_ferme`) REFERENCES `ferme` (`id_ferme`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table: face_data (from security branch)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `face_data` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `user_id` int(11) NOT NULL,
    `face_model` LONGBLOB NOT NULL,
    `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
    `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_user` (`user_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id_user`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table: user_log (from security/expertise branch)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_log` (
    `id_log` int(11) NOT NULL AUTO_INCREMENT,
    `user_id` int(11) NOT NULL,
    `action` enum('CREATE','UPDATE','DELETE','LOGIN','LOGOUT') NOT NULL,
    `performed_by` varchar(150) NOT NULL,
    `description` text DEFAULT NULL,
    `timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
    PRIMARY KEY (`id_log`),
    KEY `user_id` (`user_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id_user`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Sample Data (for testing)
-- --------------------------------------------------------

-- Default users (password: password123)
INSERT INTO `user` (`nom`, `prenom`, `email`, `password`, `cin`, `adresse`, `telephone`, `role`) VALUES
('Admin', 'Super', 'admin@farmai.tn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJ0dRG7dlq', '00000000', 'Tunis, Tunisie', '+216 70 000 000', 'ADMIN'),
('Expert', 'Test', 'expert@farmai.tn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJ0dRG7dlq', '11111111', 'Sfax, Tunisie', '+216 71 000 000', 'EXPERT'),
('Agricole', 'Test', 'agricole@farmai.tn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJ0dRG7dlq', '22222222', 'Sousse, Tunisie', '+216 72 000 000', 'AGRICOLE'),
('Fournisseur', 'Test', 'fournisseur@farmai.tn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJ0dRG7dlq', '33333333', 'Bizerte, Tunisie', '+216 73 000 000', 'FOURNISSEUR');

-- Sample farms
INSERT INTO `ferme` (`nom_ferme`, `lieu`, `surface`, `id_fermier`) VALUES
('Ferme Pilote', 'Tunis, Tunisie', 100.0, 1),
('Ferme Sfax', 'Sfax, Tunisie', 75.0, 2),
('Ferme Sousse', 'Sousse, Tunisie', 50.0, 3);

COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
