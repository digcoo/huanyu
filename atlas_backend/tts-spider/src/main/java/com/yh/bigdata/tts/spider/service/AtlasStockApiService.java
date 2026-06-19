package com.yh.bigdata.tts.spider.service;

import com.yh.bigdata.tts.common.dto.atlas.*;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.UltraShortStrategyParams;

import java.util.List;
import java.util.Map;

public interface AtlasStockApiService {

    List<AtlasStockSummaryVo> search(String keyword, int limit);

    AtlasStockSummaryVo getSummary(String code);

    List<AtlasKlineBarVo> getKlines(String code, String period, int limit);

    /** 雪球实时拉取 K 线，仅返回不落库 */
    List<AtlasKlineBarVo> refreshKlines(String code, String period, int limit);

    /** 梯子突破策略 · 30m 基准 K / 突破 K（用于 K 线标记） */
    AtlasUlowMin30MarkersVo getUlowMin30Markers(String code);

    AtlasUlowMin30MarkersVo getLadderMarkers(String code, String period);

    /** 超短线策略 · 30m 基准 K / 突破 K（用于 K 线标记） */
    AtlasUlowMin30MarkersVo getUltraMarkers(String code, String period, UltraShortStrategyParams params);

    AtlasRetestMarkersVo getRetestMarkers(String code, String period);

    AtlasGc2MarkersVo getGc2Markers(String code, String period);

    AtlasDc2MarkersVo getDc2Markers(String code, String period);

    AtlasStockDetailVo getDetail(String code);

    Map<String, AtlasCompassModuleVo> getCompass(String code);

    List<AtlasMarketIndexVo> getMarketIndices(String market, String period, int limit);

    boolean isCacheReady();

    StockBase requireStock(String code);
}
