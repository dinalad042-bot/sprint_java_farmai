-- ============================================================
-- FarmAI ERP Tables Migration
-- Run this script once against your farmia_new database
-- to create all ERP tables required by the Java desktop app.
-- ============================================================

USE `farmia_new`;

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET FOREIGN_KEY_CHECKS = 0;

-- --------------------------------------------------------
-- erp_matiere  (raw materials)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `erp_matiere` (
  `id_matiere`     INT(11)        NOT NULL AUTO_INCREMENT,
  `nom`            VARCHAR(255)   NOT NULL,
  `description`    TEXT           DEFAULT NULL,
  `unite`          VARCHAR(50)    NOT NULL DEFAULT 'unité',
  `stock`          DOUBLE         NOT NULL DEFAULT 0,
  `prix_unitaire`  DECIMAL(10,2)  NOT NULL DEFAULT '0.00',
  `seuil_critique` DOUBLE         NOT NULL DEFAULT 0,
  PRIMARY KEY (`id_matiere`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- erp_produit  (finished products)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `erp_produit` (
  `id_produit`        INT(11)       NOT NULL AUTO_INCREMENT,
  `nom`               VARCHAR(255)  NOT NULL,
  `description`       TEXT          DEFAULT NULL,
  `prix_vente`        DOUBLE        NOT NULL DEFAULT 0,
  `quantite_produite` DOUBLE        NOT NULL DEFAULT 1,
  `stock`             DOUBLE        NOT NULL DEFAULT 0,
  `is_simple`         TINYINT(1)    NOT NULL DEFAULT 0,
  PRIMARY KEY (`id_produit`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- erp_recette_ingredient  (product recipe)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `erp_recette_ingredient` (
  `id`          INT(11) NOT NULL AUTO_INCREMENT,
  `id_produit`  INT(11) NOT NULL,
  `id_matiere`  INT(11) NOT NULL,
  `quantite`    DOUBLE  NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_recette` (`id_produit`, `id_matiere`),
  CONSTRAINT `fk_recette_produit`  FOREIGN KEY (`id_produit`) REFERENCES `erp_produit`  (`id_produit`) ON DELETE CASCADE,
  CONSTRAINT `fk_recette_matiere`  FOREIGN KEY (`id_matiere`) REFERENCES `erp_matiere`  (`id_matiere`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- erp_service  (services offered by fournisseur)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `erp_service` (
  `id_service`     INT(11)       NOT NULL AUTO_INCREMENT,
  `nom`            VARCHAR(255)  NOT NULL,
  `description`    TEXT          DEFAULT NULL,
  `prix`           DOUBLE        NOT NULL DEFAULT 0,
  `stock`          INT(11)       NOT NULL DEFAULT 0,
  `seuil_critique` INT(11)       NOT NULL DEFAULT 0,
  PRIMARY KEY (`id_service`),
  KEY `idx_erp_service_stock` (`stock`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- erp_achat  (purchase orders)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `erp_achat` (
  `id_achat`   INT(11)        NOT NULL AUTO_INCREMENT,
  `date_achat` DATE           NOT NULL,
  `total`      DECIMAL(10,2)  NOT NULL DEFAULT '0.00',
  `paid`       TINYINT(1)     NOT NULL DEFAULT 0,
  PRIMARY KEY (`id_achat`),
  KEY `idx_erp_achat_date` (`date_achat`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- erp_ligne_achat  (purchase order lines)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `erp_ligne_achat` (
  `id`            INT(11) NOT NULL AUTO_INCREMENT,
  `id_achat`      INT(11) NOT NULL,
  `id_matiere`    INT(11) NOT NULL,
  `quantite`      DOUBLE  NOT NULL DEFAULT 1,
  `prix_unitaire` DOUBLE  NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ligne_achat` (`id_achat`, `id_matiere`),
  CONSTRAINT `fk_lachat_achat`   FOREIGN KEY (`id_achat`)   REFERENCES `erp_achat`   (`id_achat`)   ON DELETE CASCADE,
  CONSTRAINT `fk_lachat_matiere` FOREIGN KEY (`id_matiere`) REFERENCES `erp_matiere` (`id_matiere`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- erp_vente  (sale orders)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `erp_vente` (
  `id_vente`   INT(11)        NOT NULL AUTO_INCREMENT,
  `date_vente` DATE           NOT NULL,
  `total`      DECIMAL(10,2)  NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`id_vente`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- erp_ligne_vente  (sale order lines)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `erp_ligne_vente` (
  `id`            INT(11) NOT NULL AUTO_INCREMENT,
  `id_vente`      INT(11) NOT NULL,
  `id_produit`    INT(11) NOT NULL,
  `quantite`      INT(11) NOT NULL DEFAULT 1,
  `prix_unitaire` DOUBLE  NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_lvente_vente`   FOREIGN KEY (`id_vente`)   REFERENCES `erp_vente`   (`id_vente`)   ON DELETE CASCADE,
  CONSTRAINT `fk_lvente_produit` FOREIGN KEY (`id_produit`) REFERENCES `erp_produit` (`id_produit`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- Sample data (optional — remove if not needed)
-- ============================================================
INSERT IGNORE INTO `erp_matiere` (`nom`, `description`, `unite`, `stock`, `prix_unitaire`, `seuil_critique`) VALUES
('Engrais NPK',    'Engrais minéral complet',      'kg',    500, 2.50, 50),
('Semences Blé',   'Semences certifiées blé dur',  'kg',    200, 5.00, 20),
('Pesticide Bio',  'Traitement biologique',        'L',     100, 12.00, 10),
('Eau distillée',  'Eau pour irrigation',          'L',    1000, 0.10, 100);

INSERT IGNORE INTO `erp_service` (`nom`, `description`, `prix`, `stock`, `seuil_critique`) VALUES
('Analyse de sol',       'Analyse complète NPK + pH',    150.00, 20, 5),
('Traitement phyto',     'Application pesticides',        80.00, 15, 3),
('Conseil agronomique',  'Visite et rapport expert',     200.00, 10, 2);
