package com.yh.bigdata.tts.spider.service;

import com.yh.bigdata.tts.common.model.StockAnnualReport;
import com.yh.bigdata.tts.common.model.StockBase;

import java.util.Map;

public interface AtlasIndustryProfileService {

    Map<String, Object> buildProfile(StockBase stock, StockAnnualReport latest);
}
