package com.yh.bigdata.tts.spider.strategy.tools.resonance;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;

/**
 * 周期共振 · 评分与展示档位（S=短线 A=中线 B=长线）
 */
public final class ResonanceScoreCalculator {

    private ResonanceScoreCalculator() {
    }

    public static int computeScore(ResonanceEvaluator.ResonanceEvaluation eval) {
        int score = 0;
        if (eval.isShortHit()) {
            score += 40;
        }
        if (eval.isMediumHit()) {
            score += 35;
        }
        if (eval.isLongHit()) {
            score += 30;
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

    public static char computeTier(ResonanceEvaluator.ResonanceEvaluation eval) {
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
                return "短线共振";
            case 'A':
                return "中线共振";
            case 'B':
                return "长线共振";
            default:
                return "周期共振";
        }
    }

    public static String buildTrendDetail(ResonanceEvaluator.ResonanceEvaluation eval) {
        StringBuilder sb = new StringBuilder();
        if (eval.isShortHit()) {
            sb.append("周MACD>0,日MACD>0非金叉,");
        }
        if (eval.isMediumHit()) {
            sb.append("月MACD>0,周MACD>0非金叉,");
        }
        if (eval.isLongHit()) {
            sb.append("年MACD>0,月MACD>0非金叉,");
        }
        if (sb.length() == 0) {
            return "MACD";
        }
        return sb.substring(0, sb.length() - 1);
    }

    public static String buildSignalDetail(ResonanceEvaluator.ResonanceEvaluation eval) {
        StringBuilder sb = new StringBuilder();
        if (eval.isShortHit()) {
            sb.append("日K突破前高/实体,");
        }
        if (eval.isMediumHit()) {
            sb.append("周K突破前高/实体,");
        }
        if (eval.isLongHit()) {
            sb.append("月K突破前高/实体,");
        }
        if (sb.length() == 0) {
            return "";
        }
        return sb.substring(0, sb.length() - 1);
    }
}
