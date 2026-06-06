package com.yh.bigdata.tts.spider.strategy;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.TrendToolsWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
  * 右侧（顶脱离、底支撑）、梯子（半山腰|脱离）、深坑、上移
 */
@Slf4j
@Component
public class DefaultTrendStrategy extends AbstractStrategy {


    @Override
    public StrategyTypeEnum getStrategy() {
        return StrategyTypeEnum.TREND_NEW;
    }

    @Override
    public PeriodTypeEnum getOpPeriodType() {
        return PeriodTypeEnum.DAY;
    }

    @Override
    public List<PeriodTypeEnum> getTrendPeriodTypes() {
        return Arrays.asList(PeriodTypeEnum.WEEK,PeriodTypeEnum.MONTH);
    }

}