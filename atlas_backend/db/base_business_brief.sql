-- 业务摘要与经营范围语义拆分
SET NAMES utf8;

ALTER TABLE `base`
  ADD COLUMN `business_scope` varchar(800) DEFAULT NULL COMMENT '工商经营范围' AFTER `main_business`,
  ADD COLUMN `business_brief` varchar(200) DEFAULT NULL COMMENT '用户可读业务摘要' AFTER `business_scope`;

-- 将现有 main_business 中非经营范围式文本迁移到 business_brief（需应用层重爬更准确）
UPDATE `base`
SET `business_scope` = `main_business`
WHERE `main_business` IS NOT NULL AND TRIM(`main_business`) != ''
  AND (`business_scope` IS NULL OR TRIM(`business_scope`) = '');
