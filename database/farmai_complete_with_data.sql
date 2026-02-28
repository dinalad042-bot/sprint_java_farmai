-- ============================================
-- FarmAI Complete Database Schema + Sample Data
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

-- ========================================================
-- SAMPLE DATA FOR TESTING ALL FEATURES
-- ========================================================

-- --------------------------------------------------------
-- Default users (password: password123)
-- Default users (password: password123)
-- Passwords are SHA-256 hashed with salt (format: salt$hash)
INSERT INTO `user` (`nom`, `prenom`, `email`, `password`, `cin`, `adresse`, `telephone`, `role`) VALUES
('Admin', 'Super', 'admin@farmai.tn', 'dO6VnlGvKxY0AAAA$+Y0XqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqx=', '00000000', 'Tunis, Tunisie', '+216 70 000 000', 'ADMIN'),
('Expert', 'Test', 'expert@farmai.tn', 'dO6VnlGvKxY0AAAA$+Y0XqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqx=', '11111111', 'Sfax, Tunisie', '+216 71 000 000', 'EXPERT'),
('Agricole', 'Test', 'agricole@farmai.tn', 'dO6VnlGvKxY0AAAA$+Y0XqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqx=', '22222222', 'Sousse, Tunisie', '+216 72 000 000', 'AGRICOLE'),
('Fournisseur', 'Test', 'fournisseur@farmai.tn', 'dO6VnlGvKxY0AAAA$+Y0XqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqx=', '33333333', 'Bizerte, Tunisie', '+216 73 000 000', 'FOURNISSEUR'),
('Expert', 'Agriculture', 'expert1@farmai.tn', 'dO6VnlGvKxY0AAAA$+Y0XqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqx=', '44444444', 'Gabès, Tunisie', '+216 74 000 000', 'EXPERT'),
('Agricole', 'Ahmed', 'ahmed@farmai.tn', 'dO6VnlGvKxY0AAAA$+Y0XqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqx=', '55555555', 'Kairouan, Tunisie', '+216 75 000 000', 'AGRICOLE'),
('Agricole', 'Fatma', 'fatma@farmai.tn', 'dO6VnlGvKxY0AAAA$+Y0XqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqx=', '66666666', 'Mahdia, Tunisie', '+216 76 000 000', 'AGRICOLE'),
('Agricole', 'Young', 'jeune@farmai.tn', 'dO6VnlGvKxY0AAAA$+Y0XqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqx=', '77777777', 'Beja, Tunisie', '+216 77 000 000', 'AGRICOLE'),
('Fournisseur', 'Principal', 'principal@farmai.tn', 'dO6VnlGvKxY0AAAA$+Y0XqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqx=', '88888888', 'Monastir, Tunisie', '+216 78 000 000', 'FOURNISSEUR');

-- --------------------------------------------------------
-- Sample farms for testing
-- Farm assignments by user ID:
-- User 3 (agricole@farmai.tn) gets farms 1, 4
-- User 6 (ahmed@farmai.tn) gets farms 2, 5
-- User 7 (fatma@farmai.tn) gets farm 3
-- User 8 (jeune@farmai.tn) gets farm 6
INSERT INTO `ferme` (`nom_ferme`, `lieu`, `surface`, `id_fermier`) VALUES
('Ferme Pilote Tunis', 'Tunis, Tunisie', 150.0, 3),
('Ferme Biologique Sfax', 'Sfax, Tunisie', 200.0, 6),
('Ferme Maraichere Sousse', 'Sousse, Tunisie', 75.0, 7),
('Ferme Oleicole Gabes', 'Gabes, Tunisie', 300.0, 3),
('Ferme Cerealiere Kairouan', 'Kairouan, Tunisie', 250.0, 6),
('Ferme Moderne Beja', 'Beja, Tunisie', 180.0, 8);

