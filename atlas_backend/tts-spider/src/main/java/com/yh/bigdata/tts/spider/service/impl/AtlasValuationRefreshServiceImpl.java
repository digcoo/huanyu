package com.yh.bigdata.tts.spider.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.yh.bigdata.tts.common.dao.StockAnnualReportMapper;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.model.StockAnnualReport;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.spider.service.AtlasValuationRefreshService;
import com.yh.bigdata.tts.spider.utils.EastMoneyQuoteClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AtlasValuationRefreshServiceImpl implements AtlasValuationRefreshService {

    private static final Logger log = LoggerFactory.getLogger(AtlasValuationRefreshServiceImpl.class);

    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile Map<String, Object> lastResult = new LinkedHashMap<>();

    @Autowired
    private StockBaseMapper stockBaseMapper;

    @Autowired
    private StockAnnualReportMapper stockAnnualReportMapper;

    @Value("${atlas.spider.valuation.page-size:100}")
    private int pageSize;

    @Value("${atlas.spider.valuation.page-interval-ms:400}")
    private long pageIntervalMs;

    @Value("${atlas.spider.valuation.page-retries:2}")
    private int pageRetries;

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public Map<String, Object> lastResult() {
        return lastResult;
    }

    @Override
    public boolean refreshAllAsync() {
        if (!running.compareAndSet(false, true)) {
            return false;
        }
        CompletableFuture.runAsync(() -> {
            try {
                doRefreshAll();
            } finally {
                running.set(false);
            }
        });
        return true;
    }

    @Override
    public Map<String, Object> refreshAll() {
        if (!running.compareAndSet(false, true)) {
            Map<String, Object> busy = new LinkedHashMap<>();
            busy.put("started", false);
            busy.put("message", "估值刷新任务已在运行");
            return busy;
        }
        try {
            return doRefreshAll();
        } finally {
            running.set(false);
        }
    }

    private Map<String, Object> doRefreshAll() {
        long start = System.currentTimeMillis();
        int clistUpdated = 0;
        int clistSkipped = 0;
        int pages = 0;
        int apiTotal = 0;
        try {
            log.info("AtlasValuationRefresh start, pageSize={}", pageSize);
            int page = 1;
            while (true) {
                EastMoneyQuoteClient.ClistPage clistPage = fetchPageWithRetry(page);
                List<JSONObject> rows = clistPage.getRows();
                if (rows.isEmpty()) {
                    break;
                }
                if (page == 1) {
                    apiTotal = clistPage.getTotal();
                }
                pages++;
                for (JSONObject row : rows) {
                    StockBase patch = EastMoneyQuoteClient.parseValuationRow(row);
                    if (patch == null || StringUtils.isBlank(patch.getCode())) {
                        clistSkipped++;
                        continue;
                    }
                    if (!hasValuationData(patch)) {
                        clistSkipped++;
                        continue;
                    }
                    stockBaseMapper.updateByPrimaryKeySelective(patch);
                    clistUpdated++;
                }
                if (rows.size() < pageSize) {
                    break;
                }
                if (apiTotal > 0 && page * pageSize >= apiTotal) {
                    break;
                }
                page++;
                sleepInterval();
            }

            int fallbackUpdated = fillPeFromAnnualReports();
            int withPe = stockBaseMapper.countWithPeTtm();
            int stockTotal = stockBaseMapper.countAll();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("started", true);
            result.put("apiTotal", apiTotal);
            result.put("pages", pages);
            result.put("clistUpdated", clistUpdated);
            result.put("clistSkipped", clistSkipped);
            result.put("fallbackUpdated", fallbackUpdated);
            result.put("withPeTtm", withPe);
            result.put("stockTotal", stockTotal);
            result.put("peTtmRate", stockTotal > 0 ? round4(withPe * 1.0 / stockTotal) : 0);
            result.put("costSec", (System.currentTimeMillis() - start) / 1000);
            lastResult = result;
            log.info("AtlasValuationRefresh done, {}", result);
            return result;
        } catch (Exception e) {
            log.error("AtlasValuationRefresh failed", e);
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("started", true);
            err.put("error", e.getMessage());
            err.put("clistUpdated", clistUpdated);
            err.put("costSec", (System.currentTimeMillis() - start) / 1000);
            lastResult = err;
            return err;
        }
    }

    private EastMoneyQuoteClient.ClistPage fetchPageWithRetry(int page) {
        EastMoneyQuoteClient.ClistPage last = new EastMoneyQuoteClient.ClistPage(0, null);
        for (int i = 0; i <= pageRetries; i++) {
            last = EastMoneyQuoteClient.fetchClistPage(page, pageSize);
            if (!last.getRows().isEmpty()) {
                return last;
            }
            if (i < pageRetries) {
                sleepInterval();
            }
        }
        return last;
    }

    private int fillPeFromAnnualReports() {
        List<StockBase> missing = stockBaseMapper.selectWithoutPositivePeTtm();
        if (missing == null || missing.isEmpty()) {
            return 0;
        }
        int updated = 0;
        for (StockBase stock : missing) {
            if (stock == null || StringUtils.isBlank(stock.getCode())) {
                continue;
            }
            Double totalMvYi = stock.getTotalMvYi();
            if (totalMvYi == null || totalMvYi <= 0) {
                continue;
            }
            StockAnnualReport latest = stockAnnualReportMapper.selectLatestByCode(stock.getCode());
            if (latest == null || latest.getNetProfit() == null || latest.getNetProfit() == 0) {
                continue;
            }
            double pe = totalMvYi * 100_000_000.0 / latest.getNetProfit();
            if (Double.isNaN(pe) || Double.isInfinite(pe)) {
                continue;
            }
            StockBase patch = new StockBase();
            patch.setCode(stock.getCode());
            patch.setPeTtm(round2(pe));
            stockBaseMapper.updateByPrimaryKeySelective(patch);
            updated++;
        }
        return updated;
    }

    private static boolean hasValuationData(StockBase patch) {
        return patch.getPeTtm() != null
                || patch.getPb() != null
                || patch.getTotalMvYi() != null
                || patch.getHigh52w() != null
                || patch.getLow52w() != null
                || patch.getDividendYield() != null;
    }

    private void sleepInterval() {
        if (pageIntervalMs <= 0) {
            return;
        }
        try {
            Thread.sleep(pageIntervalMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private static double round4(double v) {
        return Math.round(v * 10000.0) / 10000.0;
    }
}
