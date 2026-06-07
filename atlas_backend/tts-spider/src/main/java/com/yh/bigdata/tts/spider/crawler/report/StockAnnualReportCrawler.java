package com.yh.bigdata.tts.spider.crawler.report;

import com.alibaba.fastjson.JSONObject;
import com.yh.bigdata.tts.common.dao.StockAnnualReportMapper;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.model.StockAnnualReport;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.utils.StockCodeUtil;
import com.yh.bigdata.tts.spider.utils.EastMoneyFinanceClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class StockAnnualReportCrawler {

    private static final Logger log = LoggerFactory.getLogger(StockAnnualReportCrawler.class);

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Autowired
    private StockBaseMapper stockBaseMapper;

    @Autowired
    private StockAnnualReportMapper stockAnnualReportMapper;

    @Value("${atlas.spider.report.years:15}")
    private int reportYears;

    @Value("${atlas.spider.report.interval-ms:300}")
    private long intervalMs;

    public boolean isRunning() {
        return running.get();
    }

    /** 全 A 股年报爬取 */
    public void runAll() {
        if (!running.compareAndSet(false, true)) {
            log.warn("StockAnnualReportCrawler already running, skip");
            return;
        }
        long start = System.currentTimeMillis();
        log.info("StockAnnualReportCrawler ALL start, years={}", reportYears);
        try {
            List<StockBase> all = stockBaseMapper.selectAll();
            int ok = 0;
            int fail = 0;
            for (StockBase stock : all) {
                if (stock == null || StringUtils.isBlank(stock.getCode())) {
                    continue;
                }
                try {
                    int n = runOne(stock.getCode(), stock.getName());
                    if (n > 0) {
                        ok++;
                    } else {
                        fail++;
                    }
                    sleepInterval();
                } catch (Exception e) {
                    fail++;
                    log.warn("annual crawl failed, code={}", stock.getCode(), e);
                }
            }
            log.info("StockAnnualReportCrawler ALL done, ok={}, fail={}, cost={}s",
                    ok, fail, (System.currentTimeMillis() - start) / 1000);
        } finally {
            running.set(false);
        }
    }

    /** 单股年报爬取，返回写入条数 */
    public int runOne(String code, String name) {
        String normalized = StockCodeUtil.normalizeCnCode(code);
        String securityCode = StockCodeUtil.toSecurityCode(normalized);
        if (StringUtils.isBlank(securityCode)) {
            return 0;
        }
        List<JSONObject> rows = EastMoneyFinanceClient.fetchAnnualReports(securityCode, reportYears);
        if (rows.isEmpty()) {
            return 0;
        }
        Map<Integer, StockAnnualReport> byYear = new LinkedHashMap<>();
        for (JSONObject row : rows) {
            StockAnnualReport report = mapRow(normalized, name, row);
            if (report == null || report.getReportYear() == null) {
                continue;
            }
            byYear.merge(report.getReportYear(), report, this::pickNewerReport);
        }
        int count = 0;
        for (StockAnnualReport report : byYear.values()) {
            stockAnnualReportMapper.upsert(report);
            count++;
        }
        return count;
    }

    private StockAnnualReport pickNewerReport(StockAnnualReport existing, StockAnnualReport incoming) {
        if (existing.getReportDate() == null) {
            return incoming;
        }
        if (incoming.getReportDate() == null) {
            return existing;
        }
        return incoming.getReportDate().after(existing.getReportDate()) ? incoming : existing;
    }

    private StockAnnualReport mapRow(String code, String fallbackName, JSONObject row) {
        String reportDateStr = EastMoneyFinanceClient.getString(row, "REPORT_DATE");
        if (StringUtils.isBlank(reportDateStr)) {
            return null;
        }
        Date reportDate = parseDate(reportDateStr);
        Integer reportYear = resolveReportYear(row, reportDate);
        if (reportYear == null) {
            return null;
        }

        StockAnnualReport r = new StockAnnualReport();
        r.setCode(code);
        r.setName(StringUtils.defaultIfBlank(EastMoneyFinanceClient.getString(row, "SECURITY_NAME_ABBR"), fallbackName));
        r.setReportYear(reportYear);
        r.setReportDate(reportDate);
        r.setTotalRevenue(EastMoneyFinanceClient.getDouble(row, "TOTALOPERATEREVE", "TOTAL_OPERATE_INCOME"));
        r.setNetProfit(EastMoneyFinanceClient.getDouble(row, "PARENTNETPROFIT", "NETPROFIT"));
        r.setParentNetProfit(EastMoneyFinanceClient.getDouble(row, "PARENTNETPROFIT", "PARENT_NETPROFIT"));
        r.setGrossMargin(EastMoneyFinanceClient.getDouble(row, "XSMLL", "MLL"));
        r.setNetMargin(EastMoneyFinanceClient.getDouble(row, "XSJLL", "JLL"));
        r.setRoe(EastMoneyFinanceClient.getDouble(row, "ROEJQ", "ROE"));
        r.setOperatingCashFlow(EastMoneyFinanceClient.getDouble(row, "NETCASH_OPERATE_PK", "JYXJL"));
        r.setDebtRatio(EastMoneyFinanceClient.getDouble(row, "ZCFZL"));
        r.setCurrentRatio(EastMoneyFinanceClient.getDouble(row, "LD", "LDZCZZL"));
        r.setInventoryDays(EastMoneyFinanceClient.getDouble(row, "CHZZTS"));
        r.setReceivableDays(EastMoneyFinanceClient.getDouble(row, "YSZKZZTS"));
        r.setRevenueYoy(EastMoneyFinanceClient.getDouble(row, "TOTALOPERATEREVETZ", "YSTZ"));
        r.setProfitYoy(EastMoneyFinanceClient.getDouble(row, "PARENTNETPROFITTZ", "JLRTBZCL"));
        r.setSource("eastmoney");
        return r;
    }

    /**
     * 会计年度：优先 API 字段，否则按报告期截止日（12-31 为当年，Q1 披露的上年年报减 1）
     */
    private Integer resolveReportYear(JSONObject row, Date reportDate) {
        Integer year = EastMoneyFinanceClient.getInteger(row, "REPORT_YEAR", "STD_REPORT_YEAR", "YEAR");
        if (year != null && year > 1990 && year < 2100) {
            return year;
        }
        if (reportDate == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(reportDate);
        int y = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        if (month == 12) {
            return y;
        }
        if (month <= 4) {
            return y - 1;
        }
        return y;
    }

    private Date parseDate(String s) {
        try {
            if (s.length() >= 10) {
                return new SimpleDateFormat("yyyy-MM-dd").parse(s.substring(0, 10));
            }
        } catch (ParseException ignored) {
        }
        return null;
    }

    private void sleepInterval() {
        if (intervalMs <= 0) {
            return;
        }
        try {
            Thread.sleep(intervalMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
