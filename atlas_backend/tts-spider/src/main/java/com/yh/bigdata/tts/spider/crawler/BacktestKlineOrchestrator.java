package com.yh.bigdata.tts.spider.crawler;

import com.yh.bigdata.tts.spider.service.KlineSnapshotService;
import com.yh.bigdata.tts.spider.xueqiu.BacktestDayXueQiuCrawler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 回测 K 线管线：雪球爬取 backtest_dayk → 回放生成周/月/年快照。
 */
@Component
public class BacktestKlineOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(BacktestKlineOrchestrator.class);

    private final AtomicBoolean pipelineRunning = new AtomicBoolean(false);

    @Autowired
    private BacktestDayXueQiuCrawler backtestDayXueQiuCrawler;
    @Autowired
    private KlineSnapshotService klineSnapshotService;

    @Value("${atlas.backtest.kline.crawl-days:500}")
    private int defaultCrawlDays;

    public boolean isPipelineRunning() {
        return pipelineRunning.get();
    }

    public Map<String, Object> status() {
        Map<String, Object> s = new HashMap<>();
        s.put("pipelineRunning", pipelineRunning.get());
        s.put("crawlRunning", backtestDayXueQiuCrawler.isRunning());
        s.put("crawledStocks", backtestDayXueQiuCrawler.getCrawledStocks());
        s.put("crawlLastError", backtestDayXueQiuCrawler.getLastError());
        s.put("crawlLastStats", backtestDayXueQiuCrawler.getLastCrawlStats());
        s.put("snapshotBuilding", klineSnapshotService.isBuilding());
        s.put("snapshotLast", klineSnapshotService.lastBuildResult());
        return s;
    }

    /** 异步：爬取日 K；单股且 full=true 或 countDays 为空时拉完整历史 */
    public boolean crawlDaykAsync(String code, Integer countDays, Boolean full) {
        if (backtestDayXueQiuCrawler.isRunning()) {
            return false;
        }
        Integer effective = resolveCountDays(code, countDays, full);
        CompletableFuture.runAsync(() -> backtestDayXueQiuCrawler.run(code, effective));
        return true;
    }

    /** 异步：单股完整日 K 分页爬取 */
    public boolean crawlFullHistoryAsync(String code) {
        if (StringUtils.isBlank(code) || backtestDayXueQiuCrawler.isRunning()) {
            return false;
        }
        CompletableFuture.runAsync(() -> backtestDayXueQiuCrawler.run(code.trim(), null));
        return true;
    }

    private Integer resolveCountDays(String code, Integer countDays, Boolean full) {
        if (StringUtils.isNotBlank(code) && (Boolean.TRUE.equals(full) || countDays == null)) {
            return null;
        }
        return countDays != null ? countDays : defaultCrawlDays;
    }

    /** 异步：爬取 + 对最新交易日生成周/月/年快照 */
    public boolean crawlAndBuildLatestAsync(String code, Integer countDays, Boolean full) {
        if (!pipelineRunning.compareAndSet(false, true)) {
            return false;
        }
        Integer effective = resolveCountDays(code, countDays, full);
        CompletableFuture.runAsync(() -> {
            try {
                log.info("BacktestKline pipeline start code={} countDays={}", code, effective);
                backtestDayXueQiuCrawler.run(code, effective);
                String asOfDay = klineSnapshotService.resolveLatestTradingDay();
                if (asOfDay != null) {
                    if (StringUtils.isNotBlank(code)) {
                        klineSnapshotService.buildPeriodSnapshots(code.trim(), asOfDay);
                    } else {
                        klineSnapshotService.buildPeriodSnapshotsAll(asOfDay);
                    }
                } else {
                    log.warn("BacktestKline pipeline: no trading day in backtest_dayk");
                }
            } finally {
                pipelineRunning.set(false);
            }
        });
        return true;
    }

    public boolean crawlDaykAsync(String code, Integer countDays) {
        return crawlDaykAsync(code, countDays, null);
    }

    public boolean crawlAndBuildLatestAsync(String code, Integer countDays) {
        return crawlAndBuildLatestAsync(code, countDays, StringUtils.isNotBlank(code) ? true : null);
    }

    /** 异步：对日期区间逐日生成周/月/年快照 */
    public boolean buildSnapshotsRangeAsync(String fromDay, String toDay) {
        if (klineSnapshotService.isBuilding()) {
            return false;
        }
        CompletableFuture.runAsync(() -> klineSnapshotService.buildPeriodSnapshotsRange(fromDay, toDay));
        return true;
    }
}
