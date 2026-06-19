package com.yh.bigdata.tts.spider.strategy.tools.gc2;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;

/**
 * 金叉二次突破 v2.0 · 评分与展示（S=短线 B=长线）
 */
public final class Gc2ScoreCalculator {

    private Gc2ScoreCalculator() {
    }

    public static int computeScore(Gc2Evaluator.Gc2Evaluation eval) {
        int score = 0;
        if (eval.getShortHit() != null) {
            score += 40;
            if (eval.isMonthMacdPositive() && eval.isWeekMacdPositive()) {
                score += 10;
            }
        }
        if (eval.getLongHit() != null) {
            score += 35;
            if (eval.isYearMacdPositive() && eval.isMonthMacdPositive()) {
                score += 10;
            }
        }
        if (eval.getShortHit() != null && eval.getLongHit() != null) {
            score += 15;
        }
        return Math.max(score, 20);
    }

    public static char computeTier(Gc2Evaluator.Gc2Evaluation eval) {
        if (eval.getShortHit() != null) {
            return 'S';
        }
        if (eval.getLongHit() != null) {
            return 'B';
        }
        return 'N';
    }

    public static PeriodTypeEnum trendPeriodForTier(char tier) {
        switch (tier) {
            case 'S':
                return PeriodTypeEnum.MONTH;
            case 'B':
                return PeriodTypeEnum.YEAR;
            default:
                return PeriodTypeEnum.WEEK;
        }
    }

    public static PeriodTypeEnum signalPeriodForTier(char tier) {
        switch (tier) {
            case 'S':
                return PeriodTypeEnum.DAY;
            case 'B':
                return PeriodTypeEnum.WEEK;
            default:
                return PeriodTypeEnum.DAY;
        }
    }

    public static String buildTrendLabel(char tier) {
        switch (tier) {
            case 'S':
                return "短线·金叉二次突破";
            case 'B':
                return "长线·金叉二次突破";
            default:
                return "金叉二次突破";
        }
    }

    public static String buildTrendDetail(Gc2Evaluator.Gc2Evaluation eval, char tier) {
        Gc2BreakoutTools.TierHit hit = eval.hitForTier(tier);
        String macro = tier == 'S' ? buildShortMacroDetail(eval) : buildLongMacroDetail(eval);
        if (hit == null || hit.getReferenceBar() == null) {
            return macro + ",MACD金叉后突破";
        }
        return macro + String.format(",金叉K=%s,refHigh=%.2f",
                dayOf(hit.getReferenceBar()),
                hit.getReferenceBar().getHigh() != null ? hit.getReferenceBar().getHigh() : 0);
    }

    public static String buildSignalDetail(Gc2Evaluator.Gc2Evaluation eval, char tier) {
        Gc2BreakoutTools.TierHit hit = eval.hitForTier(tier);
        if (hit == null || hit.getReferenceBar() == null || hit.getSignalBar() == null) {
            return "";
        }
        Trade ref = hit.getReferenceBar();
        Trade sig = hit.getSignalBar();
        String periodLabel = tier == 'S' ? "日K" : "周K";
        return String.format("%s突破金叉高,中间K close<refHigh,refDay=%s,refHigh=%.2f,sigDay=%s,sigClose=%.2f,tier=%s",
                periodLabel,
                dayOf(ref),
                ref.getHigh() != null ? ref.getHigh() : 0,
                dayOf(sig),
                sig.getClose() != null ? sig.getClose() : 0,
                tier);
    }

    private static String buildShortMacroDetail(Gc2Evaluator.Gc2Evaluation eval) {
        if (eval.isMonthMacdPositive() && eval.isWeekMacdPositive()) {
            return "月/周MACD>0";
        }
        if (eval.isMonthMacdPositive()) {
            return "月MACD>0";
        }
        return "周MACD>0";
    }

    private static String buildLongMacroDetail(Gc2Evaluator.Gc2Evaluation eval) {
        if (eval.isYearMacdPositive() && eval.isMonthMacdPositive()) {
            return "年/月MACD>0";
        }
        if (eval.isYearMacdPositive()) {
            return "年MACD>0";
        }
        return "月MACD>0";
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }
}
