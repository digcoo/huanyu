package com.yh.bigdata.tts.spider.controller;

import com.yh.bigdata.tts.common.dao.StockAnnualReportMapper;
import com.yh.bigdata.tts.common.param.base.Response;
import com.yh.bigdata.tts.common.param.base.ResponseUtil;
import com.yh.bigdata.tts.spider.crawler.StockFullCrawlOrchestrator;
import com.yh.bigdata.tts.spider.crawler.detail.StockCompanyDetailCrawler;
import com.yh.bigdata.tts.spider.crawler.report.StockAnnualReportCrawler;
import com.yh.bigdata.tts.spider.scheduler.StockTargetScheduler;
import com.yh.bigdata.tts.spider.service.AtlasDetailDataQualityService;
import com.yh.bigdata.tts.spider.service.AtlasIndustryBenchmarkService;
import com.yh.bigdata.tts.spider.service.AtlasValuationRefreshService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 开发/运维用手动触发爬虫（atlas.spider.report.manual-enabled=true）
 */
@RestController
@RequestMapping("/stock/admin")
public class AtlasSpiderAdminController {

    @Autowired
    private StockAnnualReportCrawler stockAnnualReportCrawler;

    @Autowired
    private StockAnnualReportMapper stockAnnualReportMapper;

    @Autowired
    private StockCompanyDetailCrawler stockCompanyDetailCrawler;

    @Autowired
    private StockFullCrawlOrchestrator stockFullCrawlOrchestrator;

    @Autowired
    private AtlasIndustryBenchmarkService atlasIndustryBenchmarkService;

    @Autowired
    private AtlasDetailDataQualityService atlasDetailDataQualityService;

    @Autowired
    private StockTargetScheduler stockTargetScheduler;

    @Autowired
    private AtlasValuationRefreshService atlasValuationRefreshService;

    @Autowired
    private StockBaseController stockBaseController;

    @Value("${atlas.spider.report.manual-enabled:false}")
    private boolean manualEnabled;

    @Value("${atlas.strategy.manual-enabled:true}")
    private boolean strategyManualEnabled;

    @GetMapping("/crawl-status")
    public Response<Map<String, Object>> crawlStatus() {
        Map<String, Object> data = new LinkedHashMap<>(stockFullCrawlOrchestrator.status());
        data.put("valuationRunning", atlasValuationRefreshService.isRunning());
        data.put("valuationLast", atlasValuationRefreshService.lastResult());
        data.put("dataQuality", atlasDetailDataQualityService.snapshot());
        return ResponseUtil.success(data);
    }

    @GetMapping("/data-quality")
    public Response<Map<String, Object>> dataQuality() {
        return ResponseUtil.success(atlasDetailDataQualityService.snapshot());
    }

    /** 全量爬取：公司详情 → 年报 → 行业均值（异步）；可用 skipAnnual / skipDetail 跳过已跑步骤 */
    @PostMapping("/crawl-all")
    public Response<Map<String, Object>> crawlAll(
            @RequestParam(value = "skipAnnual", defaultValue = "false") boolean skipAnnual,
            @RequestParam(value = "skipDetail", defaultValue = "false") boolean skipDetail) {
        if (!manualEnabled) {
            return ResponseUtil.fail(ResponseUtil.OPERATE_FAILED);
        }
        Map<String, Object> data = new HashMap<>();
        boolean started = stockFullCrawlOrchestrator.runAllAsync(skipDetail, skipAnnual);
        data.put("started", started);
        data.put("mode", "all_async");
        data.put("skipAnnual", skipAnnual);
        data.put("skipDetail", skipDetail);
        if (!started) {
            data.put("message", "已有全量任务在运行");
        }
        return ResponseUtil.success(data);
    }

    /** 公司详情：主营业务、行业、估值、竞品 */
    @PostMapping("/crawl-company")
    public Response<Map<String, Object>> crawlCompany(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "name", required = false) String name) {
        if (!manualEnabled) {
            return ResponseUtil.fail(ResponseUtil.OPERATE_FAILED);
        }
        Map<String, Object> data = new HashMap<>();
        if (code != null && !code.trim().isEmpty()) {
            boolean ok = stockCompanyDetailCrawler.runOne(code.trim(), name);
            data.put("mode", "single");
            data.put("code", code);
            data.put("ok", ok);
        } else {
            CompletableFuture.runAsync(() -> stockCompanyDetailCrawler.runAll());
            data.put("mode", "all_async");
        }
        return ResponseUtil.success(data);
    }

    @PostMapping("/crawl-annual")
    public Response<Map<String, Object>> crawlAnnual(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "name", required = false) String name) {
        if (!manualEnabled) {
            return ResponseUtil.fail(ResponseUtil.OPERATE_FAILED);
        }
        Map<String, Object> data = new HashMap<>();
        if (code != null && !code.trim().isEmpty()) {
            int n = stockAnnualReportCrawler.runOne(code.trim(), name);
            data.put("mode", "single");
            data.put("code", code);
            data.put("rows", n);
        } else {
            CompletableFuture.runAsync(() -> stockAnnualReportCrawler.runAll());
            data.put("mode", "all_async");
        }
        return ResponseUtil.success(data);
    }

  @PostMapping("/rebuild-industry-benchmark")
  public Response<Map<String, Object>> rebuildIndustryBenchmark() {
    if (!manualEnabled) {
      return ResponseUtil.fail(ResponseUtil.OPERATE_FAILED);
    }
    int n = atlasIndustryBenchmarkService.rebuildAll();
    Map<String, Object> data = new HashMap<>();
    data.put("industries", n);
    return ResponseUtil.success(data);
  }

  /** 清理年报重复行并确保 (code, report_year) 唯一 */
  @PostMapping("/dedupe-annual-report")
  public Response<Map<String, Object>> dedupeAnnualReport() {
    if (!manualEnabled) {
      return ResponseUtil.fail(ResponseUtil.OPERATE_FAILED);
    }
    int removed = stockAnnualReportMapper.deleteDuplicateCodeYears();
    Map<String, Object> data = new HashMap<>();
    data.put("removed", removed);
    return ResponseUtil.success(data);
  }

    /** 批量刷新估值：PE TTM / PB / 市值 / 52 周高低（东财 clist + 年报兜底） */
    @PostMapping("/refresh-valuation")
    public Response<Map<String, Object>> refreshValuation(
            @RequestParam(value = "async", defaultValue = "true") boolean async) {
        if (!manualEnabled) {
            return ResponseUtil.fail(ResponseUtil.OPERATE_FAILED);
        }
        Map<String, Object> data = new HashMap<>();
        if (async) {
            boolean started = atlasValuationRefreshService.refreshAllAsync();
            data.put("mode", "async");
            data.put("started", started);
            if (!started) {
                data.put("message", "估值刷新任务已在运行");
            }
        } else {
            data.put("mode", "sync");
            data.putAll(atlasValuationRefreshService.refreshAll());
        }
        return ResponseUtil.success(data);
    }

    /** 手动触发单边趋势策略扫描并写入 strategy_stock */
    @PostMapping("/scan-strategy")
    public Response<Map<String, Object>> scanStrategy() {
        if (!strategyManualEnabled) {
            return ResponseUtil.fail(ResponseUtil.OPERATE_FAILED);
        }
        int saved = stockTargetScheduler.recommendSaveInternal(
                com.yh.bigdata.tts.common.param.QueryContextParam.empty(), true);
        stockBaseController.clearRecommendCache();
        Map<String, Object> data = new HashMap<>();
        data.put("strategy", "qsn");
        data.put("saved", saved);
        return ResponseUtil.success(data);
    }
}
