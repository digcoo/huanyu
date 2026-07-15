package com.yh.bigdata.tts.spider.strategy.tools.waveconcave;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.WaveConcaveStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeEvaluator;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeTools;
import lombok.Getter;

public final class WaveConcaveEvaluator {

    private WaveConcaveEvaluator() {
    }

    public static WaveConcaveEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                 WaveConcaveStrategyParams params) {
        WaveConcaveStrategyParams p = params != null ? params : WaveConcaveStrategyParams.defaults();
        WaveShapeEvaluator.WaveShapeEvaluation eval = WaveShapeEvaluator.evaluate(
                stock, checkResult, p.isEnableAllYangGate(), p.isEnableMin30Gate(), p.getMinAvgAmount(),
                p.getLookbackDay(), p.getLookbackWeek(), p.getLookbackMonth(),
                p.isEnableDay(), p.isEnableWeek(), p.isEnableMonth(),
                WaveShapeTools.BandShape.CONCAVE,
                WaveShapeTools.BreakLineConfig.of(
                        p.isEnableLastHighBreak(), p.isEnableLastMedianBreak(), p.isEnableLastLowBreak()));
        if (!eval.isHit()) {
            return WaveConcaveEvaluation.miss();
        }
        WaveConcaveEvaluation concaveEval = new WaveConcaveEvaluation(
                eval.getDayHit(), eval.getWeekHit(), eval.getMonthHit(), true);
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH,
                    WaveConcaveScoreCalculator.buildTrendMessage(concaveEval, p));
            appendTierSignals(checkResult, concaveEval);
            concaveEval.setScore(WaveConcaveScoreCalculator.computeScore(concaveEval));
        }
        return concaveEval;
    }

    private static void appendTierSignals(CheckResult checkResult, WaveConcaveEvaluation eval) {
        appendIfHit(checkResult, PeriodTypeEnum.MONTH, eval.getMonthHit());
        appendIfHit(checkResult, PeriodTypeEnum.WEEK, eval.getWeekHit());
        appendIfHit(checkResult, PeriodTypeEnum.DAY, eval.getDayHit());
    }

    private static void appendIfHit(CheckResult checkResult, PeriodTypeEnum period,
                                    WaveShapeTools.TierHit hit) {
        if (hit != null) {
            checkResult.addSignal(period, WaveConcaveScoreCalculator.buildSignalMessage(hit));
        }
    }

    @Getter
    public static final class WaveConcaveEvaluation {
        private final WaveShapeTools.TierHit dayHit;
        private final WaveShapeTools.TierHit weekHit;
        private final WaveShapeTools.TierHit monthHit;
        private final boolean hit;
        private int score;

        WaveConcaveEvaluation(WaveShapeTools.TierHit dayHit,
                              WaveShapeTools.TierHit weekHit,
                              WaveShapeTools.TierHit monthHit,
                              boolean hit) {
            this.dayHit = dayHit;
            this.weekHit = weekHit;
            this.monthHit = monthHit;
            this.hit = hit;
        }

        static WaveConcaveEvaluation miss() {
            return new WaveConcaveEvaluation(null, null, null, false);
        }

        void setScore(int score) {
            this.score = score;
        }

        public boolean isHit() {
            return hit;
        }
    }
}
