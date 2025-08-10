-- Sobok Database Schema
-- Generated from production dump and optimized for development

-- Member table (핵심 사용자 테이블)
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
  `point` int DEFAULT 0,
  `is_oauth` bit(1) DEFAULT b'0',
  `is_premium` bit(1) DEFAULT b'0',
  `consecutive_achieve_count` int DEFAULT 0,
  `premium_price` int DEFAULT NULL,
  `total_achieved_time` int DEFAULT 0,
  `total_account_balance` int DEFAULT 0,
  `weekly_routine_time` int DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_username` (`username`),
  UNIQUE KEY `unique_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- OAuth Account table (소셜 로그인)
CREATE TABLE `oauth_account` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `oauth_id` varchar(255) DEFAULT NULL,
  `provider` varchar(255) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKc9j2dejpupppmsibwoytucgrt` (`user_id`),
  CONSTRAINT `FKc9j2dejpupppmsibwoytucgrt` FOREIGN KEY (`user_id`) REFERENCES `member` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Account table (적금 계좌)
CREATE TABLE `account` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `duration` int DEFAULT NULL,
  `time` int DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `is_expired` bit(1) DEFAULT b'0',
  `created_at` date DEFAULT NULL,
  `balance` int DEFAULT 0,
  `interest` float DEFAULT 0.0,
  `is_valid` bit(1) DEFAULT b'1',
  `expired_at` date DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `interest_balance` bigint DEFAULT 0,
  `is_ended` bit(1) DEFAULT b'0',
  `is_extended` bit(1) DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `FK9tbde7uf5oh8y0t33m9poi1od` (`user_id`),
  CONSTRAINT `FK9tbde7uf5oh8y0t33m9poi1od` FOREIGN KEY (`user_id`) REFERENCES `member` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Account Log table (적금 입금 기록)
CREATE TABLE `account_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `deposit_time` int DEFAULT NULL,
  `account_id` bigint DEFAULT NULL,
  `balance` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKra6d7dsxy8ys08w9k6smsgrrk` (`account_id`),
  CONSTRAINT `FKra6d7dsxy8ys08w9k6smsgrrk` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- AI Routine table (AI 추천 루틴)
CREATE TABLE `ai_routine` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `end_time` time(6) DEFAULT NULL,
  `start_time` time(6) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_ai_routine_member` (`user_id`),
  CONSTRAINT `FK_ai_routine_member` FOREIGN KEY (`user_id`) REFERENCES `member` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- AI Routine Days table (AI 루틴 요일 설정)
CREATE TABLE `ai_routine_days` (
  `my_row_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `ai_routine_id` bigint NOT NULL,
  `day` bit(1) DEFAULT NULL,
  PRIMARY KEY (`my_row_id`),
  KEY `FK9tjxfe3l4klewhtndmb558a8m` (`ai_routine_id`),
  CONSTRAINT `FK9tjxfe3l4klewhtndmb558a8m` FOREIGN KEY (`ai_routine_id`) REFERENCES `ai_routine` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Category table (카테고리)
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKd7qtd46ngp06lnc19g6wtoh8t` (`member_id`),
  CONSTRAINT `FKd7qtd46ngp06lnc19g6wtoh8t` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Daily Achieve table (일일 달성 기록)
CREATE TABLE `daily_achieve` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `date` date DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4272dj4rxvj95k7jqj1qnfmhf` (`member_id`),
  CONSTRAINT `FK4272dj4rxvj95k7jqj1qnfmhf` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Ended Account table (종료된 계좌)
CREATE TABLE `ended_account` (
  `id` bigint NOT NULL,
  `balance` int DEFAULT NULL,
  `created_at` date DEFAULT NULL,
  `duration` int DEFAULT NULL,
  `expired_at` date DEFAULT NULL,
  `interest` float DEFAULT NULL,
  `time` int DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKtdmphfnns5hquw711n50rhjxv` (`member_id`),
  CONSTRAINT `FKtdmphfnns5hquw711n50rhjxv` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- FCM Token table (푸시 알림 토큰)
CREATE TABLE `fcm_token` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) DEFAULT b'1',
  `created_at` datetime(6) DEFAULT NULL,
  `fcm_token` varchar(255) DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKf1rbjf8lle4r2in6ovkcgl0w8` (`member_id`),
  CONSTRAINT `FKf1rbjf8lle4r2in6ovkcgl0w8` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Interest Log table (이자 기록)
CREATE TABLE `interest_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account_id` bigint DEFAULT NULL,
  `interest` int DEFAULT NULL,
  `target_year_month` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_interest_log_account` (`account_id`),
  CONSTRAINT `FK_interest_log_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Link Apps table (연동 앱 목록)
CREATE TABLE `link_apps` (
  `my_row_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `link_apps` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`my_row_id`),
  KEY `FKofv52hpjw7yr92ipvxfwbx3vb` (`member_id`),
  CONSTRAINT `FKofv52hpjw7yr92ipvxfwbx3vb` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Monthly User Report table (월간 사용자 리포트)
