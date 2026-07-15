package com.yh.bigdata.tts.spider.strategy.tools.cascadewaveconvex;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.CascadeWaveConvexStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.cascadewave.CascadeWaveBreakoutTools;
import com.yh.bigdata.tts.spider.strategy.tools.cascadewave.CascadeWaveEvaluator;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeTools;
import lombok.Getter;

public final class CascadeWaveConvexEvaluator {

    private CascadeWaveConvexEvaluator() {
    }

    public static CascadeWaveConvexEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                       CascadeWaveConvexStrategyParams params) {
        CascadeWaveConvexStrategyParams p = params != null ? params : CascadeWaveConvexStrategyParams.defaults();
        CascadeWaveEvaluator.CascadeWaveEvaluation eval = CascadeWaveEvaluator.evaluate(
                stock, checkResult, p.isEnableAllYangGate(), p.isEnableMin30Gate(),
                p.isEnableBandLastYangLowGate(), p.isEnableYangBandTrendGate(), p.getMinAvgAmount(),
                p.getLookbackDay(), p.getLookbackWeek(), p.getLookbackMonth(),
                p.isEnableDay(), p.isEnableWeek(), p.isEnableMonth(),
                p.isEnablePrevBandBreak(), WaveShapeTools.BandShape.CONVEX);
        if (!eval.isHit()) {
            return CascadeWaveConvexEvaluation.miss();
        }
        CascadeWaveConvexEvaluation convexEval = new CascadeWaveConvexEvaluation(
                eval.getDayHit(), eval.getWeekHit(), eval.getMonthHit(), true);
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH,
                    CascadeWaveConvexScoreCalculator.buildTrendMessage(convexEval, p));
            appendTierSignals(checkResult, convexEval);
            convexEval.setScore(CascadeWaveConvexScoreCalculator.computeScore(convexEval));
        }
        return convexEval;
    }

    private static void appendTierSignals(CheckResult checkResult, CascadeWaveConvexEvaluation eval) {
        appendIfHit(checkResult, PeriodTypeEnum.MONTH, eval.getMonthHit());
        appendIfHit(checkResult, PeriodTypeEnum.WEEK, eval.getWeekHit());
        appendIfHit(checkResult, PeriodTypeEnum.DAY, eval.getDayHit());
    }

    private static void appendIfHit(CheckResult checkResult, PeriodTypeEnum period,
                                    CascadeWaveBreakoutTools.TierHit hit) {
        if (hit != null) {
            checkResult.addSignal(period, CascadeWaveConvexScoreCalculator.buildSignalMessage(hit));
        }
    }

    @Getter
    public static final class CascadeWaveConvexEvaluation {
        private final CascadeWaveBreakoutTools.TierHit dayHit;
        private final CascadeWaveBreakoutTools.TierHit weekHit;
        private final CascadeWaveBreakoutTools.TierHit monthHit;
        private final boolean hit;
        private int score;

        CascadeWaveConvexEvaluation(CascadeWaveBreakoutTools.TierHit dayHit,
                                    CascadeWaveBreakoutTools.TierHit weekHit,
                                    CascadeWaveBreakoutTools.TierHit monthHit,
                                    boolean hit) {
            this.dayHit = dayHit;
            this.weekHit = weekHit;
            this.monthHit = monthHit;
            this.hit = hit;
        }

        static CascadeWaveConvexEvaluation miss() {
            return new CascadeWaveConvexEvaluation(null, null, null, false);
        }

        void setScore(int score) {
            this.score = score;
        }

        public boolean isHit() {
            return hit;
        }
    }
}
