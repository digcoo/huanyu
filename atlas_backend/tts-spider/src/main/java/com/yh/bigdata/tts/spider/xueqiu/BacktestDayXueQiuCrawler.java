package com.yh.bigdata.tts.spider.xueqiu;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yh.bigdata.tts.common.dao.BacktestDaykMapper;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockDay;
import com.yh.bigdata.tts.common.param.StockPageQuery;
import com.yh.bigdata.tts.spider.utils.XueQiuHttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 雪球日 K 爬虫 → backtest_dayk（与线上 dayk 隔离）。
 * 单股支持 begin 分页，尽量拉取完整上市日 K。
 */
@Component
@Slf4j
public class BacktestDayXueQiuCrawler {

    private static final long ONE_DAY_MS = 86_400_000L;

    @Autowired
    private StockBaseMapper stockBaseMapper;
    @Autowired
    private BacktestDaykMapper backtestDaykMapper;

    @Value("${spider.day.startpage:1}")
    private String startPage;

    @Value("${atlas.backtest.kline.crawl-interval-ms:200}")
    private long crawlIntervalMs;

    @Value("${atlas.backtest.kline.page-size:1000}")
    private int pageSize;

    @Value("${atlas.backtest.kline.max-pages:30}")
    private int maxPages;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger crawledStocks = new AtomicInteger(0);
    private volatile String lastError = "";
    private volatile Map<String, Object> lastCrawlStats = new HashMap<>();

    public boolean isRunning() {
        return running.get();
    }

    public int getCrawledStocks() {
        return crawledStocks.get();
    }

    public String getLastError() {
        return lastError;
    }

    public Map<String, Object> getLastCrawlStats() {
        return lastCrawlStats;
    }

    /**
     * 全市场或单股爬取。
     *
     * @param code      可选单股
     * @param countDays 非空且 &gt;0 时单次限量；单股且为 null/&lt;=0 时走完整分页
     */
    public int run(String code, Integer countDays) {
        if (!running.compareAndSet(false, true)) {
            log.warn("BacktestDayXueQiuCrawler already running");
            return 0;
        }
        crawledStocks.set(0);
        lastError = "";
        lastCrawlStats = new HashMap<>();
        long start = System.currentTimeMillis();
        try {
            if (StringUtils.isNotBlank(code)) {
                StockBase stock = stockBaseMapper.selectByPrimaryKey(code.trim());
                if (stock == null) {
                    lastError = "股票不存在: " + code;
                    return 0;
                }
                boolean full = countDays == null || countDays <= 0;
                try {
                    if (full) {
                        spiderOneFullHistory(stock);
                    } else {
                        spiderOnePage(stock, -Math.max(countDays, 30));
                    }
                } catch (Exception e) {
                    lastError = e.getMessage();
                    log.error("backtest dayk single crawl failed code={}", code, e);
                }
                crawledStocks.set(1);
                return 1;
            }

            int page = Integer.parseInt(startPage);
            int size = 100;
            int count = -Math.max(countDays != null ? countDays : 500, 30);
            while (true) {
                PageHelper.startPage(page, size);
                StockPageQuery pageQuery = new StockPageQuery(page, size);
                pageQuery.setIsSelectMode(false);
                Page<StockBase> pages = stockBaseMapper.selectByPageQuery(pageQuery);
                if (CollectionUtils.isEmpty(pages.getResult())) {
                    break;
                }
                for (StockBase stockBase : pages) {
                    if (stockBase.getCode().startsWith("sh688")) {
                        continue;
                    }
                    try {
                        spiderOnePage(stockBase, count);
                        crawledStocks.incrementAndGet();
                        if (crawlIntervalMs > 0) {
                            Thread.sleep(crawlIntervalMs);
                        }
                    } catch (Exception e) {
                        lastError = e.getMessage();
                        log.error("backtest dayk crawl failed code={}", stockBase.getCode(), e);
                        if (e instanceof HttpResponseException) {
                            Thread.sleep(20 * 60 * 1000L + 10_000L);
                            spiderOnePage(stockBase, count);
                            crawledStocks.incrementAndGet();
                        }
                    }
                }
                page++;
                if (page > pages.getPages()) {
                    break;
                }
            }
        } catch (Exception e) {
            lastError = e.getMessage();
            log.error("BacktestDayXueQiuCrawler fatal", e);
        } finally {
            running.set(false);
            log.info("BacktestDayXueQiuCrawler done stocks={} cost={}s",
                    crawledStocks.get(), (System.currentTimeMillis() - start) / 1000);
        }
        return crawledStocks.get();
    }

