package com.yh.bigdata.tts.spider.strategy;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.QueryContextParam;
import com.yh.bigdata.tts.common.param.WeekWaveCcBreakoutStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.weekwavecc.WeekWaveCcBreakoutEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class WeekWaveCcBreakoutStrategy extends AbstractStrategy {

    @Override
    public StrategyTypeEnum getStrategy() {
        return StrategyTypeEnum.WEEK_WAVE_CC_BREAKOUT;
    }

    @Override
    public PeriodTypeEnum getOpPeriodType() {
        return PeriodTypeEnum.WEEK;
    }

    @Override
    public List<PeriodTypeEnum> getTrendPeriodTypes() {
        return Collections.singletonList(PeriodTypeEnum.WEEK);
    }

    @Override
    public CheckResult check(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes,
                             PeriodTypeEnum opPeriodType, QueryContextParam queryContextParam) {
        CheckResult checkResult = new CheckResult(stockBase.getCode(), stockBase.getChangeRate());
        try {
            WeekWaveCcBreakoutStrategyParams params = resolveParams(queryContextParam);
            WeekWaveCcBreakoutEvaluator.WeekWaveCcBreakoutEvaluation eval =
                    WeekWaveCcBreakoutEvaluator.evaluate(stockBase, checkResult, params);
            if (!eval.isHit()) {
                return checkResult;
            }
            checkResult.setHasTrend(true);
            checkResult.setHasSignal(true);
            checkResult.setSortValue(50);
            checkResult.setTrendPeriodType(PeriodTypeEnum.WEEK);
            checkResult.setOpPeriodType(PeriodTypeEnum.WEEK);
        } catch (Exception ex) {
            log.error("{} - check exception : stock = {}", getClass().getName(), stockBase.getCode(), ex);
        } finally {
            applyFallbackSortValue(stockBase, checkResult);
        }
        return checkResult;
    }

    private WeekWaveCcBreakoutStrategyParams resolveParams(QueryContextParam queryContextParam) {
        if (queryContextParam == null || queryContextParam.getWeekWaveCcBreakout() == null) {
            return WeekWaveCcBreakoutStrategyParams.defaults();
        }
        return WeekWaveCcBreakoutStrategyParams.merge(queryContextParam.getWeekWaveCcBreakout());
    }

    private void applyFallbackSortValue(StockBase stockBase, CheckResult checkResult) {
        if (checkResult.getSortValue() > 0 || !checkResult.isSuccess()) {
            return;
        }
        try {
            Trade weekTrade = RealtimeStockCache.getLastTrade(stockBase, PeriodTypeEnum.WEEK, 0);
            if (weekTrade != null && weekTrade.getChangeRate() != null) {
                checkResult.setSortValue(weekTrade.getChangeRate());
            } else if (stockBase.getChangeRate() != null) {
                checkResult.setSortValue(stockBase.getChangeRate());
            }
        } catch (Exception e2) {
            log.error("setSortValue error...{}", stockBase.getCode(), e2);
        }
    }
}
