-- phpMyAdmin SQL Dump
-- version 5.2.0
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1:3306
-- Généré le : ven. 10 avr. 2026 à 20:15
-- Version du serveur : 8.0.31
-- Version de PHP : 8.0.26

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `gestion_hotel`
--

-- --------------------------------------------------------

--
-- Structure de la table `chambre`
--

DROP TABLE IF EXISTS `chambre`;
CREATE TABLE IF NOT EXISTS `chambre` (
  `id_chambre` int NOT NULL AUTO_INCREMENT,
  `numero` varchar(20) NOT NULL,
  `etat` enum('DISPONIBLE','OCCUPEE','EN_NETTOYAGE','EN_MAINTENANCE') NOT NULL,
  `prix_par_nuit` decimal(10,2) NOT NULL,
  `id_type` int NOT NULL,
  `id_employe` int DEFAULT NULL,
  PRIMARY KEY (`id_chambre`),
  UNIQUE KEY `numero` (`numero`),
  KEY `id_type` (`id_type`),
  KEY `idx_chambre_numero` (`numero`),
  KEY `fk_chambre_employe` (`id_employe`)
) ;

--
-- Déchargement des données de la table `chambre`
--

INSERT INTO `chambre` (`id_chambre`, `numero`, `etat`, `prix_par_nuit`, `id_type`, `id_employe`) VALUES
(1, '101', 'DISPONIBLE', '25000.00', 1, NULL),
(2, '102', 'DISPONIBLE', '25000.00', 1, NULL),
(3, '103', 'EN_NETTOYAGE', '25000.00', 1, 1),
(4, '104', 'DISPONIBLE', '25000.00', 1, NULL),
(5, '105', 'DISPONIBLE', '25000.00', 1, NULL),
(6, '201', 'DISPONIBLE', '40000.00', 2, NULL),
(7, '202', 'DISPONIBLE', '40000.00', 2, NULL),
(8, '203', 'EN_MAINTENANCE', '40000.00', 2, 3),
(9, '204', 'DISPONIBLE', '40000.00', 2, NULL),
(10, '205', 'DISPONIBLE', '40000.00', 2, NULL),
(11, '206', 'DISPONIBLE', '40000.00', 2, NULL),
(12, '207', 'DISPONIBLE', '40000.00', 2, NULL),
(13, '301', 'DISPONIBLE', '85000.00', 3, NULL),
(14, '302', 'DISPONIBLE', '85000.00', 3, NULL),
(15, '303', 'DISPONIBLE', '85000.00', 3, NULL),
(16, '304', 'EN_NETTOYAGE', '85000.00', 3, 2),
(17, '401', 'DISPONIBLE', '40000.00', 2, NULL),
(18, '402', 'DISPONIBLE', '40000.00', 2, NULL),
(19, '501', 'DISPONIBLE', '85000.00', 3, NULL),
(20, '502', 'DISPONIBLE', '85000.00', 3, NULL);

-- --------------------------------------------------------

--
-- Structure de la table `client`
--