    /** 单股完整日 K（分页直至历史尽头或达到 max-pages）。 */
    public Map<String, Object> runFullHistory(String code) {
        run(code, null);
        return lastCrawlStats;
    }

    int spiderOneFullHistory(StockBase stockBase) throws IOException, InterruptedException {
        long begin = System.currentTimeMillis();
        int totalSaved = 0;
        int pages = 0;
        String oldestDay = null;
        String newestDay = null;

        while (pages < maxPages) {
            String ret = fetchDayKline(stockBase, begin, -pageSize);
            if (StringUtils.isBlank(ret) || "null".equals(ret)) {
                break;
            }
            List<StockDay> days = XueQiuHttpUtils.parseStockTrades(ret, stockBase, StockDay.class);
            if (days == null || days.isEmpty()) {
                break;
            }
            int saved = persistDays(days);
            totalSaved += saved;
            pages++;

            if (!days.isEmpty()) {
                String batchOldest = days.get(0).getDay();
                String batchNewest = days.get(days.size() - 1).getDay();
                if (oldestDay == null || batchOldest.compareTo(oldestDay) < 0) {
                    oldestDay = batchOldest;
                }
                if (newestDay == null || batchNewest.compareTo(newestDay) > 0) {
                    newestDay = batchNewest;
                }
            }

            log.info("backtest dayk full code={} page={} batch={} saved={} range=[{}, {}]",
                    stockBase.getCode(), pages, days.size(), saved,
                    days.get(0).getDay(), days.get(days.size() - 1).getDay());

            if (days.size() < pageSize) {
                break;
            }

            Long oldestMs = XueQiuHttpUtils.extractOldestTimestampMs(ret);
            if (oldestMs == null) {
                break;
            }
            long nextBegin = oldestMs - ONE_DAY_MS;
            if (nextBegin >= begin) {
                break;
            }
            begin = nextBegin;
            if (crawlIntervalMs > 0) {
                Thread.sleep(crawlIntervalMs);
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("code", stockBase.getCode());
        stats.put("mode", "full");
        stats.put("pages", pages);
        stats.put("barsSaved", totalSaved);
        stats.put("oldestDay", backtestDaykMapper.selectMinDayByCode(stockBase.getCode()));
        stats.put("newestDay", backtestDaykMapper.selectMaxDayByCode(stockBase.getCode()));
        stats.put("totalRows", backtestDaykMapper.countByCode(stockBase.getCode()));
        lastCrawlStats = stats;
        log.info("backtest dayk full done code={} stats={}", stockBase.getCode(), JSON.toJSONString(stats));
        return totalSaved;
    }

    void spiderOnePage(StockBase stockBase, int count) throws IOException, InterruptedException {
        String ret = fetchDayKline(stockBase, System.currentTimeMillis(), count);
        if (StringUtils.isBlank(ret) || "null".equals(ret)) {
            return;
        }
        List<StockDay> days = XueQiuHttpUtils.parseStockTrades(ret, stockBase, StockDay.class);
        if (days == null) {
            return;
        }
        int saved = persistDays(days);
        Map<String, Object> stats = new HashMap<>();
        stats.put("code", stockBase.getCode());
        stats.put("mode", "single");
        stats.put("barsSaved", saved);
        stats.put("count", Math.abs(count));
        lastCrawlStats = stats;
    }

    private String fetchDayKline(StockBase stockBase, long beginMs, int count)
            throws IOException, InterruptedException {
        String url = String.format(XueQiuHttpUtils.base_url,
                stockBase.getCode().toUpperCase(), beginMs, "day", count);
        try {
            return XueQiuHttpUtils.getData(url);
        } catch (HttpResponseException e) {
            Thread.sleep(20 * 60 * 1000L + 10_000L);
            return XueQiuHttpUtils.getData(url);
        }
    }

    private int persistDays(List<StockDay> days) {
        int saved = 0;
        for (StockDay stockDay : days) {
            if (stockDay.getOpen() == null || stockDay.getLow() == null
                    || stockDay.getOpen() < 0.001 || stockDay.getLow() < 0.001) {
                continue;
            }
            try {
                StockDay local = backtestDaykMapper.selectByPrimaryKey(stockDay.getCode(), stockDay.getDay());
                if (local == null) {
                    backtestDaykMapper.insert(stockDay);
                } else {
                    backtestDaykMapper.updateByPrimaryKey(stockDay);
                }
                saved++;
            } catch (DuplicateKeyException ignored) {
                // concurrent insert
            }
        }
        return saved;
    }
}
