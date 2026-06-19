package com.yh.bigdata.tts.spider.strategy.tools.dc2;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;

/**
 * 死叉突破 · 评分与展示（S=短线 B=长线）
 */
public final class Dc2ScoreCalculator {

    private Dc2ScoreCalculator() {
    }

    public static int computeScore(Dc2Evaluator.Dc2Evaluation eval) {
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

    public static char computeTier(Dc2Evaluator.Dc2Evaluation eval) {
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
                return "短线·死叉突破";
            case 'B':
                return "长线·死叉突破";
            default:
                return "死叉突破";
        }
    }

    public static String buildTrendDetail(Dc2Evaluator.Dc2Evaluation eval, char tier) {
        Dc2BreakoutTools.TierHit hit = eval.hitForTier(tier);
        String macro = tier == 'S' ? buildShortMacroDetail(eval) : buildLongMacroDetail(eval);
        if (hit == null || hit.getReferenceBar() == null) {
            return macro + ",MACD死叉后突破";
        }
        return macro + String.format(",死叉K=%s,refHigh=%.2f",
                dayOf(hit.getReferenceBar()),
                hit.getReferenceBar().getHigh() != null ? hit.getReferenceBar().getHigh() : 0);
    }

    public static String buildSignalDetail(Dc2Evaluator.Dc2Evaluation eval, char tier) {
        Dc2BreakoutTools.TierHit hit = eval.hitForTier(tier);
        if (hit == null || hit.getReferenceBar() == null || hit.getSignalBar() == null) {
            return "";
        }
        Trade ref = hit.getReferenceBar();
        Trade sig = hit.getSignalBar();
        String periodLabel = tier == 'S' ? "日K" : "周K";
        return String.format("%s突破死叉高,前根在ref下,refDay=%s,refHigh=%.2f,sigDay=%s,sigClose=%.2f,tier=%s",
                periodLabel,
                dayOf(ref),
                ref.getHigh() != null ? ref.getHigh() : 0,
                dayOf(sig),
                sig.getClose() != null ? sig.getClose() : 0,
                tier);
    }

    private static String buildShortMacroDetail(Dc2Evaluator.Dc2Evaluation eval) {
        if (eval.isMonthMacdPositive() && eval.isWeekMacdPositive()) {
            return "月/周MACD>0";
        }
        if (eval.isMonthMacdPositive()) {
            return "月MACD>0";
        }
        return "周MACD>0";
    }

    private static String buildLongMacroDetail(Dc2Evaluator.Dc2Evaluation eval) {
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
