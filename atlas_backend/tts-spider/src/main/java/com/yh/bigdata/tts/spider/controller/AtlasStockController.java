package com.yh.bigdata.tts.spider.controller;

import com.yh.bigdata.tts.common.dto.atlas.AtlasCompassModuleVo;
import com.yh.bigdata.tts.common.dto.atlas.AtlasKlineBarVo;
import com.yh.bigdata.tts.common.dto.atlas.AtlasMarketIndexVo;
import com.yh.bigdata.tts.common.dto.atlas.AtlasStockDetailVo;
import com.yh.bigdata.tts.common.dto.atlas.AtlasStockSummaryVo;
import com.yh.bigdata.tts.common.param.base.Response;
import com.yh.bigdata.tts.common.param.base.ResponseUtil;
import com.yh.bigdata.tts.spider.service.AtlasStockApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/stock")
@Slf4j
public class AtlasStockController {

    @Autowired
    private AtlasStockApiService atlasStockApiService;

    @GetMapping("/health")
    public Response<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "ok");
        data.put("cacheReady", atlasStockApiService.isCacheReady());
        return ResponseUtil.success(data);
    }

    @GetMapping("/search")
    public Response<List<AtlasStockSummaryVo>> search(
            @RequestParam("q") String keyword,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        return ResponseUtil.success(atlasStockApiService.search(keyword, limit));
    }

    @GetMapping("/indices")
    public Response<List<AtlasMarketIndexVo>> getIndices(
            @RequestParam(value = "market", defaultValue = "cn") String market,
            @RequestParam(value = "period", defaultValue = "week") String period,
            @RequestParam(value = "limit", defaultValue = "50") int limit) {
        return ResponseUtil.success(atlasStockApiService.getMarketIndices(market, period, limit));
    }

    @GetMapping("/{code}")
    public Response<AtlasStockSummaryVo> getStock(@PathVariable("code") String code) {
        try {
            return ResponseUtil.success(atlasStockApiService.getSummary(code));
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping("/{code}/klines")
    public Response<List<AtlasKlineBarVo>> getKlines(
            @PathVariable("code") String code,
            @RequestParam(value = "period", defaultValue = "week") String period,
            @RequestParam(value = "limit", defaultValue = "50") int limit) {
        try {
            return ResponseUtil.success(atlasStockApiService.getKlines(code, period, limit));
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping("/{code}/detail")
    public Response<AtlasStockDetailVo> getDetail(@PathVariable("code") String code) {
        try {
            return ResponseUtil.success(atlasStockApiService.getDetail(code));
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping("/{code}/compass")
    public Response<Map<String, AtlasCompassModuleVo>> getCompass(@PathVariable("code") String code) {
        try {
            return ResponseUtil.success(atlasStockApiService.getCompass(code));
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }
}
