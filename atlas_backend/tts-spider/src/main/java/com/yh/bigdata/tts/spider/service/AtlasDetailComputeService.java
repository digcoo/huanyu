package com.yh.bigdata.tts.spider.service;

import com.yh.bigdata.tts.common.model.StockAnnualReport;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockIndustryBenchmark;

import java.util.List;
import java.util.Map;

public interface AtlasDetailComputeService {

    Map<String, Object> computeStage(StockAnnualReport latest);

    Map<String, Object> computeHealth(StockBase stock, StockAnnualReport latest, StockIndustryBenchmark benchmark);

    Map<String, Object> computeRadar(StockAnnualReport latest, StockIndustryBenchmark benchmark);

    List<Map<String, Object>> buildCompetitorList(String code);
}
