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
        boolean full = resonance.fullResonance();
        boolean hasTrigger = breakout.hasTrigger();

        if (full && hasTrigger
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

        if (resonance.year != null && resonance.year.coreResonance()) {
            score += 25;
        }
        if (resonance.month != null && resonance.month.coreResonance()) {
            score += 22;
        }
        if (resonance.week != null && resonance.week.coreResonance()) {
            score += 20;
        }
        if (resonance.day != null && resonance.day.coreResonance()) {
            score += 18;
        }
        score += countStructureBonus(resonance) * 3;

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
        if (resonance.fullResonance() && breakout.hasTrigger()) {
            score += 30;
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

    private static int countStructureBonus(MultiResonanceTools.ResonanceSnapshot resonance) {
        int n = 0;
        for (MultiResonanceTools.PeriodResonance pr : new MultiResonanceTools.PeriodResonance[]{
                resonance.year, resonance.month, resonance.week, resonance.day}) {
            if (pr != null && pr.coreResonance()) {
                if (pr.prevYang) {
                    n++;
                }
                if (pr.prevHighAboveZuojia) {
                    n++;
                }
            }
        }
        return n;
    }

    public static String buildTrendLabel(char tier) {
        switch (tier) {
            case 'S':
                return "四周期共振·突破";
            case 'A':
                return "多周期共振·突破";
            case 'B':
                return "共振对齐·待确认";
            case 'C':
                return "单周期观察";
            default:
                return "";
        }
    }

    public static String buildTrendDetail(MultiResonanceTools.ResonanceSnapshot resonance) {
        StringBuilder sb = new StringBuilder();
        sb.append("共振").append(resonance.coreCount()).append("/4");
        appendPeriod(sb, "年", resonance.year);
        appendPeriod(sb, "月", resonance.month);
        appendPeriod(sb, "周", resonance.week);
        appendPeriod(sb, "日", resonance.day);
        return sb.toString();
    }

    private static void appendPeriod(StringBuilder sb, String label,
                                     MultiResonanceTools.PeriodResonance pr) {
        if (pr != null && pr.coreResonance()) {
            sb.append(',').append(label).append("阳");
            if (pr.prevYang) {
                sb.append("+上阳");
            } else if (pr.prevHighAboveZuojia) {
                sb.append("+破座架");
            }
        }
    }
}
