-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Mar 02, 2026 at 11:29 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `farmai`
--

-- --------------------------------------------------------

--
-- Table structure for table `analyse`
--

CREATE TABLE `analyse` (
  `id_analyse` int(11) NOT NULL,
  `date_analyse` timestamp NOT NULL DEFAULT current_timestamp(),
  `resultat_technique` text DEFAULT NULL,
  `id_technicien` int(11) NOT NULL,
  `id_ferme` int(11) NOT NULL,
  `image_url` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `analyse`
--

INSERT INTO `analyse` (`id_analyse`, `date_analyse`, `resultat_technique`, `id_technicien`, `id_ferme`, `image_url`) VALUES
(1, '2024-01-15 09:30:00', 'Analyse du sol: pH 7.2, Azote 45mg/kg, Phosphore 12mg/kg, Potassium 180mg/kg. Recommandation: Fumure phosphatee.', 2, 1, '/images/analyses/sol1.jpg'),
(2, '2024-01-16 10:15:00', 'Analyse foliaire tomate: Carence en magnesium detectee. Application dolomie recommandee.', 2, 1, '/images/analyses/foliar1.jpg'),
(3, '2024-01-17 14:20:00', 'Analyse parasitaire: Presence de pucerons sur cultures de tomates. Traitement biologique recommande.', 2, 1, '/images/analyses/pest1.jpg'),
(4, '2024-01-18 11:45:00', 'Analyse de la qualite de leau: Conductivite 0.8 dS/m, pH 6.8. Taux de sel acceptable pour irrigation.', 5, 2, '/images/analyses/water1.jpg'),
(5, '2024-01-19 16:30:00', 'Analyse nutritionnelle vaches: Taux de proteines adequat, legere carence en calcium. Supplement calcique recommande.', 5, 2, '/images/analyses/nutrition1.jpg'),
(6, '2024-01-20 08:00:00', 'Analyse de la qualite du lait: Matiere grasse 3.8%, Matiere proteique 3.2%, Cellules somatiques 250000/ml. Qualite bonne.', 5, 2, '/images/analyses/milk1.jpg'),
(7, '2024-01-21 13:15:00', 'Analyse des graines: Taux de germination 95%, humidite 12%. Qualite excellente pour semis.', 2, 3, '/images/analyses/seeds1.jpg'),
(8, '2024-01-22 15:00:00', 'Analyse microbiologique sol: Activite microbienne elevee, rapport C/N optimal. Sante du sol excellente.', 5, 4, '/images/analyses/microbio1.jpg');

-- --------------------------------------------------------

--
-- Table structure for table `animaux`
--

CREATE TABLE `animaux` (
  `id_animal` int(11) NOT NULL,
  `espece` varchar(100) NOT NULL,
  `etat_sante` varchar(50) DEFAULT 'Bon',
  `date_naissance` date DEFAULT NULL,
  `id_ferme` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `animaux`
--

INSERT INTO `animaux` (`id_animal`, `espece`, `etat_sante`, `date_naissance`, `id_ferme`, `created_at`, `updated_at`) VALUES
(1, 'Vache Holstein', 'Bon', '2022-03-15', 1, '2026-03-02 22:22:29', '2026-03-02 22:22:29'),
(2, 'Vache Jersey', 'Excellent', '2021-08-22', 1, '2026-03-02 22:22:29', '2026-03-02 22:22:29'),
(3, 'Mouton Dman', 'Bon', '2023-01-10', 1, '2026-03-02 22:22:29', '2026-03-02 22:22:29'),
(4, 'Chèvre Alpine', 'Excellent', '2022-11-05', 1, '2026-03-02 22:22:29', '2026-03-02 22:22:29'),
(5, 'Poule Rhode Island', 'Bon', '2023-06-12', 1, '2026-03-02 22:22:29', '2026-03-02 22:22:29'),
(6, 'Vache Simmental', 'Malade', '2020-12-30', 2, '2026-03-02 22:22:29', '2026-03-02 22:22:29'),
(7, 'Mouton Barbarin', 'Bon', '2022-09-18', 2, '2026-03-02 22:22:29', '2026-03-02 22:22:29'),
(8, 'Chèvre Saanen', 'Excellent', '2023-02-25', 2, '2026-03-02 22:22:29', '2026-03-02 22:22:29'),
(9, 'Dinde Bronze', 'Bon', '2023-07-08', 2, '2026-03-02 22:22:29', '2026-03-02 22:22:29'),
(10, 'Canard Mulard', 'Excellent', '2023-04-15', 3, '2026-03-02 22:22:29', '2026-03-02 22:22:29');

-- --------------------------------------------------------

--
-- Table structure for table `conseil`
--

CREATE TABLE `conseil` (
  `id_conseil` int(11) NOT NULL,
  `description_conseil` text NOT NULL,
  `priorite` enum('HAUTE','MOYENNE','BASSE') DEFAULT 'MOYENNE',
  `id_analyse` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `conseil`
--

INSERT INTO `conseil` (`id_conseil`, `description_conseil`, `priorite`, `id_analyse`) VALUES
(1, 'Appliquer 150 kg/ha de superphosphate triple immediatement apres la plantation. Repartir en deux applications espaces de 15 jours.', 'HAUTE', 1),
(2, 'Pulveriser une solution de sulfate de magnesium (20 g/l) sur le feuillage tous les 15 jours pendant 2 mois.', 'MOYENNE', 2),
(3, 'Introduire des coccinelles et installer des pieges jaunes colles pour controler les pucerons. Eviter les pesticides chimiques.', 'HAUTE', 3),
(4, 'Installer un systeme de filtration par osmose inverse si la conductivite depasse 1.0 dS/m. Surveiller regulierement.', 'MOYENNE', 4),
(5, 'Ajouter du carbonate de calcium a la ration (50 g/jour par animal) pendant 30 jours. Surveiller la consommation.', 'MOYENNE', 5),
(6, 'Maintenir la refrigeration du lait a 4°C et assurer une hygiene stricte lors de la traite pour reduire les cellules somatiques.', 'BASSE', 6),
(7, 'Semer immediatement les graines dans un terreau sterilise. Maintenir une humidite de 80% et une temperature de 25°C.', 'HAUTE', 7),
(8, 'Appliquer du compost bien decompose (10 t/ha) en automne et travailler legerement le sol pour ameliorer lactivite microbienne.', 'BASSE', 8);

-- --------------------------------------------------------

--
-- Table structure for table `face_data`
--

CREATE TABLE `face_data` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `face_model` longblob NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ferme`
--

CREATE TABLE `ferme` (
  `id_ferme` int(11) NOT NULL,
  `nom_ferme` varchar(100) NOT NULL,
  `lieu` varchar(255) NOT NULL,
  `surface` double DEFAULT 0,
  `id_fermier` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `ferme`
--

INSERT INTO `ferme` (`id_ferme`, `nom_ferme`, `lieu`, `surface`, `id_fermier`, `created_at`, `updated_at`) VALUES
(1, 'Ferme Pilote Tunis', 'Tunis, Tunisie', 150, 3, '2026-03-02 22:22:29', '2026-03-02 22:22:29'),
(2, 'Ferme Biologique Sfax', 'Sfax, Tunisie', 200, 6, '2026-03-02 22:22:29', '2026-03-02 22:22:29'),
(3, 'Ferme Maraichere Sousse', 'Sousse, Tunisie', 75, 7, '2026-03-02 22:22:29', '2026-03-02 22:22:29'),
(4, 'Ferme Oleicole Gabes', 'Gabes, Tunisie', 300, 3, '2026-03-02 22:22:29', '2026-03-02 22:22:29'),
(5, 'Ferme Cerealiere Kairouan', 'Kairouan, Tunisie', 250, 6, '2026-03-02 22:22:29', '2026-03-02 22:22:29'),
(6, 'Ferme Moderne Beja', 'Beja, Tunisie', 180, 8, '2026-03-02 22:22:29', '2026-03-02 22:22:29');

-- --------------------------------------------------------

--
-- Table structure for table `notification`
--

CREATE TABLE `notification` (
  `id_notification` int(11) NOT NULL,
  `id_user` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `message` text NOT NULL,
  `type` varchar(50) NOT NULL,
  `id_reference` int(11) DEFAULT NULL,
  `is_read` tinyint(1) DEFAULT 0,
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `plantes`
--

CREATE TABLE `plantes` (
  `id_plante` int(11) NOT NULL,
  `nom_espece` varchar(100) NOT NULL,
  `cycle_vie` varchar(50) DEFAULT NULL,
  `id_ferme` int(11) NOT NULL,
  `quantite` double DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `plantes`
--

INSERT INTO `plantes` (`id_plante`, `nom_espece`, `cycle_vie`, `id_ferme`, `quantite`, `created_at`, `updated_at`) VALUES
(1, 'Tomate Roma', 'Semis: Mars-Avril, Récolte: Juin-Juillet', 1, 500, '2026-03-02 22:22:29', '2026-03-02 22:22:29'),
(2, 'Tomate Cœur de Bœuf', 'Semis: Février-Mars, Récolte: Mai-Juin', 1, 300, '2026-03-02 22:22:29', '2026-03-02 22:22:29'),
(3, 'Concombre Vert', 'Semis: Avril-Mai, Récolte: Juin-Juillet', 1, 200, '2026-03-02 22:22:29', '2026-03-02 22:22:29'),
(4, 'Carotte Nantaise', 'Semis: Février-Mars, Récolte: Juin-Juillet', 1, 1000, '2026-03-02 22:22:29', '2026-03-02 22:22:29'),
(5, 'Pomme de Terre', 'Plantation: Février-Mars, Récolte: Juin-Juillet', 1, 2000, '2026-03-02 22:22:29', '2026-03-02 22:22:29'),
(6, 'Olivier', 'Plantation: Octobre-Novembre, Récolte: Octobre-Déc', 2, 150, '2026-03-02 22:22:29', '2026-03-02 22:22:29'),
(7, 'Amandier', 'Plantation: Novembre-Décembre, Récolte: Août-Septe', 2, 75, '2026-03-02 22:22:29', '2026-03-02 22:22:29'),
(8, 'Figuier', 'Plantation: Novembre-Décembre, Récolte: Juillet-Se', 2, 50, '2026-03-02 22:22:29', '2026-03-02 22:22:29'),
(9, 'Pastèque Sugar Baby', 'Semis: Avril-Mai, Récolte: Juillet-Août', 3, 100, '2026-03-02 22:22:29', '2026-03-02 22:22:29'),
(10, 'Melon Charentais', 'Semis: Avril-Mai, Récolte: Juillet-Août', 3, 80, '2026-03-02 22:22:29', '2026-03-02 22:22:29');

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

CREATE TABLE `user` (
  `id_user` int(11) NOT NULL,
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
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`id_user`, `nom`, `prenom`, `email`, `password`, `cin`, `adresse`, `telephone`, `image_url`, `role`, `created_at`, `updated_at`) VALUES
(1, 'Admin', 'Super', 'admin@farmai.tn', 'ucIScUvL+4e0EV2kljiCuw==$MFHmjNuZz1C5NPow9UENfqFDtduthOxE3pa7oeBIJYA=', '00000000', 'Tunis, Tunisie', '+216 70 000 000', NULL, 'ADMIN', '2026-03-02 22:22:29', '2026-03-02 22:22:51'),
(2, 'Expert', 'Test', 'expert@farmai.tn', 'YT70hLwGs7PxUu41gPZhuw==$6W2egm/PQLWTnf3eyNDNWE2ENKCFBKt6i70Kx/CDU24=', '11111111', 'Sfax, Tunisie', '+216 71 000 000', NULL, 'EXPERT', '2026-03-02 22:22:29', '2026-03-02 22:22:51'),
(3, 'Agricole', 'Test', 'agricole@farmai.tn', 'chbNEXfMwEHQUc9VCW8d1A==$XjWLBcC9jzKf7yRd2XJ+NAlN8al1OXL2DdqOo/I6k2k=', '22222222', 'Sousse, Tunisie', '+216 72 000 000', NULL, 'AGRICOLE', '2026-03-02 22:22:29', '2026-03-02 22:22:51'),
(4, 'Fournisseur', 'Test', 'fournisseur@farmai.tn', 'E/xM+SqGCC2WP2HbucxZ9w==$rsZ9+dZkSr983sxw94bGc1bLerTba8q7dU4568PfkZE=', '33333333', 'Bizerte, Tunisie', '+216 73 000 000', NULL, 'FOURNISSEUR', '2026-03-02 22:22:29', '2026-03-02 22:22:51'),
(5, 'Expert', 'Agriculture', 'expert1@farmai.tn', 'rd92eh7cK6f9TWtzPhF7Tg==$wdGJBVu3Dl0ZNjwQtXneMt6qzGI6XkpwuyXFEC7Wbxk=', '44444444', 'Gabès, Tunisie', '+216 74 000 000', NULL, 'EXPERT', '2026-03-02 22:22:29', '2026-03-02 22:22:51'),
(6, 'Agricole', 'Ahmed', 'ahmed@farmai.tn', 'Bm31pm3kzeGiCZBUY/foLg==$h/sAHWGq01Y5QaPll2OyvKz0HbhqUuLTqklIoZeel+U=', '55555555', 'Kairouan, Tunisie', '+216 75 000 000', NULL, 'AGRICOLE', '2026-03-02 22:22:29', '2026-03-02 22:22:51'),
(7, 'Agricole', 'Fatma', 'fatma@farmai.tn', '9x8ognakKWMGul/TpPZvJg==$H3iZRskK7GBT4D22dlcpfEpZQyM5tbsh9r1gnrAjTiw=', '66666666', 'Mahdia, Tunisie', '+216 76 000 000', NULL, 'AGRICOLE', '2026-03-02 22:22:29', '2026-03-02 22:22:51'),
(8, 'Agricole', 'Young', 'jeune@farmai.tn', 'v5ttRjYw90yXKP0DqHvERg==$5fiDxeqvnAUGPIYjwVDX6mSYY5QrxU/YZUimz3GVTPQ=', '77777777', 'Beja, Tunisie', '+216 77 000 000', NULL, 'AGRICOLE', '2026-03-02 22:22:29', '2026-03-02 22:22:51'),
(9, 'Fournisseur', 'Principal', 'principal@farmai.tn', 'wX9Ii1xTafpvClVxZ1lDBg==$zT5YoCbVTCCcrHOuioLphKRYjAyKe6IRVnG0G9mW1JY=', '88888888', 'Monastir, Tunisiev', '+216 78 000 000', '', 'FOURNISSEUR', '2026-03-02 22:22:29', '2026-03-02 22:27:37');

-- --------------------------------------------------------

--
-- Table structure for table `user_log`
--

CREATE TABLE `user_log` (
  `id_log` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `action` enum('CREATE','UPDATE','DELETE','LOGIN','LOGOUT') NOT NULL,
  `performed_by` varchar(150) NOT NULL,
  `description` text DEFAULT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `user_log`
--

INSERT INTO `user_log` (`id_log`, `user_id`, `action`, `performed_by`, `description`, `timestamp`) VALUES
(1, 1, 'LOGIN', 'admin@farmai.tn', 'Connexion reussie depuis 192.168.1.100', '2026-03-02 22:22:29'),
(2, 2, 'LOGIN', 'expert@farmai.tn', 'Connexion reussie depuis 192.168.1.101', '2026-03-02 22:22:29'),
(3, 3, 'LOGIN', 'agricole@farmai.tn', 'Connexion reussie depuis 192.168.1.102', '2026-03-02 22:22:29'),
(4, 1, 'CREATE', 'admin@farmai.tn', 'Creation d\'un nouvel utilisateur: ahmed@farmai.tn', '2026-03-02 22:22:29'),
(5, 2, 'CREATE', 'expert@farmai.tn', 'Creation d\'une nouvelle analyse pour ferme ID: 1', '2026-03-02 22:22:29'),
(6, 3, 'UPDATE', 'agricole@farmai.tn', 'Mise a jour des informations de la ferme ID: 1', '2026-03-02 22:22:29'),
(7, 4, 'LOGIN', 'fournisseur@farmai.tn', 'Connexion reussie depuis 192.168.1.103', '2026-03-02 22:22:29'),
(8, 5, 'LOGIN', 'expert1@farmai.tn', 'Connexion reussie depuis 192.168.1.104', '2026-03-02 22:22:29'),
(9, 6, 'LOGIN', 'ahmed@farmai.tn', 'Connexion reussie depuis 192.168.1.105', '2026-03-02 22:22:29'),
(10, 7, 'LOGIN', 'fatma@farmai.tn', 'Connexion reussie depuis 192.168.1.106', '2026-03-02 22:22:29');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `analyse`
--
ALTER TABLE `analyse`
  ADD PRIMARY KEY (`id_analyse`),
  ADD KEY `id_technicien` (`id_technicien`),
  ADD KEY `id_ferme` (`id_ferme`);

--
-- Indexes for table `animaux`
--
ALTER TABLE `animaux`
  ADD PRIMARY KEY (`id_animal`),
  ADD KEY `id_ferme` (`id_ferme`);

--
-- Indexes for table `conseil`
--
ALTER TABLE `conseil`
  ADD PRIMARY KEY (`id_conseil`),
  ADD KEY `id_analyse` (`id_analyse`);

--
-- Indexes for table `face_data`
--
ALTER TABLE `face_data`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_user` (`user_id`);

--
-- Indexes for table `ferme`
--
ALTER TABLE `ferme`
  ADD PRIMARY KEY (`id_ferme`),
  ADD KEY `id_fermier` (`id_fermier`);

--
-- Indexes for table `notification`
--
ALTER TABLE `notification`
  ADD PRIMARY KEY (`id_notification`),
  ADD KEY `idx_user_read` (`id_user`,`is_read`);

--
-- Indexes for table `plantes`
--
ALTER TABLE `plantes`
  ADD PRIMARY KEY (`id_plante`),
  ADD KEY `id_ferme` (`id_ferme`);

--
-- Indexes for table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id_user`),
  ADD UNIQUE KEY `unique_email` (`email`),
  ADD UNIQUE KEY `unique_cin` (`cin`);

--
-- Indexes for table `user_log`
--
ALTER TABLE `user_log`
  ADD PRIMARY KEY (`id_log`),
  ADD KEY `user_id` (`user_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `analyse`
--
ALTER TABLE `analyse`
  MODIFY `id_analyse` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `animaux`
--
ALTER TABLE `animaux`
  MODIFY `id_animal` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `conseil`
--
ALTER TABLE `conseil`
  MODIFY `id_conseil` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `face_data`
--
ALTER TABLE `face_data`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ferme`
--
ALTER TABLE `ferme`
  MODIFY `id_ferme` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `notification`
--
ALTER TABLE `notification`
  MODIFY `id_notification` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `plantes`
--
ALTER TABLE `plantes`
  MODIFY `id_plante` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `user`
--
ALTER TABLE `user`
  MODIFY `id_user` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `user_log`
--
ALTER TABLE `user_log`
  MODIFY `id_log` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `analyse`
--
ALTER TABLE `analyse`
  ADD CONSTRAINT `analyse_ibfk_1` FOREIGN KEY (`id_technicien`) REFERENCES `user` (`id_user`) ON DELETE CASCADE,
  ADD CONSTRAINT `analyse_ibfk_2` FOREIGN KEY (`id_ferme`) REFERENCES `ferme` (`id_ferme`) ON DELETE CASCADE;

--
-- Constraints for table `animaux`
--
ALTER TABLE `animaux`
  ADD CONSTRAINT `animaux_ibfk_1` FOREIGN KEY (`id_ferme`) REFERENCES `ferme` (`id_ferme`) ON DELETE CASCADE;

--
-- Constraints for table `conseil`
--
ALTER TABLE `conseil`
  ADD CONSTRAINT `conseil_ibfk_1` FOREIGN KEY (`id_analyse`) REFERENCES `analyse` (`id_analyse`) ON DELETE CASCADE;

--
-- Constraints for table `face_data`
--
ALTER TABLE `face_data`
  ADD CONSTRAINT `face_data_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id_user`) ON DELETE CASCADE;

--
-- Constraints for table `ferme`
--
ALTER TABLE `ferme`
  ADD CONSTRAINT `ferme_ibfk_1` FOREIGN KEY (`id_fermier`) REFERENCES `user` (`id_user`) ON DELETE CASCADE;

--
-- Constraints for table `notification`
--
ALTER TABLE `notification`
  ADD CONSTRAINT `notification_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `user` (`id_user`) ON DELETE CASCADE;

--
-- Constraints for table `plantes`
--
ALTER TABLE `plantes`
  ADD CONSTRAINT `plantes_ibfk_1` FOREIGN KEY (`id_ferme`) REFERENCES `ferme` (`id_ferme`) ON DELETE CASCADE;

--
-- Constraints for table `user_log`
--
ALTER TABLE `user_log`
  ADD CONSTRAINT `user_log_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id_user`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
