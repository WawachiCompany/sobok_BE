-- MySQL dump 10.13  Distrib 9.4.0, for macos15.4 (arm64)
--
-- Host: springmysql1.mysql.database.azure.com    Database: sobok
-- ------------------------------------------------------
-- Server version	8.0.41-azure

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `member`
--

DROP TABLE IF EXISTS `member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `display_name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `birth` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `point` int DEFAULT '0',
  `is_oauth` bit(1) DEFAULT NULL,
  `is_premium` bit(1) DEFAULT b'0',
  `consecutive_achieve_count` int DEFAULT '0',
  `premium_price` int DEFAULT '9999',
  `total_achieved_time` int DEFAULT '0',
  `total_account_balance` int DEFAULT '0',
  `weekly_routine_time` int DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `member_link_app`
--

DROP TABLE IF EXISTS `member_link_app`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member_link_app` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint DEFAULT NULL,
  `link_app` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_member_link_app_member` (`member_id`),
  CONSTRAINT `FK_member_link_app_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `oauth_account`
--

DROP TABLE IF EXISTS `oauth_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oauth_account` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `oauth_id` varchar(255) DEFAULT NULL,
  `provider` varchar(255) DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_oauth_account_member` (`member_id`),
  CONSTRAINT `FK_oauth_account_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `duration` int DEFAULT NULL,
  `time` int DEFAULT NULL,
  `is_expired` bit(1) DEFAULT b'0',
  `created_at` date DEFAULT NULL,
  `balance` int DEFAULT '0',
  `interest` float DEFAULT NULL,
  `is_valid` bit(1) DEFAULT b'0',
  `expired_at` date DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `interest_balance` bigint DEFAULT '0',
  `is_ended` bit(1) DEFAULT b'0',
  `is_extended` bit(1) DEFAULT b'0',
  `member_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_account_member` (`member_id`),
  CONSTRAINT `FK_account_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `account_log`
--

DROP TABLE IF EXISTS `account_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `deposit_time` int DEFAULT NULL,
  `account_id` bigint DEFAULT NULL,
  `balance` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_account_log_account` (`account_id`),
  CONSTRAINT `FK_account_log_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_category_member` (`member_id`),
  CONSTRAINT `FK_category_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `daily_achieve`
--

DROP TABLE IF EXISTS `daily_achieve`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `daily_achieve` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `date` date DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_daily_achieve_member` (`member_id`),
  CONSTRAINT `FK_daily_achieve_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `fcm_token`
--

DROP TABLE IF EXISTS `fcm_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fcm_token` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `fcm_token` varchar(255) DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_fcm_token_member` (`member_id`),
  CONSTRAINT `FK_fcm_token_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `interest_log`
--

DROP TABLE IF EXISTS `interest_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interest_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account_id` bigint DEFAULT NULL,
  `interest` int DEFAULT NULL,
  `target_year_month` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_interest_log_account` (`account_id`),
  CONSTRAINT `FK_interest_log_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `monthly_user_report`
--

DROP TABLE IF EXISTS `monthly_user_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `monthly_user_report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint DEFAULT NULL,
  `total_accumulated_time` bigint DEFAULT NULL,
  `average_accumulated_time` bigint DEFAULT NULL,
  `target_year_month` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_monthly_user_report_member` (`member_id`),
  CONSTRAINT `FK_monthly_user_report_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `point_log`
--

DROP TABLE IF EXISTS `point_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `point_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  `point` int DEFAULT NULL,
  `balance` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_point_log_member` (`member_id`),
  CONSTRAINT `FK_point_log_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `premium`
--

DROP TABLE IF EXISTS `premium`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `premium` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `end_at` date DEFAULT NULL,
  `start_at` date DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_premium_member` (`member_id`),
  CONSTRAINT `FK_premium_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `refresh_token`
--

DROP TABLE IF EXISTS `refresh_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refresh_token` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `expired_at` datetime(6) DEFAULT NULL,
  `refresh_token` varchar(3000) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `routine`
--

DROP TABLE IF EXISTS `routine`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `routine` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `duration` bigint DEFAULT NULL,
  `end_time` time(6) DEFAULT NULL,
  `is_suspended` bit(1) DEFAULT b'0',
  `start_time` time(6) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `account_id` bigint DEFAULT NULL,
  `is_ended` bit(1) DEFAULT b'0',
  `is_achieved` bit(1) DEFAULT b'0',
  `is_ai_routine` bit(1) DEFAULT NULL,
  `is_completed` bit(1) DEFAULT b'0',
  `member_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_routine_member` (`member_id`),
  KEY `FK_routine_account` (`account_id`),
  CONSTRAINT `FK_routine_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`),
  CONSTRAINT `FK_routine_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `routine_day`
