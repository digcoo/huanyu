package com.yh.bigdata.tts.spider.strategy;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MacdGoldenCrossStrategyParams;
import com.yh.bigdata.tts.common.param.MacdPositiveGateParams;
import com.yh.bigdata.tts.common.param.QueryContextParam;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.macd.MacdPositiveGateTools;
import com.yh.bigdata.tts.spider.strategy.tools.macdgc.MacdGoldenCrossEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * MACD金叉（macdgc）：日/周/月单档末 K MACD 金叉。
 */
@Slf4j
@Component
public class MacdGoldenCrossStrategy extends AbstractStrategy {

    @Override
    public StrategyTypeEnum getStrategy() {
        return StrategyTypeEnum.MACD_GOLDEN_CROSS;
    }

    @Override
    public PeriodTypeEnum getOpPeriodType() {
        return PeriodTypeEnum.DAY;
    }

    @Override
    public List<PeriodTypeEnum> getTrendPeriodTypes() {
        return Collections.singletonList(PeriodTypeEnum.DAY);
    }

    @Override
    public CheckResult check(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes,
                             PeriodTypeEnum opPeriodType, QueryContextParam queryContextParam) {
        CheckResult checkResult = new CheckResult(stockBase.getCode(), stockBase.getChangeRate());
        try {
            MacdGoldenCrossStrategyParams params = resolveParams(queryContextParam);
            MacdGoldenCrossEvaluator.MacdGoldenCrossEvaluation eval =
                    MacdGoldenCrossEvaluator.evaluate(stockBase, checkResult, params);
            if (!eval.isHit()) {
                return checkResult;
            }
            if (!MacdPositiveGateTools.passGate(stockBase, checkResult, resolveMacdPositiveGate(queryContextParam))) {
                return checkResult;
            }
            PeriodTypeEnum primaryPeriod = eval.getPeriod();
            checkResult.setHasTrend(true);
            checkResult.setHasSignal(true);
            checkResult.setSortValue(50);
            checkResult.setTrendPeriodType(primaryPeriod);
            checkResult.setOpPeriodType(primaryPeriod);
        } catch (Exception ex) {
            log.error("{} - check exception : stock = {}", getClass().getName(), stockBase.getCode(), ex);
        } finally {
            applyFallbackSortValue(stockBase, checkResult);
        }
        return checkResult;
    }

    private MacdGoldenCrossStrategyParams resolveParams(QueryContextParam queryContextParam) {
        if (queryContextParam == null || queryContextParam.getMacdGoldenCross() == null) {
            return MacdGoldenCrossStrategyParams.defaults();
        }
        return MacdGoldenCrossStrategyParams.merge(queryContextParam.getMacdGoldenCross());
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
