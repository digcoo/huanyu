package com.yh.bigdata.tts.spider.strategy.tools.macedge;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MacdEdgeStrategyParams;
import com.yh.bigdata.tts.spider.strategy.tools.macd.MacdCrossStructureTools;

public final class MacdEdgeScoreCalculator {

    private MacdEdgeScoreCalculator() {
    }

    public static int computeScore(MacdEdgeEvaluator.MacdEdgeEvaluation eval) {
        int score = 30;
        int hits = 0;
        if (eval.getMin30Hit() != null) {
            score += 6;
            hits++;
        }
        if (eval.getDayHit() != null) {
            score += 10;
            hits++;
        }
        if (eval.getWeekHit() != null) {
            score += 14;
            hits++;
        }
        if (eval.getMonthHit() != null) {
            score += 18;
            hits++;
        }
        if (eval.getYearHit() != null) {
            score += 22;
            hits++;
        }
        if (hits > 1) {
            score += 8;
        }
        return score;
    }

    public static MacdEdgeBreakoutTools.TierHit primaryHit(MacdEdgeEvaluator.MacdEdgeEvaluation eval) {
        if (eval.getYearHit() != null) {
            return eval.getYearHit();
        }
        if (eval.getMonthHit() != null) {
            return eval.getMonthHit();
        }
        if (eval.getWeekHit() != null) {
            return eval.getWeekHit();
        }
        if (eval.getDayHit() != null) {
            return eval.getDayHit();
        }
        return eval.getMin30Hit();
    }

    public static PeriodTypeEnum primaryPeriod(MacdEdgeEvaluator.MacdEdgeEvaluation eval) {
        MacdEdgeBreakoutTools.TierHit hit = primaryHit(eval);
        return hit != null ? hit.getSignalTier() : PeriodTypeEnum.DAY;
    }

    public static String buildTrendMessage(MacdEdgeEvaluator.MacdEdgeEvaluation eval,
                                           MacdEdgeStrategyParams params) {
        StringBuilder sb = new StringBuilder("[MACEDGE]MACD交叉边沿突破");
        MacdEdgeStrategyParams p = params != null ? params : MacdEdgeStrategyParams.defaults();
        if (p.isEnableDualLowGate() || p.isEnableMacdGate() || p.isEnableMacdDcHighGate()
                || p.isEnableCrossLowGate()
                || p.isEnableBarHighGate() || p.isEnableAllYangGate()) {
            sb.append("|可选六门");
        }
        appendTierTrendLabel(sb, eval.getYearHit());
        appendTierTrendLabel(sb, eval.getMonthHit());
        appendTierTrendLabel(sb, eval.getWeekHit());
        appendTierTrendLabel(sb, eval.getDayHit());
        appendTierTrendLabel(sb, eval.getMin30Hit());
        return sb.toString();
    }

    public static String buildSignalMessage(MacdEdgeBreakoutTools.TierHit hit) {
        if (hit == null || hit.getReferenceBar() == null || hit.getSignalBar() == null) {
            return "";
        }
        Trade ref = hit.getReferenceBar();
        Trade signal = hit.getSignalBar();
        Trade prev = hit.getPrevBar();
        return String.format(
                "MACD交叉边沿突破,crossType=%s,signalTier=%s,refPeriod=%s,refDay=%s,refHigh=%.2f,"
                        + "sigDay=%s,sigClose=%.2f,prevDay=%s,prevClose=%.2f",
                crossTypeCode(hit.getCrossKind()),
                hit.getSignalTier().getCode(),
                hit.getSignalTier().getCode(),
                dayOf(ref),
                ref.getHigh() != null ? ref.getHigh() : 0,
                dayOf(signal),
                signal.getClose() != null ? signal.getClose() : 0,
                dayOf(prev),
                prev != null && prev.getClose() != null ? prev.getClose() : 0);
    }

    private static void appendTierTrendLabel(StringBuilder sb, MacdEdgeBreakoutTools.TierHit hit) {
        if (hit != null) {
            sb.append('|').append(periodLabel(hit.getSignalTier())).append("基准边沿突破");
        }
    }

    private static String crossTypeCode(MacdCrossStructureTools.CrossKind kind) {
        return kind == MacdCrossStructureTools.CrossKind.DEATH ? "DC" : "GC";
    }

    private static String periodLabel(PeriodTypeEnum period) {
        if (period == PeriodTypeEnum.YEAR) {
            return "年K";
        }
        if (period == PeriodTypeEnum.MONTH) {
            return "月K";
        }
        if (period == PeriodTypeEnum.WEEK) {
            return "周K";
        }
        if (period == PeriodTypeEnum.MIN30) {
            return "Min30";
        }
        return "日K";
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }
}