CREATE TABLE `monthly_user_report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint DEFAULT NULL,
  `total_accumulated_time` bigint DEFAULT NULL,
  `average_accumulated_time` bigint DEFAULT NULL,
  `target_year_month` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_monthly_report_member` (`member_id`),
  CONSTRAINT `FK_monthly_report_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Point Log table (포인트 기록)
CREATE TABLE `point_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  `point` int DEFAULT NULL,
  `balance` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKpc5s54gy4e1gcvel1ymljexv` (`member_id`),
  CONSTRAINT `FKpc5s54gy4e1gcvel1ymljexv` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Premium table (프리미엄 구독)
CREATE TABLE `premium` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `end_at` date DEFAULT NULL,
  `start_at` date DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK26kikovtv22dlmi6so61v56ii` (`member_id`),
  CONSTRAINT `FK26kikovtv22dlmi6so61v56ii` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Refresh Token table (리프레시 토큰)
CREATE TABLE `refresh_token` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `expired_at` datetime(6) DEFAULT NULL,
  `refresh_token` varchar(3000) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_refresh_token_username` (`username`),
  KEY `idx_refresh_token_expired` (`expired_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Routine table (루틴)
CREATE TABLE `routine` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `duration` bigint DEFAULT NULL,
  `end_time` time(6) DEFAULT NULL,
  `is_suspended` bit(1) DEFAULT b'0',
  `start_time` time(6) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `account_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `is_ended` bit(1) DEFAULT b'0',
  `is_achieved` bit(1) DEFAULT b'0',
  `is_ai_routine` bit(1) DEFAULT b'0',
  `is_completed` bit(1) DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `FK7edqx1cgxme9mpsqw7ckl0wsx` (`account_id`),
  KEY `FKtf33d229v4l7g6bk7x0w30jjf` (`user_id`),
  CONSTRAINT `FK7edqx1cgxme9mpsqw7ckl0wsx` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE SET NULL,
  CONSTRAINT `FKtf33d229v4l7g6bk7x0w30jjf` FOREIGN KEY (`user_id`) REFERENCES `member` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Routine Days table (루틴 요일 설정)
CREATE TABLE `routine_days` (
  `my_row_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `routine_id` bigint NOT NULL,
  `day` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`my_row_id`),
  KEY `FK3w0wkb9t6bgw7sfo2j7u1goyx` (`routine_id`),
  CONSTRAINT `FK3w0wkb9t6bgw7sfo2j7u1goyx` FOREIGN KEY (`routine_id`) REFERENCES `routine` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Routine Log table (루틴 실행 기록)
CREATE TABLE `routine_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `end_time` datetime(6) DEFAULT NULL,
  `is_completed` bit(1) DEFAULT b'0',
  `start_time` datetime(6) DEFAULT NULL,
  `routine_id` bigint DEFAULT NULL,
  `duration` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9jgkl0758evd9vdnqk7gd28qt` (`routine_id`),
  CONSTRAINT `FK9jgkl0758evd9vdnqk7gd28qt` FOREIGN KEY (`routine_id`) REFERENCES `routine` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Snow Card table (스노우 카드)
