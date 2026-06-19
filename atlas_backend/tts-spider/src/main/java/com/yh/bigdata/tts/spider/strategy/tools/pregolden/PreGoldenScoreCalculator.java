package com.yh.bigdata.tts.spider.strategy.tools.pregolden;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;

/**
 * 预判金叉 v2.0 · 评分与展示档位（S=短线 B=长线）
 */
public final class PreGoldenScoreCalculator {

    private PreGoldenScoreCalculator() {
    }

    public static int computeScore(PreGoldenEvaluator.PreGoldenEvaluation eval) {
        int score = 0;
        if (eval.isShortHit()) {
            score += 40;
            if (eval.isMonthMacdPositive() && eval.isWeekMacdPositive()) {
                score += 10;
            }
        }
        if (eval.isLongHit()) {
            score += 35;
            if (eval.isYearMacdPositive() && eval.isMonthMacdPositive()) {
                score += 10;
            }
        }
        if (eval.isShortHit() && eval.isLongHit()) {
            score += 15;
        }
        return score;
    }

    public static char computeTier(PreGoldenEvaluator.PreGoldenEvaluation eval) {
        if (eval.isShortHit()) {
            return 'S';
        }
        if (eval.isLongHit()) {
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
                return "短线预判";
            case 'B':
                return "长线预判";
            default:
                return "预判金叉";
        }
    }

    public static String buildTrendDetail(PreGoldenEvaluator.PreGoldenEvaluation eval) {
        if (eval.isShortHit()) {
            return buildShortMacroDetail(eval) + ",日MACD<0";
        }
        if (eval.isLongHit()) {
            return buildLongMacroDetail(eval) + ",周MACD<0";
        }
        return "MACD";
    }

    public static String buildSignalDetail(PreGoldenEvaluator.PreGoldenEvaluation eval) {
        StringBuilder sb = new StringBuilder();
        if (eval.isShortHit()) {
            sb.append("日K close>前高,");
        }
        if (eval.isLongHit()) {
            sb.append("周K close>前高,");
        }
        if (sb.length() == 0) {
            return "";
        }
        return sb.substring(0, sb.length() - 1);
    }

    private static String buildShortMacroDetail(PreGoldenEvaluator.PreGoldenEvaluation eval) {
        if (eval.isMonthMacdPositive() && eval.isWeekMacdPositive()) {
            return "月/周MACD>0";
        }
        if (eval.isMonthMacdPositive()) {
            return "月MACD>0";
        }
        return "周MACD>0";
    }

    private static String buildLongMacroDetail(PreGoldenEvaluator.PreGoldenEvaluation eval) {
        if (eval.isYearMacdPositive() && eval.isMonthMacdPositive()) {
            return "年/月MACD>0";
        }
        if (eval.isYearMacdPositive()) {
            return "年MACD>0";
        }
        return "月MACD>0";
    }
}
