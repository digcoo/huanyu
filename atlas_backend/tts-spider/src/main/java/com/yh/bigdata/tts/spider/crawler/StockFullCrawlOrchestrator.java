package com.yh.bigdata.tts.spider.crawler;

import com.yh.bigdata.tts.spider.crawler.detail.StockCompanyDetailCrawler;
import com.yh.bigdata.tts.spider.crawler.report.StockAnnualReportCrawler;
import com.yh.bigdata.tts.spider.service.AtlasIndustryBenchmarkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 全量详情爬取编排：公司详情 → 年报 → 行业均值
 */
@Component
public class StockFullCrawlOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(StockFullCrawlOrchestrator.class);

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Autowired
    private StockCompanyDetailCrawler stockCompanyDetailCrawler;

    @Autowired
    private StockAnnualReportCrawler stockAnnualReportCrawler;

    @Autowired
    private AtlasIndustryBenchmarkService atlasIndustryBenchmarkService;

    public boolean isRunning() {
        return running.get();
    }

    public Map<String, Object> status() {
        Map<String, Object> s = new HashMap<>();
        s.put("orchestratorRunning", running.get());
        s.put("detailRunning", stockCompanyDetailCrawler.isRunning());
        s.put("annualRunning", stockAnnualReportCrawler.isRunning());
        return s;
    }

    /** 异步全量爬取（默认：详情 → 年报 → 行业均值） */
    public boolean runAllAsync() {
        return runAllAsync(false, false);
    }

    /**
     * @param skipDetail 跳过公司详情（昨日已跑过详情时用）
     * @param skipAnnual 跳过年报（昨日已跑过年报时用）
     */
    public boolean runAllAsync(boolean skipDetail, boolean skipAnnual) {
        if (!running.compareAndSet(false, true)) {
            log.warn("StockFullCrawlOrchestrator already running");
            return false;
        }
        CompletableFuture.runAsync(() -> {
            long start = System.currentTimeMillis();
            log.info("StockFullCrawlOrchestrator start, skipDetail={}, skipAnnual={}", skipDetail, skipAnnual);
            try {
                int step = 0;
                int total = (skipDetail ? 0 : 1) + (skipAnnual ? 0 : 1) + 1;

                if (!skipDetail) {
                    step++;
                    log.info("Step {}/{}: company detail (main_business, industry, valuation, competitors)", step, total);
                    stockCompanyDetailCrawler.runAll();
                } else {
                    log.info("Skip company detail crawl");
                }

                if (!skipAnnual) {
                    step++;
                    log.info("Step {}/{}: annual reports", step, total);
                    stockAnnualReportCrawler.runAll();
                } else {
                    log.info("Skip annual report crawl");
                }

                step++;
                log.info("Step {}/{}: industry benchmarks", step, total);
                int industries = atlasIndustryBenchmarkService.rebuildAll();
                log.info("StockFullCrawlOrchestrator done, industries={}, cost={}s",
                        industries, (System.currentTimeMillis() - start) / 1000);
            } catch (Exception e) {
                log.error("StockFullCrawlOrchestrator failed", e);
            } finally {
                running.set(false);
            }
        });
        return true;
    }
}