CREATE TABLE `snow_card` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint DEFAULT NULL,
  `snow_card` varchar(255) DEFAULT NULL,
  `target_year_month` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_snow_card_member` (`member_id`),
  CONSTRAINT `FK_snow_card_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Spare Time table (여가 시간)
CREATE TABLE `spare_time` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `duration` bigint DEFAULT NULL,
  `end_time` varchar(255) DEFAULT NULL,
  `start_time` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKh0yfo2tdrhexqhwes0iitqcjs` (`member_id`),
  CONSTRAINT `FKh0yfo2tdrhexqhwes0iitqcjs` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Spare Time Days table (여가 시간 요일 설정)
CREATE TABLE `spare_time_days` (
  `my_row_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `spare_time_id` bigint NOT NULL,
  `days` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`my_row_id`),
  KEY `FKii4ju2cmedr09fj9dhk745m1e` (`spare_time_id`),
  CONSTRAINT `FKii4ju2cmedr09fj9dhk745m1e` FOREIGN KEY (`spare_time_id`) REFERENCES `spare_time` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Survey table (설문조사)
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
  CONSTRAINT `FK8jxem4c3k9lo8nj4ebempero9` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Survey Like Option table (설문 선호 옵션)
CREATE TABLE `survey_like_option` (
  `my_row_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `survey_id` bigint NOT NULL,
  `like_option` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`my_row_id`),
  KEY `FK1je2cn8hgwl11ir2qbgjvhbax` (`survey_id`),
  CONSTRAINT `FK1je2cn8hgwl11ir2qbgjvhbax` FOREIGN KEY (`survey_id`) REFERENCES `survey` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Survey Spare Time table (설문 여가 시간)
CREATE TABLE `survey_spare_time` (
  `my_row_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `survey_id` bigint NOT NULL,
  `spare_time` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`my_row_id`),
  KEY `FKu2eoxgy7vi6gwucgw5wnlbeb` (`survey_id`),
  CONSTRAINT `FKu2eoxgy7vi6gwucgw5wnlbeb` FOREIGN KEY (`survey_id`) REFERENCES `survey` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Todo table (할 일)
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
  KEY `FKfobka11j4naitg1ljetsf2vif` (`routine_id`),
  CONSTRAINT `FKfobka11j4naitg1ljetsf2vif` FOREIGN KEY (`routine_id`) REFERENCES `routine` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Todo Log table (할 일 실행 기록)
CREATE TABLE `todo_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `end_time` datetime(6) DEFAULT NULL,
  `is_completed` bit(1) DEFAULT b'0',
  `start_time` datetime(6) DEFAULT NULL,
  `todo_id` bigint DEFAULT NULL,
  `duration` bigint DEFAULT NULL,
  `routine_log_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4tpjdj0t1mic0x8j2b87o4x9d` (`todo_id`),
  KEY `FK_todo_log_routine_log` (`routine_log_id`),
  CONSTRAINT `FK4tpjdj0t1mic0x8j2b87o4x9d` FOREIGN KEY (`todo_id`) REFERENCES `todo` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_todo_log_routine_log` FOREIGN KEY (`routine_log_id`) REFERENCES `routine_log` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 인덱스 추가 (성능 최적화)
CREATE INDEX `idx_member_username` ON `member` (`username`);
CREATE INDEX `idx_member_email` ON `member` (`email`);
CREATE INDEX `idx_account_user_id` ON `account` (`user_id`);
CREATE INDEX `idx_routine_user_id` ON `routine` (`user_id`);
CREATE INDEX `idx_routine_account_id` ON `routine` (`account_id`);
CREATE INDEX `idx_daily_achieve_member_date` ON `daily_achieve` (`member_id`, `date`);
CREATE INDEX `idx_fcm_token_member` ON `fcm_token` (`member_id`);
CREATE INDEX `idx_point_log_member` ON `point_log` (`member_id`);
CREATE INDEX `idx_routine_log_routine` ON `routine_log` (`routine_id`);
CREATE INDEX `idx_todo_log_todo` ON `todo_log` (`todo_id`);