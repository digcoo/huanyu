package com.yh.bigdata.tts.spider.service;

import java.util.Map;

public interface AtlasSupplierCustomerService {

    boolean isRunning();

    Map<String, Object> lastResult();

    Map<String, Object> crawlOne(String code);

    boolean crawlAllAsync();
}
