package com.yh.bigdata.tts.spider.strategy.tools.waveconcaveday;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.WaveConcaveDayStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeEvaluator;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeTools;
import lombok.Getter;

public final class WaveConcaveDayEvaluator {

    private WaveConcaveDayEvaluator() {
    }

    public static WaveConcaveDayEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                  WaveConcaveDayStrategyParams params) {
        WaveConcaveDayStrategyParams p = params != null ? params : WaveConcaveDayStrategyParams.defaults();
        WaveShapeEvaluator.WaveShapeEvaluation eval = WaveShapeEvaluator.evaluate(
                stock, checkResult, p.isEnableAllYangGate(), p.isEnableMin30Gate(), p.getMinAvgAmount(),
                p.getLookbackDay(), p.getLookbackWeek(), p.getLookbackMonth(),
                p.isEnableDay(), p.isEnableWeek(), p.isEnableMonth(),
                WaveShapeTools.BandShape.CONCAVE,
                WaveShapeTools.BreakLineConfig.of(
                        p.isEnableLastHighBreak(), p.isEnableLastMedianBreak(), p.isEnableLastLowBreak()),
                WaveShapeTools.EdgeMode.DAY);
        if (!eval.isHit()) {
            return WaveConcaveDayEvaluation.miss();
        }
        WaveConcaveDayEvaluation concaveEval = new WaveConcaveDayEvaluation(
                eval.getDayHit(), eval.getWeekHit(), eval.getMonthHit(), true);
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH,
                    WaveConcaveDayScoreCalculator.buildTrendMessage(concaveEval, p));
            appendTierSignals(checkResult, concaveEval);
            concaveEval.setScore(WaveConcaveDayScoreCalculator.computeScore(concaveEval));
        }
        return concaveEval;
    }

    private static void appendTierSignals(CheckResult checkResult, WaveConcaveDayEvaluation eval) {
        appendIfHit(checkResult, PeriodTypeEnum.MONTH, eval.getMonthHit());
        appendIfHit(checkResult, PeriodTypeEnum.WEEK, eval.getWeekHit());
        appendIfHit(checkResult, PeriodTypeEnum.DAY, eval.getDayHit());
    }

    private static void appendIfHit(CheckResult checkResult, PeriodTypeEnum period,
                                    WaveShapeTools.TierHit hit) {
        if (hit != null) {
            checkResult.addSignal(period, WaveConcaveDayScoreCalculator.buildSignalMessage(hit));
        }
    }

    @Getter
    public static final class WaveConcaveDayEvaluation {
        private final WaveShapeTools.TierHit dayHit;
        private final WaveShapeTools.TierHit weekHit;
        private final WaveShapeTools.TierHit monthHit;
        private final boolean hit;
        private int score;

        WaveConcaveDayEvaluation(WaveShapeTools.TierHit dayHit,
                                 WaveShapeTools.TierHit weekHit,
                                 WaveShapeTools.TierHit monthHit,
                                 boolean hit) {
            this.dayHit = dayHit;
            this.weekHit = weekHit;
            this.monthHit = monthHit;
            this.hit = hit;
        }

        static WaveConcaveDayEvaluation miss() {
            return new WaveConcaveDayEvaluation(null, null, null, false);
        }

        void setScore(int score) {
            this.score = score;
        }

        public boolean isHit() {
            return hit;
        }
    }
}
