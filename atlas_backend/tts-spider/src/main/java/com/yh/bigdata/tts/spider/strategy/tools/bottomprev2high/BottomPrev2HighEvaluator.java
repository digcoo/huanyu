package com.yh.bigdata.tts.spider.strategy.tools.bottomprev2high;



import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;

import com.yh.bigdata.tts.common.model.StockBase;

import com.yh.bigdata.tts.common.param.BottomPrev2HighStrategyParams;

import com.yh.bigdata.tts.spider.response.CheckResult;

import com.yh.bigdata.tts.spider.strategy.tools.DayLastBarYangGateTools;

import com.yh.bigdata.tts.spider.strategy.tools.ParentPeriodMacdPositiveGateTools;

import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;

import lombok.Getter;



public final class BottomPrev2HighEvaluator {



    private BottomPrev2HighEvaluator() {

    }



    public static BottomPrev2HighEvaluation evaluate(StockBase stock, CheckResult checkResult,

                                                     BottomPrev2HighStrategyParams params) {

        BottomPrev2HighStrategyParams p = params != null ? params : BottomPrev2HighStrategyParams.defaults();

        PeriodTypeEnum period = BottomPrev2HighTools.resolvePeriod(p.getTier());

        if (!UnilateralMacdTools.isMacdNegative(stock, period)) {

            if (checkResult != null) {

                checkResult.addTrendPeriod(period, "[BP2H]" + periodLabel(period) + "MACD≥0");

            }

            return BottomPrev2HighEvaluation.miss();

        }

        if (!ParentPeriodMacdPositiveGateTools.passWithMessage(stock, checkResult, period, "[BP2H]")) {

            return BottomPrev2HighEvaluation.miss();

        }

        BottomPrev2HighTools.Hit hit = BottomPrev2HighTools.findHit(stock, p);

        if (hit == null) {

            if (checkResult != null) {

                checkResult.addTrendPeriod(period, "[BP2H]未满足末K破前2K max(high)");

            }

            return BottomPrev2HighEvaluation.miss();

        }

        if (!DayLastBarYangGateTools.passWithMessage(stock, checkResult)) {

            return BottomPrev2HighEvaluation.miss();

        }

        if (!BottomPrev2HighTools.passesOptionalGates(stock, checkResult, p)) {

            return BottomPrev2HighEvaluation.miss();

        }

        if (checkResult != null) {

            checkResult.addTrendPeriod(period, BottomPrev2HighTools.buildTrendMessage(hit));

            checkResult.addSignal(period, BottomPrev2HighTools.buildSignalMessage(hit));

        }

        return BottomPrev2HighEvaluation.hit(period);

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

    public static final class BottomPrev2HighEvaluation {

        private final boolean hit;

        private final PeriodTypeEnum period;



        BottomPrev2HighEvaluation(boolean hit, PeriodTypeEnum period) {

            this.hit = hit;

            this.period = period;

        }



        static BottomPrev2HighEvaluation miss() {

            return new BottomPrev2HighEvaluation(false, null);

        }



        static BottomPrev2HighEvaluation hit(PeriodTypeEnum period) {

            return new BottomPrev2HighEvaluation(true, period);

        }

    }

}