DROP TABLE IF EXISTS `client`;
CREATE TABLE IF NOT EXISTS `client` (
  `id_client` int NOT NULL AUTO_INCREMENT,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `telephone` varchar(20) DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `adresse` varchar(255) DEFAULT NULL,
  `numero_carte_fidelite` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id_client`),
  KEY `idx_client_nom` (`nom`)
) ENGINE=MyISAM AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `client`
--

INSERT INTO `client` (`id_client`, `nom`, `prenom`, `telephone`, `email`, `adresse`, `numero_carte_fidelite`) VALUES
(1, 'Konaté', 'Aboubacar', '+224 620 11 22 33', 'abou.konate@gmail.com', 'Conakry, Guinée', 'FID-0001'),
(2, 'Baldé', 'Kadiatou', '+224 622 44 55 66', 'kadia.balde@yahoo.fr', 'Labé, Guinée', 'FID-0002'),
(3, 'Diallo', 'Alpha', '+224 628 77 88 99', 'alpha.diallo@orange.gn', 'Kindia, Guinée', 'FID-0003'),
(4, 'Laye', 'Mamou', '+224 664 12 34 56', 'mamou.laye@gmail.com', 'Mamou, Guinée', 'FID-0004'),
(5, 'Soumah', 'Ibrahima', '+224 621 98 76 54', 'ibra.soumah@gmail.com', 'Coyah, Guinée', 'FID-0005'),
(6, 'Sako', 'Fatoumata', '+221 77 123 45 67', 'fatou.sako@free.sn', 'Dakar, Sénégal', 'FID-0006'),
(7, 'Traoré', 'Adama', '+223 70 234 56 78', 'adama.traore@gmail.com', 'Bamako, Mali', 'FID-0007'),
(8, 'Ouédraogo', 'Salif', '+226 70 345 67 89', 'salif.o@gmail.com', 'Ouagadougou, Burkina Faso', 'FID-0008'),
(9, 'Coulibaly', 'Mariam', '+225 07 456 78 90', 'mariam.coul@yahoo.fr', 'Abidjan, Côte d\'Ivoire', 'FID-0009'),
(10, 'Touré', 'Moussa', '+212 66 567 89 01', 'moussa.toure@gmail.com', 'Casablanca, Maroc', 'FID-0010'),
(11, 'Diarra', 'Fanta', '+223 65 678 90 12', 'fanta.diarra@gmail.com', 'Mopti, Mali', 'FID-0011'),
(12, 'Kamara', 'Yusuf', '+232 76 789 01 23', 'yusuf.kamara@yahoo.com', 'Freetown, Sierra Leone', 'FID-0012'),
(13, 'Ndiaye', 'Ndèye', '+221 70 890 12 34', 'ndeye.nd@orange.sn', 'Saint-Louis, Sénégal', 'FID-0013'),
(14, 'Bah', 'Tidiane', '+224 627 90 12 34', 'tidiane.bah@gmail.com', 'Pita, Guinée', 'FID-0014'),
(15, 'Sanogo', 'Djeneba', '+223 79 012 34 56', 'djeneba.s@gmail.com', 'Sikasso, Mali', 'FID-0015'),
(16, 'Gueye', 'Malick', '+221 76 123 45 67', 'malick.gueye@hotmail.fr', 'Thiès, Sénégal', 'FID-0016'),
(17, 'Kourouma', 'Aminata', '+224 625 23 45 67', 'ami.kourouma@gmail.com', 'Nzerekore, Guinée', 'FID-0017'),
(18, 'Sawaneh', 'Omar', '+220 99 345 67 89', 'omar.saw@gambia.gm', 'Banjul, Gambie', 'FID-0018'),
(19, 'Mensah', 'Kofi', '+233 24 456 78 90', 'kofi.mensah@gmail.com', 'Accra, Ghana', 'FID-0019'),
(20, 'Abiodun', 'Chioma', '+234 80 567 89 01', 'chioma.abi@gmail.com', 'Lagos, Nigeria', 'FID-0020'),
(21, 'Mbeki', 'Thabo', '+27 71 678 90 12', 'thabo.mbeki@gmail.com', 'Johannesburg, Afrique du S.', 'FID-0021'),
(22, 'Keita', 'Saran', '+224 660 78 90 12', 'saran.keita@gmail.com', 'Kankan, Guinée', 'FID-0022'),
(23, 'Diop', 'Serigne', '+221 78 890 12 34', 'serigne.diop@orange.sn', 'Ziguinchor, Sénégal', 'FID-0023'),
(24, 'Faye', 'Astou', '+221 77 901 23 45', 'astou.faye@gmail.com', 'Kaolack, Sénégal', 'FID-0024'),
(25, 'Wague', 'Aminata', '+222 36 012 34 56', 'aminata.wague@gmail.com', 'Nouakchott, Mauritanie', 'FID-0025'),
(26, 'Cissoko', 'Bocar', '+221 75 123 45 67', 'bocar.cissoko@yahoo.fr', 'Tambacounda, Sénégal', 'FID-0026'),
(27, 'Sall', 'Rokhaya', '+221 70 234 56 78', 'rokhaya.sall@gmail.com', 'Diourbel, Sénégal', 'FID-0027'),
(28, 'Maiga', 'Hamidou', '+223 66 345 67 89', 'hamidou.maiga@gmail.com', 'Gao, Mali', 'FID-0028'),
(29, 'Bocoum', 'Fatoumata', '+223 72 456 78 90', 'fatou.bocoum@gmail.com', 'Kayes, Mali', 'FID-0029'),
(30, 'Dembele', 'Mamadou', '+223 68 567 89 01', 'mamadou.d@gmail.com', 'Segou, Mali', 'FID-0030');

-- --------------------------------------------------------

--
-- Structure de la table `demande_speciale`
--

DROP TABLE IF EXISTS `demande_speciale`;
CREATE TABLE IF NOT EXISTS `demande_speciale` (
  `id_demande` int NOT NULL AUTO_INCREMENT,
  `nom_demande` varchar(100) NOT NULL,
  `description` text,
  PRIMARY KEY (`id_demande`)
) ENGINE=MyISAM AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `demande_speciale`
--

INSERT INTO `demande_speciale` (`id_demande`, `nom_demande`, `description`) VALUES
(1, 'Petit-dejeuner inclus', 'Petit-dejeuner continental servi en chambre ou au restaurant.'),
(2, 'Lit bebe', 'Lit bebe portable installe dans la chambre.'),
(3, 'Acces PMR', 'Chambre adaptee pour personnes a mobilite reduite.'),
(4, 'Vue piscine', 'Attribution d\'une chambre avec vue sur la piscine.'),
(5, 'Transfert aeroport', 'Navette aller-retour avec l\'aeroport international.'),
(6, 'Menu vegetarien', 'Repas vegetariens prepares sur demande.'),
(7, 'Jacuzzi prive', 'Jacuzzi privatif dans la suite.'),
(8, 'Decoration romantique', 'Petales de fleurs et bougies pour arrivee en couple.');

-- --------------------------------------------------------

--
-- Structure de la table `employe`
--

DROP TABLE IF EXISTS `employe`;
CREATE TABLE IF NOT EXISTS `employe` (
  `id_employe` int NOT NULL AUTO_INCREMENT,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `role` enum('NETTOYEUR','MAINTENANCIER') NOT NULL,
  PRIMARY KEY (`id_employe`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `employe`
--

INSERT INTO `employe` (`id_employe`, `nom`, `prenom`, `role`) VALUES
(1, 'Diallo', 'Mamadou', 'NETTOYEUR'),
(2, 'Koné', 'Fatoumata', 'NETTOYEUR'),
(3, 'Traoré', 'Moussa', 'MAINTENANCIER'),
(4, 'Coulibaly', 'Ibrahim', 'MAINTENANCIER'),
(5, 'Bah', 'Aissatou', 'NETTOYEUR'),
(6, 'Camara', 'Boubacar', 'MAINTENANCIER'),
(7, 'Sow', 'Mariama', 'NETTOYEUR'),
(8, 'Barry', 'Abdoulaye', 'NETTOYEUR'),
(9, 'Keita', 'Seydou', 'MAINTENANCIER'),
(10, 'Touré', 'Kadiatou', 'NETTOYEUR'),
(11, 'Doumbia', 'Alhassan', 'MAINTENANCIER'),
(12, 'Sylla', 'Hawa', 'NETTOYEUR');

-- --------------------------------------------------------

--
-- Structure de la table `facture`
--

DROP TABLE IF EXISTS `facture`;
CREATE TABLE IF NOT EXISTS `facture` (
  `id_facture` int NOT NULL AUTO_INCREMENT,
  `date_emission` date NOT NULL,
  `montant_total` decimal(10,2) NOT NULL,
  `statut_paiement` enum('NON_PAYE','PARTIEL','PAYE') NOT NULL DEFAULT 'NON_PAYE',
  `id_reservation` int NOT NULL,
  PRIMARY KEY (`id_facture`),
  UNIQUE KEY `id_reservation` (`id_reservation`),
  KEY `idx_facture_statut` (`statut_paiement`)
) ;

--
-- Déchargement des données de la table `facture`
--

INSERT INTO `facture` (`id_facture`, `date_emission`, `montant_total`, `statut_paiement`, `id_reservation`) VALUES
(1, '2026-01-10', '125000.00', 'PAYE', 1),
(2, '2026-01-15', '280000.00', 'PAYE', 2),
(3, '2026-01-14', '50000.00', 'PAYE', 3),
(4, '2026-01-25', '425000.00', 'PAYE', 4),
(5, '2026-02-05', '100000.00', 'PAYE', 5),
(6, '2026-02-14', '160000.00', 'PAYE', 6),
(7, '2026-02-20', '510000.00', 'PAYE', 7),
(8, '2026-02-28', '150000.00', 'PARTIEL', 8),
(9, '2026-03-07', '280000.00', 'PAYE', 9),
(10, '2026-03-10', '425000.00', 'PAYE', 10),
(11, '2026-03-15', '75000.00', 'PAYE', 11),
(12, '2026-03-22', '160000.00', 'PAYE', 12),
(13, '2026-03-30', '200000.00', 'NON_PAYE', 13),
(14, '2026-04-05', '100000.00', 'NON_PAYE', 14),
(15, '2026-04-08', '200000.00', 'NON_PAYE', 15);

-- --------------------------------------------------------

--
-- Structure de la table `paiement`
--

DROP TABLE IF EXISTS `paiement`;
CREATE TABLE IF NOT EXISTS `paiement` (
  `id_paiement` int NOT NULL AUTO_INCREMENT,
  `date_paiement` date NOT NULL,
  `montant` decimal(10,2) NOT NULL,
  `mode_paiement` enum('ESPECES','CARTE','MOBILE_MONEY') NOT NULL,
  `id_facture` int NOT NULL,
  PRIMARY KEY (`id_paiement`),
  KEY `id_facture` (`id_facture`)
) ;

--
-- Déchargement des données de la table `paiement`
--

INSERT INTO `paiement` (`id_paiement`, `date_paiement`, `montant`, `mode_paiement`, `id_facture`) VALUES
(1, '2026-01-10', '125000.00', 'ESPECES', 1),
(2, '2026-01-15', '280000.00', 'CARTE', 2),
(3, '2026-01-14', '50000.00', 'MOBILE_MONEY', 3),
(4, '2026-01-25', '425000.00', 'CARTE', 4),
(5, '2026-02-05', '100000.00', 'ESPECES', 5),
(6, '2026-02-14', '160000.00', 'CARTE', 6),
(7, '2026-02-20', '510000.00', 'CARTE', 7),
(8, '2026-02-28', '100000.00', 'ESPECES', 8),
(9, '2026-03-07', '280000.00', 'CARTE', 9),
(10, '2026-03-10', '425000.00', 'MOBILE_MONEY', 10),
(11, '2026-03-15', '75000.00', 'ESPECES', 11),
(12, '2026-03-22', '160000.00', 'CARTE', 12);

--
-- Déclencheurs `paiement`
--
DROP TRIGGER IF EXISTS `update_statut_facture`;
DELIMITER $$
CREATE TRIGGER `update_statut_facture` AFTER INSERT ON `paiement` FOR EACH ROW BEGIN

    DECLARE total_paye DECIMAL(10,2);

    SELECT SUM(montant)
    INTO total_paye
    FROM paiement
    WHERE id_facture = NEW.id_facture;

    IF total_paye >= (
        SELECT montant_total
        FROM facture
        WHERE id_facture = NEW.id_facture
    ) THEN

        UPDATE facture
        SET statut_paiement = 'PAYE'
        WHERE id_facture = NEW.id_facture;

    ELSE

        UPDATE facture
        SET statut_paiement = 'PARTIEL'
        WHERE id_facture = NEW.id_facture;

    END IF;

END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Structure de la table `rapport`
--

DROP TABLE IF EXISTS `rapport`;
CREATE TABLE IF NOT EXISTS `rapport` (
  `id_rapport` int NOT NULL AUTO_INCREMENT,
  `type_rapport` varchar(100) NOT NULL,
  `date_generation` date NOT NULL,
  `id_utilisateur` int NOT NULL,
  PRIMARY KEY (`id_rapport`),
  KEY `id_utilisateur` (`id_utilisateur`)
) ENGINE=MyISAM AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `rapport`
--

INSERT INTO `rapport` (`id_rapport`, `type_rapport`, `date_generation`, `id_utilisateur`) VALUES
(1, 'MENSUEL_JANVIER', '2026-02-01', 4),
(2, 'MENSUEL_FEVRIER', '2026-03-01', 4),
(3, 'MENSUEL_MARS', '2026-04-01', 4),
(4, 'TAUX_OCCUPATION_Q1', '2026-04-05', 4),
(5, 'CHIFFRE_AFFAIRES', '2026-04-05', 4);

-- --------------------------------------------------------

--
-- Structure de la table `reservation`
--

DROP TABLE IF EXISTS `reservation`;
CREATE TABLE IF NOT EXISTS `reservation` (
  `id_reservation` int NOT NULL AUTO_INCREMENT,
  `date_debut` date NOT NULL,
  `date_fin` date NOT NULL,
  `date_creation` datetime DEFAULT CURRENT_TIMESTAMP,
  `statut` enum('EN_ATTENTE','CONFIRMEE','ANNULEE','TERMINEE') NOT NULL,
  `id_client` int NOT NULL,
  `id_chambre` int NOT NULL,
  `notes` text,
  `id_utilisateur` int DEFAULT NULL,
  PRIMARY KEY (`id_reservation`)
) ENGINE=MyISAM AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `reservation`
--

INSERT INTO `reservation` (`id_reservation`, `date_debut`, `date_fin`, `date_creation`, `statut`, `id_client`, `id_chambre`, `notes`, `id_utilisateur`) VALUES
(1, '2026-01-05', '2026-01-10', '2025-12-20 09:00:00', 'TERMINEE', 1, 1, 'Client regulier, chambre calme svp.', 2),
(2, '2026-01-08', '2026-01-15', '2025-12-22 10:30:00', 'TERMINEE', 2, 6, 'Anniversaire de mariage.', 2),
(3, '2026-01-12', '2026-01-14', '2025-12-28 11:00:00', 'TERMINEE', 3, 2, 'Arrivee tardive apres 22h.', 3),
(4, '2026-01-20', '2026-01-25', '2026-01-01 08:00:00', 'TERMINEE', 4, 13, 'Suite demandee - client VIP.', 2),
(5, '2026-02-01', '2026-02-05', '2026-01-15 09:00:00', 'TERMINEE', 5, 4, NULL, 3),
(6, '2026-02-10', '2026-02-14', '2026-01-20 14:00:00', 'TERMINEE', 6, 7, 'Petit-dejeuner inclus chaque matin.', 2),
(7, '2026-02-14', '2026-02-20', '2026-01-28 10:00:00', 'TERMINEE', 7, 14, 'Decoration romantique souhaitee.', 3),
(8, '2026-02-22', '2026-02-28', '2026-02-05 11:30:00', 'TERMINEE', 8, 5, NULL, 2),
(9, '2026-03-01', '2026-03-07', '2026-02-10 09:00:00', 'TERMINEE', 9, 8, 'Transfert aeroport requis.', 3),
(10, '2026-03-05', '2026-03-10', '2026-02-15 10:00:00', 'TERMINEE', 10, 15, 'Vue piscine souhaitee.', 2),
(11, '2026-03-12', '2026-03-15', '2026-02-20 08:30:00', 'TERMINEE', 11, 9, NULL, 3),
(12, '2026-03-18', '2026-03-22', '2026-02-25 09:00:00', 'TERMINEE', 12, 10, 'Lit bebe necessaire.', 2),
(13, '2026-03-25', '2026-03-30', '2026-03-05 11:00:00', 'TERMINEE', 13, 11, NULL, 3),
(14, '2026-04-01', '2026-04-05', '2026-03-10 10:00:00', 'CONFIRMEE', 14, 12, 'Acces PMR requis.', 2),
(15, '2026-04-03', '2026-04-08', '2026-03-12 09:30:00', 'CONFIRMEE', 15, 16, NULL, 3),
(16, '2026-04-05', '2026-04-10', '2026-03-18 14:00:00', 'CONFIRMEE', 16, 2, 'Menu vegetarien.', 2),
(17, '2026-04-07', '2026-04-11', '2026-03-20 08:00:00', 'CONFIRMEE', 17, 17, NULL, 3),
(18, '2026-04-09', '2026-04-12', '2026-03-25 10:30:00', 'EN_ATTENTE', 18, 4, 'Appeler pour confirmer.', 2),
(19, '2026-04-10', '2026-04-15', '2026-04-01 09:00:00', 'EN_ATTENTE', 19, 18, NULL, 3),
(20, '2026-04-12', '2026-04-18', '2026-04-02 11:00:00', 'EN_ATTENTE', 20, 19, 'Jacuzzi prive souhaite.', 2),
(21, '2026-04-15', '2026-04-20', '2026-04-03 10:00:00', 'EN_ATTENTE', 21, 6, NULL, 3),
(22, '2026-04-18', '2026-04-22', '2026-04-04 09:30:00', 'EN_ATTENTE', 22, 20, 'Decoration speciale.', 2),
(23, '2026-04-20', '2026-04-25', '2026-04-05 08:00:00', 'EN_ATTENTE', 23, 7, NULL, 3),
(24, '2026-02-05', '2026-02-08', '2026-01-25 10:00:00', 'ANNULEE', 24, 10, 'Annulation pour raisons pro.', 2),
(25, '2026-03-20', '2026-03-23', '2026-03-01 11:00:00', 'ANNULEE', 25, 11, 'Annule 2 jours avant.', 3);

-- --------------------------------------------------------

--
-- Structure de la table `reservation_demande`
--

DROP TABLE IF EXISTS `reservation_demande`;
CREATE TABLE IF NOT EXISTS `reservation_demande` (
  `id_reservation` int NOT NULL,
  `id_demande` int NOT NULL,
  `statut` enum('EN_ATTENTE','ACCEPTEE','REFUSEE','TERMINEE') DEFAULT 'EN_ATTENTE',
  `date_traitement` datetime DEFAULT NULL,
  PRIMARY KEY (`id_reservation`,`id_demande`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `reservation_demande`
--

INSERT INTO `reservation_demande` (`id_reservation`, `id_demande`, `statut`, `date_traitement`) VALUES
(1, 1, 'TERMINEE', '2026-01-05 08:00:00'),
(2, 8, 'TERMINEE', '2026-01-08 09:00:00'),
(2, 1, 'TERMINEE', '2026-01-08 09:00:00'),
(3, 5, 'TERMINEE', '2026-01-12 07:30:00'),
(4, 7, 'TERMINEE', '2026-01-20 10:00:00'),
(6, 1, 'TERMINEE', '2026-02-10 07:45:00'),
(7, 8, 'TERMINEE', '2026-02-14 09:00:00'),
(7, 1, 'TERMINEE', '2026-02-14 09:00:00'),
(9, 5, 'TERMINEE', '2026-03-01 06:00:00'),
(10, 4, 'TERMINEE', '2026-03-05 10:00:00'),
(12, 2, 'TERMINEE', '2026-03-18 09:00:00'),
(14, 3, 'ACCEPTEE', '2026-04-01 09:00:00'),
(16, 6, 'ACCEPTEE', '2026-04-05 08:00:00'),
(20, 7, 'EN_ATTENTE', NULL),
(22, 8, 'EN_ATTENTE', NULL);

-- --------------------------------------------------------

--
-- Structure de la table `retour`
--

DROP TABLE IF EXISTS `retour`;
CREATE TABLE IF NOT EXISTS `retour` (
  `id_retour` int NOT NULL AUTO_INCREMENT,
  `date_retour` date NOT NULL,
  `commentaire` text NOT NULL,
  `satisfaction` int DEFAULT NULL,
  `id_client` int NOT NULL,
  PRIMARY KEY (`id_retour`),
  KEY `id_client` (`id_client`)
) ;

--
-- Déchargement des données de la table `retour`
--

INSERT INTO `retour` (`id_retour`, `date_retour`, `commentaire`, `satisfaction`, `id_client`) VALUES
(1, '2026-01-10', 'Sejour excellent ! Personnel tres accueillant. Je reviendrai.', 5, 1),
(2, '2026-01-15', 'Tres beau cadre, petit-dejeuner delicieux. Chambre un peu bruyante.', 4, 2),
(3, '2026-01-14', 'Correct pour un court sejour. Proprete impeccable.', 4, 3),
(4, '2026-01-25', 'Suite magnifique ! Service 5 etoiles, j\'ai adore chaque moment.', 5, 4),
(5, '2026-02-05', 'Bon rapport qualite-prix. Accueil chaleureux.', 4, 5),
(6, '2026-02-14', 'Tres satisfaite, decor parfait pour notre anniversaire.', 5, 6),
(7, '2026-02-20', 'Sejour romantique inoubliable. Merci a toute l\'equipe !', 5, 7),
(8, '2026-02-28', 'Bien en general, mais la clim faisait du bruit la nuit.', 3, 8),
(9, '2026-03-07', 'Transfert aeroport tres appreciable. Chauffeur ponctuel.', 5, 9),
(10, '2026-03-10', 'Vue sublime sur la piscine. Personnel professionnel.', 5, 10),
(11, '2026-03-15', 'Sejour satisfaisant. Rien a signaler de negatif.', 4, 11),
(12, '2026-03-22', 'Le lit bebe etait de bonne qualite. Merci pour l\'attention !', 5, 12);

-- --------------------------------------------------------

--
-- Structure de la table `type_chambre`
--

DROP TABLE IF EXISTS `type_chambre`;
CREATE TABLE IF NOT EXISTS `type_chambre` (
  `id_type` int NOT NULL AUTO_INCREMENT,
  `libelle` varchar(100) NOT NULL,
  `capacite` int NOT NULL,
  `description` text,
  PRIMARY KEY (`id_type`)
) ;

--
-- Déchargement des données de la table `type_chambre`
--

INSERT INTO `type_chambre` (`id_type`, `libelle`, `capacite`, `description`) VALUES
(1, 'Simple', 1, 'Chambre confortable pour 1 personne, lit queen, vue sur jardin.'),
(2, 'Double', 2, 'Chambre spacieuse pour 2 personnes, 2 lits ou grand lit, A/C.'),
(3, 'Suite', 4, 'Suite de luxe avec salon, jacuzzi et terrasse panoramique.');

-- --------------------------------------------------------

--
-- Structure de la table `utilisateur`
--

DROP TABLE IF EXISTS `utilisateur`;
CREATE TABLE IF NOT EXISTS `utilisateur` (
  `id_utilisateur` int NOT NULL AUTO_INCREMENT,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `nom_utilisateur` varchar(100) NOT NULL,
  `mot_de_passe` varchar(255) NOT NULL,
  `role` enum('ADMIN','RECEPTIONNISTE','DIRECTION') NOT NULL,
  PRIMARY KEY (`id_utilisateur`),
  UNIQUE KEY `nom_utilisateur` (`nom_utilisateur`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `utilisateur`
--

INSERT INTO `utilisateur` (`id_utilisateur`, `nom`, `prenom`, `nom_utilisateur`, `mot_de_passe`, `role`) VALUES
(1, 'Kouyaté', 'Sékou', 'admin', 'admin', 'ADMIN'),
(2, 'Diakité', 'Rokhaya', 'reception1', 'pass123', 'RECEPTIONNISTE'),
(3, 'Bamba', 'Cheikh', 'reception2', 'pass123', 'RECEPTIONNISTE'),
(4, 'Fofana', 'Aminata', 'direction', 'direction', 'DIRECTION'),
(5, 'Ndiaye', 'Ousmane', 'ousmane.n', 'pass123', 'RECEPTIONNISTE'),
(6, 'Cissé', 'Mariam', 'mariam.c', 'pass123', 'RECEPTIONNISTE');
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
