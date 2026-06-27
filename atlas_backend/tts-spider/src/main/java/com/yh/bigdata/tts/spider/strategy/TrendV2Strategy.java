package com.yh.bigdata.tts.spider.strategy;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.QueryContextParam;
import com.yh.bigdata.tts.common.param.TrendV2StrategyParams;
import com.yh.bigdata.tts.common.param.UltraShortStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.BarHighLadderGateTools;
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.MacdCrossLowGateTools;
import com.yh.bigdata.tts.spider.strategy.tools.trend.TrendV2Evaluator;
import com.yh.bigdata.tts.spider.strategy.tools.trend.TrendV2FilterTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 短线策略（trend · v3）· 自然周桶日K 基准 + 本周日K 突破
 */
@Slf4j
@Component
public class TrendV2Strategy extends AbstractStrategy {

    @Override
    public StrategyTypeEnum getStrategy() {
        return StrategyTypeEnum.TREND_V2;
    }

    @Override
    public PeriodTypeEnum getOpPeriodType() {
        return PeriodTypeEnum.DAY;
    }

    @Override
    public List<PeriodTypeEnum> getTrendPeriodTypes() {
        return Arrays.asList(PeriodTypeEnum.MONTH, PeriodTypeEnum.WEEK, PeriodTypeEnum.DAY);
    }

    @Override
    public CheckResult check(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes,
                             PeriodTypeEnum opPeriodType, QueryContextParam queryContextParam) {
        CheckResult checkResult = new CheckResult(stockBase.getCode(), stockBase.getChangeRate());
        try {
            if (!MacdCrossLowGateTools.passesAll(stockBase, checkResult)) {
                return checkResult;
            }
            if (!BarHighLadderGateTools.passesAll(stockBase, checkResult)) {
                return checkResult;
            }

            TrendV2StrategyParams params = resolveParams(queryContextParam);

            if (!TrendV2FilterTools.passFilters(stockBase, checkResult, params)) {
                return checkResult;
            }

            TrendV2Evaluator.TrendV2Evaluation eval =
                    TrendV2Evaluator.evaluate(stockBase, checkResult, params, resolveUltraParams(queryContextParam));
            if (!eval.isHit()) {
                return checkResult;
            }

            checkResult.setHasTrend(true);
            checkResult.setHasSignal(true);
            checkResult.setSortValue(eval.getScore());
            checkResult.setTrendPeriodType(PeriodTypeEnum.WEEK);
            checkResult.setOpPeriodType(PeriodTypeEnum.DAY);

        } catch (Exception ex) {
            log.error("{} - check exception : stock = {}", getClass().getName(), stockBase.getCode(), ex);
        } finally {
            applyFallbackSortValue(stockBase, checkResult);
        }
        return checkResult;
    }

    private TrendV2StrategyParams resolveParams(QueryContextParam queryContextParam) {
        if (queryContextParam == null || queryContextParam.getTrendV2() == null) {
            return TrendV2StrategyParams.defaults();
        }
        return TrendV2StrategyParams.merge(queryContextParam.getTrendV2());
    }

    private UltraShortStrategyParams resolveUltraParams(QueryContextParam queryContextParam) {
        if (queryContextParam == null || queryContextParam.getUltraShort() == null) {
            return UltraShortStrategyParams.defaults();
        }
        return UltraShortStrategyParams.merge(queryContextParam.getUltraShort());
    }

    private void applyFallbackSortValue(StockBase stockBase, CheckResult checkResult) {
        if (checkResult.getSortValue() > 0 || !checkResult.isSuccess()) {
            return;
        }
        try {
            Trade dayTrade = RealtimeStockCache.getLastTrade(stockBase, PeriodTypeEnum.DAY, -1);
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
