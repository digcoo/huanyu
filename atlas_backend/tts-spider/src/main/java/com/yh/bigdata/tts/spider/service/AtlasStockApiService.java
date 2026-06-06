package com.yh.bigdata.tts.spider.service;

import com.yh.bigdata.tts.common.dto.atlas.*;
import com.yh.bigdata.tts.common.model.StockBase;

import java.util.List;
import java.util.Map;

public interface AtlasStockApiService {

    List<AtlasStockSummaryVo> search(String keyword, int limit);

    AtlasStockSummaryVo getSummary(String code);

    List<AtlasKlineBarVo> getKlines(String code, String period, int limit);

    AtlasStockDetailVo getDetail(String code);

    Map<String, AtlasCompassModuleVo> getCompass(String code);

    boolean isCacheReady();

    StockBase requireStock(String code);
}
