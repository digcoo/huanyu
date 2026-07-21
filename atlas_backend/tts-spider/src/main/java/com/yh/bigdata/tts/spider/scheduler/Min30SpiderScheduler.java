package com.yh.bigdata.tts.spider.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.yh.bigdata.tts.spider.realtime.TradingSessionUtils;
import com.yh.bigdata.tts.spider.xueqiu.StockMin30XueQiuCrawler;

/**
 * 盘中定时用雪球刷新 min30 到内存（不写库），校准新浪合成的 30 分钟 K。
 */
@Component
public class Min30SpiderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(Min30SpiderScheduler.class);

    @Value("${realtime.spider.on:false}")
    private boolean spiderEnable;

    @Autowired
    private StockMin30XueQiuCrawler stockMin30Crawler;

    @Scheduled(cron = "${min30.spider.cron:0 0/2 9-11,13-14 * * 1-5}")
    public void run() {
        try {
            if (!spiderEnable) {
                return;
            }
            if (!TradingSessionUtils.isInTradingSession()) {
                return;
            }

            stockMin30Crawler.refreshRealtimePool(8);
        } catch (Exception e) {
            logger.error("Min30SpiderScheduler run exception...", e);
        }
    }
}
