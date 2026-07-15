package com.yh.bigdata.tts.spider.strategy.tools.cascadewaveconcave;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.CascadeWaveConcaveStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.cascadewave.CascadeWaveBreakoutTools;
import com.yh.bigdata.tts.spider.strategy.tools.cascadewave.CascadeWaveEvaluator;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeTools;
import lombok.Getter;

public final class CascadeWaveConcaveEvaluator {

    private CascadeWaveConcaveEvaluator() {
    }

    public static CascadeWaveConcaveEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                        CascadeWaveConcaveStrategyParams params) {
        CascadeWaveConcaveStrategyParams p = params != null ? params : CascadeWaveConcaveStrategyParams.defaults();
        CascadeWaveEvaluator.CascadeWaveEvaluation eval = CascadeWaveEvaluator.evaluate(
                stock, checkResult, p.isEnableAllYangGate(), p.isEnableMin30Gate(),
                p.isEnableBandLastYangLowGate(), p.isEnableYangBandTrendGate(), p.getMinAvgAmount(),
                p.getLookbackDay(), p.getLookbackWeek(), p.getLookbackMonth(),
                p.isEnableDay(), p.isEnableWeek(), p.isEnableMonth(),
                p.isEnablePrevBandBreak(), WaveShapeTools.BandShape.CONCAVE);
        if (!eval.isHit()) {
            return CascadeWaveConcaveEvaluation.miss();
        }
        CascadeWaveConcaveEvaluation concaveEval = new CascadeWaveConcaveEvaluation(
                eval.getDayHit(), eval.getWeekHit(), eval.getMonthHit(), true);
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH,
                    CascadeWaveConcaveScoreCalculator.buildTrendMessage(concaveEval, p));
            appendTierSignals(checkResult, concaveEval);
            concaveEval.setScore(CascadeWaveConcaveScoreCalculator.computeScore(concaveEval));
        }
        return concaveEval;
    }

    private static void appendTierSignals(CheckResult checkResult, CascadeWaveConcaveEvaluation eval) {
        appendIfHit(checkResult, PeriodTypeEnum.MONTH, eval.getMonthHit());
        appendIfHit(checkResult, PeriodTypeEnum.WEEK, eval.getWeekHit());
        appendIfHit(checkResult, PeriodTypeEnum.DAY, eval.getDayHit());
    }

    private static void appendIfHit(CheckResult checkResult, PeriodTypeEnum period,
                                    CascadeWaveBreakoutTools.TierHit hit) {
        if (hit != null) {
            checkResult.addSignal(period, CascadeWaveConcaveScoreCalculator.buildSignalMessage(hit));
        }
    }

    @Getter
    public static final class CascadeWaveConcaveEvaluation {
        private final CascadeWaveBreakoutTools.TierHit dayHit;
        private final CascadeWaveBreakoutTools.TierHit weekHit;
        private final CascadeWaveBreakoutTools.TierHit monthHit;
        private final boolean hit;
        private int score;

        CascadeWaveConcaveEvaluation(CascadeWaveBreakoutTools.TierHit dayHit,
                                       CascadeWaveBreakoutTools.TierHit weekHit,
                                       CascadeWaveBreakoutTools.TierHit monthHit,
                                       boolean hit) {
            this.dayHit = dayHit;
            this.weekHit = weekHit;
            this.monthHit = monthHit;
            this.hit = hit;
        }

        static CascadeWaveConcaveEvaluation miss() {
            return new CascadeWaveConcaveEvaluation(null, null, null, false);
        }

        void setScore(int score) {
            this.score = score;
        }

        public boolean isHit() {
            return hit;
        }
    }
}
