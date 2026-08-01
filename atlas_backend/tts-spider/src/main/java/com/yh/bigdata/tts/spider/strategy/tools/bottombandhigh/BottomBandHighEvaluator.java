package com.yh.bigdata.tts.spider.strategy.tools.bottombandhigh;



import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;

import com.yh.bigdata.tts.common.model.StockBase;

import com.yh.bigdata.tts.common.param.BottomBandHighStrategyParams;

import com.yh.bigdata.tts.spider.response.CheckResult;

import com.yh.bigdata.tts.spider.strategy.tools.DayLastBarYangGateTools;

import com.yh.bigdata.tts.spider.strategy.tools.ParentPeriodMacdPositiveGateTools;

import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;

import lombok.Getter;



public final class BottomBandHighEvaluator {



    private BottomBandHighEvaluator() {

    }



    public static BottomBandHighEvaluation evaluate(StockBase stock, CheckResult checkResult,

                                                    BottomBandHighStrategyParams params) {

        BottomBandHighStrategyParams p = params != null ? params : BottomBandHighStrategyParams.defaults();

        PeriodTypeEnum period = BottomBandHighTools.resolvePeriod(p.getTier());

        if (!UnilateralMacdTools.isMacdNegative(stock, period)) {

            if (checkResult != null) {

                checkResult.addTrendPeriod(period, "[BBH]" + periodLabel(period) + "MACD≥0");

            }

            return BottomBandHighEvaluation.miss();

        }

        if (!ParentPeriodMacdPositiveGateTools.passWithMessage(stock, checkResult, period, "[BBH]")) {

            return BottomBandHighEvaluation.miss();

        }

        BottomBandHighTools.Hit hit = BottomBandHighTools.findHit(stock, p);

        if (hit == null) {

            if (checkResult != null) {

                checkResult.addTrendPeriod(period, "[BBH]未满足末K首次破bandHigh");

            }

            return BottomBandHighEvaluation.miss();

        }

        if (!DayLastBarYangGateTools.passWithMessage(stock, checkResult)) {

            return BottomBandHighEvaluation.miss();

        }

        if (!BottomBandHighTools.passesOptionalGates(stock, checkResult, p)) {

            return BottomBandHighEvaluation.miss();

        }

        if (checkResult != null) {

            checkResult.addTrendPeriod(period, BottomBandHighTools.buildTrendMessage(hit));

            checkResult.addSignal(period, BottomBandHighTools.buildSignalMessage(hit));

        }

        return BottomBandHighEvaluation.hit(period);

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

    public static final class BottomBandHighEvaluation {

        private final boolean hit;

        private final PeriodTypeEnum period;



        BottomBandHighEvaluation(boolean hit, PeriodTypeEnum period) {

            this.hit = hit;

            this.period = period;

        }



        static BottomBandHighEvaluation miss() {

            return new BottomBandHighEvaluation(false, null);

        }



        static BottomBandHighEvaluation hit(PeriodTypeEnum period) {

            return new BottomBandHighEvaluation(true, period);

        }

    }

}

