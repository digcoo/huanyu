package com.yh.bigdata.tts.spider.strategy.tools.ultralow;

import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.StrategyGlobalGateTools;

public final class UltraShortScoreCalculator {

    private UltraShortScoreCalculator() {
    }

    public static int computeScore(UltraShortEvaluator.UltraShortEvaluation eval) {
        int score = 40;
        if (eval.getHit() != null && eval.getHit().getSignalBar() != null) {
            Trade signal = eval.getHit().getSignalBar();
            if (signal.getShitiRate() != null) {
                score += (int) (signal.getShitiRate() * 400);
            }
        }
        return Math.max(score, 20);
    }

    public static String buildTrendMessage(UltraShortEvaluator.UltraShortEvaluation eval) {
        String prefix = "[ULTRA]超短线|" + StrategyGlobalGateTools.FULL_GATE_LABEL;
        if (eval.getHit() == null || eval.getHit().getReferenceBar() == null) {
            return prefix + "|30m跨日桶柱内上移";
        }
        Trade ref = eval.getHit().getReferenceBar();
        return String.format(prefix + "|30m跨日桶柱内上移|refDay=%s,refHigh=%.2f,窗口%d根",
                safeDay(ref),
                ref.getHigh() != null ? ref.getHigh() : 0,
                eval.getHit().getScanWindowSize());
    }

    public static String buildSignalMessage(UltraShortEvaluator.UltraShortEvaluation eval) {
        if (eval.getHit() == null || eval.getHit().getSignalBar() == null
                || eval.getHit().getReferenceBar() == null) {
            return "";
        }
        Trade sig = eval.getHit().getSignalBar();
        Trade ref = eval.getHit().getReferenceBar();
        return String.format("柱内突破K,period=min30,refDay=%s,refHigh=%.2f,sigDay=%s,sigClose=%.2f",
                safeDay(ref),
                ref.getHigh() != null ? ref.getHigh() : 0,
                safeDay(sig),
                sig.getClose() != null ? sig.getClose() : 0);
    }

    private static String safeDay(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }
}
