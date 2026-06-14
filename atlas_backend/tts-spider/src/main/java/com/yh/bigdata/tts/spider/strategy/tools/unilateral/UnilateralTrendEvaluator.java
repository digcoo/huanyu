package com.yh.bigdata.tts.spider.strategy.tools.unilateral;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.UnilateralStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

/**
 * 金叉策略 v3.0 · Gate 之后：短/中/长三档 MACD 金叉
 */
public final class UnilateralTrendEvaluator {

    private UnilateralTrendEvaluator() {
    }

    public static UnilateralEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                UnilateralStrategyParams params) {
        UnilateralStrategyParams p = params != null ? params : UnilateralStrategyParams.defaults();

        boolean yearMacdPositive = UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.YEAR);
        boolean weekMacdPositive = UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.WEEK);
        boolean monthMacdPositive = UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.MONTH);
        boolean dayGoldenCross = UnilateralMacdTools.isGoldenCross(stock, PeriodTypeEnum.DAY);
        boolean weekGoldenCross = UnilateralMacdTools.isGoldenCross(stock, PeriodTypeEnum.WEEK);
        boolean monthGoldenCross = UnilateralMacdTools.isGoldenCross(stock, PeriodTypeEnum.MONTH);

        boolean shortHit = p.isEnableShort() && weekMacdPositive && dayGoldenCross;
        boolean mediumHit = p.isEnableMedium() && monthMacdPositive && weekGoldenCross;
        boolean longHit = p.isEnableLong() && yearMacdPositive && monthGoldenCross;
        boolean hit = shortHit || mediumHit || longHit;

        UnilateralEvaluation eval = new UnilateralEvaluation(
                yearMacdPositive, weekMacdPositive, monthMacdPositive,
                dayGoldenCross, weekGoldenCross, monthGoldenCross,
                shortHit, mediumHit, longHit, hit);
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

        String trendLabel = UnilateralScoreCalculator.buildTrendLabel(tier);
        String trendDetail = UnilateralScoreCalculator.buildTrendDetail(eval);
        PeriodTypeEnum trendPeriod = UnilateralScoreCalculator.trendPeriodForTier(tier);
        checkResult.addTrendPeriod(trendPeriod, "[" + tier + "]" + trendLabel + "|" + trendDetail);
        checkResult.addSignal(
                UnilateralScoreCalculator.signalPeriodForTier(tier),
                UnilateralScoreCalculator.buildSignalDetail(eval));
        eval.setTier(tier);
        eval.setScore(UnilateralScoreCalculator.computeScore(eval));
    }

    @Getter
    public static final class UnilateralEvaluation {
        private final boolean yearMacdPositive;
        private final boolean weekMacdPositive;
        private final boolean monthMacdPositive;
        private final boolean dayGoldenCross;
        private final boolean weekGoldenCross;
        private final boolean monthGoldenCross;
        private final boolean shortHit;
        private final boolean mediumHit;
        private final boolean longHit;
        private boolean hit;
        private int score;
        private char tier;

        public UnilateralEvaluation(boolean yearMacdPositive, boolean weekMacdPositive,
                                    boolean monthMacdPositive,
                                    boolean dayGoldenCross, boolean weekGoldenCross,
                                    boolean monthGoldenCross,
                                    boolean shortHit, boolean mediumHit, boolean longHit,
                                    boolean hit) {
            this.yearMacdPositive = yearMacdPositive;
            this.weekMacdPositive = weekMacdPositive;
            this.monthMacdPositive = monthMacdPositive;
            this.dayGoldenCross = dayGoldenCross;
            this.weekGoldenCross = weekGoldenCross;
            this.monthGoldenCross = monthGoldenCross;
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
    }
}
