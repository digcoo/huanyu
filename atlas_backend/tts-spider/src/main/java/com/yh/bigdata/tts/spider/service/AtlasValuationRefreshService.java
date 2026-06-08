package com.yh.bigdata.tts.spider.service;

import java.util.Map;

/**
 * 批量刷新 A 股估值字段（PE TTM / PB / 市值等）
 */
public interface AtlasValuationRefreshService {

    boolean isRunning();

    Map<String, Object> lastResult();

    /** 同步全量刷新：东财 clist → 年报兜底 PE */
    Map<String, Object> refreshAll();

    /** 异步全量刷新 */
    boolean refreshAllAsync();
}
