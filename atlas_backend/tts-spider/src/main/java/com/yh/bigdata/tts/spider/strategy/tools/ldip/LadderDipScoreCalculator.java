package com.yh.bigdata.tts.spider.strategy.tools.ldip;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.LadderDipStrategyParams;

public final class LadderDipScoreCalculator {

    private LadderDipScoreCalculator() {
    }

    public static int computeScore(LadderDipEvaluator.LadderDipEvaluation eval) {
        int score = 40;
        int hits = 0;
        if (eval.getDayHit() != null) {
            score += 8;
            hits++;
        }
        if (eval.getWeekHit() != null) {
            score += 12;
            hits++;
        }
        if (eval.getMonthHit() != null) {
            score += 16;
            hits++;
        }
        if (hits > 1) {
            score += 8;
        }
        return score;
    }

    public static PeriodTypeEnum primaryPeriod(LadderDipEvaluator.LadderDipEvaluation eval) {
        if (eval.getMonthHit() != null) {
            return PeriodTypeEnum.MONTH;
        }
        if (eval.getWeekHit() != null) {
            return PeriodTypeEnum.WEEK;
        }
        return PeriodTypeEnum.DAY;
    }

    public static String buildTrendMessage(LadderDipEvaluator.LadderDipEvaluation eval,
                                           LadderDipStrategyParams params) {
        StringBuilder sb = new StringBuilder("[LDIP]级联梯子探底回升");
        LadderDipStrategyParams p = params != null ? params : LadderDipStrategyParams.defaults();
        if (p.isEnableDualLowGate() || p.isEnableMacdGate() || p.isEnableCrossLowGate()
                || p.isEnableBarHighGate()) {
            sb.append("|可选四门");
        }
        appendTierTrendLabel(sb, eval.getMonthHit());
        appendTierTrendLabel(sb, eval.getWeekHit());
        appendTierTrendLabel(sb, eval.getDayHit());
        return sb.toString();
    }

    public static String buildSignalMessage(LadderDipBreakoutTools.PeriodHit hit) {
        if (hit == null || hit.getReferenceBar() == null || hit.getLastBar() == null) {
            return "";
        }
        Trade ref = hit.getReferenceBar();
        Trade last = hit.getLastBar();
        Trade prev = hit.getPrevBar();
        return String.format(
                "级联梯子探底回升,period=%s,refDay=%s,refHigh=%.2f,refLow=%.2f,"
                        + "sigDay=%s,sigClose=%.2f,prevDay=%s,prevClose=%.2f",
                hit.getPeriod().getCode(),
                dayOf(ref),
                ref.getHigh() != null ? ref.getHigh() : 0,
                ref.getLow() != null ? ref.getLow() : 0,
                dayOf(last),
                last.getClose() != null ? last.getClose() : 0,
                dayOf(prev),
                prev != null && prev.getClose() != null ? prev.getClose() : 0);
    }

    private static void appendTierTrendLabel(StringBuilder sb, LadderDipBreakoutTools.PeriodHit hit) {
        if (hit != null) {
            sb.append('|').append(periodLabel(hit.getPeriod())).append("探底回升");
        }
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
