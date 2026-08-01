package com.yh.bigdata.tts.spider.strategy;



import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;

import com.yh.bigdata.tts.common.constants.RealtimeStockCache;

import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;

import com.yh.bigdata.tts.common.model.StockBase;

import com.yh.bigdata.tts.common.model.Trade;

import com.yh.bigdata.tts.common.param.BottomBandHighStrategyParams;

import com.yh.bigdata.tts.common.param.QueryContextParam;

import com.yh.bigdata.tts.spider.response.CheckResult;

import com.yh.bigdata.tts.spider.strategy.tools.bottombandhigh.BottomBandHighEvaluator;

import com.yh.bigdata.tts.spider.strategy.tools.bottombandhigh.BottomBandHighTools;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;



import java.util.Collections;

import java.util.List;



@Slf4j

@Component

public class BottomBandHighStrategy extends AbstractStrategy {



    @Override

    public StrategyTypeEnum getStrategy() {

        return StrategyTypeEnum.BOTTOM_BAND_HIGH;

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

            BottomBandHighStrategyParams params = resolveParams(queryContextParam);

            BottomBandHighEvaluator.BottomBandHighEvaluation eval =

                    BottomBandHighEvaluator.evaluate(stockBase, checkResult, params);

            if (!eval.isHit()) {

                return checkResult;

            }

            PeriodTypeEnum primary = eval.getPeriod() != null

                    ? eval.getPeriod()

                    : BottomBandHighTools.resolvePeriod(params.getTier());

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



    private BottomBandHighStrategyParams resolveParams(QueryContextParam queryContextParam) {

        if (queryContextParam == null || queryContextParam.getBottomBandHigh() == null) {

            return BottomBandHighStrategyParams.defaults();

        }

        return BottomBandHighStrategyParams.merge(queryContextParam.getBottomBandHigh());

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

