package com.yh.bigdata.tts.spider.scheduler;

import com.yh.bigdata.tts.spider.service.AtlasValuationRefreshService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 估值字段定时刷新（东财 clist 批量）
 * 开启：atlas.spider.valuation.enabled=true
 */
@Component
@ConditionalOnProperty(name = "atlas.spider.valuation.enabled", havingValue = "true")
public class AtlasValuationRefreshScheduler {

    private static final Logger log = LoggerFactory.getLogger(AtlasValuationRefreshScheduler.class);

    @Autowired
    private AtlasValuationRefreshService atlasValuationRefreshService;

    @Scheduled(cron = "${atlas.spider.valuation.morning-cron:0 0 9 * * 1-5}")
    public void morningRefresh() {
        trigger("morning");
    }

    @Scheduled(cron = "${atlas.spider.valuation.close-cron:0 10 16 * * 1-5}")
    public void closeRefresh() {
        trigger("close");
    }

    private void trigger(String slot) {
        log.info("AtlasValuationRefreshScheduler {} trigger", slot);
        if (!atlasValuationRefreshService.refreshAllAsync()) {
            log.warn("AtlasValuationRefreshScheduler {} skipped: already running", slot);
        }
    }
}
