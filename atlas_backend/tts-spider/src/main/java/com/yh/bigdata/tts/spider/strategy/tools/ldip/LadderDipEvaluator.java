package com.yh.bigdata.tts.spider.strategy.tools.ldip;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.LadderDipStrategyParams;
import com.yh.bigdata.tts.common.param.UltraShortStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.UltraShortGateTools;
import lombok.Getter;

public final class LadderDipEvaluator {

    private LadderDipEvaluator() {
    }

    public static LadderDipEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                 LadderDipStrategyParams params,
                                                 UltraShortStrategyParams ultraParams) {
        LadderDipStrategyParams p = params != null ? params : LadderDipStrategyParams.defaults();

        if (!LadderDipOptionalGateTools.passGate(stock, checkResult, p)) {
            return LadderDipEvaluation.miss();
        }
        if (!LadderDipFilterTools.passFilters(stock, checkResult, p)) {
            return LadderDipEvaluation.miss();
        }

        LadderDipBreakoutTools.PeriodHit dayHit = null;
        LadderDipBreakoutTools.PeriodHit weekHit = null;
        LadderDipBreakoutTools.PeriodHit monthHit = null;

        LadderDipBreakoutTools.PeriodHit dayHitQualified =
                LadderDipBreakoutTools.findDayHit(stock, p.getLookbackDay());
        if (dayHitQualified != null
                && (!LadderDipBreakoutTools.passesHitCloseAboveRefLow(dayHitQualified)
                || !LadderDipBreakoutTools.passesDayCrossWeekConfirm(stock, p.getLookbackWeek())
                || !LadderDipBreakoutTools.passesDayCloseCrossPrevDayHigh(stock)
                || !LadderDipBreakoutTools.passesSecondDayCloseBelowRefHigh(
                        stock, dayHitQualified.getReferenceBar()))) {
            dayHitQualified = null;
        }

        if (p.isEnableDay()) {
            dayHit = dayHitQualified;
        }
        if (p.isEnableWeek()) {
            weekHit = LadderDipBreakoutTools.findWeekHit(stock, p.getLookbackWeek());
            if (weekHit != null
                    && (!LadderDipBreakoutTools.passesHitCloseAboveRefLow(weekHit)
                    || !LadderDipBreakoutTools.passesDayLowConfirm(stock, p.getLookbackDay())
                    || !LadderDipBreakoutTools.passesWeekCrossMonthConfirm(stock, p.getLookbackMonth())
                    || !LadderDipBreakoutTools.passesDayCloseEdgeCrossPrevWeekHigh(stock, p.getLookbackWeek())
                    || !LadderDipBreakoutTools.passesSecondDayCloseBelowRefHigh(
                            stock, weekHit.getReferenceBar()))) {
                weekHit = null;
            }
        }
        if (p.isEnableMonth()) {
            monthHit = LadderDipBreakoutTools.findMonthHit(stock, p.getLookbackMonth());
            if (monthHit != null
                    && (!LadderDipBreakoutTools.passesHitCloseAboveRefLow(monthHit)
                    || !LadderDipBreakoutTools.passesDayLowConfirm(stock, p.getLookbackDay())
                    || !LadderDipBreakoutTools.passesDayCloseEdgeCrossPrevMonthHigh(stock, p.getLookbackMonth())
                    || !LadderDipBreakoutTools.passesSecondDayCloseBelowRefHigh(
                            stock, monthHit.getReferenceBar()))) {
                monthHit = null;
            }
        }
        if (dayHit == null && weekHit == null && monthHit == null) {
            return LadderDipEvaluation.miss();
        }
        return finish(stock, checkResult, p, ultraParams, dayHit, weekHit, monthHit);
    }

    private static LadderDipEvaluation finish(StockBase stock, CheckResult checkResult, LadderDipStrategyParams p,
                                              UltraShortStrategyParams ultraParams,
                                              LadderDipBreakoutTools.PeriodHit dayHit,
                                              LadderDipBreakoutTools.PeriodHit weekHit,
                                              LadderDipBreakoutTools.PeriodHit monthHit) {
        UltraShortGateTools.GateResult ultraGate =
                UltraShortGateTools.evaluate(stock, p.isRequireUltra(), ultraParams);
        if (!ultraGate.isPassed()) {
            return LadderDipEvaluation.miss();
        }

        LadderDipEvaluation eval = new LadderDipEvaluation(dayHit, weekHit, monthHit, true);
        if (checkResult != null) {
            if (ultraGate.isRequired()) {
                UltraShortGateTools.appendMessages(checkResult, ultraGate);
            }
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH,
                    LadderDipScoreCalculator.buildTrendMessage(eval, p));
            appendTierSignals(checkResult, eval);
            eval.setScore(LadderDipScoreCalculator.computeScore(eval));
        }
        return eval;
    }

    private static void appendTierSignals(CheckResult checkResult, LadderDipEvaluation eval) {
        if (eval.getDayHit() != null) {
            checkResult.addSignal(PeriodTypeEnum.DAY,
                    LadderDipScoreCalculator.buildSignalMessage(eval.getDayHit()));
        }
        if (eval.getWeekHit() != null) {
            checkResult.addSignal(PeriodTypeEnum.WEEK,
                    LadderDipScoreCalculator.buildSignalMessage(eval.getWeekHit()));
        }
        if (eval.getMonthHit() != null) {
            checkResult.addSignal(PeriodTypeEnum.MONTH,
                    LadderDipScoreCalculator.buildSignalMessage(eval.getMonthHit()));
        }
    }

    @Getter
    public static final class LadderDipEvaluation {
        private final LadderDipBreakoutTools.PeriodHit dayHit;
        private final LadderDipBreakoutTools.PeriodHit weekHit;
        private final LadderDipBreakoutTools.PeriodHit monthHit;
        private final boolean hit;
        private int score;

        LadderDipEvaluation(LadderDipBreakoutTools.PeriodHit dayHit,
                            LadderDipBreakoutTools.PeriodHit weekHit,
                            LadderDipBreakoutTools.PeriodHit monthHit,
                            boolean hit) {
            this.dayHit = dayHit;
            this.weekHit = weekHit;
            this.monthHit = monthHit;
            this.hit = hit;
        }

        static LadderDipEvaluation miss() {
            return new LadderDipEvaluation(null, null, null, false);
        }

        void setScore(int score) {
            this.score = score;
        }

        public boolean isHit() {
            return hit;
        }
    }
}
