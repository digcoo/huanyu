package com.yh.bigdata.tts.spider.strategy.tools.cascadewaveconcaveday;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.CascadeWaveConcaveDayStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.cascadewave.CascadeWaveBreakoutTools;
import com.yh.bigdata.tts.spider.strategy.tools.cascadewave.CascadeWaveDayEvaluator;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeTools;
import lombok.Getter;

public final class CascadeWaveConcaveDayEvaluator {

    private CascadeWaveConcaveDayEvaluator() {
    }

    public static CascadeWaveConcaveDayEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                           CascadeWaveConcaveDayStrategyParams params) {
        CascadeWaveConcaveDayStrategyParams p = params != null ? params : CascadeWaveConcaveDayStrategyParams.defaults();
        CascadeWaveDayEvaluator.CascadeWaveDayEvaluation eval = CascadeWaveDayEvaluator.evaluate(
                stock, checkResult, p.isEnableAllYangGate(), p.isEnableMin30Gate(),
                p.isEnableBandLastYangLowGate(), p.isEnableYangBandTrendGate(), p.getMinAvgAmount(),
                p.getLookbackDay(), p.getLookbackWeek(), p.getLookbackMonth(),
                p.isEnableDay(), p.isEnableWeek(), p.isEnableMonth(),
                p.isEnablePrevBandBreak(), WaveShapeTools.BandShape.CONCAVE);
        if (!eval.isHit()) {
            return CascadeWaveConcaveDayEvaluation.miss();
        }
        CascadeWaveConcaveDayEvaluation concaveEval = new CascadeWaveConcaveDayEvaluation(
                eval.getDayHit(), eval.getWeekHit(), eval.getMonthHit(), true);
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH,
                    CascadeWaveConcaveDayScoreCalculator.buildTrendMessage(concaveEval, p));
            appendTierSignals(checkResult, concaveEval);
            concaveEval.setScore(CascadeWaveConcaveDayScoreCalculator.computeScore(concaveEval));
        }
        return concaveEval;
    }

    private static void appendTierSignals(CheckResult checkResult, CascadeWaveConcaveDayEvaluation eval) {
        appendIfHit(checkResult, PeriodTypeEnum.MONTH, eval.getMonthHit());
        appendIfHit(checkResult, PeriodTypeEnum.WEEK, eval.getWeekHit());
        appendIfHit(checkResult, PeriodTypeEnum.DAY, eval.getDayHit());
    }

    private static void appendIfHit(CheckResult checkResult, PeriodTypeEnum period,
                                    CascadeWaveBreakoutTools.TierHit hit) {
        if (hit != null) {
            checkResult.addSignal(period, CascadeWaveConcaveDayScoreCalculator.buildSignalMessage(hit));
        }
    }

    @Getter
    public static final class CascadeWaveConcaveDayEvaluation {
        private final CascadeWaveBreakoutTools.TierHit dayHit;
        private final CascadeWaveBreakoutTools.TierHit weekHit;
        private final CascadeWaveBreakoutTools.TierHit monthHit;
        private final boolean hit;
        private int score;

        CascadeWaveConcaveDayEvaluation(CascadeWaveBreakoutTools.TierHit dayHit,
                                        CascadeWaveBreakoutTools.TierHit weekHit,
                                        CascadeWaveBreakoutTools.TierHit monthHit,
                                        boolean hit) {
            this.dayHit = dayHit;
            this.weekHit = weekHit;
            this.monthHit = monthHit;
            this.hit = hit;
        }

        static CascadeWaveConcaveDayEvaluation miss() {
            return new CascadeWaveConcaveDayEvaluation(null, null, null, false);
        }

        void setScore(int score) {
            this.score = score;
        }

        public boolean isHit() {
            return hit;
        }
    }
}
