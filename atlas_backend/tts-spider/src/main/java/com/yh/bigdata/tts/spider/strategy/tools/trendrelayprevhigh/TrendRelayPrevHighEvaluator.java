package com.yh.bigdata.tts.spider.strategy.tools.trendrelayprevhigh;



import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;

import com.yh.bigdata.tts.common.model.StockBase;

import com.yh.bigdata.tts.common.param.TrendRelayPrevHighStrategyParams;

import com.yh.bigdata.tts.spider.response.CheckResult;

import com.yh.bigdata.tts.spider.strategy.tools.DayLastBarYangGateTools;

import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;

import lombok.Getter;



public final class TrendRelayPrevHighEvaluator {



    private TrendRelayPrevHighEvaluator() {

    }



    public static TrendRelayPrevHighEvaluation evaluate(StockBase stock, CheckResult checkResult,

                                                        TrendRelayPrevHighStrategyParams params) {

        TrendRelayPrevHighStrategyParams p = params != null ? params : TrendRelayPrevHighStrategyParams.defaults();

        PeriodTypeEnum period = TrendRelayPrevHighTools.resolvePeriod(p.getTier());

        if (!UnilateralMacdTools.isMacdPositive(stock, period)) {

            if (checkResult != null) {

                checkResult.addTrendPeriod(period, "[TRPH]" + periodLabel(period) + "MACD≤0");

            }

            return TrendRelayPrevHighEvaluation.miss();

        }

        TrendRelayPrevHighTools.Hit hit = TrendRelayPrevHighTools.findHit(stock, p);

        if (hit == null) {

            if (checkResult != null) {

                checkResult.addTrendPeriod(period, "[TRPH]未满足未回踩+末K阳+破前K high+close≤High");

            }

            return TrendRelayPrevHighEvaluation.miss();

        }

        if (!DayLastBarYangGateTools.passWithMessage(stock, checkResult)) {

            return TrendRelayPrevHighEvaluation.miss();

        }

        if (!TrendRelayPrevHighTools.passesOptionalGates(stock, checkResult, p)) {

            return TrendRelayPrevHighEvaluation.miss();

        }

        if (checkResult != null) {

            checkResult.addTrendPeriod(period, TrendRelayPrevHighTools.buildTrendMessage(hit));

            checkResult.addSignal(period, TrendRelayPrevHighTools.buildSignalMessage(hit));

        }

        return TrendRelayPrevHighEvaluation.hit(period);

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

    public static final class TrendRelayPrevHighEvaluation {

        private final boolean hit;

        private final PeriodTypeEnum period;



        TrendRelayPrevHighEvaluation(boolean hit, PeriodTypeEnum period) {

            this.hit = hit;

            this.period = period;

        }



        static TrendRelayPrevHighEvaluation miss() {

            return new TrendRelayPrevHighEvaluation(false, null);

        }



        static TrendRelayPrevHighEvaluation hit(PeriodTypeEnum period) {

            return new TrendRelayPrevHighEvaluation(true, period);

        }

    }

}

