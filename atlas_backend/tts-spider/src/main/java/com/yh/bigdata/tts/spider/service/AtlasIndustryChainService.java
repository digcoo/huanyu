package com.yh.bigdata.tts.spider.service;

import java.util.Map;

public interface AtlasIndustryChainService {

    Map<String, Object> buildIndustryChain(String code);

    void refreshSegmentsIfMissingAsync(String code);

    /** 详情页：缺 segment 时同步补爬（仅一次） */
    void ensureSegments(String code);

    void crawlAndSaveSegments(String code);

    /** 由主营构成 segment 合成一句业务摘要 */
    String buildSegmentBrief(String code);
}
