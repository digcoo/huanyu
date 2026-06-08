package com.yh.bigdata.tts.spider.service;

import com.yh.bigdata.tts.common.dto.atlas.AtlasSeriesPointVo;
import com.yh.bigdata.tts.common.model.IndustryYearlyMetrics;

import java.util.List;
import java.util.function.Function;

public interface AtlasCompassBenchmarkService {

    List<IndustryYearlyMetrics> loadIndustryMetrics(String industry);

    List<AtlasSeriesPointVo> alignIndustrySeries(List<AtlasSeriesPointVo> companyData,
                                                   List<IndustryYearlyMetrics> industryRows,
                                                   Function<IndustryYearlyMetrics, Double> metricFn,
                                                   boolean scaleYi,
                                                   double fallbackRatio);
}
