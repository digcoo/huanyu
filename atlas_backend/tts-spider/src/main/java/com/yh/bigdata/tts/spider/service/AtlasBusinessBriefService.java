package com.yh.bigdata.tts.spider.service;

import com.yh.bigdata.tts.common.model.StockBase;

public interface AtlasBusinessBriefService {

    /** 生成用户可读的业务摘要（≤160 字） */
    String buildBrief(StockBase stock, String industryTemplateLine);

    /** 摘要来源：segment / org_profile / stored / template */
    String resolveBriefSource(StockBase stock, String industryTemplateLine);

    boolean isRegistrationScope(String text);

    /** crawl 后写入 base：business_scope + business_brief + mainBusiness(兼容) */
    void applyCrawlFields(StockBase patch, String businessScope, String orgProfile, String code,
                          String industryTemplateLine);
}
