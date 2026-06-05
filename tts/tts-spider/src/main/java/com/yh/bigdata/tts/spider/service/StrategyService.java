package com.yh.bigdata.tts.spider.service;

import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.model.StockTarget;

import java.util.List;

public interface StrategyService {

    /**
     * 查询并更新实时推荐
     */
    List<StockTarget> getAndUpdateTriggerStockTargets(StrategyTypeEnum strategyType);

}
