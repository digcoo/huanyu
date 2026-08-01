package com.yh.bigdata.tts.spider.strategy;



import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;

import com.yh.bigdata.tts.common.constants.RealtimeStockCache;

import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;

import com.yh.bigdata.tts.common.model.StockBase;

import com.yh.bigdata.tts.common.model.Trade;

import com.yh.bigdata.tts.common.param.BottomPrev2HighStrategyParams;

import com.yh.bigdata.tts.common.param.QueryContextParam;

import com.yh.bigdata.tts.spider.response.CheckResult;

import com.yh.bigdata.tts.spider.strategy.tools.bottomprev2high.BottomPrev2HighEvaluator;

import com.yh.bigdata.tts.spider.strategy.tools.bottomprev2high.BottomPrev2HighTools;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;



import java.util.Collections;

import java.util.List;



@Slf4j

@Component

public class BottomPrev2HighStrategy extends AbstractStrategy {



    @Override

    public StrategyTypeEnum getStrategy() {

        return StrategyTypeEnum.BOTTOM_PREV2_HIGH;

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

            BottomPrev2HighStrategyParams params = resolveParams(queryContextParam);

            BottomPrev2HighEvaluator.BottomPrev2HighEvaluation eval =

                    BottomPrev2HighEvaluator.evaluate(stockBase, checkResult, params);

            if (!eval.isHit()) {

                return checkResult;

            }

            PeriodTypeEnum primary = eval.getPeriod() != null

                    ? eval.getPeriod()

                    : BottomPrev2HighTools.resolvePeriod(params.getTier());

            checkResult.setHasTrend(true);

            checkResult.setHasSignal(true);

            checkResult.setSortValue(50);

            checkResult.setTrendPeriodType(primary);

            checkResult.setOpPeriodType(primary);

        } catch (Exception ex) {

            log.error("{} - check exception : stock = {}", getClass().getName(), stockBase.getCode(), ex);

        } finally {

            applyFallbackSortValue(stockBase, checkResult);

        }

        return checkResult;

    }



    private BottomPrev2HighStrategyParams resolveParams(QueryContextParam queryContextParam) {

        if (queryContextParam == null || queryContextParam.getBottomPrev2High() == null) {

            return BottomPrev2HighStrategyParams.defaults();

        }

        return BottomPrev2HighStrategyParams.merge(queryContextParam.getBottomPrev2High());

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

