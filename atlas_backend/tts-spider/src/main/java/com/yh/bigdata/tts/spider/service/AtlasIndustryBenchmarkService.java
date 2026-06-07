package com.yh.bigdata.tts.spider.service;

public interface AtlasIndustryBenchmarkService {

    /** 基于 base.industry + 最新年报重算行业均值，返回行业数 */
    int rebuildAll();
}
