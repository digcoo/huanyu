package com.yh.bigdata.tts.spider.strategy;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.QueryContextParam;
import com.yh.bigdata.tts.common.param.ReboundStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.rebound.ReboundEvaluator;
import com.yh.bigdata.tts.spider.strategy.tools.rebound.ReboundGateTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 深坑反弹（default）· v1.0：Gate + 深坑 Context + 脱离/上移/底背离
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
        return Arrays.asList(PeriodTypeEnum.WEEK, PeriodTypeEnum.MONTH);
    }

    @Override
    public CheckResult check(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes,
                             PeriodTypeEnum opPeriodType, QueryContextParam queryContextParam) {
        CheckResult checkResult = new CheckResult(stockBase.getCode(), stockBase.getChangeRate());
        try {
            ReboundStrategyParams params = resolveParams(queryContextParam);
            if (!ReboundGateTools.passGate(stockBase, checkResult, params)) {
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
            checkResult.setTrendPeriodType(PeriodTypeEnum.WEEK);
            checkResult.setOpPeriodType(PeriodTypeEnum.DAY);

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
