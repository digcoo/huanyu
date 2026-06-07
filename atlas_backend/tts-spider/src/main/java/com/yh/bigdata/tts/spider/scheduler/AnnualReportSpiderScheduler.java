package com.yh.bigdata.tts.spider.scheduler;

import com.yh.bigdata.tts.spider.crawler.report.StockAnnualReportCrawler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 年报爬虫调度：默认关闭（atlas.spider.report.enabled=false）
 * - 每月 1 日 03:00 全量
 * - 4–6 月每周日 03:00 额外全量（披露季加频）
 */
@Component
@ConditionalOnProperty(name = "atlas.spider.report.enabled", havingValue = "true")
public class AnnualReportSpiderScheduler {

    private static final Logger log = LoggerFactory.getLogger(AnnualReportSpiderScheduler.class);

    @Autowired
    private StockAnnualReportCrawler stockAnnualReportCrawler;

    @Scheduled(cron = "${atlas.spider.report.monthly.cron:0 0 3 1 * ?}")
    public void runMonthly() {
        log.info("AnnualReportSpiderScheduler monthly trigger");
        stockAnnualReportCrawler.runAll();
    }

    @Scheduled(cron = "${atlas.spider.report.peak.cron:0 0 3 ? * SUN}")
    public void runPeakSeasonWeekly() {
        int month = LocalDate.now().getMonthValue();
        if (month < 4 || month > 6) {
            return;
        }
        log.info("AnnualReportSpiderScheduler peak-season weekly trigger");
        stockAnnualReportCrawler.runAll();
    }
}
