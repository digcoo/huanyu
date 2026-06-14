package com.yh.bigdata.tts.spider.strategy.tools.pregolden;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.PreGoldenStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;
import lombok.Getter;

/**
 * 预判金叉 v1.0 · 大周期 MACD&gt;0 + 小周期 MACD&lt;0 + K 线突破
 */
public final class PreGoldenEvaluator {

    private PreGoldenEvaluator() {
    }

    public static PreGoldenEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                               PreGoldenStrategyParams params) {
        PreGoldenStrategyParams p = params != null ? params : PreGoldenStrategyParams.defaults();

        boolean yearMacdPositive = UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.YEAR);
        boolean monthMacdPositive = UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.MONTH);
        boolean weekMacdPositive = UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.WEEK);
        boolean monthMacdNegative = UnilateralMacdTools.isMacdNegative(stock, PeriodTypeEnum.MONTH);
        boolean weekMacdNegative = UnilateralMacdTools.isMacdNegative(stock, PeriodTypeEnum.WEEK);
        boolean dayMacdNegative = UnilateralMacdTools.isMacdNegative(stock, PeriodTypeEnum.DAY);

        boolean dayBreakout = PreGoldenBreakoutTools.checkBreakout(stock, PeriodTypeEnum.DAY);
        boolean weekBreakout = PreGoldenBreakoutTools.checkBreakout(stock, PeriodTypeEnum.WEEK);
        boolean monthBreakout = PreGoldenBreakoutTools.checkBreakout(stock, PeriodTypeEnum.MONTH);

        boolean shortHit = p.isEnableShort() && weekMacdPositive && dayMacdNegative && dayBreakout;
        boolean mediumHit = p.isEnableMedium() && monthMacdPositive && weekMacdNegative && weekBreakout;
        boolean longHit = p.isEnableLong() && yearMacdPositive && monthMacdNegative && monthBreakout;
        boolean hit = shortHit || mediumHit || longHit;

        PreGoldenEvaluation eval = new PreGoldenEvaluation(
                shortHit, mediumHit, longHit, dayBreakout, weekBreakout, monthBreakout, hit);
        if (hit && checkResult != null) {
            fillMessages(checkResult, eval, p);
        }
        return eval;
    }

    private static void fillMessages(CheckResult checkResult, PreGoldenEvaluation eval,
                                     PreGoldenStrategyParams params) {
        char tier = PreGoldenScoreCalculator.computeTier(eval);
        if (!params.passTierFilter(tier)) {
            eval.hit = false;
            return;
        }

        String trendLabel = PreGoldenScoreCalculator.buildTrendLabel(tier);
        String trendDetail = PreGoldenScoreCalculator.buildTrendDetail(eval);
        checkResult.addTrendPeriod(
                PreGoldenScoreCalculator.trendPeriodForTier(tier),
                "[" + tier + "]" + trendLabel + "|" + trendDetail);
        checkResult.addSignal(
                PreGoldenScoreCalculator.signalPeriodForTier(tier),
                PreGoldenScoreCalculator.buildSignalDetail(eval));
        eval.setTier(tier);
        eval.setScore(PreGoldenScoreCalculator.computeScore(eval));
    }

    @Getter
    public static final class PreGoldenEvaluation {
        private final boolean shortHit;
        private final boolean mediumHit;
        private final boolean longHit;
        private final boolean dayBreakout;
        private final boolean weekBreakout;
        private final boolean monthBreakout;
        private boolean hit;
        private int score;
        private char tier;

        public PreGoldenEvaluation(boolean shortHit, boolean mediumHit, boolean longHit,
                                   boolean dayBreakout, boolean weekBreakout, boolean monthBreakout,
                                   boolean hit) {
            this.shortHit = shortHit;
            this.mediumHit = mediumHit;
            this.longHit = longHit;
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
    }
}
