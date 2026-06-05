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

 Date: 05/04/2023 12:58:01 PM
*/

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `base`
-- ----------------------------
DROP TABLE IF EXISTS `base`;
CREATE TABLE `base` (
  `code` varchar(10) NOT NULL,
  `exchange` varchar(2) NOT NULL,
  `name` varchar(20) NOT NULL,
  `is_st` tinyint(1) NOT NULL,
  `is_trade` tinyint(1) DEFAULT '1',
  `last_trade` double DEFAULT NULL,
  `trade` double DEFAULT NULL,
  `ma5` double DEFAULT NULL,
  `ma10` double DEFAULT NULL,
  `ma20` double DEFAULT NULL,
  `ma30` double DEFAULT NULL,
  `ma60` double DEFAULT NULL,
  `ma120` double DEFAULT NULL,
  `dea` double DEFAULT NULL,
  `dif` double DEFAULT NULL,
  `macd` double DEFAULT NULL,
  `open` double DEFAULT NULL,
  `high` double DEFAULT NULL,
  `low` double DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `volume` bigint(20) DEFAULT NULL,
  `day` date DEFAULT NULL,
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
  `mgsy` double DEFAULT NULL COMMENT '每股收益',
  `mgjzc` double DEFAULT NULL,
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
  `dea` double DEFAULT NULL,
  `dif` double DEFAULT NULL,
  `macd` double DEFAULT NULL,
  `day_rank` int(11) DEFAULT NULL COMMENT '访问量排名',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
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
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
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
  `dea` double DEFAULT NULL,
  `dif` double DEFAULT NULL,
  `macd` double DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
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
  `dea` double DEFAULT NULL,
  `dif` double DEFAULT NULL,
  `macd` double DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`code`,`day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `min60k_copy`
-- ----------------------------
DROP TABLE IF EXISTS `min60k_copy`;
CREATE TABLE `min60k_copy` (
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
  `dea` double DEFAULT NULL,
  `dif` double DEFAULT NULL,
  `macd` double DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
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
  `dea` double DEFAULT NULL,
  `dif` double DEFAULT NULL,
  `macd` double DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`code`,`day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `quarter_report`
-- ----------------------------
DROP TABLE IF EXISTS `quarter_report`;
CREATE TABLE `quarter_report` (
  `code` varchar(10) NOT NULL,
  `date` varchar(20) NOT NULL,
  `name` varchar(20) DEFAULT NULL,
  `jbmgsy` varchar(50) DEFAULT NULL COMMENT '基本每股收益(元)',
  `kfmgsy` varchar(50) DEFAULT NULL COMMENT '扣非每股收益(元)',
  `xsmgsy` varchar(50) DEFAULT NULL COMMENT '稀释每股收益(元)',
  `mgjzc` varchar(50) DEFAULT NULL COMMENT '每股净资产(元)',
  `mggjj` varchar(50) DEFAULT NULL COMMENT '每股公积金(元)',
  `mgwfply` varchar(50) DEFAULT NULL COMMENT '每股未分配利润(元)',
  `mgjyxjl` varchar(50) DEFAULT NULL COMMENT '每股经营现金流(元)',
  `yyzsr` varchar(50) DEFAULT NULL COMMENT '营业总收入(元)',
  `mlr` varchar(50) DEFAULT NULL COMMENT '毛利润(元)',
  `gsjlr` varchar(50) DEFAULT NULL COMMENT '归属净利润(元)',
  `kfjlr` varchar(50) DEFAULT NULL COMMENT '扣非净利润(元)',
  `yyzsrtbzz` varchar(50) DEFAULT NULL COMMENT '营业总收入同比增长(%)',
  `gsjlrtbzz` varchar(50) DEFAULT NULL COMMENT '归属净利润同比增长(%)',
  `kfjlrtbzz` varchar(50) DEFAULT NULL COMMENT '扣非净利润同比增长(%)',
  `yyzsrgdhbzz` varchar(50) DEFAULT NULL COMMENT '营业总收入滚动环比增长(%)',
  `gsjlrgdhbzz` varchar(50) DEFAULT NULL COMMENT '归属净利润滚动环比增长(%)',
  `kfjlrgdhbzz` varchar(50) DEFAULT NULL COMMENT '扣非净利润滚动环比增长(%)',
  `jqjzcsyl` varchar(50) DEFAULT NULL COMMENT '加权净资产收益率(%)',
  `tbjzcsyl` varchar(50) DEFAULT NULL COMMENT '摊薄净资产收益率(%)',
  `tbzzcsyl` varchar(50) DEFAULT NULL COMMENT '摊薄总资产收益率(%)',
  `mll` varchar(50) DEFAULT NULL COMMENT '毛利率(%)',
  `jll` varchar(50) DEFAULT NULL COMMENT '净利率(%)',
  `sjsl` varchar(50) DEFAULT NULL COMMENT '实际税率(%)',
  `yskyysr` varchar(50) DEFAULT NULL COMMENT '预收款/营业收入',
  `xsxjlyysr` varchar(50) DEFAULT NULL COMMENT '销售现金流/营业收入',
  `jyxjlyysr` varchar(50) DEFAULT NULL COMMENT '经营现金流/营业收入',
  `zzczzy` varchar(50) DEFAULT NULL COMMENT '总资产周转率(次)',
  `yszkzzts` varchar(50) DEFAULT NULL COMMENT '应收账款周转天数(天)',
  `chzzts` varchar(50) DEFAULT NULL COMMENT '存货周转天数(天)',
  `zcfzl` varchar(50) DEFAULT NULL COMMENT '资产负债率(%)',
  `ldzczfz` varchar(50) DEFAULT NULL COMMENT '流动负债/总负债(%)',
  `ldbl` varchar(50) DEFAULT NULL COMMENT '流动比率',
  `sdbl` varchar(50) DEFAULT NULL COMMENT '速动比率',
  `create_time` timestamp NULL DEFAULT NULL,
  `update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`code`,`date`)
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
  `dea` double DEFAULT NULL,
  `dif` double DEFAULT NULL,
  `macd` double DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`code`,`day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `strategy_stock`
-- ----------------------------
DROP TABLE IF EXISTS `stock_target`;
CREATE TABLE `stock_target` (
  `day` varchar(10) NOT NULL,
  `code` varchar(10) NOT NULL,
  `name` varchar(20) NOT NULL,
  `trade` double DEFAULT NULL,
  `strategy` varchar(20) NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  KEY `day_code_strategy_index` (`day`,`code`,`strategy`) USING HASH
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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
  `dea` double DEFAULT NULL,
  `dif` double DEFAULT NULL,
  `macd` double DEFAULT NULL,
  `source_type` int(11) DEFAULT '3' COMMENT '1=分时计算，2=日计算，3=周拉取',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
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
  `dea` double DEFAULT NULL,
  `dif` double DEFAULT NULL,
  `macd` double DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`code`,`day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS = 1;
