package com.yh.bigdata.tts.spider.strategy;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MediumStrategyParams;
import com.yh.bigdata.tts.common.param.QueryContextParam;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.medium.MediumEvaluator;
import com.yh.bigdata.tts.spider.strategy.tools.medium.MediumFilterTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 中线策略（medium）· 自然月桶周K 基准 + 本月周K 突破
 */
@Slf4j
@Component
public class MediumStrategy extends AbstractStrategy {

    @Override
    public StrategyTypeEnum getStrategy() {
        return StrategyTypeEnum.MEDIUM;
    }

    @Override
    public PeriodTypeEnum getOpPeriodType() {
        return PeriodTypeEnum.WEEK;
    }

    @Override
    public List<PeriodTypeEnum> getTrendPeriodTypes() {
        return Arrays.asList(PeriodTypeEnum.YEAR, PeriodTypeEnum.MONTH, PeriodTypeEnum.WEEK);
    }

    @Override
    public CheckResult check(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes,
                             PeriodTypeEnum opPeriodType, QueryContextParam queryContextParam) {
        CheckResult checkResult = new CheckResult(stockBase.getCode(), stockBase.getChangeRate());
        try {
            MediumStrategyParams params = resolveParams(queryContextParam);

            if (!MediumFilterTools.passFilters(stockBase, checkResult, params)) {
                return checkResult;
            }

            MediumEvaluator.MediumEvaluation eval =
                    MediumEvaluator.evaluate(stockBase, checkResult, params);
            if (!eval.isHit()) {
                return checkResult;
            }

            checkResult.setHasTrend(true);
            checkResult.setHasSignal(true);
            checkResult.setSortValue(eval.getScore());
            checkResult.setTrendPeriodType(PeriodTypeEnum.MONTH);
            checkResult.setOpPeriodType(PeriodTypeEnum.WEEK);

        } catch (Exception ex) {
            log.error("{} - check exception : stock = {}", getClass().getName(), stockBase.getCode(), ex);
        } finally {
            applyFallbackSortValue(stockBase, checkResult);
        }
        return checkResult;
    }

    private MediumStrategyParams resolveParams(QueryContextParam queryContextParam) {
        if (queryContextParam == null || queryContextParam.getMedium() == null) {
            return MediumStrategyParams.defaults();
        }
        return MediumStrategyParams.merge(queryContextParam.getMedium());
    }

    private void applyFallbackSortValue(StockBase stockBase, CheckResult checkResult) {
        if (checkResult.getSortValue() > 0 || !checkResult.isSuccess()) {
            return;
        }
        try {
            Trade weekTrade = RealtimeStockCache.getLastTrade(stockBase, PeriodTypeEnum.WEEK, -1);
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
