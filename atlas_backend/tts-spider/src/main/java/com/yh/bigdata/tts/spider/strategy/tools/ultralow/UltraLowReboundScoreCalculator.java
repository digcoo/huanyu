package com.yh.bigdata.tts.spider.strategy.tools.ultralow;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;

/**
 * 梯子突破 · 四档评分与展示（S=超短 A=短 B=中 C=长）
 */
public final class UltraLowReboundScoreCalculator {

    private UltraLowReboundScoreCalculator() {
    }

    public static int computeScore(UltraLowReboundEvaluator.UltraLowEvaluation eval) {
        int score = 0;
        if (eval.getUltraHit() != null) {
            score += 35;
        }
        if (eval.getShortHit() != null) {
            score += 30;
        }
        if (eval.getMediumHit() != null) {
            score += 25;
        }
        if (eval.getLongHit() != null) {
            score += 20;
        }
        int modeCount = (eval.getUltraHit() != null ? 1 : 0)
                + (eval.getShortHit() != null ? 1 : 0)
                + (eval.getMediumHit() != null ? 1 : 0)
                + (eval.getLongHit() != null ? 1 : 0);
        if (modeCount >= 2) {
            score += 12;
        }
        if (modeCount >= 3) {
            score += 8;
        }
        if (modeCount >= 4) {
            score += 5;
        }
        BreakoutLadderTools.TierHit primary = eval.primaryHit();
        if (primary != null && primary.getSignalBar() != null) {
            Trade signal = primary.getSignalBar();
            if (signal.getShitiRate() != null) {
                score += (int) (signal.getShitiRate() * 400);
            }
        }
        return Math.max(score, 20);
    }

    public static char computeTier(UltraLowReboundEvaluator.UltraLowEvaluation eval) {
        if (eval.getUltraHit() != null) {
            return 'S';
        }
        if (eval.getShortHit() != null) {
            return 'A';
        }
        if (eval.getMediumHit() != null) {
            return 'B';
        }
        if (eval.getLongHit() != null) {
            return 'C';
        }
        return 'N';
    }

    public static PeriodTypeEnum trendPeriodForTier(char tier) {
        switch (tier) {
            case 'S':
                return PeriodTypeEnum.MIN30;
            case 'A':
                return PeriodTypeEnum.DAY;
            case 'B':
                return PeriodTypeEnum.WEEK;
            case 'C':
                return PeriodTypeEnum.MONTH;
            default:
                return PeriodTypeEnum.MIN30;
        }
    }

    public static PeriodTypeEnum signalPeriodForTier(char tier) {
        return trendPeriodForTier(tier);
    }

    public static String buildTrendLabel(char tier) {
        switch (tier) {
            case 'S':
                return "超短·局部新高强K";
            case 'A':
                return "短线·局部新高强K";
            case 'B':
                return "中线·局部新高强K";
            case 'C':
                return "长线·局部新高强K";
            default:
                return "局部新高强K";
        }
    }

    public static String buildTrendDetail(UltraLowReboundEvaluator.UltraLowEvaluation eval, char tier) {
        BreakoutLadderTools.TierHit hit = eval.hitForTier(tier);
        if (hit == null || hit.getReferenceBar() == null) {
            return tierLabel(tier) + "背景局部新高强K";
        }
        return String.format("前高%.2f(局部新高),窗口%d根(信号桶%d根)",
                hit.getReferenceBar().getHigh(), hit.getScanWindowSize(), hit.getSignalBarCount());
    }

    public static String buildSignalDetail(UltraLowReboundEvaluator.UltraLowEvaluation eval, char tier) {
        BreakoutLadderTools.TierHit hit = eval.hitForTier(tier);
        if (hit == null || hit.getSignalBar() == null || hit.getReferenceBar() == null) {
            return "";
        }
        Trade signal = hit.getSignalBar();
        Trade ref = hit.getReferenceBar();
        double bodyPct = signal.getShitiRate() != null ? signal.getShitiRate() * 100 : 0;
        return String.format("%s首根突破 close>前K high且>ref low,实体+%.1f%%,refDay=%s,sigDay=%s",
                tierLabel(tier), bodyPct,
                ref.getDay() != null ? ref.getDay() : "",
                signal.getDay() != null ? signal.getDay() : "");
    }

    private static String tierLabel(char tier) {
        switch (tier) {
            case 'S':
                return "超短";
            case 'A':
                return "短线";
            case 'B':
                return "中线";
            case 'C':
                return "长线";
            default:
                return "";
        }
    }
}
