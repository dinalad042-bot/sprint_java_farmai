-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Feb 25, 2026 at 12:25 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.1.25

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
(1, '2026-02-24 22:28:09', 'Analyse du sol: pH neutre, richesse en azote optimale', 2, 1, '/images/analyse1.jpg'),
(2, '2026-02-24 22:28:09', 'Détection de maladie: Mildiou présent à 15%', 2, 2, '/images/analyse2.jpg'),
(3, '2026-02-24 22:30:21', 'Analyse foliaire: Carence en potassium détectée', 2, 3, 'C:\\Users\\sliti\\Pictures\\Screenshots\\Screenshot 2025-12-29 192915.png'),
(4, '2026-02-24 22:29:34', 'ala2vdsdv', 1, 2, 'C:\\Users\\sliti\\Pictures\\Screenshots\\Screenshot 2026-02-23 024842.png'),
(5, '2026-02-24 23:22:53', 'Analyse de sol: pH 6.8, bonne teneur en matière organique. Recommandation: fertilisation azotée légère.', 2, 5, NULL),
(6, '2026-02-24 23:22:53', 'Détection de nuisibles: Pucerons présents à 5%. Traitement biologique recommandé.', 2, 5, NULL),
(7, '2026-02-24 23:22:53', 'Analyse foliaire: État sanitaire excellent. Pas de carences détectées.', 5, 5, NULL);

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
(1, 'Appliquer un engrais riche en potassium dans les 7 jours', 'HAUTE', 1),
(2, 'Surveiller l humidité et aérer les plants si nécessaire', 'MOYENNE', 1),
(3, 'Traiter avec un fongicide approprié contre le mildiou', 'HAUTE', 2),
(4, 'Irrigation recommandée: 2 fois par semaine', 'MOYENNE', 3),
(5, 'Appliquer un traitement biologique contre les pucerons dans les 3 jours', 'HAUTE', 6),
(6, 'Fertilisation azotée légère recommandée avant la prochaine saison', 'MOYENNE', 5),
(7, 'Continuer les pratiques actuelles, excellent état sanitaire', 'BASSE', 7),
(8, 'Irrigation modérée: 2 fois par semaine maximum', 'MOYENNE', 3);

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
(1, 'Ferme Pilote Admin', 'Tunis, Tunisie', 100, 1, '2026-02-24 22:28:09', '2026-02-24 22:28:09'),
(2, 'Ferme Experte Sfax', 'Sfax, Tunisie', 75, 2, '2026-02-24 22:28:09', '2026-02-24 22:28:09'),
(3, 'Ferme Test Agricole', 'Sousse, Tunisie', 50, 3, '2026-02-24 22:28:09', '2026-02-24 22:28:09'),
(4, 'Ferme Bizerte', 'Bizerte, Tunisie', 60, 4, '2026-02-24 22:28:09', '2026-02-24 22:28:09'),
(5, 'Ferme Fermier 1', 'Nabeul, Tunisie', 45, 10, '2026-02-24 23:22:53', '2026-02-24 23:22:53');

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
(1, 'Admin', 'Super', 'admin@farmai.tn', 'dO6VnlGvKxY0AAAA$+Y0XqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqx=', '00000000', 'Tunis, Tunisie', '+216 70 000 000', NULL, 'ADMIN', '2026-02-24 22:09:00', '2026-02-24 22:09:00'),
(2, 'Expert', 'Test', 'expert@farmai.tn', 'dO6VnlGvKxY0AAAA$+Y0XqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqx=', '11111111', 'Sfax, Tunisie', '+216 71 000 000', NULL, 'EXPERT', '2026-02-24 22:09:00', '2026-02-24 22:09:00'),
(3, 'Agricole', 'Test', 'agricole@farmai.tn', 'dO6VnlGvKxY0AAAA$+Y0XqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqx=', '22222222', 'Sousse, Tunisie', '+216 72 000 000', NULL, 'AGRICOLE', '2026-02-24 22:09:00', '2026-02-24 22:09:00'),
(4, 'Fournisseur', 'Test', 'fournisseur@farmai.tn', 'dO6VnlGvKxY0AAAA$+Y0XqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqxZXqx=', '33333333', 'Bizerte, Tunisie', '+216 73 000 000', NULL, 'FOURNISSEUR', '2026-02-24 22:09:00', '2026-02-24 22:09:00'),
(5, 'ala', 'ala', 'ala@farmai.com', 'cMRxwgv6FHG9r2TKr8v/fg==$/r3AAqqIWNemgTwSIgyZZHgfISbVUbYiPeaqsmiOgdY=', '11111112', 'vd', '5552528565', NULL, 'EXPERT', '2026-02-24 22:10:46', '2026-02-24 22:10:46'),
(10, 'fermier1@farmai.comfermier1@farmai.com', 'fermier1@farmai.com', 'fermier1@farmai.com', '8oqQeesIzEblF1KxUKiwsw==$mqT7/MhfHmi4j2ILmJClyd7FidMcn65cqblOkaNgu3E=', '25135316', 'vds', '25282588', NULL, 'AGRICOLE', '2026-02-24 23:00:24', '2026-02-24 23:00:24'),
(11, 'ad1@farmai.com', 'ad1@farmai.com', 'ad1@farmai.com', 'Z1TYl1Ks6O1zT83K61c45g==$eLwJ7JPt5fXJ+0qaqsvaAD0NuC/csuXmIsZayAecZZc=', '15462655', '5564351335', '3524168516', NULL, 'ADMIN', '2026-02-24 23:01:01', '2026-02-24 23:01:01');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `analyse`
--
ALTER TABLE `analyse`
  ADD PRIMARY KEY (`id_analyse`),
  ADD KEY `id_technicien` (`id_technicien`);

--
-- Indexes for table `conseil`
--
ALTER TABLE `conseil`
  ADD PRIMARY KEY (`id_conseil`),
  ADD KEY `id_analyse` (`id_analyse`);

--
-- Indexes for table `ferme`
--
ALTER TABLE `ferme`
  ADD PRIMARY KEY (`id_ferme`),
  ADD UNIQUE KEY `unique_fermier` (`id_fermier`);

--
-- Indexes for table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id_user`),
  ADD UNIQUE KEY `unique_email` (`email`),
  ADD UNIQUE KEY `unique_cin` (`cin`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `analyse`
--
ALTER TABLE `analyse`
  MODIFY `id_analyse` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `conseil`
--
ALTER TABLE `conseil`
  MODIFY `id_conseil` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `ferme`
--
ALTER TABLE `ferme`
  MODIFY `id_ferme` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `user`
--
ALTER TABLE `user`
  MODIFY `id_user` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `analyse`
--
ALTER TABLE `analyse`
  ADD CONSTRAINT `analyse_ibfk_1` FOREIGN KEY (`id_technicien`) REFERENCES `user` (`id_user`) ON DELETE CASCADE;

--
-- Constraints for table `conseil`
--
ALTER TABLE `conseil`
  ADD CONSTRAINT `conseil_ibfk_1` FOREIGN KEY (`id_analyse`) REFERENCES `analyse` (`id_analyse`) ON DELETE CASCADE;

--
-- Constraints for table `ferme`
--
ALTER TABLE `ferme`
  ADD CONSTRAINT `ferme_ibfk_1` FOREIGN KEY (`id_fermier`) REFERENCES `user` (`id_user`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
