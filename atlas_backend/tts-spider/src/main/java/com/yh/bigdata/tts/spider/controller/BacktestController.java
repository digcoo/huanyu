package com.yh.bigdata.tts.spider.controller;

import com.yh.bigdata.tts.common.dto.atlas.BacktestResultVo;
import com.yh.bigdata.tts.common.dto.atlas.BacktestStrategyOptionVo;
import com.yh.bigdata.tts.common.param.BacktestParam;
import com.yh.bigdata.tts.common.param.base.Response;
import com.yh.bigdata.tts.common.param.base.ResponseUtil;
import com.yh.bigdata.tts.spider.service.BacktestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/backtest")
@Slf4j
public class BacktestController {

    @Autowired
    private BacktestService backtestService;

    @GetMapping("/health")
    public Response<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "ok");
        data.put("cacheReady", backtestService.isCacheReady());
        return ResponseUtil.success(data);
    }

    @GetMapping("/strategies")
    public Response<List<BacktestStrategyOptionVo>> strategies() {
        return ResponseUtil.success(backtestService.listStrategies());
    }

    @PostMapping("/run")
    public Response<BacktestResultVo> run(@RequestBody BacktestParam param) {
        try {
            BacktestResultVo result = backtestService.run(param);
            log.info("backtest done strategy={} signals={} elapsedMs={}",
                    result.getSummary().getStrategy(),
                    result.getSummary().getSignalCount(),
                    result.getSummary().getElapsedMs());
            return ResponseUtil.success(result);
        } catch (IllegalArgumentException ex) {
            return ResponseUtil.fail(ResponseUtil.PARAM_ILLEGAL);
        } catch (IllegalStateException ex) {
            log.warn("backtest cache not ready: {}", ex.getMessage());
            return ResponseUtil.fail(ResponseUtil.OPERATE_FAILED);
        } catch (Exception ex) {
            log.error("backtest failed", ex);
            return ResponseUtil.fail(ResponseUtil.SERVER_ERROR);
        }
    }
}
