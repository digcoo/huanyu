/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50720
 Source Host           : localhost
 Source Database       : tts

 Target Server Type    : MySQL
 Target Server Version : 50720
 File Encoding         : utf-8

 Date: 03/08/2025 09:05:06 AM
*/

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `base`
-- ----------------------------
DROP TABLE IF EXISTS `base`;
CREATE TABLE `base` (
  `code` varchar(10) NOT NULL,
  `name` varchar(20) NOT NULL,
  `open` double DEFAULT NULL,
  `trade` double DEFAULT NULL,
  `low` double DEFAULT NULL,
  `high` double DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `day` date DEFAULT NULL,
  `last_trade` double DEFAULT NULL,
  `exchange` varchar(2) DEFAULT NULL,
  `is_st` tinyint(1) DEFAULT NULL,
  `is_trade` tinyint(1) DEFAULT '1',
  `ma5` double DEFAULT NULL,
  `ma10` double DEFAULT NULL,
  `ma20` double DEFAULT NULL,
  `ma30` double DEFAULT NULL,
  `ma60` double DEFAULT NULL,
  `ma120` double DEFAULT NULL,
  `week_ma5` double DEFAULT NULL,
  `week_ma10` double DEFAULT NULL,
  `week_ma20` double DEFAULT NULL,
  `week_ma30` double DEFAULT NULL,
  `week_ma60` double DEFAULT NULL,
  `week_ma120` double DEFAULT NULL,
  `month_ma5` double DEFAULT NULL,
  `month_ma10` double DEFAULT NULL,
  `month_ma20` double DEFAULT NULL,
  `month_ma30` double DEFAULT NULL,
  `month_ma60` double DEFAULT NULL,
  `month_ma120` double DEFAULT NULL,
  `year_ma5` double DEFAULT NULL,
  `year_ma10` double DEFAULT NULL,
  `year_ma20` double DEFAULT NULL,
  `year_ma30` double DEFAULT NULL,
  `volume` bigint(20) DEFAULT NULL,
  `day_rank` int(11) DEFAULT NULL COMMENT '日访问排名',
  `quancheng` varchar(200) DEFAULT NULL COMMENT '公司全称',
  `main_business` varchar(500) DEFAULT NULL COMMENT '主营业务',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `buy1_price` double DEFAULT NULL,
  `buy1_volume` bigint(20) DEFAULT NULL,
  `buy2_price` double DEFAULT NULL,
  `buy2_volume` bigint(20) DEFAULT NULL,
  `buy3_price` double DEFAULT NULL,
  `buy3_volume` bigint(20) DEFAULT NULL,
  `buy4_price` double DEFAULT NULL,
  `buy4_volume` bigint(20) DEFAULT NULL,
  `buy5_price` double DEFAULT NULL,
  `buy5_volume` bigint(20) DEFAULT NULL,
  `sell1_price` double DEFAULT NULL,
  `sell1_volume` bigint(20) DEFAULT NULL,
  `sell2_price` double DEFAULT NULL,
  `sell2_volume` bigint(20) DEFAULT NULL,
  `sell3_price` double DEFAULT NULL,
  `sell3_volume` bigint(20) DEFAULT NULL,
  `sell4_price` double DEFAULT NULL,
  `sell4_volume` bigint(20) DEFAULT NULL,
  `sell5_price` double DEFAULT NULL,
  `sell5_volume` bigint(20) DEFAULT NULL,
  `min60_ma5` double DEFAULT NULL,
  `min60_ma10` double DEFAULT NULL,
  `min60_ma20` double DEFAULT NULL,
  `min60_ma30` double DEFAULT NULL,
  `min60_ma60` double DEFAULT NULL,
  `min60_ma120` double DEFAULT NULL,
  `min30_ma5` double DEFAULT NULL,
  `min30_ma10` double DEFAULT NULL,
  `min30_ma20` double DEFAULT NULL,
  `min30_ma30` double DEFAULT NULL,
  `min30_ma60` double DEFAULT NULL,
  `min30_ma120` double DEFAULT NULL,
  `mgsy` double DEFAULT NULL COMMENT '每股收益',
  `mgjzc` double DEFAULT NULL,
  `turnover_rate` double DEFAULT NULL COMMENT '换手率',
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `capital`
-- ----------------------------
DROP TABLE IF EXISTS `capital`;
CREATE TABLE `capital` (
  `day` date NOT NULL,
  `code` varchar(10) NOT NULL,
  `name` varchar(20) NOT NULL,
  `zhuli` double DEFAULT NULL,
  `zhuli_ratio` varchar(255) DEFAULT NULL,
  `extra_large` double DEFAULT NULL,
  `extra_large_ratio` double DEFAULT NULL,
  `large` double DEFAULT NULL,
  `large_ratio` double DEFAULT NULL,
  `middle` double DEFAULT NULL,
  `middle_ratio` double DEFAULT NULL,
  `small` double DEFAULT NULL,
  `small_ratio` double DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL,
  `update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`code`,`day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `dayk`
-- ----------------------------
DROP TABLE IF EXISTS `dayk`;
CREATE TABLE `dayk` (
  `day` date NOT NULL,
  `code` varchar(10) NOT NULL,
  `name` varchar(20) DEFAULT NULL,
  `open` double DEFAULT NULL,
  `high` double DEFAULT NULL,
  `low` double DEFAULT NULL,
  `trade` double DEFAULT NULL,
  `last_trade` double DEFAULT NULL,
  `volume` bigint(20) DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `ma5` double DEFAULT NULL,
  `ma10` double DEFAULT NULL,
  `ma20` double DEFAULT NULL,
  `ma30` double DEFAULT NULL,
  `ma60` double DEFAULT NULL,
  `ma120` double DEFAULT NULL,
  `cross_params` varchar(512) DEFAULT NULL,
  `day_rank` int(11) DEFAULT NULL COMMENT '访问量排名',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `turnover_rate` double DEFAULT NULL COMMENT '换手率',
  `percent` double DEFAULT NULL COMMENT '变化率、涨幅',
  `zhangting_time` datetime DEFAULT NULL,
  PRIMARY KEY (`code`,`day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `fenshik`
-- ----------------------------
DROP TABLE IF EXISTS `fenshik`;
CREATE TABLE `fenshik` (
  `day` date NOT NULL,
  `time` time NOT NULL,
  `code` varchar(10) NOT NULL,
  `name` varchar(20) NOT NULL,
  `trade` double NOT NULL,
  `last_trade` double DEFAULT NULL,
  `open` double NOT NULL,
  `high` double NOT NULL,
  `low` double NOT NULL,
  `volume` bigint(20) NOT NULL,
  `amount` double NOT NULL,
  `buy1_price` double NOT NULL,
  `buy1_volume` bigint(20) NOT NULL,
  `buy2_price` double NOT NULL,
  `buy2_volume` bigint(20) NOT NULL,
  `buy3_price` double NOT NULL,
  `buy3_volume` bigint(20) NOT NULL,
  `buy4_price` double NOT NULL,
  `buy4_volume` bigint(20) NOT NULL,
  `buy5_price` double NOT NULL,
  `buy5_volume` bigint(20) NOT NULL,
  `sell1_price` double NOT NULL,
  `sell1_volume` bigint(20) NOT NULL,
  `sell2_price` double NOT NULL,
  `sell2_volume` bigint(20) NOT NULL,
  `sell3_price` double NOT NULL,
  `sell3_volume` bigint(20) NOT NULL,
  `sell4_price` double NOT NULL,
  `sell4_volume` bigint(20) NOT NULL,
  `sell5_price` double NOT NULL,
  `sell5_volume` bigint(20) NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`day`,`time`,`code`),
  KEY `code_day_time` (`day`,`time`,`code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `min15k`
-- ----------------------------
DROP TABLE IF EXISTS `min15k`;
CREATE TABLE `min15k` (
  `day` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `code` varchar(10) NOT NULL,
  `name` varchar(20) DEFAULT NULL,
  `open` double DEFAULT NULL,
  `high` double DEFAULT NULL,
  `low` double DEFAULT NULL,
  `trade` double DEFAULT NULL,
  `last_trade` double DEFAULT NULL,
  `volume` bigint(20) DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `ma5` double DEFAULT NULL,
  `ma10` double DEFAULT NULL,
  `ma20` double DEFAULT NULL,
  `ma30` double DEFAULT NULL,
  `ma60` double DEFAULT NULL,
  `ma120` double DEFAULT NULL,
  `cross_params` varchar(512) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `percent` double DEFAULT NULL COMMENT '变化率、涨幅',
  PRIMARY KEY (`code`,`day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `min30k`
-- ----------------------------
DROP TABLE IF EXISTS `min30k`;
CREATE TABLE `min30k` (
  `day` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `code` varchar(10) NOT NULL,
  `name` varchar(20) DEFAULT NULL,
  `open` double DEFAULT NULL,
  `high` double DEFAULT NULL,
  `low` double DEFAULT NULL,
  `trade` double DEFAULT NULL,
  `last_trade` double DEFAULT NULL,
  `volume` bigint(20) DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `ma5` double DEFAULT NULL,
  `ma10` double DEFAULT NULL,
  `ma20` double DEFAULT NULL,
  `ma30` double DEFAULT NULL,
  `ma60` double DEFAULT NULL,
  `ma120` double DEFAULT NULL,
  `cross_params` varchar(512) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `percent` double DEFAULT NULL COMMENT '变化率、涨幅',
  PRIMARY KEY (`code`,`day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `min5k`
-- ----------------------------
DROP TABLE IF EXISTS `min5k`;
CREATE TABLE `min5k` (
  `day` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `code` varchar(10) NOT NULL,
  `name` varchar(20) DEFAULT NULL,
  `open` double DEFAULT NULL,
  `high` double DEFAULT NULL,
  `low` double DEFAULT NULL,
  `trade` double DEFAULT NULL,
  `last_trade` double DEFAULT NULL,
  `volume` bigint(20) DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `ma5` double DEFAULT NULL,
  `ma10` double DEFAULT NULL,
  `ma20` double DEFAULT NULL,
  `ma30` double DEFAULT NULL,
  `ma60` double DEFAULT NULL,
  `ma120` double DEFAULT NULL,
  `cross_params` varchar(512) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `percent` double DEFAULT NULL COMMENT '变化率、涨幅',
  PRIMARY KEY (`code`,`day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `min60k`
-- ----------------------------
DROP TABLE IF EXISTS `min60k`;
CREATE TABLE `min60k` (
  `day` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `code` varchar(10) NOT NULL,
  `name` varchar(20) DEFAULT NULL,
  `open` double DEFAULT NULL,
  `high` double DEFAULT NULL,
  `low` double DEFAULT NULL,
  `trade` double DEFAULT NULL,
  `last_trade` double DEFAULT NULL,
  `volume` bigint(20) DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `ma5` double DEFAULT NULL,
  `ma10` double DEFAULT NULL,
  `ma20` double DEFAULT NULL,
  `ma30` double DEFAULT NULL,
  `ma60` double DEFAULT NULL,
  `ma120` double DEFAULT NULL,
  `cross_params` varchar(512) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `percent` double DEFAULT NULL COMMENT '变化率、涨幅',
  PRIMARY KEY (`code`,`day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `monthk`
-- ----------------------------
DROP TABLE IF EXISTS `monthk`;
CREATE TABLE `monthk` (
  `day` date NOT NULL,
  `code` varchar(10) NOT NULL,
  `name` varchar(20) DEFAULT NULL,
  `open` double DEFAULT NULL,
  `high` double DEFAULT NULL,
  `low` double DEFAULT NULL,
  `trade` double DEFAULT NULL,
  `last_trade` double DEFAULT NULL,
  `volume` bigint(20) DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `ma5` double DEFAULT NULL,
  `ma10` double DEFAULT NULL,
  `ma20` double DEFAULT NULL,
  `ma30` double DEFAULT NULL,
  `ma60` double DEFAULT NULL,
  `ma120` double DEFAULT NULL,
  `cross_params` varchar(512) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `percent` double DEFAULT NULL COMMENT '变化率、涨幅',
  PRIMARY KEY (`code`,`day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `quarterk`
-- ----------------------------
DROP TABLE IF EXISTS `quarterk`;
CREATE TABLE `quarterk` (
  `day` date NOT NULL,
  `code` varchar(10) NOT NULL,
  `name` varchar(20) NOT NULL,
  `open` double NOT NULL,
  `high` double NOT NULL,
  `low` double NOT NULL,
  `trade` double NOT NULL,
  `last_trade` double DEFAULT NULL,
  `volume` bigint(20) DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `ma5` double DEFAULT NULL,
  `ma10` double DEFAULT NULL,
  `ma20` double DEFAULT NULL,
  `ma30` double DEFAULT NULL,
  `cross_params` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `percent` double DEFAULT NULL COMMENT '变化率、涨幅',
  PRIMARY KEY (`code`,`day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `strategy_stock`
-- ----------------------------
DROP TABLE IF EXISTS `strategy_stock`;
CREATE TABLE `strategy_stock` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `day` varchar(10) NOT NULL,
  `code` varchar(10) NOT NULL,
  `name` varchar(20) NOT NULL,
  `trade` double DEFAULT NULL,
  `strategy` varchar(20) NOT NULL,
  `is_new` tinyint(1) DEFAULT '1',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `qushi_message` varchar(64) DEFAULT NULL,
  `signal_message` varchar(512) DEFAULT NULL,
  `change_rate` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `day_code_strategy_index` (`day`,`code`,`strategy`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=23335 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `test`
-- ----------------------------
DROP TABLE IF EXISTS `test`;
CREATE TABLE `test` (
  `id` bigint(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `top`
-- ----------------------------
DROP TABLE IF EXISTS `top`;
CREATE TABLE `top` (
  `code` varchar(10) NOT NULL,
  `name` varchar(20) NOT NULL,
  `day` varchar(255) DEFAULT NULL COMMENT '最近上榜日期',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `weekk`
-- ----------------------------
DROP TABLE IF EXISTS `weekk`;
CREATE TABLE `weekk` (
  `day` date NOT NULL,
  `code` varchar(10) NOT NULL,
  `name` varchar(20) DEFAULT NULL,
  `open` double DEFAULT NULL,
  `high` double DEFAULT NULL,
  `low` double DEFAULT NULL,
  `trade` double DEFAULT NULL,
  `last_trade` double DEFAULT NULL,
  `volume` bigint(20) DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `ma5` double DEFAULT NULL,
  `ma10` double DEFAULT NULL,
  `ma20` double DEFAULT NULL,
  `ma30` double DEFAULT NULL,
  `ma60` double DEFAULT NULL,
  `ma120` double DEFAULT NULL,
  `cross_params` varchar(512) DEFAULT NULL,
  `source_type` int(11) DEFAULT '3' COMMENT '1=分时计算，2=日计算，3=周拉取',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `percent` double DEFAULT NULL COMMENT '变化率、涨幅',
  PRIMARY KEY (`code`,`day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `yeark`
-- ----------------------------
DROP TABLE IF EXISTS `yeark`;
CREATE TABLE `yeark` (
  `day` date NOT NULL,
  `code` varchar(10) NOT NULL,
  `name` varchar(20) NOT NULL,
  `open` double NOT NULL,
  `high` double NOT NULL,
  `low` double NOT NULL,
  `trade` double NOT NULL,
  `last_trade` double DEFAULT NULL,
  `volume` bigint(20) NOT NULL,
  `amount` double DEFAULT NULL,
  `ma5` double DEFAULT NULL,
  `ma10` double DEFAULT NULL,
  `ma20` double DEFAULT NULL,
  `ma30` double DEFAULT NULL,
  `cross_params` varchar(512) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `percent` double DEFAULT NULL COMMENT '变化率、涨幅',
  PRIMARY KEY (`code`,`day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS = 1;
