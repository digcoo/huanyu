package com.yh.bigdata.tts.spider.strategy.tools.waveconvexday;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.WaveConvexDayStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeEvaluator;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeTools;
import lombok.Getter;

public final class WaveConvexDayEvaluator {

    private WaveConvexDayEvaluator() {
    }

    public static WaveConvexDayEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                   WaveConvexDayStrategyParams params) {
        WaveConvexDayStrategyParams p = params != null ? params : WaveConvexDayStrategyParams.defaults();
        WaveShapeEvaluator.WaveShapeEvaluation eval = WaveShapeEvaluator.evaluate(
                stock, checkResult, p.isEnableAllYangGate(), p.isEnableMin30Gate(), p.getMinAvgAmount(),
                p.getLookbackDay(), p.getLookbackWeek(), p.getLookbackMonth(),
                p.isEnableDay(), p.isEnableWeek(), p.isEnableMonth(),
                WaveShapeTools.BandShape.CONVEX,
                WaveShapeTools.BreakLineConfig.of(
                        p.isEnableLastHighBreak(), p.isEnableLastMedianBreak(), p.isEnableLastLowBreak()),
                WaveShapeTools.EdgeMode.DAY);
        if (!eval.isHit()) {
            return WaveConvexDayEvaluation.miss();
        }
        WaveConvexDayEvaluation convexEval = new WaveConvexDayEvaluation(
                eval.getDayHit(), eval.getWeekHit(), eval.getMonthHit(), true);
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH,
                    WaveConvexDayScoreCalculator.buildTrendMessage(convexEval, p));
            appendTierSignals(checkResult, convexEval);
            convexEval.setScore(WaveConvexDayScoreCalculator.computeScore(convexEval));
        }
        return convexEval;
    }

    private static void appendTierSignals(CheckResult checkResult, WaveConvexDayEvaluation eval) {
        appendIfHit(checkResult, PeriodTypeEnum.MONTH, eval.getMonthHit());
        appendIfHit(checkResult, PeriodTypeEnum.WEEK, eval.getWeekHit());
        appendIfHit(checkResult, PeriodTypeEnum.DAY, eval.getDayHit());
    }

    private static void appendIfHit(CheckResult checkResult, PeriodTypeEnum period,
                                    WaveShapeTools.TierHit hit) {
        if (hit != null) {
            checkResult.addSignal(period, WaveConvexDayScoreCalculator.buildSignalMessage(hit));
        }
    }

    @Getter
    public static final class WaveConvexDayEvaluation {
        private final WaveShapeTools.TierHit dayHit;
        private final WaveShapeTools.TierHit weekHit;
        private final WaveShapeTools.TierHit monthHit;
        private final boolean hit;
        private int score;

        WaveConvexDayEvaluation(WaveShapeTools.TierHit dayHit,
                                WaveShapeTools.TierHit weekHit,
                                WaveShapeTools.TierHit monthHit,
                                boolean hit) {
            this.dayHit = dayHit;
            this.weekHit = weekHit;
            this.monthHit = monthHit;
            this.hit = hit;
        }

        static WaveConvexDayEvaluation miss() {
            return new WaveConvexDayEvaluation(null, null, null, false);
        }

        void setScore(int score) {
            this.score = score;
        }

        public boolean isHit() {
            return hit;
        }
    }
}
