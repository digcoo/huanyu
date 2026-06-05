SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `crypto_base`
-- ----------------------------
DROP TABLE IF EXISTS `crypto_base`;
CREATE TABLE `crypto_base` (
  `symbol` varchar(20) NOT NULL,
  `close` double DEFAULT NULL,
  `open` double DEFAULT NULL,
  `high` double DEFAULT NULL,
  `low` double DEFAULT NULL,
  `last_close` double DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `volume` bigint(20) DEFAULT NULL,
  `day` date DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`symbol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------
--  Table structure for `crypto_day`
-- ----------------------------
DROP TABLE IF EXISTS `crypto_day`;
CREATE TABLE `crypto_day` (
  `day` date NOT NULL,
  `symbol` varchar(20) NOT NULL,
  `close` decimal(36, 16) DEFAULT NULL,
  `open` decimal(36, 16) DEFAULT NULL,
  `high` decimal(36, 16) DEFAULT NULL,
  `low` decimal(36, 16) DEFAULT NULL,
  `last_close` decimal(36, 16) DEFAULT NULL,
  `volume` decimal(36, 16) DEFAULT NULL,
  `amount` decimal(36, 16) DEFAULT NULL,
  `ma5` decimal(36, 16) DEFAULT NULL,
  `ma10` decimal(36, 16) DEFAULT NULL,
  `ma20` decimal(36, 16) DEFAULT NULL,
  `ma30` decimal(36, 16) DEFAULT NULL,
  `ma60` decimal(36, 16) DEFAULT NULL,
  `dea` decimal(36, 16) DEFAULT NULL,
  `dif` decimal(36, 16) DEFAULT NULL,
  `macd` decimal(36, 16) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`symbol`,`day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



-- ----------------------------
--  Table structure for `crypto_min30`
-- ----------------------------
DROP TABLE IF EXISTS `crypto_min30`;
CREATE TABLE `crypto_min30` (
  `day` date NOT NULL,
  `symbol` varchar(20) NOT NULL,
  `close` decimal(36, 16) DEFAULT NULL,
  `open` decimal(36, 16) DEFAULT NULL,
  `high` decimal(36, 16) DEFAULT NULL,
  `low` decimal(36, 16) DEFAULT NULL,
  `last_close` decimal(36, 16) DEFAULT NULL,
  `volume` decimal(36, 16) DEFAULT NULL,
  `amount` decimal(36, 16) DEFAULT NULL,
  `ma5` decimal(36, 16) DEFAULT NULL,
  `ma10` decimal(36, 16) DEFAULT NULL,
  `ma20` decimal(36, 16) DEFAULT NULL,
  `ma30` decimal(36, 16) DEFAULT NULL,
  `ma60` decimal(36, 16) DEFAULT NULL,
  `dea` decimal(36, 16) DEFAULT NULL,
  `dif` decimal(36, 16) DEFAULT NULL,
  `macd` decimal(36, 16) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`symbol`,`day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



-- ----------------------------
--  Table structure for `crypto_hour4`
-- ----------------------------
DROP TABLE IF EXISTS `crypto_hour4`;
CREATE TABLE `crypto_hour4` (
  `day` date NOT NULL,
  `symbol` varchar(20) NOT NULL,
  `close` decimal(36, 16) DEFAULT NULL,
  `open` decimal(36, 16) DEFAULT NULL,
  `high` decimal(36, 16) DEFAULT NULL,
  `low` decimal(36, 16) DEFAULT NULL,
  `last_close` decimal(36, 16) DEFAULT NULL,
  `volume` decimal(36, 16) DEFAULT NULL,
  `amount` decimal(36, 16) DEFAULT NULL,
  `ma5` decimal(36, 16) DEFAULT NULL,
  `ma10` decimal(36, 16) DEFAULT NULL,
  `ma20` decimal(36, 16) DEFAULT NULL,
  `ma30` decimal(36, 16) DEFAULT NULL,
  `ma60` decimal(36, 16) DEFAULT NULL,
  `dea` decimal(36, 16) DEFAULT NULL,
  `dif` decimal(36, 16) DEFAULT NULL,
  `macd` decimal(36, 16) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`symbol`,`day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



-- ----------------------------
--  Table structure for `crypto_week`
-- ----------------------------
DROP TABLE IF EXISTS `crypto_week`;
CREATE TABLE `crypto_week` (
  `day` date NOT NULL,
  `symbol` varchar(20) NOT NULL,
  `close` decimal(36, 16) DEFAULT NULL,
  `open` decimal(36, 16) DEFAULT NULL,
  `high` decimal(36, 16) DEFAULT NULL,
  `low` decimal(36, 16) DEFAULT NULL,
  `last_close` decimal(36, 16) DEFAULT NULL,
  `volume` decimal(36, 16) DEFAULT NULL,
  `amount` decimal(36, 16) DEFAULT NULL,
  `ma5` decimal(36, 16) DEFAULT NULL,
  `ma10` decimal(36, 16) DEFAULT NULL,
  `ma20` decimal(36, 16) DEFAULT NULL,
  `ma30` decimal(36, 16) DEFAULT NULL,
  `ma60` decimal(36, 16) DEFAULT NULL,
  `dea` decimal(36, 16) DEFAULT NULL,
  `dif` decimal(36, 16) DEFAULT NULL,
  `macd` decimal(36, 16) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`symbol`,`day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



-- ----------------------------
--  Table structure for `crypto_month`
-- ----------------------------
DROP TABLE IF EXISTS `crypto_month`;
CREATE TABLE `crypto_month` (
  `day` date NOT NULL,
  `symbol` varchar(20) NOT NULL,
  `close` decimal(36, 16) DEFAULT NULL,
  `open` decimal(36, 16) DEFAULT NULL,
  `high` decimal(36, 16) DEFAULT NULL,
  `low` decimal(36, 16) DEFAULT NULL,
  `last_close` decimal(36, 16) DEFAULT NULL,
  `volume` decimal(36, 16) DEFAULT NULL,
  `amount` decimal(36, 16) DEFAULT NULL,
  `ma5` decimal(36, 16) DEFAULT NULL,
  `ma10` decimal(36, 16) DEFAULT NULL,
  `ma20` decimal(36, 16) DEFAULT NULL,
  `ma30` decimal(36, 16) DEFAULT NULL,
  `ma60` decimal(36, 16) DEFAULT NULL,
  `dea` decimal(36, 16) DEFAULT NULL,
  `dif` decimal(36, 16) DEFAULT NULL,
  `macd` decimal(36, 16) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`symbol`,`day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



-- ----------------------------
--  Table structure for `crypto_year`
-- ----------------------------
DROP TABLE IF EXISTS `crypto_year`;
CREATE TABLE `crypto_year` (
  `day` date NOT NULL,
  `symbol` varchar(20) NOT NULL,
  `close` decimal(36, 16) DEFAULT NULL,
  `open` decimal(36, 16) DEFAULT NULL,
  `high` decimal(36, 16) DEFAULT NULL,
  `low` decimal(36, 16) DEFAULT NULL,
  `last_close` decimal(36, 16) DEFAULT NULL,
  `volume` decimal(36, 16) DEFAULT NULL,
  `amount` decimal(36, 16) DEFAULT NULL,
  `ma5` decimal(36, 16) DEFAULT NULL,
  `ma10` decimal(36, 16) DEFAULT NULL,
  `ma20` decimal(36, 16) DEFAULT NULL,
  `ma30` decimal(36, 16) DEFAULT NULL,
  `ma60` decimal(36, 16) DEFAULT NULL,
  `dea` decimal(36, 16) DEFAULT NULL,
  `dif` decimal(36, 16) DEFAULT NULL,
  `macd` decimal(36, 16) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`symbol`,`day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

