package com.yh.bigdata.tts.spider.strategy.tools.retest;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;

/**
 * 回踩抬升 · 评分与展示
 */
public final class RetestScoreCalculator {

    private RetestScoreCalculator() {
    }

    public static int computeScore(RetestEvaluator.RetestEvaluation eval) {
        int score = 0;
        if (eval.getUltraHit() != null) {
            score += 35;
        }
        if (eval.getShortHit() != null) {
            score += 30;
        }
        if (eval.getMediumHit() != null) {
            score += 25;
        }
        if (eval.getLongHit() != null) {
            score += 20;
        }
        RetestTierTools.TierHit primary = eval.primaryHit();
        if (primary != null) {
            if (primary.isBear()) {
                score += 8;
            }
            if (primary.isBull()) {
                score += 8;
            }
            if (primary.isBear() && primary.isBull()) {
                score += 5;
            }
            RetestStructureTools.StructureHit s = primary.getStructure();
            if (s != null && s.getSignal() != null && s.getSignal().getShitiRate() != null) {
                score += (int) (s.getSignal().getShitiRate() * 400);
            }
        }
        return Math.max(score, 20);
    }

    public static char computeTier(RetestEvaluator.RetestEvaluation eval) {
        if (eval.getUltraHit() != null) {
            return 'S';
        }
        if (eval.getShortHit() != null) {
            return 'A';
        }
        if (eval.getMediumHit() != null) {
            return 'B';
        }
        if (eval.getLongHit() != null) {
            return 'C';
        }
        return 'N';
    }

    public static PeriodTypeEnum trendPeriodForTier(char tier) {
        switch (tier) {
            case 'S':
                return PeriodTypeEnum.MIN30;
            case 'A':
                return PeriodTypeEnum.DAY;
            case 'B':
                return PeriodTypeEnum.WEEK;
            case 'C':
                return PeriodTypeEnum.MONTH;
            default:
                return PeriodTypeEnum.DAY;
        }
    }

    public static PeriodTypeEnum signalPeriodForTier(char tier) {
        return trendPeriodForTier(tier);
    }

    public static String buildTrendLabel(char tier, RetestEvaluator.RetestEvaluation eval) {
        RetestTierTools.TierHit hit = eval.hitForTier(tier);
        StringBuilder modes = new StringBuilder();
        if (hit != null) {
            if (hit.isBear()) {
                modes.append("下跌反转");
            }
            if (hit.isBull()) {
                if (modes.length() > 0) {
                    modes.append("+");
                }
                modes.append("上涨中继");
            }
        }
        String modeStr = modes.length() > 0 ? modes.toString() : "回踩抬升";
        switch (tier) {
            case 'S':
                return "超短·" + modeStr;
            case 'A':
                return "短线·" + modeStr;
            case 'B':
                return "中线·" + modeStr;
            case 'C':
                return "长线·" + modeStr;
            default:
                return modeStr;
        }
    }

    public static String buildTrendDetail(RetestEvaluator.RetestEvaluation eval, char tier) {
        RetestTierTools.TierHit hit = eval.hitForTier(tier);
        if (hit == null || hit.getStructure() == null) {
            return tierLabel(tier) + "强弹回踩结构";
        }
        RetestStructureTools.StructureHit s = hit.getStructure();
        String modes = buildModesToken(hit);
        return String.format("强弹%.1f%%→回踩%.1f%%→再升,modes=%s",
                impulsePct(s), pullbackPct(s), modes);
    }

    public static String buildSignalDetail(RetestEvaluator.RetestEvaluation eval, char tier) {
        RetestTierTools.TierHit hit = eval.hitForTier(tier);
        if (hit == null || hit.getStructure() == null) {
            return "";
        }
        RetestStructureTools.StructureHit s = hit.getStructure();
        Trade signal = s.getSignal();
        if (signal == null) {
            return "";
        }
        String modes = buildModesToken(hit);
        return String.format("%s突破回踩高%.2f,l0Day=%s,h1Day=%s,l1Day=%s,sigDay=%s,modes=%s",
                tierLabel(tier),
                s.getPullbackHigh(),
                dayOf(s.getL0()),
                dayOf(s.getH1()),
                dayOf(s.getL1()),
                dayOf(signal),
                modes);
    }

    private static String buildModesToken(RetestTierTools.TierHit hit) {
        StringBuilder sb = new StringBuilder();
        if (hit.isBear()) {
            sb.append("bear");
        }
        if (hit.isBull()) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append("bull");
        }
        return sb.toString();
    }

    private static double impulsePct(RetestStructureTools.StructureHit s) {
        Double l0 = s.getL0() != null ? s.getL0().getLow() : null;
        Double h1 = s.getH1() != null ? s.getH1().getHigh() : null;
        if (l0 == null || h1 == null || l0 <= 0) {
            return 0;
        }
        return (h1 - l0) / l0 * 100;
    }

    private static double pullbackPct(RetestStructureTools.StructureHit s) {
        Double l0 = s.getL0() != null ? s.getL0().getLow() : null;
        Double h1 = s.getH1() != null ? s.getH1().getHigh() : null;
        Double l1 = s.getL1() != null ? s.getL1().getLow() : null;
        if (l0 == null || h1 == null || l1 == null) {
            return 0;
        }
        double range = h1 - l0;
        if (range <= 0) {
            return 0;
        }
        return (h1 - l1) / range * 100;
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }

    private static String tierLabel(char tier) {
        switch (tier) {
            case 'S':
                return "超短";
            case 'A':
                return "短线";
            case 'B':
                return "中线";
            case 'C':
                return "长线";
            default:
                return "";
        }
    }
}
