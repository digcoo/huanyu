package com.yh.bigdata.tts.spider.scheduler;

import com.yh.bigdata.tts.spider.crawler.StockFullCrawlOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 详情页数据底座定时任务：公司详情 → 年报 → 行业均值
 * 默认关闭，开启：atlas.spider.foundation.enabled=true
 */
@Component
@ConditionalOnProperty(name = "atlas.spider.foundation.enabled", havingValue = "true")
public class AtlasFoundationSpiderScheduler {

    private static final Logger log = LoggerFactory.getLogger(AtlasFoundationSpiderScheduler.class);

    @Autowired
    private StockFullCrawlOrchestrator stockFullCrawlOrchestrator;

    @Scheduled(cron = "${atlas.spider.foundation.cron:0 0 2 ? * SUN}")
    public void runWeeklyFoundation() {
        log.info("AtlasFoundationSpiderScheduler weekly trigger");
        if (!stockFullCrawlOrchestrator.runAllAsync()) {
            log.warn("AtlasFoundationSpiderScheduler skipped: orchestrator already running");
        }
    }
}
