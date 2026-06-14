package com.yh.bigdata.tts.spider.strategy;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.QueryContextParam;
import com.yh.bigdata.tts.common.param.ReboundStrategyParams;
import com.yh.bigdata.tts.common.param.UnilateralStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.rebound.ReboundEvaluator;
import com.yh.bigdata.tts.spider.strategy.tools.rebound.ReboundScoreCalculator;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralGateTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 深跌反弹（default）· v3.0：Gate + 短/中/长 MACD&lt;0 + K 线突破
 * @see docs/strategies/深坑反弹策略.md
 */
@Slf4j
@Component
public class DefaultReboundStrategy extends AbstractStrategy {

    @Override
    public StrategyTypeEnum getStrategy() {
        return StrategyTypeEnum.DEFAUL;
    }

    @Override
    public PeriodTypeEnum getOpPeriodType() {
        return PeriodTypeEnum.DAY;
    }

    @Override
    public List<PeriodTypeEnum> getTrendPeriodTypes() {
        return Arrays.asList(
                PeriodTypeEnum.YEAR,
                PeriodTypeEnum.MONTH,
                PeriodTypeEnum.WEEK,
                PeriodTypeEnum.DAY);
    }

    @Override
    public CheckResult check(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes,
                             PeriodTypeEnum opPeriodType, QueryContextParam queryContextParam) {
        CheckResult checkResult = new CheckResult(stockBase.getCode(), stockBase.getChangeRate());
        try {
            ReboundStrategyParams params = resolveParams(queryContextParam);
            UnilateralStrategyParams gateParams = UnilateralStrategyParams.builder()
                    .minAvgAmount(params.getMinAvgAmount())
                    .build();
            if (!UnilateralGateTools.passGate(stockBase, checkResult, gateParams)) {
                return checkResult;
            }

            ReboundEvaluator.ReboundEvaluation eval =
                    ReboundEvaluator.evaluate(stockBase, checkResult, params);
            if (!eval.isHit()) {
                return checkResult;
            }

            checkResult.setHasTrend(true);
            checkResult.setHasSignal(true);
            checkResult.setSortValue(eval.getScore());
            checkResult.setTrendPeriodType(ReboundScoreCalculator.trendPeriodForTier(eval.getTier()));
            checkResult.setOpPeriodType(ReboundScoreCalculator.signalPeriodForTier(eval.getTier()));

        } catch (Exception ex) {
            log.error("{} - check exception : stock = {}", getClass().getName(), stockBase.getCode(), ex);
        } finally {
            applyFallbackSortValue(stockBase, checkResult);
        }
        return checkResult;
    }

    private ReboundStrategyParams resolveParams(QueryContextParam queryContextParam) {
        if (queryContextParam == null || queryContextParam.getRebound() == null) {
            return ReboundStrategyParams.defaults();
        }
        return ReboundStrategyParams.merge(queryContextParam.getRebound());
    }

    private void applyFallbackSortValue(StockBase stockBase, CheckResult checkResult) {
        if (checkResult.getSortValue() > 0 || !checkResult.isSuccess()) {
            return;
        }
        try {
            Trade monthTrade1 = RealtimeStockCache.getLastTrade(stockBase, PeriodTypeEnum.MONTH, -1);
            Trade weekTrade1 = RealtimeStockCache.getLastTrade(stockBase, PeriodTypeEnum.WEEK, -1);
            Trade dayTrade1 = RealtimeStockCache.getLastTrade(stockBase, PeriodTypeEnum.DAY, -1);
            if (monthTrade1 != null) {
                checkResult.setSortValue(monthTrade1.getChangeRate());
            } else if (weekTrade1 != null) {
                checkResult.setSortValue(weekTrade1.getChangeRate());
            } else {
                checkResult.setSortValue(dayTrade1 == null ? 0 : dayTrade1.getChangeRate());
            }
        } catch (Exception e2) {
            log.error("setSortValue error...{}", stockBase.getCode(), e2);
        }
    }
}
