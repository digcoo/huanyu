package com.yh.bigdata.tts.spider.strategy;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.QueryContextParam;
import com.yh.bigdata.tts.common.param.UltraShortStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.UltraShortEvaluator;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.UltraShortFilterTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 超短线策略（ultra）· 前两日 min30 局部新高/最近强K 基准 + 当日 min30 突破
 */
@Slf4j
@Component
public class UltraShortStrategy extends AbstractStrategy {

    @Override
    public StrategyTypeEnum getStrategy() {
        return StrategyTypeEnum.ULTRA_SHORT;
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
            UltraShortStrategyParams params = resolveParams(queryContextParam);

            if (!UltraShortFilterTools.passFilters(stockBase, checkResult, params)) {
                return checkResult;
            }

            UltraShortEvaluator.UltraShortEvaluation eval =
                    UltraShortEvaluator.evaluate(stockBase, checkResult, params);
            if (!eval.isHit()) {
                return checkResult;
            }

            checkResult.setHasTrend(true);
            checkResult.setHasSignal(true);
            checkResult.setSortValue(eval.getScore());
            checkResult.setTrendPeriodType(PeriodTypeEnum.MIN30);
            checkResult.setOpPeriodType(PeriodTypeEnum.MIN30);

        } catch (Exception ex) {
            log.error("{} - check exception : stock = {}", getClass().getName(), stockBase.getCode(), ex);
        } finally {
            applyFallbackSortValue(stockBase, checkResult);
        }
        return checkResult;
    }

    private UltraShortStrategyParams resolveParams(QueryContextParam queryContextParam) {
        if (queryContextParam == null || queryContextParam.getUltraShort() == null) {
            return UltraShortStrategyParams.defaults();
        }
        return UltraShortStrategyParams.merge(queryContextParam.getUltraShort());
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
