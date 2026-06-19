package com.yh.bigdata.tts.spider.strategy;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.QueryContextParam;
import com.yh.bigdata.tts.common.param.RetestStrategyParams;
import com.yh.bigdata.tts.common.param.UnilateralStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.retest.RetestEvaluator;
import com.yh.bigdata.tts.spider.strategy.tools.retest.RetestScoreCalculator;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralGateTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 回踩抬升（retest）· 强弹 → 回踩 → 再升
 */
@Slf4j
// @Component — v1 已下线
public class RetestStrategy extends AbstractStrategy {

    @Override
    public StrategyTypeEnum getStrategy() {
        return StrategyTypeEnum.RETEST;
    }

    @Override
    public PeriodTypeEnum getOpPeriodType() {
        return PeriodTypeEnum.DAY;
    }

    @Override
    public List<PeriodTypeEnum> getTrendPeriodTypes() {
        return Arrays.asList(
                PeriodTypeEnum.MONTH,
                PeriodTypeEnum.WEEK,
                PeriodTypeEnum.DAY,
                PeriodTypeEnum.MIN30);
    }

    @Override
    public CheckResult check(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes,
                             PeriodTypeEnum opPeriodType, QueryContextParam queryContextParam) {
        CheckResult checkResult = new CheckResult(stockBase.getCode(), stockBase.getChangeRate());
        try {
            RetestStrategyParams params = resolveParams(queryContextParam);
            UnilateralStrategyParams gateParams = UnilateralStrategyParams.builder()
                    .minAvgAmount(params.getMinAvgAmount())
                    .build();
            if (!UnilateralGateTools.passGate(stockBase, checkResult, gateParams)) {
                return checkResult;
            }

            RetestEvaluator.RetestEvaluation eval =
                    RetestEvaluator.evaluate(stockBase, checkResult, params);
            if (!eval.isHit()) {
                return checkResult;
            }

            checkResult.setHasTrend(true);
            checkResult.setHasSignal(true);
            checkResult.setSortValue(eval.getScore());
            checkResult.setTrendPeriodType(RetestScoreCalculator.trendPeriodForTier(eval.getTier()));
            checkResult.setOpPeriodType(RetestScoreCalculator.signalPeriodForTier(eval.getTier()));

        } catch (Exception ex) {
            log.error("{} - check exception : stock = {}", getClass().getName(), stockBase.getCode(), ex);
        } finally {
            applyFallbackSortValue(stockBase, checkResult);
        }
        return checkResult;
    }

    private RetestStrategyParams resolveParams(QueryContextParam queryContextParam) {
        if (queryContextParam == null || queryContextParam.getRetest() == null) {
            return RetestStrategyParams.defaults();
        }
        return RetestStrategyParams.merge(queryContextParam.getRetest());
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
