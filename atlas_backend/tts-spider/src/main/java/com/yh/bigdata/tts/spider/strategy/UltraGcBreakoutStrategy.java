package com.yh.bigdata.tts.spider.strategy;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MacdPositiveGateParams;
import com.yh.bigdata.tts.common.param.QueryContextParam;
import com.yh.bigdata.tts.common.param.UltraGcBreakoutStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.macd.MacdPositiveGateTools;
import com.yh.bigdata.tts.spider.strategy.tools.ultragc.UltraGcBreakoutEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 超短线 · MACD金叉K突破（ultragc）：Min30 金叉 K high 边沿突破。
 */
@Slf4j
@Component
public class UltraGcBreakoutStrategy extends AbstractStrategy {

    @Override
    public StrategyTypeEnum getStrategy() {
        return StrategyTypeEnum.ULTRA_GC_BREAKOUT;
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
            UltraGcBreakoutStrategyParams params = resolveParams(queryContextParam);
            UltraGcBreakoutEvaluator.UltraGcBreakoutEvaluation eval =
                    UltraGcBreakoutEvaluator.evaluate(stockBase, checkResult, params);
            if (!eval.isHit()) {
                return checkResult;
            }
            if (!MacdPositiveGateTools.passGate(stockBase, checkResult, resolveMacdPositiveGate(queryContextParam))) {
                return checkResult;
            }
            checkResult.setHasTrend(true);
            checkResult.setHasSignal(true);
            checkResult.setSortValue(50);
            checkResult.setTrendPeriodType(PeriodTypeEnum.MIN30);
            checkResult.setOpPeriodType(PeriodTypeEnum.MIN30);
        } catch (Exception ex) {
            log.error("{} - check exception : stock = {}", getClass().getName(), stockBase.getCode(), ex);
        } finally {
            applyFallbackSortValue(stockBase, checkResult);
        }
        return checkResult;
    }

    private UltraGcBreakoutStrategyParams resolveParams(QueryContextParam queryContextParam) {
        if (queryContextParam == null || queryContextParam.getUltraGcBreakout() == null) {
            return UltraGcBreakoutStrategyParams.defaults();
        }
        return UltraGcBreakoutStrategyParams.merge(queryContextParam.getUltraGcBreakout());
    }

    private MacdPositiveGateParams resolveMacdPositiveGate(QueryContextParam queryContextParam) {
        if (queryContextParam == null || queryContextParam.getMacdPositiveGate() == null) {
            return MacdPositiveGateParams.defaults();
        }
        return MacdPositiveGateParams.merge(queryContextParam.getMacdPositiveGate());
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
