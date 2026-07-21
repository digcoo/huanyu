package com.yh.bigdata.tts.spider.strategy;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.Min60WaveCcBreakoutStrategyParams;
import com.yh.bigdata.tts.common.param.QueryContextParam;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.min60wavecc.Min60WaveCcBreakoutEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class Min60WaveCcBreakoutStrategy extends AbstractStrategy {

    @Override
    public StrategyTypeEnum getStrategy() {
        return StrategyTypeEnum.MIN60_WAVE_CC_BREAKOUT;
    }

    @Override
    public PeriodTypeEnum getOpPeriodType() {
        return PeriodTypeEnum.MIN60;
    }

    @Override
    public List<PeriodTypeEnum> getTrendPeriodTypes() {
        return Arrays.asList(PeriodTypeEnum.WEEK, PeriodTypeEnum.DAY, PeriodTypeEnum.MIN60);
    }

    @Override
    public CheckResult check(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes,
                             PeriodTypeEnum opPeriodType, QueryContextParam queryContextParam) {
        CheckResult checkResult = new CheckResult(stockBase.getCode(), stockBase.getChangeRate());
        try {
            Min60WaveCcBreakoutStrategyParams params = resolveParams(queryContextParam);
            Min60WaveCcBreakoutEvaluator.Min60WaveCcBreakoutEvaluation eval =
                    Min60WaveCcBreakoutEvaluator.evaluate(stockBase, checkResult, params);
            if (!eval.isHit()) {
                return checkResult;
            }
            checkResult.setHasTrend(true);
            checkResult.setHasSignal(true);
            checkResult.setSortValue(50);
            checkResult.setTrendPeriodType(PeriodTypeEnum.WEEK);
            checkResult.setOpPeriodType(PeriodTypeEnum.MIN60);
        } catch (Exception ex) {
            log.error("{} - check exception : stock = {}", getClass().getName(), stockBase.getCode(), ex);
        } finally {
            applyFallbackSortValue(stockBase, checkResult);
        }
        return checkResult;
    }

    private Min60WaveCcBreakoutStrategyParams resolveParams(QueryContextParam queryContextParam) {
        if (queryContextParam == null || queryContextParam.getMin60WaveCcBreakout() == null) {
            return Min60WaveCcBreakoutStrategyParams.defaults();
        }
        return Min60WaveCcBreakoutStrategyParams.merge(queryContextParam.getMin60WaveCcBreakout());
    }

    private void applyFallbackSortValue(StockBase stockBase, CheckResult checkResult) {
        if (checkResult.getSortValue() > 0 || !checkResult.isSuccess()) {
            return;
        }
        try {
            Trade dayTrade = RealtimeStockCache.getLastTrade(stockBase, PeriodTypeEnum.DAY, 0);
            if (dayTrade != null && dayTrade.getChangeRate() != null) {
                checkResult.setSortValue(dayTrade.getChangeRate());
            } else if (stockBase.getChangeRate() != null) {
                checkResult.setSortValue(stockBase.getChangeRate());
            }
        } catch (Exception e2) {
            log.error("setSortValue error...{}", stockBase.getCode(), e2);
        }
    }
}
