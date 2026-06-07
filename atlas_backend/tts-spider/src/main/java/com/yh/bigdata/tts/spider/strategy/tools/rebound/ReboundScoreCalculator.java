package com.yh.bigdata.tts.spider.strategy.tools.rebound;

/**
 * 深坑反弹 v1.0 · 档位与 score
 */
public final class ReboundScoreCalculator {

    private ReboundScoreCalculator() {
    }

    public static char computeTier(ReboundEvaluator.ReboundEvaluation eval) {
        ReboundPitTools.DeepPitNarrative narrative = eval.getNarrative();
        ReboundTriggerTools.TriggerSnapshot trigger = eval.getTrigger();

        boolean fullNarrative = narrative.isComplete() && narrative.stillRecoveryZone;
        boolean breakBand = trigger.breakRevertBand();
        boolean lowUp = trigger.lowShangYi();
        boolean divergence = eval.isDivergence();

        if (fullNarrative && breakBand && (lowUp || divergence)) {
            return 'S';
        }
        if (eval.isModeA()) {
            return 'A';
        }
        if (eval.isModeB()) {
            return 'B';
        }
        if (eval.isModeC()) {
            return 'C';
        }
        return 'C';
    }

    public static int computeScore(ReboundEvaluator.ReboundEvaluation eval) {
        int score = 0;
        ReboundPitTools.DeepPitNarrative narrative = eval.getNarrative();
        ReboundPitTools.PitSnapshot week = eval.getWeekPit();
        ReboundPitTools.PitSnapshot month = eval.getMonthPit();
        ReboundTriggerTools.TriggerSnapshot trigger = eval.getTrigger();

        if (narrative.longDecline) score += 20;
        if (narrative.capitulation) score += 25;
        if (narrative.pitFloorHeld && narrative.abovePanicLow) score += 18;
        score += week.contextCount() * 8;
        score += month.contextCount() * 10;
        if (trigger.breakRevertBandWeek) score += 25;
        if (trigger.breakRevertBandMonth) score += 30;
        if (trigger.lowShangYiWeek) score += 15;
        if (trigger.lowShangYiDay) score += 10;
        if (trigger.macdCross) score += 12;
        if (trigger.yangAboveMa5) score += 8;
        if (eval.isDivergence()) score += 20;

        char tier = eval.getTier();
        if (tier == 'S') score += 20;
        else if (tier == 'A') score += 12;
        else if (tier == 'B') score += 6;

        return score;
    }

    public static String buildTrendLabel(char tier, ReboundEvaluator.ReboundEvaluation eval) {
        if (eval.isModeA()) {
            return "恐慌释放后脱离";
        }
        if (eval.isModeB()) {
            return "坑底反弹接入";
        }
        if (eval.isModeC()) {
            return "底背离反弹";
        }
        return "深坑修复";
    }

    public static String buildTrendDetail(ReboundPitTools.DeepPitNarrative narrative) {
        StringBuilder sb = new StringBuilder();
        if (narrative.longDecline) {
            sb.append("长期下跌");
        }
        if (narrative.capitulation) {
            if (sb.length() > 0) sb.append('+');
            sb.append("恐慌释放");
        }
        if (narrative.pitFloorHeld && narrative.abovePanicLow) {
            if (sb.length() > 0) sb.append('+');
            sb.append("坑底企稳");
        }
        return sb.length() > 0 ? sb.toString() : "深坑叙事未完成";
    }

    public static String buildSignalDetail(ReboundTriggerTools.TriggerSnapshot trigger,
                                           boolean divergence) {
        StringBuilder sb = new StringBuilder();
        if (trigger.breakRevertBandMonth) sb.append("月波段脱离,");
        else if (trigger.breakRevertBandWeek) sb.append("周波段脱离,");
        if (trigger.lowShangYiWeek) sb.append("周low上移,");
        if (trigger.lowShangYiDay) sb.append("日low上移,");
        if (trigger.macdCross) sb.append("日MACD,");
        if (trigger.yangAboveMa5) sb.append("日阳>MA5,");
        if (divergence) sb.append(ReboundDivergenceTools.SIGNAL_MSG).append(',');
        if (sb.length() == 0) {
            return "";
        }
        return sb.substring(0, sb.length() - 1);
    }
}
