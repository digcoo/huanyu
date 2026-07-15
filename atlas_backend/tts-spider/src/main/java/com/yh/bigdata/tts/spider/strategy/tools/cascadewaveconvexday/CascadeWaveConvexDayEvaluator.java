package com.yh.bigdata.tts.spider.strategy.tools.cascadewaveconvexday;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.CascadeWaveConvexDayStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.cascadewave.CascadeWaveBreakoutTools;
import com.yh.bigdata.tts.spider.strategy.tools.cascadewave.CascadeWaveDayEvaluator;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeTools;
import lombok.Getter;

public final class CascadeWaveConvexDayEvaluator {

    private CascadeWaveConvexDayEvaluator() {
    }

    public static CascadeWaveConvexDayEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                          CascadeWaveConvexDayStrategyParams params) {
        CascadeWaveConvexDayStrategyParams p = params != null ? params : CascadeWaveConvexDayStrategyParams.defaults();
        CascadeWaveDayEvaluator.CascadeWaveDayEvaluation eval = CascadeWaveDayEvaluator.evaluate(
                stock, checkResult, p.isEnableAllYangGate(), p.isEnableMin30Gate(),
                p.isEnableBandLastYangLowGate(), p.isEnableYangBandTrendGate(), p.getMinAvgAmount(),
                p.getLookbackDay(), p.getLookbackWeek(), p.getLookbackMonth(),
                p.isEnableDay(), p.isEnableWeek(), p.isEnableMonth(),
                p.isEnablePrevBandBreak(), WaveShapeTools.BandShape.CONVEX);
        if (!eval.isHit()) {
            return CascadeWaveConvexDayEvaluation.miss();
        }
        CascadeWaveConvexDayEvaluation convexEval = new CascadeWaveConvexDayEvaluation(
                eval.getDayHit(), eval.getWeekHit(), eval.getMonthHit(), true);
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH,
                    CascadeWaveConvexDayScoreCalculator.buildTrendMessage(convexEval, p));
            appendTierSignals(checkResult, convexEval);
            convexEval.setScore(CascadeWaveConvexDayScoreCalculator.computeScore(convexEval));
        }
        return convexEval;
    }

    private static void appendTierSignals(CheckResult checkResult, CascadeWaveConvexDayEvaluation eval) {
        appendIfHit(checkResult, PeriodTypeEnum.MONTH, eval.getMonthHit());
        appendIfHit(checkResult, PeriodTypeEnum.WEEK, eval.getWeekHit());
        appendIfHit(checkResult, PeriodTypeEnum.DAY, eval.getDayHit());
    }

    private static void appendIfHit(CheckResult checkResult, PeriodTypeEnum period,
                                    CascadeWaveBreakoutTools.TierHit hit) {
        if (hit != null) {
            checkResult.addSignal(period, CascadeWaveConvexDayScoreCalculator.buildSignalMessage(hit));
        }
    }

    @Getter
    public static final class CascadeWaveConvexDayEvaluation {
        private final CascadeWaveBreakoutTools.TierHit dayHit;
        private final CascadeWaveBreakoutTools.TierHit weekHit;
        private final CascadeWaveBreakoutTools.TierHit monthHit;
        private final boolean hit;
        private int score;

        CascadeWaveConvexDayEvaluation(CascadeWaveBreakoutTools.TierHit dayHit,
                                       CascadeWaveBreakoutTools.TierHit weekHit,
                                       CascadeWaveBreakoutTools.TierHit monthHit,
                                       boolean hit) {
            this.dayHit = dayHit;
            this.weekHit = weekHit;
            this.monthHit = monthHit;
            this.hit = hit;
        }

        static CascadeWaveConvexDayEvaluation miss() {
            return new CascadeWaveConvexDayEvaluation(null, null, null, false);
        }

        void setScore(int score) {
            this.score = score;
        }

        public boolean isHit() {
            return hit;
        }
    }
}
