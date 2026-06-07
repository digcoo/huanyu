package com.yh.bigdata.tts.spider.strategy.tools.multi;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MultiStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

/**
 * 多周期强势 v1.0 · 评估编排
 * @see docs/strategies/多周期强势策略.md
 */
public final class MultiStrengthEvaluator {

    private MultiStrengthEvaluator() {
    }

    public static MultiEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                           MultiStrategyParams params) {
        MultiStrategyParams p = params != null ? params : MultiStrategyParams.defaults();

        MultiResonanceTools.ResonanceSnapshot resonance = MultiResonanceTools.evaluate(stock);
        MultiBreakoutTools.BreakoutSnapshot breakout = MultiBreakoutTools.evaluate(stock, p);

        if (resonance.coreCount() < p.getMinResonancePeriods()) {
            return new MultiEvaluation(resonance, breakout, false);
        }

        MultiEvaluation eval = new MultiEvaluation(resonance, breakout, false);

        char tier = MultiScoreCalculator.computeTier(eval, p);
        eval.setTier(tier);
        if (tier == 'N' || !p.passTierFilter(tier)) {
            return eval;
        }

        eval.hit = true;
        eval.setScore(MultiScoreCalculator.computeScore(eval));
        if (checkResult != null) {
            fillMessages(checkResult, eval);
        }
        return eval;
    }

    private static void fillMessages(CheckResult checkResult, MultiEvaluation eval) {
        char tier = eval.getTier();
        String trendLabel = MultiScoreCalculator.buildTrendLabel(tier);
        String trendDetail = MultiScoreCalculator.buildTrendDetail(eval.getResonance());
        checkResult.addTrendPeriod(PeriodTypeEnum.WEEK,
                "[" + tier + "]" + trendLabel + "|" + trendDetail);

        String signalDetail = eval.getBreakout().buildSignalMessage();
        if (signalDetail != null && !signalDetail.isEmpty()) {
            checkResult.addSignal(PeriodTypeEnum.DAY, signalDetail);
        }
    }

    @Getter
    public static final class MultiEvaluation {
        private final MultiResonanceTools.ResonanceSnapshot resonance;
        private final MultiBreakoutTools.BreakoutSnapshot breakout;
        private boolean hit;
        private int score;
        private char tier = 'C';

        public MultiEvaluation(MultiResonanceTools.ResonanceSnapshot resonance,
                               MultiBreakoutTools.BreakoutSnapshot breakout,
                               boolean hit) {
            this.resonance = resonance;
            this.breakout = breakout;
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
    }
}
