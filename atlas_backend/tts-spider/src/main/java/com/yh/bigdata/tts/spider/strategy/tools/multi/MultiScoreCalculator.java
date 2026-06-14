package com.yh.bigdata.tts.spider.strategy.tools.multi;

/**
 * 多周期强势 v2.0 · score 与档位
 */
public final class MultiScoreCalculator {

    private MultiScoreCalculator() {
    }

    public static char computeTier(MultiStrengthEvaluator.MultiEvaluation eval) {
        if (eval.getSnapshot().hit) {
            return 'A';
        }
        return 'N';
    }

    public static int computeScore(MultiStrengthEvaluator.MultiEvaluation eval) {
        MultiMacdBreakoutTools.MacdBreakoutSnapshot snap = eval.getSnapshot();
        int score = 0;
        if (snap.weekMacdPositive) {
            score += 20;
        }
        if (snap.monthMacdPositive) {
            score += 20;
        }
        if (snap.dayBreakout) {
            score += 35;
        }
        if (snap.hit) {
            score += 25;
        }
        return score;
    }

    public static String buildTrendLabel(char tier) {
        if (tier == 'A') {
            return "周月MACD多头";
        }
        return "";
    }

    public static String buildTrendDetail(MultiMacdBreakoutTools.MacdBreakoutSnapshot snap) {
        StringBuilder sb = new StringBuilder();
        if (snap.weekMacdPositive) {
            sb.append("周MACD>0,");
        }
        if (snap.monthMacdPositive) {
            sb.append("月MACD>0,");
        }
        if (sb.length() == 0) {
            return "MACD";
        }
        return sb.substring(0, sb.length() - 1);
    }

    public static String buildSignalDetail(MultiMacdBreakoutTools.MacdBreakoutSnapshot snap) {
        return snap.dayBreakout ? "日K破前日高/实体" : "";
    }
}
