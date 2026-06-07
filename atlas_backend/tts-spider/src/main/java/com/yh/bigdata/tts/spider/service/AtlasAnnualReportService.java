package com.yh.bigdata.tts.spider.service;

import com.yh.bigdata.tts.common.dto.atlas.AtlasCompassModuleVo;
import com.yh.bigdata.tts.common.model.StockAnnualReport;
import com.yh.bigdata.tts.common.model.StockBase;

import java.util.List;
import java.util.Map;

public interface AtlasAnnualReportService {

    List<StockAnnualReport> getReports(String code, int limit);

    StockAnnualReport getLatest(String code);

    Map<String, AtlasCompassModuleVo> buildCompassFromAnnual(String code);

    Map<String, Object> buildProfileFromAnnual(StockBase stock, StockAnnualReport latest);

    List<Map<String, String>> buildKeyMetricsFromAnnual(StockBase stock, StockAnnualReport latest);

    void refreshIfMissingAsync(String code, String name);
}
