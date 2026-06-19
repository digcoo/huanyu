package com.yh.bigdata.tts.spider.strategy.tools.trend;

import com.yh.bigdata.tts.common.model.Trade;

public final class TrendV2ScoreCalculator {

    private TrendV2ScoreCalculator() {
    }

    public static int computeScore(TrendV2Evaluator.TrendV2Evaluation eval) {
        int score = 40;
        if (eval.getHit() != null && eval.getHit().getSignalBar() != null) {
            Trade signal = eval.getHit().getSignalBar();
            if (signal.getShitiRate() != null) {
                score += (int) (signal.getShitiRate() * 300);
            }
        }
        return Math.max(score, 20);
    }

    public static String buildTrendMessage(TrendV2Evaluator.TrendV2Evaluation eval) {
        if (eval.getHit() == null || eval.getHit().getReferenceBar() == null) {
            return "[SHORT]短线·前N周局部新高/最近强K基准";
        }
        Trade ref = eval.getHit().getReferenceBar();
        double bodyPct = ref.getShitiRate() != null ? ref.getShitiRate() * 100 : 0;
        return String.format("[SHORT]短线·周内局部新高/最近强K基准|refDay=%s,refHigh=%.2f,实体+%.1f%%,窗口%d根",
                safeDay(ref),
                ref.getHigh() != null ? ref.getHigh() : 0,
                bodyPct,
                eval.getHit().getScanWindowSize());
    }

    public static String buildSignalMessage(TrendV2Evaluator.TrendV2Evaluation eval) {
        if (eval.getHit() == null || eval.getHit().getSignalBar() == null
                || eval.getHit().getReferenceBar() == null) {
            return "";
        }
        Trade sig = eval.getHit().getSignalBar();
        Trade ref = eval.getHit().getReferenceBar();
        double bodyPct = sig.getShitiRate() != null ? sig.getShitiRate() * 100 : 0;
        return String.format("日K突破 close>前K high且>ref low,(sig low或前K close)<ref high,实体+%.1f%%,refDay=%s,sigDay=%s",
                bodyPct, safeDay(ref), safeDay(sig));
    }

    private static String safeDay(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }
}
