package com.yh.bigdata.tts.spider.strategy.tools.cascade;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.CascadeStrategyParams;
import com.yh.bigdata.tts.spider.strategy.tools.macd.MacdCrossStructureTools;

public final class CascadeScoreCalculator {

    private CascadeScoreCalculator() {
    }

    public static int computeScore(CascadeEvaluator.CascadeEvaluation eval) {
        int score = 35;
        int hits = 0;
        if (eval.getDayHit() != null) {
            score += 10;
            hits++;
        }
        if (eval.getWeekHit() != null) {
            score += 20;
            hits++;
        }
        if (eval.getMonthHit() != null) {
            score += 30;
            hits++;
        }
        if (hits > 1) {
            score += 10;
        }
        return score;
    }

    public static CascadeBreakoutTools.TierHit primaryHit(CascadeEvaluator.CascadeEvaluation eval) {
        if (eval.getMonthHit() != null) {
            return eval.getMonthHit();
        }
        if (eval.getWeekHit() != null) {
            return eval.getWeekHit();
        }
        return eval.getDayHit();
    }

    public static PeriodTypeEnum primaryPeriod(CascadeEvaluator.CascadeEvaluation eval) {
        CascadeBreakoutTools.TierHit hit = primaryHit(eval);
        return hit != null ? hit.getSignalTier() : PeriodTypeEnum.DAY;
    }

    public static String buildTrendMessage(CascadeEvaluator.CascadeEvaluation eval, CascadeStrategyParams params) {
        StringBuilder sb = new StringBuilder("[CASCADE]级联交叉突破");
        CascadeStrategyParams p = params != null ? params : CascadeStrategyParams.defaults();
        if (p.isEnableDualLowGate() || p.isEnableMacdGate() || p.isEnableCrossLowGate()
                || p.isEnableBarHighGate()) {
            sb.append("|可选四门");
        }
        appendTierTrendLabel(sb, eval.getMonthHit());
        appendTierTrendLabel(sb, eval.getWeekHit());
        appendTierTrendLabel(sb, eval.getDayHit());
        return sb.toString();
    }

    public static String buildSignalMessage(CascadeBreakoutTools.TierHit hit) {
        if (hit == null || hit.getReferenceBar() == null || hit.getTodayDayBar() == null) {
            return "";
        }
        Trade ref = hit.getReferenceBar();
        Trade today = hit.getTodayDayBar();
        Trade prev = hit.getPrevDayBar();
        return String.format(
                "级联交叉突破,crossType=%s,signalTier=%s,refPeriod=%s,breakPath=%s,refDay=%s,refHigh=%.2f,"
                        + "sigDay=%s,sigClose=%.2f,prevDay=%s,prevClose=%.2f",
                crossTypeCode(hit.getCrossKind()),
                hit.getSignalTier().getCode(),
                hit.getSignalTier().getCode(),
                hit.getBreakoutPath().name(),
                dayOf(ref),
                ref.getHigh() != null ? ref.getHigh() : 0,
                dayOf(today),
                today.getClose() != null ? today.getClose() : 0,
                dayOf(prev),
                prev != null && prev.getClose() != null ? prev.getClose() : 0);
    }

    private static void appendTierTrendLabel(StringBuilder sb, CascadeBreakoutTools.TierHit hit) {
        if (hit != null) {
            sb.append('|').append(periodLabel(hit.getSignalTier())).append("基准级联突破");
        }
    }

    private static String crossTypeCode(MacdCrossStructureTools.CrossKind kind) {
        return kind == MacdCrossStructureTools.CrossKind.DEATH ? "DC" : "GC";
    }

    private static String periodLabel(PeriodTypeEnum period) {
        if (period == PeriodTypeEnum.MONTH) {
            return "月K";
        }
        if (period == PeriodTypeEnum.WEEK) {
            return "周K";
        }
        return "日K";
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }
}
