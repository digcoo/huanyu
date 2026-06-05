package com.yh.bigdata.tts.spider.strategy.group.min30;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.QueryContextParam;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.AbstractStrategy;
import com.yh.bigdata.tts.spider.strategy.tools.TrendToolsWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 * 突破波段step-high
 *
 */
@Slf4j
@Component
public class CrossBandStepHighZaoPanStrategy extends AbstractStrategy {

    @Override
    public StrategyTypeEnum getStrategy() {
        return StrategyTypeEnum.CROSS_BAND_STEP_HIGH_ZAOPAN;
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
        trendPeriodTypes = List.of(PeriodTypeEnum.WEEK, PeriodTypeEnum.MONTH);
        return TrendToolsWrapper.checkOverBandLowTrend(stockBase, trendPeriodTypes, checkResult)
                && TrendToolsWrapper.checkOverRevertBandLowTrend(stockBase, trendPeriodTypes, checkResult)
                ;
    }

    @Override
    public boolean checkSignal(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, PeriodTypeEnum opPeriodType, CheckResult checkResult, QueryContextParam queryContextParam) {
        trendPeriodTypes = List.of(PeriodTypeEnum.DAY, PeriodTypeEnum.WEEK, PeriodTypeEnum.MONTH);

        // 突破趋势波段step: high
        boolean checkCrossBandHighSignal = SignalMin30ToolsWrapper.checkCrossBandStepSignal(stockBase, trendPeriodTypes, opPeriodType, checkResult, false);
        if (checkCrossBandHighSignal) {
            return true;
        }

        // 突破反转波段step: high
        boolean checkRevertBandHighSignal = SignalMin30ToolsWrapper.checkRevertBandStepSignal(stockBase, trendPeriodTypes, opPeriodType, checkResult, false);
        if (checkRevertBandHighSignal) {
            return true;
        }

        return false;

    }

}