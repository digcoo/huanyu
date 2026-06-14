package com.yh.bigdata.tts.spider.strategy.tools.resonance;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.ResonanceStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.pregolden.PreGoldenBreakoutTools;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;
import lombok.Getter;

/**
 * 周期共振 v1.0 · 大/小周期 MACD&gt;0（小周期非金叉）+ K 线突破
 */
public final class ResonanceEvaluator {

    private ResonanceEvaluator() {
    }

    public static ResonanceEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                               ResonanceStrategyParams params) {
        ResonanceStrategyParams p = params != null ? params : ResonanceStrategyParams.defaults();

        boolean yearMacdPositive = UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.YEAR);
        boolean monthMacdPositive = UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.MONTH);
        boolean weekMacdPositive = UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.WEEK);
        boolean dayMacdPositiveNotCross =
                UnilateralMacdTools.isMacdPositiveNotGoldenCross(stock, PeriodTypeEnum.DAY);
        boolean weekMacdPositiveNotCross =
                UnilateralMacdTools.isMacdPositiveNotGoldenCross(stock, PeriodTypeEnum.WEEK);
        boolean monthMacdPositiveNotCross =
                UnilateralMacdTools.isMacdPositiveNotGoldenCross(stock, PeriodTypeEnum.MONTH);

        boolean dayBreakout = PreGoldenBreakoutTools.checkBreakout(stock, PeriodTypeEnum.DAY);
        boolean weekBreakout = PreGoldenBreakoutTools.checkBreakout(stock, PeriodTypeEnum.WEEK);
        boolean monthBreakout = PreGoldenBreakoutTools.checkBreakout(stock, PeriodTypeEnum.MONTH);

        boolean shortHit = p.isEnableShort() && weekMacdPositive && dayMacdPositiveNotCross && dayBreakout;
        boolean mediumHit = p.isEnableMedium() && monthMacdPositive && weekMacdPositiveNotCross && weekBreakout;
        boolean longHit = p.isEnableLong() && yearMacdPositive && monthMacdPositiveNotCross && monthBreakout;
        boolean hit = shortHit || mediumHit || longHit;

        ResonanceEvaluation eval = new ResonanceEvaluation(
                shortHit, mediumHit, longHit, dayBreakout, weekBreakout, monthBreakout, hit);
        if (hit && checkResult != null) {
            fillMessages(checkResult, eval, p);
        }
        return eval;
    }

    private static void fillMessages(CheckResult checkResult, ResonanceEvaluation eval,
                                     ResonanceStrategyParams params) {
        char tier = ResonanceScoreCalculator.computeTier(eval);
        if (!params.passTierFilter(tier)) {
            eval.hit = false;
            return;
        }

        String trendLabel = ResonanceScoreCalculator.buildTrendLabel(tier);
        String trendDetail = ResonanceScoreCalculator.buildTrendDetail(eval);
        checkResult.addTrendPeriod(
                ResonanceScoreCalculator.trendPeriodForTier(tier),
                "[" + tier + "]" + trendLabel + "|" + trendDetail);
        checkResult.addSignal(
                ResonanceScoreCalculator.signalPeriodForTier(tier),
                ResonanceScoreCalculator.buildSignalDetail(eval));
        eval.setTier(tier);
        eval.setScore(ResonanceScoreCalculator.computeScore(eval));
    }

    @Getter
    public static final class ResonanceEvaluation {
        private final boolean shortHit;
        private final boolean mediumHit;
        private final boolean longHit;
        private final boolean dayBreakout;
        private final boolean weekBreakout;
        private final boolean monthBreakout;
        private boolean hit;
        private int score;
        private char tier;

        public ResonanceEvaluation(boolean shortHit, boolean mediumHit, boolean longHit,
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
