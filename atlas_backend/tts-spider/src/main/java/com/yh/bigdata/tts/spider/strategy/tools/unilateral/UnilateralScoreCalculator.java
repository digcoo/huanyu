package com.yh.bigdata.tts.spider.strategy.tools.unilateral;

/**
 * 单边趋势 v1.0 · 评分与展示档位
 */
public final class UnilateralScoreCalculator {

    private UnilateralScoreCalculator() {
    }

    public static int computeScore(UnilateralTrendEvaluator.UnilateralEvaluation eval) {
        int score = 0;
        UnilateralContinuationTools.ContinuationContext ctx = eval.getContext();
        UnilateralContinuationTools.ContinuationTrigger trigger = eval.getTrigger();

        if (ctx.weekOverMa20) score += 10;
        if (ctx.weekMa5OverMa20) score += 10;
        if (ctx.weekOverLastLow) score += 10;
        if (ctx.monthOverMa20) score += 8;
        if (ctx.monthDirectionOk) score += 5;

        if (trigger.platformBreakout) score += 15;
        if (trigger.ma5Reclaim) score += 12;
        if (trigger.macdOk) score += 10;

        if (eval.isModeBFull()) score += 20;
        if (eval.isModeA()) score += 15;
        if (eval.isModeBFull() && eval.isModeA()) score += 25;

        return score;
    }

    /**
     * S=共振延续 A=完整延续 B=环境待确认 C=仅拐点
     */
    public static char computeTier(UnilateralTrendEvaluator.UnilateralEvaluation eval) {
        if (eval.isModeBFull()) {
            if (eval.isModeA() || eval.getContext().monthBonusCount() >= 2) {
                return 'S';
            }
            return 'A';
        }
        if (eval.isModeBWeak()) {
            return 'B';
        }
        if (eval.isModeA()) {
            return 'C';
        }
        return 'C';
    }

    public static String buildTrendLabel(char tier, UnilateralTrendEvaluator.UnilateralEvaluation eval) {
        switch (tier) {
            case 'S':
                return "趋势延续·共振";
            case 'A':
                return "梯子试盘·均线多头";
            case 'B':
                return "趋势环境·待确认";
            default:
                return "月K拐点·观察";
        }
    }

    public static String buildTrendDetail(UnilateralContinuationTools.ContinuationContext ctx) {
        StringBuilder sb = new StringBuilder();
        if (ctx.weekMa5OverMa20) sb.append("周MA5>MA20,");
        else if (ctx.weekOverMa20) sb.append("周MA20上,");
        if (ctx.weekOverLastLow) sb.append("周Low抬高,");
        if (ctx.monthOverMa20) sb.append("月MA20上,");
        if (ctx.monthDirectionOk) sb.append("月方向OK,");
        if (sb.length() == 0) {
            return "单边趋势";
        }
        return sb.substring(0, sb.length() - 1);
    }

    public static String buildSignalDetail(UnilateralContinuationTools.ContinuationTrigger trigger) {
        StringBuilder sb = new StringBuilder();
        if (trigger.platformBreakout) sb.append("日K平台突破,");
        if (trigger.ma5Reclaim) sb.append("日K梯子试盘,");
        if (trigger.macdOk) sb.append("日K MACD确认,");
        if (sb.length() == 0) {
            return "";
        }
        return sb.substring(0, sb.length() - 1);
    }
}
