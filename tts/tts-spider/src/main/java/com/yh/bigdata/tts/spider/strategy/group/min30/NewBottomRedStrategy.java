package com.yh.bigdata.tts.spider.strategy.group.min30;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.QueryContextParam;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.AbstractStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 *  新低翻红
 *
 */
@Slf4j
@Component
public class NewBottomRedStrategy extends AbstractStrategy {

    @Override
    public StrategyTypeEnum getStrategy() {
        return StrategyTypeEnum.NEW_BOTTOM_RED;
    }

    @Override
    public PeriodTypeEnum getOpPeriodType() {
        return PeriodTypeEnum.DAY;
    }

    @Override
    public List<PeriodTypeEnum> getTrendPeriodTypes() {
        return List.of(PeriodTypeEnum.WEEK, PeriodTypeEnum.MONTH);
    }

    @Override
    public boolean checkTrend(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
//        trendPeriodTypes = List.of(PeriodTypeEnum.WEEK, PeriodTypeEnum.MONTH);
//        return TrendToolsWrapper.checkOverBandLowTrend(stockBase, trendPeriodTypes, checkResult)
//                && TrendToolsWrapper.checkOverRevertBandLowTrend(stockBase, trendPeriodTypes, checkResult)
//                ;
        return true;
    }

    @Override
    public boolean checkSignal(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, PeriodTypeEnum opPeriodType, CheckResult checkResult, QueryContextParam queryContextParam) {
        return SignalMin30ToolsWrapper.checkNewBottomRedSignal(stockBase, trendPeriodTypes, checkResult);
    }
}