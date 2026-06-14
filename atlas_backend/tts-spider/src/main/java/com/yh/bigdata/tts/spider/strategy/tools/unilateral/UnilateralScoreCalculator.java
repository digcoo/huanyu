package com.yh.bigdata.tts.spider.strategy.tools.unilateral;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;

/**
 * 金叉策略 v3.0 · 评分与展示档位（S=短线 A=中线 B=长线）
 */
public final class UnilateralScoreCalculator {

    private UnilateralScoreCalculator() {
    }

    public static int computeScore(UnilateralTrendEvaluator.UnilateralEvaluation eval) {
        int score = 0;
        if (eval.isShortHit()) {
            score += 40;
            if (eval.isWeekMacdPositive()) {
                score += 10;
            }
        }
        if (eval.isMediumHit()) {
            score += 35;
            if (eval.isMonthMacdPositive()) {
                score += 10;
            }
        }
        if (eval.isLongHit()) {
            score += 30;
            if (eval.isYearMacdPositive()) {
                score += 10;
            }
        }
        int modeCount = (eval.isShortHit() ? 1 : 0) + (eval.isMediumHit() ? 1 : 0) + (eval.isLongHit() ? 1 : 0);
        if (modeCount >= 2) {
            score += 15;
        }
        if (modeCount >= 3) {
            score += 10;
        }
        return score;
    }

    /** S=短线 A=中线 B=长线；多档同时命中时优先展示更短周期 */
    public static char computeTier(UnilateralTrendEvaluator.UnilateralEvaluation eval) {
        if (eval.isShortHit()) {
            return 'S';
        }
        if (eval.isMediumHit()) {
            return 'A';
        }
        if (eval.isLongHit()) {
            return 'B';
        }
        return 'N';
    }

    public static PeriodTypeEnum trendPeriodForTier(char tier) {
        switch (tier) {
            case 'S':
                return PeriodTypeEnum.WEEK;
            case 'A':
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
            case 'A':
                return PeriodTypeEnum.WEEK;
            case 'B':
                return PeriodTypeEnum.MONTH;
            default:
                return PeriodTypeEnum.DAY;
        }
    }

    public static String buildTrendLabel(char tier) {
        switch (tier) {
            case 'S':
                return "短线金叉";
            case 'A':
                return "中线金叉";
            case 'B':
                return "长线金叉";
            default:
                return "MACD金叉";
        }
    }

    public static String buildTrendDetail(UnilateralTrendEvaluator.UnilateralEvaluation eval) {
        StringBuilder sb = new StringBuilder();
        if (eval.isShortHit()) {
            sb.append("周MACD>0,");
        }
        if (eval.isMediumHit()) {
            sb.append("月MACD>0,");
        }
        if (eval.isLongHit()) {
            sb.append("年MACD>0,");
        }
        if (sb.length() == 0) {
            return "MACD";
        }
        return sb.substring(0, sb.length() - 1);
    }

    public static String buildSignalDetail(UnilateralTrendEvaluator.UnilateralEvaluation eval) {
        StringBuilder sb = new StringBuilder();
        if (eval.isShortHit()) {
            sb.append("日K MACD金叉,");
        }
        if (eval.isMediumHit()) {
            sb.append("周K MACD金叉,");
        }
        if (eval.isLongHit()) {
            sb.append("月K MACD金叉,");
        }
        if (sb.length() == 0) {
            return "";
        }
        return sb.substring(0, sb.length() - 1);
    }
}
