package com.yh.bigdata.tts.spider.strategy.tools.pregolden;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.PreGoldenStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;
import lombok.Getter;

/**
 * 预判金叉 v2.0 · 短线 / 长线两档
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
        boolean weekMacdNegative = UnilateralMacdTools.isMacdNegative(stock, PeriodTypeEnum.WEEK);
        boolean dayMacdNegative = UnilateralMacdTools.isMacdNegative(stock, PeriodTypeEnum.DAY);

        boolean dayCloseAbovePrevHigh = PreGoldenBreakoutTools.checkCloseAbovePrevHigh(stock, PeriodTypeEnum.DAY);
        boolean weekCloseAbovePrevHigh = PreGoldenBreakoutTools.checkCloseAbovePrevHigh(stock, PeriodTypeEnum.WEEK);

        boolean shortMacroOk = weekMacdPositive || monthMacdPositive;
        boolean longMacroOk = monthMacdPositive || yearMacdPositive;

        boolean shortHit = p.isEnableShort() && shortMacroOk && dayMacdNegative && dayCloseAbovePrevHigh;
        boolean longHit = p.isEnableLong() && longMacroOk && weekMacdNegative && weekCloseAbovePrevHigh;
        boolean hit = shortHit || longHit;

        PreGoldenEvaluation eval = new PreGoldenEvaluation(
                yearMacdPositive, weekMacdPositive, monthMacdPositive,
                dayMacdNegative, weekMacdNegative,
                dayCloseAbovePrevHigh, weekCloseAbovePrevHigh,
                shortHit, longHit, hit);
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
        private final boolean yearMacdPositive;
        private final boolean weekMacdPositive;
        private final boolean monthMacdPositive;
        private final boolean dayMacdNegative;
        private final boolean weekMacdNegative;
        private final boolean dayCloseAbovePrevHigh;
        private final boolean weekCloseAbovePrevHigh;
        private final boolean shortHit;
        private final boolean longHit;
        private boolean hit;
        private int score;
        private char tier;

        public PreGoldenEvaluation(boolean yearMacdPositive, boolean weekMacdPositive,
                                   boolean monthMacdPositive,
                                   boolean dayMacdNegative, boolean weekMacdNegative,
                                   boolean dayCloseAbovePrevHigh, boolean weekCloseAbovePrevHigh,
                                   boolean shortHit, boolean longHit, boolean hit) {
            this.yearMacdPositive = yearMacdPositive;
            this.weekMacdPositive = weekMacdPositive;
            this.monthMacdPositive = monthMacdPositive;
            this.dayMacdNegative = dayMacdNegative;
            this.weekMacdNegative = weekMacdNegative;
            this.dayCloseAbovePrevHigh = dayCloseAbovePrevHigh;
            this.weekCloseAbovePrevHigh = weekCloseAbovePrevHigh;
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
    }
}
