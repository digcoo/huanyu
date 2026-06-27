package com.yh.bigdata.tts.spider.strategy.tools.pillar;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.StrategyGlobalGateTools;

public final class PillarScoreCalculator {

    private PillarScoreCalculator() {
    }

    public static int computeScore(PillarEvaluator.PillarEvaluation eval) {
        int score = 30;
        if (eval.getDayHit() != null) {
            score += 15;
        }
        if (eval.getWeekHit() != null) {
            score += 20;
        }
        if (eval.getMonthHit() != null) {
            score += 25;
        }
        int hits = 0;
        if (eval.getDayHit() != null) {
            hits++;
        }
        if (eval.getWeekHit() != null) {
            hits++;
        }
        if (eval.getMonthHit() != null) {
            hits++;
        }
        if (hits > 1) {
            score += 10;
        }
        return Math.max(score, 20);
    }

    public static PeriodTypeEnum primaryPeriod(PillarEvaluator.PillarEvaluation eval) {
        if (eval.getMonthHit() != null) {
            return PeriodTypeEnum.MONTH;
        }
        if (eval.getWeekHit() != null) {
            return PeriodTypeEnum.WEEK;
        }
        if (eval.getDayHit() != null) {
            return PeriodTypeEnum.DAY;
        }
        return PeriodTypeEnum.DAY;
    }

    public static PillarBreakoutTools.PeriodHit primaryHit(PillarEvaluator.PillarEvaluation eval) {
        if (eval.getMonthHit() != null) {
            return eval.getMonthHit();
        }
        if (eval.getWeekHit() != null) {
            return eval.getWeekHit();
        }
        return eval.getDayHit();
    }

    public static String buildTrendMessage(PillarEvaluator.PillarEvaluation eval) {
        PillarBreakoutTools.PeriodHit hit = primaryHit(eval);
        if (hit == null) {
            return "[PILLAR]柱子内上移|" + StrategyGlobalGateTools.FULL_GATE_LABEL;
        }
        return "[PILLAR]柱子内上移|" + StrategyGlobalGateTools.FULL_GATE_LABEL + "|"
                + periodLabel(hit.getPeriod()) + "强柱基准突破";
    }

    public static String buildSignalMessage(PillarEvaluator.PillarEvaluation eval) {
        PillarBreakoutTools.PeriodHit hit = primaryHit(eval);
        if (hit == null || hit.getReferenceBar() == null || hit.getSignalBar() == null) {
            return "";
        }
        Trade ref = hit.getReferenceBar();
        Trade sig = hit.getSignalBar();
        return String.format("突破基准K,period=%s,refDay=%s,refHigh=%.2f,sigDay=%s,sigClose=%.2f",
                hit.getPeriod().getCode(),
                dayOf(ref),
                ref.getHigh() != null ? ref.getHigh() : 0,
                dayOf(sig),
                sig.getClose() != null ? sig.getClose() : 0);
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