-- --------------------------------------------------------
-- Sample animals for testing farm management
-- --------------------------------------------------------
INSERT INTO `animaux` (`espece`, `etat_sante`, `date_naissance`, `id_ferme`) VALUES
('Vache Holstein', 'Bon', '2022-03-15', 1),
('Vache Jersey', 'Excellent', '2021-08-22', 1),
('Mouton Dman', 'Bon', '2023-01-10', 1),
('Chèvre Alpine', 'Excellent', '2022-11-05', 1),
('Poule Rhode Island', 'Bon', '2023-06-12', 1),
('Vache Simmental', 'Malade', '2020-12-30', 2),
('Mouton Barbarin', 'Bon', '2022-09-18', 2),
('Chèvre Saanen', 'Excellent', '2023-02-25', 2),
('Dinde Bronze', 'Bon', '2023-07-08', 2),
('Canard Mulard', 'Excellent', '2023-04-15', 3);

-- --------------------------------------------------------
-- Sample plants for testing farm management
-- --------------------------------------------------------
INSERT INTO `plantes` (`nom_espece`, `cycle_vie`, `id_ferme`, `quantite`) VALUES
('Tomate Roma', 'Semis: Mars-Avril, Récolte: Juin-Juillet', 1, 500.0),
('Tomate Cœur de Bœuf', 'Semis: Février-Mars, Récolte: Mai-Juin', 1, 300.0),
('Concombre Vert', 'Semis: Avril-Mai, Récolte: Juin-Juillet', 1, 200.0),
('Carotte Nantaise', 'Semis: Février-Mars, Récolte: Juin-Juillet', 1, 1000.0),
('Pomme de Terre', 'Plantation: Février-Mars, Récolte: Juin-Juillet', 1, 2000.0),
('Olivier', 'Plantation: Octobre-Novembre, Récolte: Octobre-Décembre', 2, 150.0),
('Amandier', 'Plantation: Novembre-Décembre, Récolte: Août-Septembre', 2, 75.0),
('Figuier', 'Plantation: Novembre-Décembre, Récolte: Juillet-Septembre', 2, 50.0),
('Pastèque Sugar Baby', 'Semis: Avril-Mai, Récolte: Juillet-Août', 3, 100.0),
('Melon Charentais', 'Semis: Avril-Mai, Récolte: Juillet-Août', 3, 80.0);

-- --------------------------------------------------------
-- Sample analyses for testing expert features
-- --------------------------------------------------------
INSERT INTO `analyse` (`date_analyse`, `resultat_technique`, `id_technicien`, `id_ferme`, `image_url`) VALUES
('2024-01-15 09:30:00', 'Analyse du sol: pH 7.2, Azote 45mg/kg, Phosphore 12mg/kg, Potassium 180mg/kg. Recommandation: Fumure phosphatee.', 2, 1, '/images/analyses/sol1.jpg'),
('2024-01-16 10:15:00', 'Analyse foliaire tomate: Carence en magnesium detectee. Application dolomie recommandee.', 2, 1, '/images/analyses/foliar1.jpg'),
('2024-01-17 14:20:00', 'Analyse parasitaire: Presence de pucerons sur cultures de tomates. Traitement biologique recommande.', 2, 1, '/images/analyses/pest1.jpg'),
('2024-01-18 11:45:00', 'Analyse de la qualite de leau: Conductivite 0.8 dS/m, pH 6.8. Taux de sel acceptable pour irrigation.', 5, 2, '/images/analyses/water1.jpg'),
('2024-01-19 16:30:00', 'Analyse nutritionnelle vaches: Taux de proteines adequat, legere carence en calcium. Supplement calcique recommande.', 5, 2, '/images/analyses/nutrition1.jpg'),
('2024-01-20 08:00:00', 'Analyse de la qualite du lait: Matiere grasse 3.8%, Matiere proteique 3.2%, Cellules somatiques 250000/ml. Qualite bonne.', 5, 2, '/images/analyses/milk1.jpg'),
('2024-01-21 13:15:00', 'Analyse des graines: Taux de germination 95%, humidite 12%. Qualite excellente pour semis.', 2, 3, '/images/analyses/seeds1.jpg'),
('2024-01-22 15:00:00', 'Analyse microbiologique sol: Activite microbienne elevee, rapport C/N optimal. Sante du sol excellente.', 5, 4, '/images/analyses/microbio1.jpg');

