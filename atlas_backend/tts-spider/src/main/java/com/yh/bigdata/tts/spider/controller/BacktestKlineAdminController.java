package com.yh.bigdata.tts.spider.controller;

import com.yh.bigdata.tts.common.dao.BacktestDaykMapper;
import com.yh.bigdata.tts.common.model.StockDay;
import com.yh.bigdata.tts.common.param.base.Response;
import com.yh.bigdata.tts.common.param.base.ResponseUtil;
import com.yh.bigdata.tts.spider.crawler.BacktestKlineOrchestrator;
import com.yh.bigdata.tts.spider.service.KlineSnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 回测 K 线：雪球爬取 backtest_dayk + 周/月/年快照回放。
 */
@RestController
@RequestMapping("/stock/admin/backtest-kline")
public class BacktestKlineAdminController {

    @Autowired
    private BacktestKlineOrchestrator backtestKlineOrchestrator;
    @Autowired
    private KlineSnapshotService klineSnapshotService;
    @Autowired
    private BacktestDaykMapper backtestDaykMapper;

    @Value("${atlas.backtest.kline.manual-enabled:true}")
    private boolean manualEnabled;

    @GetMapping("/status")
    public Response<Map<String, Object>> status() {
        return ResponseUtil.success(backtestKlineOrchestrator.status());
    }

    /** 查询 backtest_dayk 中某股的日 K（默认截至最新） */
    @GetMapping("/dayk")
    public Response<Map<String, Object>> dayk(
            @RequestParam("code") String code,
            @RequestParam(value = "toDay", required = false) String toDay) {
        String asOf = toDay != null ? toDay : backtestDaykMapper.selectMaxDayByCode(code);
        List<StockDay> bars = asOf != null
                ? backtestDaykMapper.selectUpToDay(code, asOf)
                : Collections.emptyList();
        Map<String, Object> data = new HashMap<>();
        data.put("code", code);
        data.put("toDay", asOf);
        data.put("count", bars.size());
        data.put("oldestDay", bars.isEmpty() ? null : bars.get(0).getDay());
        data.put("newestDay", bars.isEmpty() ? null : bars.get(bars.size() - 1).getDay());
        data.put("bars", bars);
        return ResponseUtil.success(data);
    }

    /** 从雪球爬取日 K → backtest_dayk；单股不传 countDays 时默认完整历史 */
    @PostMapping("/crawl-dayk")
    public Response<Map<String, Object>> crawlDayk(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "countDays", required = false) Integer countDays,
            @RequestParam(value = "full", required = false) Boolean full,
            @RequestParam(value = "async", defaultValue = "true") boolean async) {
        if (!manualEnabled) {
            return ResponseUtil.fail(ResponseUtil.OPERATE_FAILED);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("code", code);
        data.put("countDays", countDays);
        data.put("full", full != null ? full : (code != null && countDays == null));
        if (async) {
            boolean started = backtestKlineOrchestrator.crawlDaykAsync(code, countDays, full);
            data.put("mode", "async");
            data.put("started", started);
            if (!started) {
                data.put("message", "日K爬取任务已在运行");
            }
        } else {
            data.put("mode", "sync_not_supported_use_async");
        }
        return ResponseUtil.success(data);
    }

    /** 单股完整日 K（分页直至上市初或 API 尽头） */
    @PostMapping("/crawl-dayk-full")
    public Response<Map<String, Object>> crawlDaykFull(
            @RequestParam("code") String code,
            @RequestParam(value = "async", defaultValue = "true") boolean async) {
        if (!manualEnabled) {
            return ResponseUtil.fail(ResponseUtil.OPERATE_FAILED);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("code", code);
        data.put("mode", "full");
        if (async) {
            boolean started = backtestKlineOrchestrator.crawlFullHistoryAsync(code);
            data.put("started", started);
            if (!started) {
                data.put("message", "日K爬取任务已在运行或 code 为空");
            }
        } else {
            data.put("stats", backtestKlineOrchestrator.status());
        }
        return ResponseUtil.success(data);
    }

    /** 爬取日 K 并对最新交易日生成周/月/年快照；单股默认完整日 K */
    @PostMapping("/crawl-and-build")
    public Response<Map<String, Object>> crawlAndBuild(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "countDays", required = false) Integer countDays,
            @RequestParam(value = "full", required = false) Boolean full) {
        if (!manualEnabled) {
            return ResponseUtil.fail(ResponseUtil.OPERATE_FAILED);
        }
        Map<String, Object> data = new HashMap<>();
        boolean started = backtestKlineOrchestrator.crawlAndBuildLatestAsync(code, countDays, full);
        data.put("started", started);
        if (!started) {
            data.put("message", "回测K线管线已在运行");
        }
        return ResponseUtil.success(data);
    }

    /** 基于 backtest_dayk 对指定截止日生成周/月/年快照 */
    @PostMapping("/build-snapshots")
    public Response<Map<String, Object>> buildSnapshots(
            @RequestParam("asOfDay") String asOfDay) {
        if (!manualEnabled) {
            return ResponseUtil.fail(ResponseUtil.OPERATE_FAILED);
        }
        int n = klineSnapshotService.buildPeriodSnapshotsAll(asOfDay);
        Map<String, Object> data = new HashMap<>();
        data.put("asOfDay", asOfDay);
        data.put("stocks", n);
        return ResponseUtil.success(data);
    }

    /** 对交易日区间逐日回放生成周/月/年快照（异步，耗时长） */
    @PostMapping("/build-snapshots-range")
    public Response<Map<String, Object>> buildSnapshotsRange(
            @RequestParam("fromDay") String fromDay,
            @RequestParam("toDay") String toDay) {
        if (!manualEnabled) {
            return ResponseUtil.fail(ResponseUtil.OPERATE_FAILED);
        }
        Map<String, Object> data = new HashMap<>();
        boolean started = backtestKlineOrchestrator.buildSnapshotsRangeAsync(fromDay, toDay);
        data.put("started", started);
        data.put("fromDay", fromDay);
        data.put("toDay", toDay);
        if (!started) {
            data.put("message", "快照生成任务已在运行");
        }
        return ResponseUtil.success(data);
    }
}