--

DROP TABLE IF EXISTS `routine_day`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `routine_day` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `routine_id` bigint DEFAULT NULL,
  `day` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_routine_day_routine` (`routine_id`),
  CONSTRAINT `FK_routine_day_routine` FOREIGN KEY (`routine_id`) REFERENCES `routine` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `routine_log`
--

DROP TABLE IF EXISTS `routine_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `routine_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `end_time` datetime(6) DEFAULT NULL,
  `is_completed` bit(1) DEFAULT NULL,
  `start_time` datetime(6) DEFAULT NULL,
  `routine_id` bigint DEFAULT NULL,
  `duration` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_routine_log_routine` (`routine_id`),
  CONSTRAINT `FK_routine_log_routine` FOREIGN KEY (`routine_id`) REFERENCES `routine` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `snow_card`
--

DROP TABLE IF EXISTS `snow_card`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `snow_card` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint DEFAULT NULL,
  `snow_card` varchar(255) DEFAULT NULL,
  `target_year_month` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_snow_card_member` (`member_id`),
  CONSTRAINT `FK_snow_card_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `spare_time`
--

DROP TABLE IF EXISTS `spare_time`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `spare_time` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `duration` bigint DEFAULT NULL,
  `end_time` varchar(255) DEFAULT NULL,
  `start_time` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_spare_time_member` (`member_id`),
  CONSTRAINT `FK_spare_time_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `spare_time_day`
--

DROP TABLE IF EXISTS `spare_time_day`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `spare_time_day` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `spare_time_id` bigint DEFAULT NULL,
  `day` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_spare_time_day_spare_time` (`spare_time_id`),
  CONSTRAINT `FK_spare_time_day_spare_time` FOREIGN KEY (`spare_time_id`) REFERENCES `spare_time` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `survey`
--

DROP TABLE IF EXISTS `survey`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `survey` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `preference1` varchar(255) DEFAULT NULL,
  `preference2` varchar(255) DEFAULT NULL,
  `preference3` varchar(255) DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  `extra_request` varchar(255) DEFAULT NULL,
  `spare_tpo` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKetac06cibj0amksgg9xb2c8q5` (`member_id`),
  CONSTRAINT `FK_survey_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `survey_like_option`
--

DROP TABLE IF EXISTS `survey_like_option`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `survey_like_option` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `survey_id` bigint DEFAULT NULL,
  `like_option` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_survey_like_option_survey` (`survey_id`),
  CONSTRAINT `FK_survey_like_option_survey` FOREIGN KEY (`survey_id`) REFERENCES `survey` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `survey_spare_time`
--

DROP TABLE IF EXISTS `survey_spare_time`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `survey_spare_time` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `survey_id` bigint DEFAULT NULL,
  `spare_time` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_survey_spare_time_survey` (`survey_id`),
  CONSTRAINT `FK_survey_spare_time_survey` FOREIGN KEY (`survey_id`) REFERENCES `survey` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `todo`
--

DROP TABLE IF EXISTS `todo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `todo` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `end_time` time(6) DEFAULT NULL,
  `is_completed` bit(1) DEFAULT b'0',
  `link_app` varchar(255) DEFAULT NULL,
  `start_time` time(6) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `routine_id` bigint DEFAULT NULL,
  `duration` bigint DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_todo_routine` (`routine_id`),
  CONSTRAINT `FK_todo_routine` FOREIGN KEY (`routine_id`) REFERENCES `routine` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `todo_log`
--

DROP TABLE IF EXISTS `todo_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `todo_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `end_time` datetime(6) DEFAULT NULL,
  `is_completed` bit(1) DEFAULT NULL,
  `start_time` datetime(6) DEFAULT NULL,
  `todo_id` bigint DEFAULT NULL,
  `duration` bigint DEFAULT NULL,
  `routine_log_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_todo_log_routine_log` (`routine_log_id`),
  KEY `FK_todo_log_todo` (`todo_id`),
  CONSTRAINT `FK_todo_log_routine_log` FOREIGN KEY (`routine_log_id`) REFERENCES `routine_log` (`id`),
  CONSTRAINT `FK_todo_log_todo` FOREIGN KEY (`todo_id`) REFERENCES `todo` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-08-13  0:27:40
