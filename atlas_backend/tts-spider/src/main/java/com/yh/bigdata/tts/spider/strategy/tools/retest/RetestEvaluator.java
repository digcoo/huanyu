package com.yh.bigdata.tts.spider.strategy.tools.retest;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.RetestStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

/**
 * 回踩抬升 · 四档 × bear/bull 合判
 */
public final class RetestEvaluator {

    private RetestEvaluator() {
    }

    public static RetestEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                            RetestStrategyParams params) {
        RetestStrategyParams p = params != null ? params : RetestStrategyParams.defaults();

        RetestTierTools.TierHit ultraHit = RetestTierTools.findUltraHit(stock, p);
        RetestTierTools.TierHit shortHit = RetestTierTools.findShortHit(stock, p);
        RetestTierTools.TierHit mediumHit = RetestTierTools.findMediumHit(stock, p);
        RetestTierTools.TierHit longHit = RetestTierTools.findLongHit(stock, p);

        boolean hit = ultraHit != null || shortHit != null || mediumHit != null || longHit != null;
        RetestEvaluation eval = new RetestEvaluation(ultraHit, shortHit, mediumHit, longHit, hit);

        if (hit && checkResult != null) {
            fillMessages(checkResult, eval, p);
        }
        return eval;
    }

    private static void fillMessages(CheckResult checkResult, RetestEvaluation eval,
                                     RetestStrategyParams params) {
        char tier = RetestScoreCalculator.computeTier(eval);
        if (!params.passTierFilter(tier)) {
            eval.hit = false;
            return;
        }

        String trendLabel = RetestScoreCalculator.buildTrendLabel(tier, eval);
        String trendDetail = RetestScoreCalculator.buildTrendDetail(eval, tier);
        checkResult.addTrendPeriod(
                RetestScoreCalculator.trendPeriodForTier(tier),
                "[" + tier + "]" + trendLabel + "|" + trendDetail);
        checkResult.addSignal(
                RetestScoreCalculator.signalPeriodForTier(tier),
                RetestScoreCalculator.buildSignalDetail(eval, tier));
        eval.setTier(tier);
        eval.setScore(RetestScoreCalculator.computeScore(eval));
    }

    @Getter
    public static final class RetestEvaluation {
        private final RetestTierTools.TierHit ultraHit;
        private final RetestTierTools.TierHit shortHit;
        private final RetestTierTools.TierHit mediumHit;
        private final RetestTierTools.TierHit longHit;
        private boolean hit;
        private int score;
        private char tier;

        public RetestEvaluation(RetestTierTools.TierHit ultraHit, RetestTierTools.TierHit shortHit,
                                RetestTierTools.TierHit mediumHit, RetestTierTools.TierHit longHit,
                                boolean hit) {
            this.ultraHit = ultraHit;
            this.shortHit = shortHit;
            this.mediumHit = mediumHit;
            this.longHit = longHit;
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

        public RetestTierTools.TierHit primaryHit() {
            if (ultraHit != null) {
                return ultraHit;
            }
            if (shortHit != null) {
                return shortHit;
            }
            if (mediumHit != null) {
                return mediumHit;
            }
            return longHit;
        }

        public RetestTierTools.TierHit hitForTier(char tier) {
            switch (tier) {
                case 'S':
                    return ultraHit;
                case 'A':
                    return shortHit;
                case 'B':
                    return mediumHit;
                case 'C':
                    return longHit;
                default:
                    return primaryHit();
            }
        }
    }
}
