package com.yh.bigdata.tts.spider.strategy.tools.frictionless;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.FrictionlessLadderStrategyParams;

public final class FrictionlessLadderScoreCalculator {

    private static final String NRF_GATE = StrategyGlobalGateTools.FULL_GATE_LABEL;

    private FrictionlessLadderScoreCalculator() {
    }

    public static String buildTrendMessage(FrictionlessLadderEvaluator.Evaluation eval) {
        String tierLabel = tierLabel(eval.getActiveTier());
        CrossPeriodInBarBreakoutTools.Hit hit = eval.getPeriodHit();
        if (hit == null) {
            return "[NRF]跨周期内梯子上移|" + tierLabel + "|" + NRF_GATE;
        }
        return "[NRF]跨周期内梯子上移|" + tierLabel + "|" + NRF_GATE + "|"
                + periodLabel(hit.getPeriod()) + "柱内上移";
    }

    public static String buildSignalMessage(FrictionlessLadderEvaluator.Evaluation eval) {
        CrossPeriodInBarBreakoutTools.Hit hit = eval.getPeriodHit();
        if (hit == null || hit.getReferenceBar() == null || hit.getSignalBar() == null) {
            return "";
        }
        Trade ref = hit.getReferenceBar();
        Trade sig = hit.getSignalBar();
        return String.format("柱内突破K,period=%s,refDay=%s,refHigh=%.2f,sigDay=%s,sigClose=%.2f",
                hit.getPeriod().getCode(),
                dayOf(ref),
                ref.getHigh() != null ? ref.getHigh() : 0,
                dayOf(sig),
                sig.getClose() != null ? sig.getClose() : 0);
    }

    public static int computeScore(FrictionlessLadderEvaluator.Evaluation eval) {
        return eval.getScore();
    }

    private static String tierLabel(FrictionlessLadderStrategyParams.ActiveTier tier) {
        if (tier == FrictionlessLadderStrategyParams.ActiveTier.MEDIUM) {
            return "周K跨月桶";
        }
        if (tier == FrictionlessLadderStrategyParams.ActiveTier.LONG) {
            return "月K跨年桶";
        }
        return "日K跨周桶";
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
