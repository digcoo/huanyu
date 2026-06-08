-- 扩展年报字段：资本开支 / 员工 / 人均创收 / 预收占比 / 有息负债率
SET NAMES utf8;

ALTER TABLE `stock_annual_report`
  ADD COLUMN `capex` double DEFAULT NULL COMMENT '资本开支(购建固定/无形等支付现金)，元' AFTER `operating_cash_flow`,
  ADD COLUMN `staff_num` int(11) DEFAULT NULL COMMENT '员工人数' AFTER `capex`,
  ADD COLUMN `revenue_per_staff` double DEFAULT NULL COMMENT '人均创收，万元' AFTER `staff_num`,
  ADD COLUMN `prepaid_ratio` double DEFAULT NULL COMMENT '预收占比 %' AFTER `revenue_per_staff`,
  ADD COLUMN `interest_debt_ratio` double DEFAULT NULL COMMENT '有息负债率 %' AFTER `prepaid_ratio`;
