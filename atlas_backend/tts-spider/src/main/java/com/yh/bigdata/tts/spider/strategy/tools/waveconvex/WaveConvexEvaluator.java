package com.yh.bigdata.tts.spider.strategy.tools.waveconvex;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.WaveConvexStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeEvaluator;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeTools;
import lombok.Getter;

public final class WaveConvexEvaluator {

    private WaveConvexEvaluator() {
    }

    public static WaveConvexEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                WaveConvexStrategyParams params) {
        WaveConvexStrategyParams p = params != null ? params : WaveConvexStrategyParams.defaults();
        WaveShapeEvaluator.WaveShapeEvaluation eval = WaveShapeEvaluator.evaluate(
                stock, checkResult, p.isEnableAllYangGate(), p.isEnableMin30Gate(), p.getMinAvgAmount(),
                p.getLookbackDay(), p.getLookbackWeek(), p.getLookbackMonth(),
                p.isEnableDay(), p.isEnableWeek(), p.isEnableMonth(),
                WaveShapeTools.BandShape.CONVEX,
                WaveShapeTools.BreakLineConfig.of(
                        p.isEnableLastHighBreak(), p.isEnableLastMedianBreak(), p.isEnableLastLowBreak()));
        if (!eval.isHit()) {
            return WaveConvexEvaluation.miss();
        }
        WaveConvexEvaluation convexEval = new WaveConvexEvaluation(
                eval.getDayHit(), eval.getWeekHit(), eval.getMonthHit(), true);
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH,
                    WaveConvexScoreCalculator.buildTrendMessage(convexEval, p));
            appendTierSignals(checkResult, convexEval);
            convexEval.setScore(WaveConvexScoreCalculator.computeScore(convexEval));
        }
        return convexEval;
    }

    private static void appendTierSignals(CheckResult checkResult, WaveConvexEvaluation eval) {
        appendIfHit(checkResult, PeriodTypeEnum.MONTH, eval.getMonthHit());
        appendIfHit(checkResult, PeriodTypeEnum.WEEK, eval.getWeekHit());
        appendIfHit(checkResult, PeriodTypeEnum.DAY, eval.getDayHit());
    }

    private static void appendIfHit(CheckResult checkResult, PeriodTypeEnum period,
                                    WaveShapeTools.TierHit hit) {
        if (hit != null) {
            checkResult.addSignal(period, WaveConvexScoreCalculator.buildSignalMessage(hit));
        }
    }

    @Getter
    public static final class WaveConvexEvaluation {
        private final WaveShapeTools.TierHit dayHit;
        private final WaveShapeTools.TierHit weekHit;
        private final WaveShapeTools.TierHit monthHit;
        private final boolean hit;
        private int score;

        WaveConvexEvaluation(WaveShapeTools.TierHit dayHit,
                             WaveShapeTools.TierHit weekHit,
                             WaveShapeTools.TierHit monthHit,
                             boolean hit) {
            this.dayHit = dayHit;
            this.weekHit = weekHit;
            this.monthHit = monthHit;
            this.hit = hit;
        }

        static WaveConvexEvaluation miss() {
            return new WaveConvexEvaluation(null, null, null, false);
        }

        void setScore(int score) {
            this.score = score;
        }

        public boolean isHit() {
            return hit;
        }
    }
}
