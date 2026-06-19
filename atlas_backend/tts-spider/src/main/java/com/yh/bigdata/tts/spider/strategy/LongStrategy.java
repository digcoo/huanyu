package com.yh.bigdata.tts.spider.strategy;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.LongStrategyParams;
import com.yh.bigdata.tts.common.param.QueryContextParam;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.longterm.LongEvaluator;
import com.yh.bigdata.tts.spider.strategy.tools.longterm.LongFilterTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 长线策略（long）· 自然年桶月K 基准 + 本年月K 突破
 */
@Slf4j
@Component
public class LongStrategy extends AbstractStrategy {

    @Override
    public StrategyTypeEnum getStrategy() {
        return StrategyTypeEnum.LONG;
    }

    @Override
    public PeriodTypeEnum getOpPeriodType() {
        return PeriodTypeEnum.MONTH;
    }

    @Override
    public List<PeriodTypeEnum> getTrendPeriodTypes() {
        return Arrays.asList(PeriodTypeEnum.YEAR, PeriodTypeEnum.MONTH);
    }

    @Override
    public CheckResult check(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes,
                             PeriodTypeEnum opPeriodType, QueryContextParam queryContextParam) {
        CheckResult checkResult = new CheckResult(stockBase.getCode(), stockBase.getChangeRate());
        try {
            LongStrategyParams params = resolveParams(queryContextParam);

            if (!LongFilterTools.passFilters(stockBase, checkResult, params)) {
                return checkResult;
            }

            LongEvaluator.LongEvaluation eval =
                    LongEvaluator.evaluate(stockBase, checkResult, params);
            if (!eval.isHit()) {
                return checkResult;
            }

            checkResult.setHasTrend(true);
            checkResult.setHasSignal(true);
            checkResult.setSortValue(eval.getScore());
            checkResult.setTrendPeriodType(PeriodTypeEnum.YEAR);
            checkResult.setOpPeriodType(PeriodTypeEnum.MONTH);

        } catch (Exception ex) {
            log.error("{} - check exception : stock = {}", getClass().getName(), stockBase.getCode(), ex);
        } finally {
            applyFallbackSortValue(stockBase, checkResult);
        }
        return checkResult;
    }

    private LongStrategyParams resolveParams(QueryContextParam queryContextParam) {
        if (queryContextParam == null || queryContextParam.getLongTerm() == null) {
            return LongStrategyParams.defaults();
        }
        return LongStrategyParams.merge(queryContextParam.getLongTerm());
    }

    private void applyFallbackSortValue(StockBase stockBase, CheckResult checkResult) {
        if (checkResult.getSortValue() > 0 || !checkResult.isSuccess()) {
            return;
        }
        try {
            Trade monthTrade = RealtimeStockCache.getLastTrade(stockBase, PeriodTypeEnum.MONTH, -1);
            if (monthTrade != null && monthTrade.getChangeRate() != null) {
                checkResult.setSortValue(monthTrade.getChangeRate());
            } else if (stockBase.getChangeRate() != null) {
                checkResult.setSortValue(stockBase.getChangeRate());
            }
        } catch (Exception e2) {
            log.error("setSortValue error...{}", stockBase.getCode(), e2);
        }
    }
}
