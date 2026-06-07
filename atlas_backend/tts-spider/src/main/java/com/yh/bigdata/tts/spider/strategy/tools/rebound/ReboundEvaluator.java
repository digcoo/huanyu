package com.yh.bigdata.tts.spider.strategy.tools.rebound;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.ReboundStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

/**
 * 深坑反弹 v1.0 · 评估编排
 * @see docs/strategies/深坑反弹策略.md
 */
public final class ReboundEvaluator {

    private ReboundEvaluator() {
    }

    public static ReboundEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                               ReboundStrategyParams params) {
        ReboundStrategyParams p = params != null ? params : ReboundStrategyParams.defaults();

        ReboundPitTools.DeepPitNarrative narrative = ReboundPitTools.evaluateNarrative(stock, p);
        ReboundPitTools.PitSnapshot weekPit = ReboundPitTools.evaluateWeek(stock);
        ReboundPitTools.PitSnapshot monthPit = ReboundPitTools.evaluateMonth(stock);
        ReboundTriggerTools.TriggerSnapshot trigger = ReboundTriggerTools.evaluate(stock);
        boolean divergence = ReboundDivergenceTools.checkMonthMacdBullishDivergence(stock);

        boolean pitOk = narrative.isComplete();
        boolean modeA = p.isEnableModeA() && ReboundTriggerTools.isModeA(trigger);
        boolean modeB = p.isEnableModeB() && ReboundTriggerTools.isModeB(trigger);
        boolean modeC = p.isEnableModeC() && ReboundTriggerTools.isModeC(trigger, divergence);
        boolean hit = pitOk && (modeA || modeB || modeC);

        ReboundEvaluation eval = new ReboundEvaluation(
                narrative, weekPit, monthPit, trigger, divergence, pitOk, modeA, modeB, modeC, hit);

        if (hit && checkResult != null) {
            fillMessages(checkResult, eval, p);
        }
        return eval;
    }

    private static void fillMessages(CheckResult checkResult, ReboundEvaluation eval,
                                     ReboundStrategyParams params) {
        char tier = ReboundScoreCalculator.computeTier(eval);
        if (!params.passTierFilter(tier)) {
            eval.hit = false;
            return;
        }

        String trendLabel = ReboundScoreCalculator.buildTrendLabel(tier, eval);
        String trendDetail = ReboundScoreCalculator.buildTrendDetail(eval.getNarrative());
        checkResult.addTrendPeriod(PeriodTypeEnum.WEEK, "[" + tier + "]" + trendLabel + "|" + trendDetail);

        String signalDetail = ReboundScoreCalculator.buildSignalDetail(eval.trigger, eval.divergence);
        if (!signalDetail.isEmpty()) {
            checkResult.addSignal(PeriodTypeEnum.DAY, signalDetail);
        }
        eval.setTier(tier);
        eval.setScore(ReboundScoreCalculator.computeScore(eval));
    }

    @Getter
    public static final class ReboundEvaluation {
        private final ReboundPitTools.DeepPitNarrative narrative;
        private final ReboundPitTools.PitSnapshot weekPit;
        private final ReboundPitTools.PitSnapshot monthPit;
        private final ReboundTriggerTools.TriggerSnapshot trigger;
        private final boolean divergence;
        private final boolean pitOk;
        private final boolean modeA;
        private final boolean modeB;
        private final boolean modeC;
        private boolean hit;
        private int score;
        private char tier;

        public ReboundEvaluation(ReboundPitTools.DeepPitNarrative narrative,
                                 ReboundPitTools.PitSnapshot weekPit,
                                 ReboundPitTools.PitSnapshot monthPit,
                                 ReboundTriggerTools.TriggerSnapshot trigger,
                                 boolean divergence,
                                 boolean pitOk,
                                 boolean modeA,
                                 boolean modeB,
                                 boolean modeC,
                                 boolean hit) {
            this.narrative = narrative;
            this.weekPit = weekPit;
            this.monthPit = monthPit;
            this.trigger = trigger;
            this.divergence = divergence;
            this.pitOk = pitOk;
            this.modeA = modeA;
            this.modeB = modeB;
            this.modeC = modeC;
            this.hit = hit;
        }

        void setScore(int score) {
            this.score = score;
        }

        void setTier(char tier) {
            this.tier = tier;
        }

        public boolean isHit() {
            return hit;
        }

        public boolean isModeA() {
            return modeA;
        }

        public boolean isModeB() {
            return modeB;
        }

        public boolean isModeC() {
            return modeC;
        }

        public boolean isDivergence() {
            return divergence;
        }
    }
}
