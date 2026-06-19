package com.yh.bigdata.tts.spider.strategy;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.QueryContextParam;
import com.yh.bigdata.tts.common.param.UltraLowReboundStrategyParams;
import com.yh.bigdata.tts.common.param.UnilateralStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.UltraLowReboundEvaluator;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.UltraLowReboundScoreCalculator;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralGateTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 梯子突破（ladder）· 超短/短/中/长 四档突破阶梯
 */
@Slf4j
// @Component — v1 已下线
public class UltraLowReboundStrategy extends AbstractStrategy {

    @Override
    public StrategyTypeEnum getStrategy() {
        return StrategyTypeEnum.LADDER_BREAKOUT;
    }

    @Override
    public PeriodTypeEnum getOpPeriodType() {
        return PeriodTypeEnum.MIN30;
    }

    @Override
    public List<PeriodTypeEnum> getTrendPeriodTypes() {
        return Arrays.asList(PeriodTypeEnum.WEEK, PeriodTypeEnum.DAY, PeriodTypeEnum.MIN30);
    }

    @Override
    public CheckResult check(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes,
                             PeriodTypeEnum opPeriodType, QueryContextParam queryContextParam) {
        CheckResult checkResult = new CheckResult(stockBase.getCode(), stockBase.getChangeRate());
        try {
            UltraLowReboundStrategyParams params = resolveParams(queryContextParam);
            UnilateralStrategyParams gateParams = UnilateralStrategyParams.builder()
                    .minAvgAmount(params.getMinAvgAmount())
                    .build();
            if (!UnilateralGateTools.passGate(stockBase, checkResult, gateParams)) {
                return checkResult;
            }

            UltraLowReboundEvaluator.UltraLowEvaluation eval =
                    UltraLowReboundEvaluator.evaluate(stockBase, checkResult, params);
            if (!eval.isHit()) {
                return checkResult;
            }

            checkResult.setHasTrend(true);
            checkResult.setHasSignal(true);
            checkResult.setSortValue(eval.getScore());
            checkResult.setTrendPeriodType(UltraLowReboundScoreCalculator.trendPeriodForTier(eval.getTier()));
            checkResult.setOpPeriodType(UltraLowReboundScoreCalculator.signalPeriodForTier(eval.getTier()));

        } catch (Exception ex) {
            log.error("{} - check exception : stock = {}", getClass().getName(), stockBase.getCode(), ex);
        } finally {
            applyFallbackSortValue(stockBase, checkResult);
        }
        return checkResult;
    }

    private UltraLowReboundStrategyParams resolveParams(QueryContextParam queryContextParam) {
        if (queryContextParam == null || queryContextParam.getUltraLow() == null) {
            return UltraLowReboundStrategyParams.defaults();
        }
        return UltraLowReboundStrategyParams.merge(queryContextParam.getUltraLow());
    }

    private void applyFallbackSortValue(StockBase stockBase, CheckResult checkResult) {
        if (checkResult.getSortValue() > 0 || !checkResult.isSuccess()) {
            return;
        }
        if (stockBase.getChangeRate() != null) {
            checkResult.setSortValue(stockBase.getChangeRate());
        }
    }
}
