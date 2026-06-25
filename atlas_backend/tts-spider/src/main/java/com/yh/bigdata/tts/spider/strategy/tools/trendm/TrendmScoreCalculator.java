package com.yh.bigdata.tts.spider.strategy.tools.trendm;

import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.strategy.tools.bogo.BogoBreakoutTools;
import com.yh.bigdata.tts.spider.strategy.tools.bogo.BogoStructureTools;

/**
 * 趋势策略 · 评分与展示
 */
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.StrategyGlobalGateTools;

public final class TrendmScoreCalculator {

    private TrendmScoreCalculator() {
    }

    public static int computeScore(TrendmEvaluator.TrendmEvaluation eval) {
        return 80;
    }

    public static String buildTrendMessage(TrendmEvaluator.TrendmEvaluation eval) {
        return "[TRENDM]趋势|" + StrategyGlobalGateTools.FULL_GATE_LABEL + "|日周月基准突破|min30梯子";
    }

    public static String buildDaySignal(BogoBreakoutTools.PeriodHit hit) {
        return buildPeriodSignal("day", hit);
    }

    public static String buildWeekSignal(BogoBreakoutTools.PeriodHit hit) {
        return buildPeriodSignal("week", hit);
    }

    public static String buildMonthSignal(BogoBreakoutTools.PeriodHit hit) {
        return buildPeriodSignal("month", hit);
    }

    private static String buildPeriodSignal(String periodCode, BogoBreakoutTools.PeriodHit hit) {
        if (hit == null || hit.getReferenceBar() == null || hit.getSignalBar() == null) {
            return "";
        }
        Trade ref = hit.getReferenceBar();
        Trade sig = hit.getSignalBar();
        String crossType = hit.getCrossKind() == BogoStructureTools.CrossKind.GOLDEN ? "GC" : "DC";
        return String.format("突破基准K,crossType=%s,period=%s,refDay=%s,refHigh=%.2f,sigDay=%s,sigClose=%.2f",
                crossType,
                periodCode,
                dayOf(ref),
                ref.getHigh() != null ? ref.getHigh() : 0,
                dayOf(sig),
                sig.getClose() != null ? sig.getClose() : 0);
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }
}
