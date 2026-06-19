package com.yh.bigdata.tts.spider.strategy.tools.dc2;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.Dc2StrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;
import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import lombok.Getter;

/**
 * 死叉突破 · 合判
 */
public final class Dc2Evaluator {

    private Dc2Evaluator() {
    }

    public static Dc2Evaluation evaluate(StockBase stock, CheckResult checkResult, Dc2StrategyParams params) {
        Dc2StrategyParams p = params != null ? params : Dc2StrategyParams.defaults();

        boolean yearMacdPositive = UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.YEAR);
        boolean weekMacdPositive = UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.WEEK);
        boolean monthMacdPositive = UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.MONTH);

        Dc2BreakoutTools.TierHit shortHit = Dc2TierTools.findShortHit(stock, p);
        Dc2BreakoutTools.TierHit longHit = Dc2TierTools.findLongHit(stock, p);

        boolean hit = shortHit != null || longHit != null;
        Dc2Evaluation eval = new Dc2Evaluation(
                yearMacdPositive, weekMacdPositive, monthMacdPositive,
                shortHit, longHit, hit);

        if (hit && checkResult != null) {
            fillMessages(checkResult, eval, p);
        }
        return eval;
    }

    private static void fillMessages(CheckResult checkResult, Dc2Evaluation eval, Dc2StrategyParams params) {
        char tier = Dc2ScoreCalculator.computeTier(eval);
        if (!params.passTierFilter(tier)) {
            eval.hit = false;
            return;
        }

        checkResult.addTrendPeriod(
                Dc2ScoreCalculator.trendPeriodForTier(tier),
                "[" + tier + "]" + Dc2ScoreCalculator.buildTrendLabel(tier) + "|"
                        + Dc2ScoreCalculator.buildTrendDetail(eval, tier));
        checkResult.addSignal(
                Dc2ScoreCalculator.signalPeriodForTier(tier),
                Dc2ScoreCalculator.buildSignalDetail(eval, tier));
        eval.setTier(tier);
        eval.setScore(Dc2ScoreCalculator.computeScore(eval));
    }

    @Getter
    public static final class Dc2Evaluation {
        private final boolean yearMacdPositive;
        private final boolean weekMacdPositive;
        private final boolean monthMacdPositive;
        private final Dc2BreakoutTools.TierHit shortHit;
        private final Dc2BreakoutTools.TierHit longHit;
        private boolean hit;
        private int score;
        private char tier;

        public Dc2Evaluation(boolean yearMacdPositive, boolean weekMacdPositive, boolean monthMacdPositive,
                             Dc2BreakoutTools.TierHit shortHit, Dc2BreakoutTools.TierHit longHit,
                             boolean hit) {
            this.yearMacdPositive = yearMacdPositive;
            this.weekMacdPositive = weekMacdPositive;
            this.monthMacdPositive = monthMacdPositive;
            this.shortHit = shortHit;
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

        public Dc2BreakoutTools.TierHit hitForTier(char tier) {
            switch (tier) {
                case 'S':
                    return shortHit;
                case 'B':
                    return longHit;
                default:
                    return primaryHit();
            }
        }

        public Dc2BreakoutTools.TierHit primaryHit() {
            if (shortHit != null) {
                return shortHit;
            }
            return longHit;
        }
    }
}
