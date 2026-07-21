-- 60 分钟 K 线表（本地库若缺失可单独执行）
CREATE TABLE IF NOT EXISTS `min60k` (
  `day` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `code` varchar(10) NOT NULL,
  `name` varchar(20) DEFAULT NULL,
  `open` double DEFAULT NULL,
  `high` double DEFAULT NULL,
  `low` double DEFAULT NULL,
  `close` double DEFAULT NULL,
  `prev_close` double DEFAULT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='60分钟K线';
