package com.yh.bigdata.tts.spider.strategy.tools.multi;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MultiStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

/**
 * 多周期强势 v2.0 · Gate 之后：周+月 MACD&gt;0 + 日 K 突破前一日
 */
public final class MultiStrengthEvaluator {

    private MultiStrengthEvaluator() {
    }

    public static MultiEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                           MultiStrategyParams params) {
        MultiStrategyParams p = params != null ? params : MultiStrategyParams.defaults();
        MultiMacdBreakoutTools.MacdBreakoutSnapshot snap = MultiMacdBreakoutTools.evaluate(stock);

        MultiEvaluation eval = new MultiEvaluation(snap, false);
        if (!snap.hit) {
            return eval;
        }

        char tier = MultiScoreCalculator.computeTier(eval);
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
        String trendDetail = MultiScoreCalculator.buildTrendDetail(eval.getSnapshot());
        checkResult.addTrendPeriod(PeriodTypeEnum.WEEK,
                "[" + tier + "]" + trendLabel + "|" + trendDetail);
        checkResult.addSignal(PeriodTypeEnum.DAY, MultiScoreCalculator.buildSignalDetail(eval.getSnapshot()));
    }

    @Getter
    public static final class MultiEvaluation {
        private final MultiMacdBreakoutTools.MacdBreakoutSnapshot snapshot;
        private boolean hit;
        private int score;
        private char tier = 'N';

        public MultiEvaluation(MultiMacdBreakoutTools.MacdBreakoutSnapshot snapshot, boolean hit) {
            this.snapshot = snapshot;
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