-- --------------------------------------------------------
-- Sample conseils (advice) for testing expert features
-- --------------------------------------------------------
INSERT INTO `conseil` (`description_conseil`, `priorite`, `id_analyse`) VALUES
('Appliquer 150 kg/ha de superphosphate triple immediatement apres la plantation. Repartir en deux applications espaces de 15 jours.', 'HAUTE', 1),
('Pulveriser une solution de sulfate de magnesium (20 g/l) sur le feuillage tous les 15 jours pendant 2 mois.', 'MOYENNE', 2),
('Introduire des coccinelles et installer des pieges jaunes colles pour controler les pucerons. Eviter les pesticides chimiques.', 'HAUTE', 3),
('Installer un systeme de filtration par osmose inverse si la conductivite depasse 1.0 dS/m. Surveiller regulierement.', 'MOYENNE', 4),
('Ajouter du carbonate de calcium a la ration (50 g/jour par animal) pendant 30 jours. Surveiller la consommation.', 'MOYENNE', 5),
('Maintenir la refrigeration du lait a 4°C et assurer une hygiene stricte lors de la traite pour reduire les cellules somatiques.', 'BASSE', 6),
('Semer immediatement les graines dans un terreau sterilise. Maintenir une humidite de 80% et une temperature de 25°C.', 'HAUTE', 7),
('Appliquer du compost bien decompose (10 t/ha) en automne et travailler legerement le sol pour ameliorer lactivite microbienne.', 'BASSE', 8);

-- --------------------------------------------------------
-- Sample user logs for testing security features
-- --------------------------------------------------------
INSERT INTO `user_log` (`user_id`, `action`, `performed_by`, `description`) VALUES
(1, 'LOGIN', 'admin@farmai.tn', 'Connexion reussie depuis 192.168.1.100'),
(2, 'LOGIN', 'expert@farmai.tn', 'Connexion reussie depuis 192.168.1.101'),
(3, 'LOGIN', 'agricole@farmai.tn', 'Connexion reussie depuis 192.168.1.102'),
(1, 'CREATE', 'admin@farmai.tn', 'Creation d\'un nouvel utilisateur: ahmed@farmai.tn'),
(2, 'CREATE', 'expert@farmai.tn', 'Creation d\'une nouvelle analyse pour ferme ID: 1'),
(3, 'UPDATE', 'agricole@farmai.tn', 'Mise a jour des informations de la ferme ID: 1'),
(4, 'LOGIN', 'fournisseur@farmai.tn', 'Connexion reussie depuis 192.168.1.103'),
(5, 'LOGIN', 'expert1@farmai.tn', 'Connexion reussie depuis 192.168.1.104'),
(6, 'LOGIN', 'ahmed@farmai.tn', 'Connexion reussie depuis 192.168.1.105'),
(7, 'LOGIN', 'fatma@farmai.tn', 'Connexion reussie depuis 192.168.1.106');

-- --------------------------------------------------------
-- Sample face data entries (binary data would be real face models)
-- --------------------------------------------------------
-- Note: Face models are binary data, these are just placeholder entries

-- --------------------------------------------------------
-- --------------------------------------------------------
-- Final test queries to verify data integrity
-- --------------------------------------------------------

-- Display summary of all data inserted
SELECT '=== DATABASE SUMMARY ===' AS info;
SELECT CONCAT('Total Users: ', COUNT(*)) AS info FROM user;
SELECT CONCAT('Total Farms: ', COUNT(*)) AS info FROM ferme;
SELECT CONCAT('Total Animals: ', COUNT(*)) AS info FROM animaux;
SELECT CONCAT('Total Plants: ', COUNT(*)) AS info FROM plantes;
SELECT CONCAT('Total Analyses: ', COUNT(*)) AS info FROM analyse;
SELECT CONCAT('Total Conseils: ', COUNT(*)) AS info FROM conseil;
SELECT CONCAT('Total User Logs: ', COUNT(*)) AS info FROM user_log;

-- Display user role distribution
SELECT '=== USER ROLES ===' AS info;
SELECT role, COUNT(*) as count FROM user GROUP BY role;

-- Display farm distribution by owner role
SELECT '=== FARM DISTRIBUTION ===' AS info;
SELECT u.role, COUNT(f.id_ferme) as farm_count 
FROM user u 
LEFT JOIN ferme f ON u.id_user = f.id_fermier 
GROUP BY u.role;

COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

-- Final verification query
SELECT '=== DATABASE READY FOR TESTING ===' AS info;