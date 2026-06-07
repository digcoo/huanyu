-- 年报表唯一性修复：同一 code + report_year 只保留一条（id 最大）
-- 执行前请备份。适用于 uk_code_year 缺失导致 upsert 失效、id 自增重复插入的情况。

SET NAMES utf8;

-- 1. 删除重复（保留 id 较大的一条，通常 update_time 更新）
DELETE t1 FROM stock_annual_report t1
INNER JOIN stock_annual_report t2
  ON t1.code = t2.code
 AND t1.report_year = t2.report_year
 AND t1.id < t2.id;

-- 2. 补唯一索引（若已存在会报错，可忽略）
ALTER TABLE stock_annual_report
  ADD UNIQUE KEY uk_code_year (code, report_year);
