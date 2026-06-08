-- Atlas MVP：A 股年报财务数据（东方财富 F10）
SET NAMES utf8;

DROP TABLE IF EXISTS `stock_annual_report`;
CREATE TABLE `stock_annual_report` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(10) NOT NULL COMMENT 'sh600519',
  `name` varchar(32) DEFAULT NULL,
  `report_year` int(11) NOT NULL COMMENT '会计年度，如 2023',
  `report_date` date DEFAULT NULL COMMENT '报告期截止日',
  `total_revenue` double DEFAULT NULL COMMENT '营业总收入，元',
  `net_profit` double DEFAULT NULL COMMENT '净利润，元',
  `parent_net_profit` double DEFAULT NULL COMMENT '归母净利润，元',
  `gross_margin` double DEFAULT NULL COMMENT '毛利率 %',
  `net_margin` double DEFAULT NULL COMMENT '净利率 %',
  `roe` double DEFAULT NULL COMMENT 'ROE %',
  `operating_cash_flow` double DEFAULT NULL COMMENT '经营现金流净额，元',
  `capex` double DEFAULT NULL COMMENT '资本开支(购建固定/无形等支付现金)，元',
  `staff_num` int(11) DEFAULT NULL COMMENT '员工人数',
  `revenue_per_staff` double DEFAULT NULL COMMENT '人均创收，万元',
  `prepaid_ratio` double DEFAULT NULL COMMENT '预收占比 %',
  `interest_debt_ratio` double DEFAULT NULL COMMENT '有息负债率 %',
  `debt_ratio` double DEFAULT NULL COMMENT '资产负债率 %',
  `current_ratio` double DEFAULT NULL COMMENT '流动比率',
  `inventory_days` double DEFAULT NULL COMMENT '存货周转天数',
  `receivable_days` double DEFAULT NULL COMMENT '应收周转天数',
  `revenue_yoy` double DEFAULT NULL COMMENT '营收同比 %',
  `profit_yoy` double DEFAULT NULL COMMENT '净利润同比 %',
  `source` varchar(32) DEFAULT 'eastmoney',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_year` (`code`,`report_year`),
  KEY `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='A股年报财务';
