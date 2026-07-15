-- 历史 K 线时点快照（方案 B：按周期分表）
-- as_of_day = 快照截止交易日；bar_day = K 线锚点日；in_progress = 1 表示由日 K 派生的进行中 bar
-- 回测主查：WHERE code = ? AND as_of_day = ? ORDER BY bar_day

CREATE TABLE IF NOT EXISTS `day_snapshot` (
  `as_of_day` date NOT NULL COMMENT '快照截止交易日',
  `code` varchar(10) NOT NULL,
  `bar_day` date NOT NULL COMMENT '日K交易日',
  `name` varchar(20) DEFAULT NULL,
  `open` double DEFAULT NULL,
  `high` double DEFAULT NULL,
  `low` double DEFAULT NULL,
  `close` double DEFAULT NULL,
  `prev_close` double DEFAULT NULL,
  `volume` bigint(20) DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `percent` double DEFAULT NULL,
  `in_progress` tinyint(1) NOT NULL DEFAULT 0 COMMENT '日K恒为0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`code`, `as_of_day`, `bar_day`),
  KEY `idx_code_asof` (`code`, `as_of_day`),
  KEY `idx_asof_code` (`as_of_day`, `code`),
  KEY `idx_asof_day` (`as_of_day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='日K时点快照';

CREATE TABLE IF NOT EXISTS `week_snapshot` (
  `as_of_day` date NOT NULL COMMENT '快照截止交易日',
  `code` varchar(10) NOT NULL,
  `bar_day` date NOT NULL COMMENT '周K锚点(周五)',
  `name` varchar(20) DEFAULT NULL,
  `open` double DEFAULT NULL,
  `high` double DEFAULT NULL,
  `low` double DEFAULT NULL,
  `close` double DEFAULT NULL,
  `prev_close` double DEFAULT NULL,
  `volume` bigint(20) DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `percent` double DEFAULT NULL,
  `in_progress` tinyint(1) NOT NULL DEFAULT 0 COMMENT '1=进行中周K',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`code`, `as_of_day`, `bar_day`),
  KEY `idx_code_asof` (`code`, `as_of_day`),
  KEY `idx_asof_code` (`as_of_day`, `code`),
  KEY `idx_asof_day` (`as_of_day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='周K时点快照';

CREATE TABLE IF NOT EXISTS `month_snapshot` (
  `as_of_day` date NOT NULL COMMENT '快照截止交易日',
  `code` varchar(10) NOT NULL,
  `bar_day` date NOT NULL COMMENT '月K锚点(月末)',
  `name` varchar(20) DEFAULT NULL,
  `open` double DEFAULT NULL,
  `high` double DEFAULT NULL,
  `low` double DEFAULT NULL,
  `close` double DEFAULT NULL,
  `prev_close` double DEFAULT NULL,
  `volume` bigint(20) DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `percent` double DEFAULT NULL,
  `in_progress` tinyint(1) NOT NULL DEFAULT 0 COMMENT '1=进行中月K',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`code`, `as_of_day`, `bar_day`),
  KEY `idx_code_asof` (`code`, `as_of_day`),
  KEY `idx_asof_code` (`as_of_day`, `code`),
  KEY `idx_asof_day` (`as_of_day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='月K时点快照';

CREATE TABLE IF NOT EXISTS `year_snapshot` (
  `as_of_day` date NOT NULL COMMENT '快照截止交易日',
  `code` varchar(10) NOT NULL,
  `bar_day` date NOT NULL COMMENT '年K锚点(年末)',
  `name` varchar(20) DEFAULT NULL,
  `open` double DEFAULT NULL,
  `high` double DEFAULT NULL,
  `low` double DEFAULT NULL,
  `close` double DEFAULT NULL,
  `prev_close` double DEFAULT NULL,
  `volume` bigint(20) DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `percent` double DEFAULT NULL,
  `in_progress` tinyint(1) NOT NULL DEFAULT 0 COMMENT '1=进行中年K',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`code`, `as_of_day`, `bar_day`),
  KEY `idx_code_asof` (`code`, `as_of_day`),
  KEY `idx_asof_code` (`as_of_day`, `code`),
  KEY `idx_asof_day` (`as_of_day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='年K时点快照';
