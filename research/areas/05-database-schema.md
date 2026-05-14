# Research Area: Database Schema Integration

## Status: 🟢 Complete

## What I Need To Learn
- What database tables does each branch require?
- Are there schema conflicts?
- What is the merged schema?

## Files Examined
- [x] `feature/expertise-is-alaeddin:farmai (semifinal).sql` — Main schema
- [x] `feature/securite-aymen:database/face_data_migration.sql` — Face data table
- [x] Service files for understanding table structures

## Findings

### Existing Tables (from Expertise Branch)

#### user Table
```sql
CREATE TABLE `user` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### ferme Table
```sql
CREATE TABLE `ferme` (
    `id_ferme` int(11) NOT NULL AUTO_INCREMENT,
    `nom_ferme` varchar(100) NOT NULL,
    `lieu` varchar(255) NOT NULL,
    `surface` double DEFAULT 0,
    `id_fermier` int(11) NOT NULL,
    `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
    `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
    PRIMARY KEY (`id_ferme`),
    UNIQUE KEY `unique_fermier` (`id_fermier`),
    FOREIGN KEY (`id_fermier`) REFERENCES `user` (`id_user`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### analyse Table
```sql
CREATE TABLE `analyse` (
    `id_analyse` int(11) NOT NULL AUTO_INCREMENT,
    `date_analyse` timestamp NOT NULL DEFAULT current_timestamp(),
    `resultat_technique` text DEFAULT NULL,
    `id_technicien` int(11) NOT NULL,
    `id_ferme` int(11) NOT NULL,
    `image_url` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id_analyse`),
    FOREIGN KEY (`id_technicien`) REFERENCES `user` (`id_user`) ON DELETE CASCADE,
    FOREIGN KEY (`id_ferme`) REFERENCES `ferme` (`id_ferme`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### conseil Table
```sql
CREATE TABLE `conseil` (
    `id_conseil` int(11) NOT NULL AUTO_INCREMENT,
    `description_conseil` text NOT NULL,
    `priorite` enum('HAUTE','MOYENNE','BASSE') DEFAULT 'MOYENNE',
    `id_analyse` int(11) NOT NULL,
    PRIMARY KEY (`id_conseil`),
    FOREIGN KEY (`id_analyse`) REFERENCES `analyse` (`id_analyse`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Security Branch Tables

#### face_data Table
```sql
CREATE TABLE IF NOT EXISTS face_data (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    face_model LONGBLOB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user (user_id),
    FOREIGN KEY (user_id) REFERENCES user(id_user) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### user_log Table (from UserLogService)
```sql
CREATE TABLE IF NOT EXISTS user_log (
    id_log INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    action ENUM('CREATE', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT') NOT NULL,
    performed_by VARCHAR(150) NOT NULL,
    description TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id_user) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Ferme Branch Tables (Need to Create)

#### animaux Table
```sql
CREATE TABLE IF NOT EXISTS animaux (
    id_animal INT AUTO_INCREMENT PRIMARY KEY,
    espece VARCHAR(100) NOT NULL,
    etat_sante VARCHAR(50) DEFAULT 'Bon',
    date_naissance DATE,
    id_ferme INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_ferme) REFERENCES ferme(id_ferme) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### plantes Table
```sql
CREATE TABLE IF NOT EXISTS plantes (
    id_plante INT AUTO_INCREMENT PRIMARY KEY,
    nom_espece VARCHAR(100) NOT NULL,
    cycle_vie VARCHAR(50),
    id_ferme INT NOT NULL,
    quantite DOUBLE DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_ferme) REFERENCES ferme(id_ferme) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## Merged Database Schema

### Complete SQL Script
```sql
-- ============================================
-- FarmAI Complete Database Schema
-- Merged from: expertise, security, ferme branches
-- ============================================

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

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
    UNIQUE KEY `unique_fermier` (`id_fermier`),
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

COMMIT;
```

## Entity Relationships

```
user (1) ──── (N) ferme
  │               │
  │               ├── (N) animaux
  │               ├── (N) plantes
  │               └── (N) analyse
  │                       │
  │                       └── (N) conseil
  │
  ├── (1) face_data
  └── (N) user_log
```

## Relevance to Implementation
The database schema is straightforward to merge. All tables can coexist without conflicts. The key is ensuring:
1. All foreign keys reference correct tables
2. Consistent charset (utf8mb4)
3. Consistent collation (utf8mb4_unicode_ci)
4. Timestamps for all tables

## Status Update
- [x] Analyzed all SQL files
- [x] Identified all required tables
- [x] Created merged schema
- [x] Documented entity relationships
