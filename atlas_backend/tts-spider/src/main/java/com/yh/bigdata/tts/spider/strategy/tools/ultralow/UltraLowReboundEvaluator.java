package com.yh.bigdata.tts.spider.strategy.tools.ultralow;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.UltraLowReboundStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

/**
 * 梯子突破 · 超短/短/中/长 四档合判
 */
public final class UltraLowReboundEvaluator {

    private UltraLowReboundEvaluator() {
    }

    public static UltraLowEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                              UltraLowReboundStrategyParams params) {
        UltraLowReboundStrategyParams p = params != null ? params : UltraLowReboundStrategyParams.defaults();

        BreakoutLadderTools.TierHit ultraHit = p.isEnableUltra()
                ? BreakoutLadderTools.findUltraHit(stock, p) : null;
        BreakoutLadderTools.TierHit shortHit = p.isEnableShort()
                ? BreakoutLadderTools.findShortHit(stock, p) : null;
        BreakoutLadderTools.TierHit mediumHit = p.isEnableMedium()
                ? BreakoutLadderTools.findMediumHit(stock, p) : null;
        BreakoutLadderTools.TierHit longHit = p.isEnableLong()
                ? BreakoutLadderTools.findLongHit(stock, p) : null;

        boolean hit = ultraHit != null || shortHit != null || mediumHit != null || longHit != null;
        UltraLowEvaluation eval = new UltraLowEvaluation(ultraHit, shortHit, mediumHit, longHit, hit);

        if (hit && checkResult != null) {
            fillMessages(checkResult, eval, p);
        }
        return eval;
    }

    private static void fillMessages(CheckResult checkResult, UltraLowEvaluation eval,
                                     UltraLowReboundStrategyParams params) {
        char tier = UltraLowReboundScoreCalculator.computeTier(eval);
        if (!params.passTierFilter(tier)) {
            eval.hit = false;
            return;
        }

        String trendLabel = UltraLowReboundScoreCalculator.buildTrendLabel(tier);
        String trendDetail = UltraLowReboundScoreCalculator.buildTrendDetail(eval, tier);
        checkResult.addTrendPeriod(
                UltraLowReboundScoreCalculator.trendPeriodForTier(tier),
                "[" + tier + "]" + trendLabel + "|" + trendDetail);
        checkResult.addSignal(
                UltraLowReboundScoreCalculator.signalPeriodForTier(tier),
                UltraLowReboundScoreCalculator.buildSignalDetail(eval, tier));
        eval.setTier(tier);
        eval.setScore(UltraLowReboundScoreCalculator.computeScore(eval));
    }

    @Getter
    public static final class UltraLowEvaluation {
        private final BreakoutLadderTools.TierHit ultraHit;
        private final BreakoutLadderTools.TierHit shortHit;
        private final BreakoutLadderTools.TierHit mediumHit;
        private final BreakoutLadderTools.TierHit longHit;
        private boolean hit;
        private int score;
        private char tier;

        public UltraLowEvaluation(BreakoutLadderTools.TierHit ultraHit,
                                  BreakoutLadderTools.TierHit shortHit,
                                  BreakoutLadderTools.TierHit mediumHit,
                                  BreakoutLadderTools.TierHit longHit,
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

        public BreakoutLadderTools.TierHit primaryHit() {
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

        public BreakoutLadderTools.TierHit hitForTier(char tier) {
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
