package com.yh.bigdata.tts.spider.strategy.tools.longterm;

import com.yh.bigdata.tts.common.model.Trade;

public final class LongScoreCalculator {

    private LongScoreCalculator() {
    }

    public static int computeScore(LongEvaluator.LongEvaluation eval) {
        int score = 40;
        if (eval.getHit() != null && eval.getHit().getSignalBar() != null) {
            Trade signal = eval.getHit().getSignalBar();
            if (signal.getShitiRate() != null) {
                score += (int) (signal.getShitiRate() * 250);
            }
        }
        return Math.max(score, 20);
    }

    public static String buildTrendMessage(LongEvaluator.LongEvaluation eval) {
        if (eval.getHit() == null || eval.getHit().getReferenceBar() == null) {
            return "[LONG]长线·前N年局部新高/最近强K基准";
        }
        Trade ref = eval.getHit().getReferenceBar();
        double bodyPct = ref.getShitiRate() != null ? ref.getShitiRate() * 100 : 0;
        return String.format("[LONG]长线·年内局部新高/最近强K基准|refDay=%s,refHigh=%.2f,实体+%.1f%%,窗口%d根",
                safeDay(ref),
                ref.getHigh() != null ? ref.getHigh() : 0,
                bodyPct,
                eval.getHit().getScanWindowSize());
    }

    public static String buildSignalMessage(LongEvaluator.LongEvaluation eval) {
        if (eval.getHit() == null || eval.getHit().getSignalBar() == null
                || eval.getHit().getReferenceBar() == null) {
            return "";
        }
        Trade sig = eval.getHit().getSignalBar();
        Trade ref = eval.getHit().getReferenceBar();
        double bodyPct = sig.getShitiRate() != null ? sig.getShitiRate() * 100 : 0;
        return String.format("月K突破 close>前K high且>ref low,(sig low或前K close)<ref high,实体+%.1f%%,refDay=%s,sigDay=%s",
                bodyPct, safeDay(ref), safeDay(sig));
    }

    private static String safeDay(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }
}
