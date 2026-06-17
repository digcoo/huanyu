package com.yh.bigdata.tts.spider.strategy.tools.gc2;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;

/**
 * 金叉二次突破 · 评分与展示
 */
public final class Gc2ScoreCalculator {

    private Gc2ScoreCalculator() {
    }

    public static int computeScore(Gc2Evaluator.Gc2Evaluation eval) {
        int score = 0;
        if (eval.getShortHit() != null) {
            score += 40;
        }
        if (eval.getMediumHit() != null) {
            score += 35;
        }
        if (eval.getLongHit() != null) {
            score += 30;
        }
        if (eval.getShortHit() != null && eval.getMediumHit() != null) {
            score += 10;
        }
        if (eval.getShortHit() != null && eval.getMediumHit() != null && eval.getLongHit() != null) {
            score += 10;
        }
        return Math.max(score, 20);
    }

    public static char computeTier(Gc2Evaluator.Gc2Evaluation eval) {
        if (eval.getShortHit() != null) {
            return 'S';
        }
        if (eval.getMediumHit() != null) {
            return 'A';
        }
        if (eval.getLongHit() != null) {
            return 'B';
        }
        return 'N';
    }

    public static PeriodTypeEnum trendPeriodForTier(char tier) {
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

    public static PeriodTypeEnum signalPeriodForTier(char tier) {
        return trendPeriodForTier(tier);
    }

    public static String buildTrendLabel(char tier) {
        switch (tier) {
            case 'S':
                return "短线·金叉二次突破";
            case 'A':
                return "中线·金叉二次突破";
            case 'B':
                return "长线·金叉二次突破";
            default:
                return "金叉二次突破";
        }
    }

    public static String buildTrendDetail(Gc2Evaluator.Gc2Evaluation eval, char tier) {
        Gc2BreakoutTools.TierHit hit = eval.hitForTier(tier);
        if (hit == null || hit.getReferenceBar() == null) {
            return tierLabel(tier) + "MACD金叉后突破";
        }
        return String.format("金叉K=%s,突破金叉高点,refHigh=%.2f",
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
        return String.format("%s突破金叉高,refDay=%s,refHigh=%.2f,sigDay=%s,sigHigh=%.2f,tier=%s",
                tierLabel(tier),
                dayOf(ref),
                ref.getHigh() != null ? ref.getHigh() : 0,
                dayOf(sig),
                sig.getHigh() != null ? sig.getHigh() : 0,
                tier);
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }

    private static String tierLabel(char tier) {
        switch (tier) {
            case 'S':
                return "短线";
            case 'A':
                return "中线";
            case 'B':
                return "长线";
            default:
                return "";
        }
    }
}
