package com.yh.bigdata.tts.spider.service;

import java.util.Map;

/**
 * 详情页数据底座覆盖率快照
 */
public interface AtlasDetailDataQualityService {

    Map<String, Object> snapshot();
}
