package com.yh.bigdata.tts.spider.strategy.tools.multi;

import com.yh.bigdata.tts.common.param.MultiStrategyParams;

/**
 * 多周期强势 · score 与档位
 */
public final class MultiScoreCalculator {

    private MultiScoreCalculator() {
    }

    public static char computeTier(MultiStrengthEvaluator.MultiEvaluation eval, MultiStrategyParams params) {
        MultiStrategyParams p = params != null ? params : MultiStrategyParams.defaults();
        MultiResonanceTools.ResonanceSnapshot resonance = eval.getResonance();
        MultiBreakoutTools.BreakoutSnapshot breakout = eval.getBreakout();

        int core = resonance.coreCount();
        boolean hasTrigger = breakout.hasTrigger();

        if (core >= 3 && hasTrigger
                && (breakout.signalPeriodCount >= 2 || breakout.triggerKindCount() >= 2)) {
            return 'S';
        }
        if (core >= p.getMinResonancePeriods() && hasTrigger) {
            return 'A';
        }
        if (core >= p.getMinResonancePeriods() && !hasTrigger && p.isEnableWeakContext()) {
            return 'B';
        }
        if (core == 1 && hasTrigger && p.isEnableWeakSinglePeriod()) {
            return 'C';
        }
        return 'N';
    }

    public static int computeScore(MultiStrengthEvaluator.MultiEvaluation eval) {
        int score = 0;
        MultiResonanceTools.ResonanceSnapshot resonance = eval.getResonance();
        MultiBreakoutTools.BreakoutSnapshot breakout = eval.getBreakout();

        if (resonance.day != null && resonance.day.coreResonance()) {
            score += 20;
        }
        if (resonance.week != null && resonance.week.coreResonance()) {
            score += 20;
        }
        if (resonance.month != null && resonance.month.coreResonance()) {
            score += 20;
        }
        score += resonance.bonusCount() * 5;

        if (breakout.bandHits > 0) {
            score += 18 * breakout.bandHits;
        }
        if (breakout.revertHits > 0) {
            score += 18 * breakout.revertHits;
        }
        if (breakout.tiziHits > 0) {
            score += 22 * breakout.tiziHits;
        }
        if (breakout.triggerKindCount() >= 2) {
            score += 15;
        }
        if (resonance.coreCount() >= 3 && breakout.hasTrigger()) {
            score += 25;
        }
        if (breakout.signalPeriodCount >= 2) {
            score += 20;
        }

        char tier = eval.getTier();
        if (tier == 'S') {
            score += 25;
        } else if (tier == 'A') {
            score += 12;
        } else if (tier == 'B') {
            score += 6;
        }
        return score;
    }

    public static String buildTrendLabel(char tier) {
        switch (tier) {
            case 'S':
                return "多周期共振·突破";
            case 'A':
                return "波段/梯子突破";
            case 'B':
                return "结构对齐·待确认";
            case 'C':
                return "单周期观察";
            default:
                return "";
        }
    }

    public static String buildTrendDetail(MultiResonanceTools.ResonanceSnapshot resonance) {
        StringBuilder sb = new StringBuilder();
        sb.append("共振").append(resonance.coreCount()).append("/3");
        if (resonance.day != null && resonance.day.coreResonance()) {
            sb.append(",日对齐");
        }
        if (resonance.week != null && resonance.week.coreResonance()) {
            sb.append(",周对齐");
        }
        if (resonance.month != null && resonance.month.coreResonance()) {
            sb.append(",月对齐");
        }
        return sb.toString();
    }
}
