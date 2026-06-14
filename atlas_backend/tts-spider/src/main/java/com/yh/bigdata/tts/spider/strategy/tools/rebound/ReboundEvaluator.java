package com.yh.bigdata.tts.spider.strategy.tools.rebound;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.ReboundStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.pregolden.PreGoldenBreakoutTools;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;
import lombok.Getter;

/**
 * 深跌反弹 v3.1 · 大周期 MACD&lt;0 + 小周期 MACD&gt;0 + 小周期 K 线突破
 */
public final class ReboundEvaluator {

    private ReboundEvaluator() {
    }

    public static ReboundEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                               ReboundStrategyParams params) {
        ReboundStrategyParams p = params != null ? params : ReboundStrategyParams.defaults();

        boolean weekMacdNegative = UnilateralMacdTools.isMacdNegative(stock, PeriodTypeEnum.WEEK);
        boolean monthMacdNegative = UnilateralMacdTools.isMacdNegative(stock, PeriodTypeEnum.MONTH);
        boolean yearMacdNegative = UnilateralMacdTools.isMacdNegative(stock, PeriodTypeEnum.YEAR);
        boolean dayMacdPositive = UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.DAY);
        boolean weekMacdPositive = UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.WEEK);
        boolean monthMacdPositive = UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.MONTH);

        boolean dayBreakout = PreGoldenBreakoutTools.checkBreakout(stock, PeriodTypeEnum.DAY);
        boolean weekBreakout = PreGoldenBreakoutTools.checkBreakout(stock, PeriodTypeEnum.WEEK);
        boolean monthBreakout = PreGoldenBreakoutTools.checkBreakout(stock, PeriodTypeEnum.MONTH);

        boolean shortHit = p.isEnableShort() && weekMacdNegative && dayMacdPositive && dayBreakout;
        boolean mediumHit = p.isEnableMedium() && monthMacdNegative && weekMacdPositive && weekBreakout;
        boolean longHit = p.isEnableLong() && yearMacdNegative && monthMacdPositive && monthBreakout;
        boolean hit = shortHit || mediumHit || longHit;

        ReboundEvaluation eval = new ReboundEvaluation(
                shortHit, mediumHit, longHit,
                weekMacdNegative, monthMacdNegative, yearMacdNegative,
                dayMacdPositive, weekMacdPositive, monthMacdPositive,
                dayBreakout, weekBreakout, monthBreakout, hit);

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

        String trendLabel = ReboundScoreCalculator.buildTrendLabel(tier);
        String trendDetail = ReboundScoreCalculator.buildTrendDetail(eval);
        checkResult.addTrendPeriod(
                ReboundScoreCalculator.trendPeriodForTier(tier),
                "[" + tier + "]" + trendLabel + "|" + trendDetail);
        checkResult.addSignal(
                ReboundScoreCalculator.signalPeriodForTier(tier),
                ReboundScoreCalculator.buildSignalDetail(eval));
        eval.setTier(tier);
        eval.setScore(ReboundScoreCalculator.computeScore(eval));
    }

    @Getter
    public static final class ReboundEvaluation {
        private final boolean shortHit;
        private final boolean mediumHit;
        private final boolean longHit;
        private final boolean weekMacdNegative;
        private final boolean monthMacdNegative;
        private final boolean yearMacdNegative;
        private final boolean dayMacdPositive;
        private final boolean weekMacdPositive;
        private final boolean monthMacdPositive;
        private final boolean dayBreakout;
        private final boolean weekBreakout;
        private final boolean monthBreakout;
        private boolean hit;
        private int score;
        private char tier;

        public ReboundEvaluation(boolean shortHit, boolean mediumHit, boolean longHit,
                                 boolean weekMacdNegative, boolean monthMacdNegative, boolean yearMacdNegative,
                                 boolean dayMacdPositive, boolean weekMacdPositive, boolean monthMacdPositive,
                                 boolean dayBreakout, boolean weekBreakout, boolean monthBreakout,
                                 boolean hit) {
            this.shortHit = shortHit;
            this.mediumHit = mediumHit;
            this.longHit = longHit;
            this.weekMacdNegative = weekMacdNegative;
            this.monthMacdNegative = monthMacdNegative;
            this.yearMacdNegative = yearMacdNegative;
            this.dayMacdPositive = dayMacdPositive;
            this.weekMacdPositive = weekMacdPositive;
            this.monthMacdPositive = monthMacdPositive;
            this.dayBreakout = dayBreakout;
            this.weekBreakout = weekBreakout;
            this.monthBreakout = monthBreakout;
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
