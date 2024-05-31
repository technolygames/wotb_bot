-- MariaDB dump 10.19  Distrib 10.4.24-MariaDB, for Win64 (AMD64)
--
-- Host: localhost    Database: wotb_bot
-- ------------------------------------------------------
-- Server version	10.4.24-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `tank_list`
--

DROP TABLE IF EXISTS `tank_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tank_list` (
  `tank_id` int(10) NOT NULL,
  `tank_name` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nation` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tank_tier` int(3) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tank_list`
--

LOCK TABLES `tank_list` WRITE;
/*!40000 ALTER TABLE `tank_list` DISABLE KEYS */;
INSERT INTO `tank_list` VALUES (7297,'60TP Lewandowskiego','european',10),(15617,'Object 907','ussr',10),(20257,'XM551 Sheridan','usa',10),(6209,'AMX 50 B','france',10),(6145,'IS-4','ussr',10),(9489,'E 100','germany',10),(10785,'T110E5','usa',10),(4417,'AMX M4 mle. 54','france',10),(15697,'Chieftain Mk. 6','uk',10),(13857,'T110E3','usa',10),(17745,'FV217 Badger','uk',10),(14881,'T57 Heavy Tank','usa',10),(14609,'Leopard 1','germany',10),(15905,'M60','usa',10),(14337,'Object 263','ussr',10),(22273,'Object 260','ussr',10),(18977,'T95E6','usa',10),(6225,'FV215b','uk',10),(6753,'Type 71','japan',10),(7249,'FV4202','uk',10),(12161,'Strv K','european',10),(9297,'FV215b (183)','uk',10),(21777,'VK 90.01 (P)','germany',10),(10369,'Controcarro 3 Minotauro','european',10),(24577,'Object 268 Version 4','ussr',10),(12305,'E 50 Ausf. M','germany',10),(19537,'Vickers Light 105','uk',10),(58641,'VK 72.01 (K)','germany',10),(21793,'XM551 Sheridan Missile','usa',10),(22817,'M-VI-Yoh','usa',10),(8497,'WZ-111 model 5A','china',10),(6929,'Maus','germany',10),(24321,'T-100 LT','ussr',10),(385,'Progetto M40 mod. 65','european',10),(13889,'AMX 50 Foch (155)','france',10),(12049,'Jagdpanzer E 100','germany',10),(4481,'Kranvagn','european',10),(5505,'TVP T 50/51','european',10),(13825,'T-62A','ussr',10),(19281,'Super Conqueror','uk',10),(3937,'Ho-Ri Type III','japan',10),(5681,'121B','china',10),(3649,'Bat.-Châtillon 25 t','france',10),(4145,'WZ-121','china',10),(19217,'Grille 15','germany',10),(19969,'T-22 medium','ussr',10),(8513,'AMX 30 B','france',10),(6449,'WZ-113G FT','china',10),(23313,'Kampfpanzer 50 t','germany',10),(24609,'Concept 1B','usa',10),(16897,'Object 140','ussr',10),(13089,'T110E4','usa',10),(10113,'Carro da Combattimento 45t','european',10),(14113,'M48 Patton','usa',10),(18001,'FV4005','uk',10),(13569,'Object 268','ussr',10),(3681,'STB-1','japan',10),(25857,'Object 777 Version II','ussr',10),(7169,'IS-7','ussr',10),(13185,'Vz. 55','european',10),(14977,'CS-63','european',10),(10289,'WZ-132-1','china',10),(5425,'WZ-113','china',10),(11825,'BZ-75','china',10),(11057,'114 SP2','china',10),(28705,'XM66F','usa',10),(17793,'Rinoceronte','european',10),(12849,'116-F3','china',10);
/*!40000 ALTER TABLE `tank_list` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tank_stats`
--

DROP TABLE IF EXISTS `tank_stats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tank_stats` (
  `wotb_id` int(12) NOT NULL,
  `tank_id` int(10) NOT NULL,
  `tank_tier` int(3) NOT NULL,
  `battles` int(10) NOT NULL,
  `wins` int(10) NOT NULL,
  `losses` int(10) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tank_stats`
--

LOCK TABLES `tank_stats` WRITE;
/*!40000 ALTER TABLE `tank_stats` DISABLE KEYS */;
/*!40000 ALTER TABLE `tank_stats` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `team`
--

DROP TABLE IF EXISTS `team`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `team` (
  `clantag` varchar(5) NOT NULL,
  `wotb_id` int(12) NOT NULL,
  `wotb_name` varchar(200) NOT NULL,
  `realm` varchar(10) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `team`
--

LOCK TABLES `team` WRITE;
/*!40000 ALTER TABLE `team` DISABLE KEYS */;
/*!40000 ALTER TABLE `team` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_data`
--

DROP TABLE IF EXISTS `user_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_data` (
  `discord_id` blob DEFAULT NULL,
  `wotb_id` int(12) NOT NULL,
  `wotb_name` varchar(200) NOT NULL,
  `realm` varchar(40) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_data`
--

LOCK TABLES `user_data` WRITE;
/*!40000 ALTER TABLE `user_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_data` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-05-31  3:00:46
