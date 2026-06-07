package com.yh.bigdata.tts.spider.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 开发环境自动建表 / 增量加列（atlas.db.schema-init=true）
 */
@Component
@ConditionalOnProperty(name = "atlas.db.schema-init", havingValue = "true")
public class AtlasDbSchemaInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AtlasDbSchemaInitializer.class);

    private static final String CREATE_ANNUAL_REPORT = ""
            + "CREATE TABLE IF NOT EXISTS `stock_annual_report` ("
            + "  `id` bigint(20) NOT NULL AUTO_INCREMENT,"
            + "  `code` varchar(10) NOT NULL,"
            + "  `name` varchar(32) DEFAULT NULL,"
            + "  `report_year` int(11) NOT NULL,"
            + "  `report_date` date DEFAULT NULL,"
            + "  `total_revenue` double DEFAULT NULL,"
            + "  `net_profit` double DEFAULT NULL,"
            + "  `parent_net_profit` double DEFAULT NULL,"
            + "  `gross_margin` double DEFAULT NULL,"
            + "  `net_margin` double DEFAULT NULL,"
            + "  `roe` double DEFAULT NULL,"
            + "  `operating_cash_flow` double DEFAULT NULL,"
            + "  `debt_ratio` double DEFAULT NULL,"
            + "  `current_ratio` double DEFAULT NULL,"
            + "  `inventory_days` double DEFAULT NULL,"
            + "  `receivable_days` double DEFAULT NULL,"
            + "  `revenue_yoy` double DEFAULT NULL,"
            + "  `profit_yoy` double DEFAULT NULL,"
            + "  `source` varchar(32) DEFAULT 'eastmoney',"
            + "  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,"
            + "  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
            + "  PRIMARY KEY (`id`),"
            + "  UNIQUE KEY `uk_code_year` (`code`,`report_year`),"
            + "  KEY `idx_code` (`code`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='A股年报财务'";

    private static final String CREATE_STOCK_TARGET = ""
            + "CREATE TABLE IF NOT EXISTS `stock_target` ("
            + "  `day` date NOT NULL,"
            + "  `code` varchar(10) NOT NULL,"
            + "  `name` varchar(32) DEFAULT NULL,"
            + "  `close` double DEFAULT NULL,"
            + "  `strategy` varchar(32) NOT NULL,"
            + "  `new_flag` tinyint(1) DEFAULT 1,"
            + "  `change_rate` double DEFAULT NULL,"
            + "  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,"
            + "  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
            + "  PRIMARY KEY (`day`,`code`,`strategy`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='策略推荐快照'";

    private static final String CREATE_COMPANY_RELATION = ""
            + "CREATE TABLE IF NOT EXISTS `stock_company_relation` ("
            + "  `id` bigint(20) NOT NULL AUTO_INCREMENT,"
            + "  `code` varchar(10) NOT NULL,"
            + "  `related_code` varchar(10) DEFAULT NULL,"
            + "  `related_name` varchar(64) DEFAULT NULL,"
            + "  `relation_type` varchar(16) NOT NULL,"
            + "  `sort_order` int(11) DEFAULT 0,"
            + "  `source` varchar(32) DEFAULT 'eastmoney',"
            + "  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,"
            + "  PRIMARY KEY (`id`),"
            + "  KEY `idx_code_type` (`code`,`relation_type`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='公司关联（竞品等）'";

    private static final String CREATE_INDUSTRY_BENCHMARK = ""
            + "CREATE TABLE IF NOT EXISTS `stock_industry_benchmark` ("
            + "  `industry` varchar(64) NOT NULL,"
            + "  `roe_avg` double DEFAULT NULL,"
            + "  `gross_margin_avg` double DEFAULT NULL,"
            + "  `net_margin_avg` double DEFAULT NULL,"
            + "  `revenue_yoy_avg` double DEFAULT NULL,"
            + "  `debt_ratio_avg` double DEFAULT NULL,"
            + "  `sample_count` int(11) DEFAULT 0,"
            + "  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
            + "  PRIMARY KEY (`industry`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='行业均值（雷达对标）'";

    private static final String CREATE_ATLAS_USER = ""
            + "CREATE TABLE IF NOT EXISTS `atlas_user` ("
            + "  `openid` varchar(64) NOT NULL,"
            + "  `nickname` varchar(64) DEFAULT NULL,"
            + "  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,"
            + "  PRIMARY KEY (`openid`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Atlas用户'";

    private static final String CREATE_USER_SESSION = ""
            + "CREATE TABLE IF NOT EXISTS `user_session` ("
            + "  `token` varchar(64) NOT NULL,"
            + "  `openid` varchar(64) NOT NULL,"
            + "  `expire_time` datetime DEFAULT NULL,"
            + "  PRIMARY KEY (`token`),"
            + "  KEY `idx_openid` (`openid`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='登录会话'";

    private static final String CREATE_USER_WATCHLIST = ""
            + "CREATE TABLE IF NOT EXISTS `user_watchlist` ("
            + "  `id` bigint(20) NOT NULL AUTO_INCREMENT,"
            + "  `openid` varchar(64) NOT NULL,"
            + "  `stock_id` varchar(64) NOT NULL,"
            + "  `code` varchar(10) NOT NULL,"
            + "  `name` varchar(32) DEFAULT NULL,"
            + "  `market` varchar(8) DEFAULT 'cn',"
            + "  `strategy` varchar(32) DEFAULT NULL,"
            + "  `entry_price` double DEFAULT NULL,"
            + "  `resonance` varchar(16) DEFAULT NULL,"
            + "  `summary` varchar(500) DEFAULT NULL,"
            + "  `tags_json` varchar(500) DEFAULT NULL,"
            + "  `added_at` bigint(20) DEFAULT NULL,"
            + "  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,"
            + "  PRIMARY KEY (`id`),"
            + "  UNIQUE KEY `uk_openid_stock` (`openid`,`stock_id`),"
            + "  KEY `idx_openid_added` (`openid`,`added_at`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户自选'";

    private static final String CREATE_USER_WATCH_HISTORY = ""
            + "CREATE TABLE IF NOT EXISTS `user_watch_history` ("
            + "  `id` bigint(20) NOT NULL AUTO_INCREMENT,"
            + "  `openid` varchar(64) NOT NULL,"
            + "  `record_id` varchar(80) NOT NULL,"
            + "  `stock_id` varchar(64) NOT NULL,"
            + "  `code` varchar(10) NOT NULL,"
            + "  `name` varchar(32) DEFAULT NULL,"
            + "  `market` varchar(8) DEFAULT 'cn',"
            + "  `strategy` varchar(32) DEFAULT NULL,"
            + "  `resonance` varchar(16) DEFAULT NULL,"
            + "  `tags_json` varchar(500) DEFAULT NULL,"
            + "  `entry_price` double DEFAULT NULL,"
            + "  `exit_price` double DEFAULT NULL,"
            + "  `added_at` bigint(20) DEFAULT NULL,"
            + "  `removed_at` bigint(20) DEFAULT NULL,"
            + "  `hold_days` int(11) DEFAULT NULL,"
            + "  `pnl_pct` double DEFAULT NULL,"
            + "  `remove_reason` varchar(16) DEFAULT NULL,"
            + "  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,"
            + "  PRIMARY KEY (`id`),"
            + "  KEY `idx_openid_removed` (`openid`,`removed_at`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='自选历史复盘'";

    private static final String[][] BASE_COLUMNS = {
            {"industry", "varchar(64) DEFAULT NULL COMMENT '申万行业末级'"},
            {"industry_csrc", "varchar(128) DEFAULT NULL COMMENT '证监会行业'"},
            {"org_profile", "text COMMENT '公司简介'"},
            {"pe_ttm", "double DEFAULT NULL COMMENT 'PE TTM'"},
            {"pb", "double DEFAULT NULL COMMENT 'PB'"},
            {"ps_ttm", "double DEFAULT NULL COMMENT 'PS TTM'"},
            {"dividend_yield", "double DEFAULT NULL COMMENT '股息率%'"},
            {"high_52w", "double DEFAULT NULL COMMENT '52周最高'"},
            {"low_52w", "double DEFAULT NULL COMMENT '52周最低'"},
            {"total_mv_yi", "double DEFAULT NULL COMMENT '总市值(亿)'"},
            {"detail_crawl_time", "datetime DEFAULT NULL COMMENT '详情爬取时间'"}
    };

    private static final String[] BASE_MA_COLUMNS = {
            "ma5", "ma10", "ma20", "ma30", "ma60", "ma120",
            "week_ma5", "week_ma10", "week_ma20", "week_ma30", "week_ma60", "week_ma120",
            "month_ma5", "month_ma10", "month_ma20", "month_ma30", "month_ma60", "month_ma120",
            "year_ma5", "year_ma10", "year_ma20", "year_ma30",
            "min60_ma5", "min60_ma10", "min60_ma20", "min60_ma30", "min60_ma60", "min60_ma120",
            "min30_ma5", "min30_ma10", "min30_ma20", "min30_ma30", "min30_ma60", "min30_ma120"
    };

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_ANNUAL_REPORT);
            stmt.execute(CREATE_STOCK_TARGET);
            stmt.execute(CREATE_COMPANY_RELATION);
            stmt.execute(CREATE_INDUSTRY_BENCHMARK);
            stmt.execute(CREATE_ATLAS_USER);
            stmt.execute(CREATE_USER_SESSION);
            stmt.execute(CREATE_USER_WATCHLIST);
            stmt.execute(CREATE_USER_WATCH_HISTORY);
            migrateBaseColumns(conn);
            dropBaseMaColumns(conn);
            migrateAnnualReportUnique(conn);
            log.info("AtlasDbSchemaInitializer: schema ready (annual, target, relation, benchmark, base columns)");
        } catch (Exception e) {
            log.error("AtlasDbSchemaInitializer failed", e);
            throw e;
        }
    }

    private void migrateBaseColumns(Connection conn) throws Exception {
        for (String[] col : BASE_COLUMNS) {
            if (!columnExists(conn, "base", col[0])) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE `base` ADD COLUMN `" + col[0] + "` " + col[1]);
                    log.info("AtlasDbSchemaInitializer: added base.{}", col[0]);
                }
            }
        }
    }

    private void dropBaseMaColumns(Connection conn) throws Exception {
        for (String col : BASE_MA_COLUMNS) {
            if (columnExists(conn, "base", col)) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE `base` DROP COLUMN `" + col + "`");
                    log.info("AtlasDbSchemaInitializer: dropped base.{}", col);
                }
            }
        }
    }

    /**
     * 年报表唯一性：先删 (code, report_year) 重复，再补 uk_code_year，使 upsert 生效
     */
    private void migrateAnnualReportUnique(Connection conn) throws Exception {
        if (!tableExists(conn, "stock_annual_report")) {
            return;
        }
        int removed = 0;
        try (Statement stmt = conn.createStatement()) {
            removed = stmt.executeUpdate(
                    "DELETE t1 FROM stock_annual_report t1 "
                            + "INNER JOIN stock_annual_report t2 "
                            + "ON t1.code = t2.code AND t1.report_year = t2.report_year AND t1.id < t2.id");
        }
        if (removed > 0) {
            log.warn("AtlasDbSchemaInitializer: removed {} duplicate stock_annual_report rows", removed);
        }
        if (!indexExists(conn, "stock_annual_report", "uk_code_year")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE `stock_annual_report` "
                        + "ADD UNIQUE KEY `uk_code_year` (`code`, `report_year`)");
                log.info("AtlasDbSchemaInitializer: added stock_annual_report.uk_code_year");
            }
        }
    }

    private boolean tableExists(Connection conn, String table) throws Exception {
        String sql = "SELECT COUNT(*) FROM information_schema.TABLES "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '" + table + "'";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    private boolean indexExists(Connection conn, String table, String indexName) throws Exception {
        String sql = "SELECT COUNT(*) FROM information_schema.STATISTICS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '" + table + "' AND INDEX_NAME = '" + indexName + "'";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    private boolean columnExists(Connection conn, String table, String column) throws Exception {
        String sql = "SELECT COUNT(*) FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '" + table + "' AND COLUMN_NAME = '" + column + "'";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
}
