package com.yh.bigdata.tts.spider.strategy.tools.trendrelaybandhigh;



import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;

import com.yh.bigdata.tts.common.model.StockBase;

import com.yh.bigdata.tts.common.param.TrendRelayBandHighStrategyParams;

import com.yh.bigdata.tts.spider.response.CheckResult;

import com.yh.bigdata.tts.spider.strategy.tools.DayLastBarYangGateTools;

import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;

import lombok.Getter;



public final class TrendRelayBandHighEvaluator {



    private TrendRelayBandHighEvaluator() {

    }



    public static TrendRelayBandHighEvaluation evaluate(StockBase stock, CheckResult checkResult,

                                                        TrendRelayBandHighStrategyParams params) {

        TrendRelayBandHighStrategyParams p = params != null ? params : TrendRelayBandHighStrategyParams.defaults();

        PeriodTypeEnum period = TrendRelayBandHighTools.resolvePeriod(p.getTier());

        if (!UnilateralMacdTools.isMacdPositive(stock, period)) {

            if (checkResult != null) {

                checkResult.addTrendPeriod(period, "[TRBH]" + periodLabel(period) + "MACD≤0");

            }

            return TrendRelayBandHighEvaluation.miss();

        }

        TrendRelayBandHighTools.Hit hit = TrendRelayBandHighTools.findHit(stock, p);

        if (hit == null) {

            if (checkResult != null) {

                checkResult.addTrendPeriod(period, "[TRBH]未满足未回踩+末K首次破bandHigh");

            }

            return TrendRelayBandHighEvaluation.miss();

        }

        if (!DayLastBarYangGateTools.passWithMessage(stock, checkResult)) {

            return TrendRelayBandHighEvaluation.miss();

        }

        if (!TrendRelayBandHighTools.passesOptionalGates(stock, checkResult, p)) {

            return TrendRelayBandHighEvaluation.miss();

        }

        if (checkResult != null) {

            checkResult.addTrendPeriod(period, TrendRelayBandHighTools.buildTrendMessage(hit));

            checkResult.addSignal(period, TrendRelayBandHighTools.buildSignalMessage(hit));

        }

        return TrendRelayBandHighEvaluation.hit(period);

    }



    private static String periodLabel(PeriodTypeEnum period) {

        if (period == PeriodTypeEnum.MONTH) {

            return "月";

        }

        if (period == PeriodTypeEnum.WEEK) {

            return "周";

        }

        return "日";

    }



    @Getter

    public static final class TrendRelayBandHighEvaluation {

        private final boolean hit;

        private final PeriodTypeEnum period;



        TrendRelayBandHighEvaluation(boolean hit, PeriodTypeEnum period) {

            this.hit = hit;

            this.period = period;

        }



        static TrendRelayBandHighEvaluation miss() {

            return new TrendRelayBandHighEvaluation(false, null);

        }



        static TrendRelayBandHighEvaluation hit(PeriodTypeEnum period) {

            return new TrendRelayBandHighEvaluation(true, period);

        }

    }

}

