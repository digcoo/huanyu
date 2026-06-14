package com.yh.bigdata.tts.spider.strategy.tools.ultralow;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;

/**
 * 超短线 · 评分与展示
 */
public final class UltraLowReboundScoreCalculator {

    private UltraLowReboundScoreCalculator() {
    }

    public static int computeScore(UltraLowReboundEvaluator.UltraLowEvaluation eval) {
        int score = 30;
        Min30BreakoutTools.BreakoutHit hit = eval.getBreakout();
        if (hit == null) {
            return score;
        }
        Trade signal = hit.getSignalBar();
        Trade ref = hit.getReferenceBar();
        if (signal != null && signal.getShitiRate() != null) {
            score += (int) (signal.getShitiRate() * 500);
        }
        if (signal != null && ref != null
                && signal.getHigh() != null && ref.getHigh() != null && ref.getHigh() > 0) {
            score += (int) ((signal.getHigh() - ref.getHigh()) / ref.getHigh() * 300);
        }
        return score;
    }

    public static char computeTier(UltraLowReboundEvaluator.UltraLowEvaluation eval) {
        return eval.isHit() ? 'S' : 'N';
    }

    public static PeriodTypeEnum trendPeriodForTier(char tier) {
        return PeriodTypeEnum.MIN30;
    }

    public static PeriodTypeEnum signalPeriodForTier(char tier) {
        return PeriodTypeEnum.MIN30;
    }

    public static String buildTrendLabel(char tier) {
        return "前日新高强K";
    }

    public static String buildTrendDetail(UltraLowReboundEvaluator.UltraLowEvaluation eval) {
        Min30BreakoutTools.BreakoutHit hit = eval.getBreakout();
        if (hit == null || hit.getReferenceBar() == null) {
            return "前1~2日30m新高强K";
        }
        return String.format("前高%.2f(区间新高),窗口%d根(今日%d根)",
                hit.getReferenceBar().getHigh(), hit.getScanWindowSize(), hit.getTodayBarCount());
    }

    public static String buildSignalDetail(UltraLowReboundEvaluator.UltraLowEvaluation eval) {
        Min30BreakoutTools.BreakoutHit hit = eval.getBreakout();
        if (hit == null || hit.getSignalBar() == null || hit.getReferenceBar() == null) {
            return "";
        }
        Trade signal = hit.getSignalBar();
        Trade ref = hit.getReferenceBar();
        double bodyPct = signal.getShitiRate() != null ? signal.getShitiRate() * 100 : 0;
        return String.format("当日30m high%.2f>%.2f close>bodyMax%.2f,实体+%.1f%%",
                signal.getHigh(), ref.getHigh(), ref.getShitiMax(), bodyPct);
    }
}
