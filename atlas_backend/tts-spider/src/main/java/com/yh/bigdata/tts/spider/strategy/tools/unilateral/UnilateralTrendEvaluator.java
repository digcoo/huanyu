package com.yh.bigdata.tts.spider.strategy.tools.unilateral;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.UnilateralStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

/**
 * 单边趋势 v1.0 · 评估编排
 * @see docs/strategies/单边趋势策略.md
 */
public final class UnilateralTrendEvaluator {

    private UnilateralTrendEvaluator() {
    }

    public static UnilateralEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                UnilateralStrategyParams params) {
        UnilateralStrategyParams p = params != null ? params : UnilateralStrategyParams.defaults();

        UnilateralContinuationTools.ContinuationContext ctx =
                UnilateralContinuationTools.evaluateContext(stock);
        UnilateralContinuationTools.ContinuationTrigger trigger =
                UnilateralContinuationTools.evaluateTrigger(stock, p);

        boolean modeA = p.isEnableModeA() && UnilateralPivotTools.checkMonthPivot(stock, checkResult);
        boolean modeBFull = p.isEnableModeB()
                && UnilateralContinuationTools.isFullModeB(ctx, trigger, p);
        boolean modeBWeak = p.isEnableModeB() && p.isEnableModeBWeak()
                && UnilateralContinuationTools.isWeakModeB(ctx, trigger, p);
        boolean hit = modeBFull || modeBWeak || modeA;

        UnilateralEvaluation eval = new UnilateralEvaluation(ctx, trigger, modeA, modeBFull, modeBWeak, hit);
        if (hit && checkResult != null) {
            fillMessages(checkResult, eval, p);
        }
        return eval;
    }

    private static void fillMessages(CheckResult checkResult, UnilateralEvaluation eval,
                                     UnilateralStrategyParams params) {
        char tier = UnilateralScoreCalculator.computeTier(eval);
        if (!params.passTierFilter(tier)) {
            eval.hit = false;
            return;
        }

        String trendLabel = UnilateralScoreCalculator.buildTrendLabel(tier, eval);
        String trendDetail = UnilateralScoreCalculator.buildTrendDetail(eval.getContext());
        checkResult.addTrendPeriod(PeriodTypeEnum.WEEK, "[" + tier + "]" + trendLabel + "|" + trendDetail);

        String signalDetail = UnilateralScoreCalculator.buildSignalDetail(eval.getTrigger());
        if (!signalDetail.isEmpty()) {
            checkResult.addSignal(PeriodTypeEnum.DAY, signalDetail);
        }
        eval.setTier(tier);
        eval.setScore(UnilateralScoreCalculator.computeScore(eval));
    }

    @Getter
    public static final class UnilateralEvaluation {
        private final UnilateralContinuationTools.ContinuationContext context;
        private final UnilateralContinuationTools.ContinuationTrigger trigger;
        private final boolean modeA;
        private final boolean modeBFull;
        private final boolean modeBWeak;
        private boolean hit;
        private int score;
        private char tier;

        public UnilateralEvaluation(UnilateralContinuationTools.ContinuationContext context,
                                    UnilateralContinuationTools.ContinuationTrigger trigger,
                                    boolean modeA, boolean modeBFull, boolean modeBWeak, boolean hit) {
            this.context = context;
            this.trigger = trigger;
            this.modeA = modeA;
            this.modeBFull = modeBFull;
            this.modeBWeak = modeBWeak;
            this.hit = hit;
        }

        void setScore(int score) {
            this.score = score;
        }

        void setTier(char tier) {
            this.tier = tier;
        }

        public boolean isModeA() {
            return modeA;
        }

        public boolean isModeBFull() {
            return modeBFull;
        }

        public boolean isModeBWeak() {
            return modeBWeak;
        }
    }
}
