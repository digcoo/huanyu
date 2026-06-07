-- 收盘价/昨收列重命名（trade → close，last_trade → prev_close）
-- 启动时 AtlasDbSchemaInitializer 也会自动执行同等迁移；本脚本供手动执行或参考。

SET NAMES utf8;

-- base
ALTER TABLE `base` CHANGE COLUMN `trade` `close` double DEFAULT NULL;
ALTER TABLE `base` CHANGE COLUMN `last_trade` `prev_close` double DEFAULT NULL;

-- K 线
ALTER TABLE `dayk` CHANGE COLUMN `trade` `close` double DEFAULT NULL;
ALTER TABLE `dayk` CHANGE COLUMN `last_trade` `prev_close` double DEFAULT NULL;

ALTER TABLE `weekk` CHANGE COLUMN `trade` `close` double DEFAULT NULL;
ALTER TABLE `weekk` CHANGE COLUMN `last_trade` `prev_close` double DEFAULT NULL;

ALTER TABLE `monthk` CHANGE COLUMN `trade` `close` double DEFAULT NULL;
ALTER TABLE `monthk` CHANGE COLUMN `last_trade` `prev_close` double DEFAULT NULL;

ALTER TABLE `quarterk` CHANGE COLUMN `trade` `close` double DEFAULT NULL;
ALTER TABLE `quarterk` CHANGE COLUMN `last_trade` `prev_close` double DEFAULT NULL;

ALTER TABLE `yeark` CHANGE COLUMN `trade` `close` double DEFAULT NULL;
ALTER TABLE `yeark` CHANGE COLUMN `last_trade` `prev_close` double DEFAULT NULL;

-- 分时 / 分钟
ALTER TABLE `fenshik` CHANGE COLUMN `trade` `close` double NOT NULL;
ALTER TABLE `fenshik` CHANGE COLUMN `last_trade` `prev_close` double DEFAULT NULL;

ALTER TABLE `min5k` CHANGE COLUMN `trade` `close` double DEFAULT NULL;
ALTER TABLE `min5k` CHANGE COLUMN `last_trade` `prev_close` double DEFAULT NULL;

ALTER TABLE `min15k` CHANGE COLUMN `trade` `close` double DEFAULT NULL;
ALTER TABLE `min15k` CHANGE COLUMN `last_trade` `prev_close` double DEFAULT NULL;

ALTER TABLE `min30k` CHANGE COLUMN `trade` `close` double DEFAULT NULL;
ALTER TABLE `min30k` CHANGE COLUMN `last_trade` `prev_close` double DEFAULT NULL;

ALTER TABLE `min60k` CHANGE COLUMN `trade` `close` double DEFAULT NULL;
ALTER TABLE `min60k` CHANGE COLUMN `last_trade` `prev_close` double DEFAULT NULL;

-- 旧策略快照表（若仍在使用）
ALTER TABLE `strategy_stock` CHANGE COLUMN `trade` `close` double DEFAULT NULL;
