package com.yh.bigdata.tts.spider.strategy;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.Dc2StrategyParams;
import com.yh.bigdata.tts.common.param.QueryContextParam;
import com.yh.bigdata.tts.common.param.UnilateralStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.dc2.Dc2Evaluator;
import com.yh.bigdata.tts.spider.strategy.tools.dc2.Dc2ScoreCalculator;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralGateTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 死叉突破（dc2）· 大周期 MACD&gt;0 + 突破死叉柱 high
 */
@Slf4j
// @Component — v1 已下线
public class Dc2BreakoutStrategy extends AbstractStrategy {

    @Override
    public StrategyTypeEnum getStrategy() {
        return StrategyTypeEnum.DC2_BREAKOUT;
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
                PeriodTypeEnum.DAY);
    }

    @Override
    public CheckResult check(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes,
                             PeriodTypeEnum opPeriodType, QueryContextParam queryContextParam) {
        CheckResult checkResult = new CheckResult(stockBase.getCode(), stockBase.getChangeRate());
        try {
            Dc2StrategyParams params = resolveParams(queryContextParam);
            UnilateralStrategyParams gateParams = UnilateralStrategyParams.builder()
                    .minAvgAmount(params.getMinAvgAmount())
                    .build();
            if (!UnilateralGateTools.passGate(stockBase, checkResult, gateParams)) {
                return checkResult;
            }

            Dc2Evaluator.Dc2Evaluation eval = Dc2Evaluator.evaluate(stockBase, checkResult, params);
            if (!eval.isHit()) {
                return checkResult;
            }

            checkResult.setHasTrend(true);
            checkResult.setHasSignal(true);
            checkResult.setSortValue(eval.getScore());
            checkResult.setTrendPeriodType(Dc2ScoreCalculator.trendPeriodForTier(eval.getTier()));
            checkResult.setOpPeriodType(Dc2ScoreCalculator.signalPeriodForTier(eval.getTier()));

        } catch (Exception ex) {
            log.error("{} - check exception : stock = {}", getClass().getName(), stockBase.getCode(), ex);
        } finally {
            applyFallbackSortValue(stockBase, checkResult);
        }
        return checkResult;
    }

    private Dc2StrategyParams resolveParams(QueryContextParam queryContextParam) {
        if (queryContextParam == null || queryContextParam.getDc2() == null) {
            return Dc2StrategyParams.defaults();
        }
        return Dc2StrategyParams.merge(queryContextParam.getDc2());
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
