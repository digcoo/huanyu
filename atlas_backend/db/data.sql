-- 删除过期数据
delete from min30k where day < '2025-01-01 00:00:00';
delete from dayk where day < '2024-01-01';
delete from weekk where day < '2024-01-01';
delete from monthk where day < '2024-01-01';
delete from quarterk where day < '2024-01-01';
delete from yeark where day < '2024-01-01';
