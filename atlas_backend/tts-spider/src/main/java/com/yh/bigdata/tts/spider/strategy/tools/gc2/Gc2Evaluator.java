package com.yh.bigdata.tts.spider.strategy.tools.gc2;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.Gc2StrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

/**
 * 金叉二次突破 · 合判
 */
public final class Gc2Evaluator {

    private Gc2Evaluator() {
    }

    public static Gc2Evaluation evaluate(StockBase stock, CheckResult checkResult, Gc2StrategyParams params) {
        Gc2StrategyParams p = params != null ? params : Gc2StrategyParams.defaults();

        Gc2BreakoutTools.TierHit shortHit = Gc2TierTools.findShortHit(stock, p);
        Gc2BreakoutTools.TierHit mediumHit = Gc2TierTools.findMediumHit(stock, p);
        Gc2BreakoutTools.TierHit longHit = Gc2TierTools.findLongHit(stock, p);

        boolean hit = shortHit != null || mediumHit != null || longHit != null;
        Gc2Evaluation eval = new Gc2Evaluation(shortHit, mediumHit, longHit, hit);

        if (hit && checkResult != null) {
            fillMessages(checkResult, eval, p);
        }
        return eval;
    }

    private static void fillMessages(CheckResult checkResult, Gc2Evaluation eval, Gc2StrategyParams params) {
        char tier = Gc2ScoreCalculator.computeTier(eval);
        if (!params.passTierFilter(tier)) {
            eval.hit = false;
            return;
        }

        checkResult.addTrendPeriod(
                Gc2ScoreCalculator.trendPeriodForTier(tier),
                "[" + tier + "]" + Gc2ScoreCalculator.buildTrendLabel(tier) + "|"
                        + Gc2ScoreCalculator.buildTrendDetail(eval, tier));
        checkResult.addSignal(
                Gc2ScoreCalculator.signalPeriodForTier(tier),
                Gc2ScoreCalculator.buildSignalDetail(eval, tier));
        eval.setTier(tier);
        eval.setScore(Gc2ScoreCalculator.computeScore(eval));
    }

    @Getter
    public static final class Gc2Evaluation {
        private final Gc2BreakoutTools.TierHit shortHit;
        private final Gc2BreakoutTools.TierHit mediumHit;
        private final Gc2BreakoutTools.TierHit longHit;
        private boolean hit;
        private int score;
        private char tier;

        public Gc2Evaluation(Gc2BreakoutTools.TierHit shortHit, Gc2BreakoutTools.TierHit mediumHit,
                             Gc2BreakoutTools.TierHit longHit, boolean hit) {
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

        public Gc2BreakoutTools.TierHit hitForTier(char tier) {
            switch (tier) {
                case 'S':
                    return shortHit;
                case 'A':
                    return mediumHit;
                case 'B':
                    return longHit;
                default:
                    return primaryHit();
            }
        }

        public Gc2BreakoutTools.TierHit primaryHit() {
            if (shortHit != null) {
                return shortHit;
            }
            if (mediumHit != null) {
                return mediumHit;
            }
            return longHit;
        }
    }
}
