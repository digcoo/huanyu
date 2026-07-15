package com.yh.bigdata.tts.spider.strategy.tools.cascadewaveconvexday;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.CascadeWaveConvexDayStrategyParams;
import com.yh.bigdata.tts.spider.strategy.tools.cascadewave.CascadeWaveBreakoutTools;
import com.yh.bigdata.tts.spider.strategy.tools.cascadewave.CascadeWaveMacdTools;

public final class CascadeWaveConvexDayScoreCalculator {

    private CascadeWaveConvexDayScoreCalculator() {
    }

    public static int computeScore(CascadeWaveConvexDayEvaluator.CascadeWaveConvexDayEvaluation eval) {
        int score = 34;
        int hits = 0;
        if (eval.getDayHit() != null) {
            score += 10;
            hits++;
        }
        if (eval.getWeekHit() != null) {
            score += 16;
            hits++;
        }
        if (eval.getMonthHit() != null) {
            score += 22;
            hits++;
        }
        if (hits > 1) {
            score += 8;
        }
        return score;
    }

    public static CascadeWaveBreakoutTools.TierHit primaryHit(
            CascadeWaveConvexDayEvaluator.CascadeWaveConvexDayEvaluation eval) {
        if (eval.getMonthHit() != null) {
            return eval.getMonthHit();
        }
        if (eval.getWeekHit() != null) {
            return eval.getWeekHit();
        }
        return eval.getDayHit();
    }

    public static PeriodTypeEnum primaryPeriod(CascadeWaveConvexDayEvaluator.CascadeWaveConvexDayEvaluation eval) {
        CascadeWaveBreakoutTools.TierHit hit = primaryHit(eval);
        return hit != null ? hit.getSignalTier() : PeriodTypeEnum.DAY;
    }

    public static String buildTrendMessage(CascadeWaveConvexDayEvaluator.CascadeWaveConvexDayEvaluation eval,
                                           CascadeWaveConvexDayStrategyParams params) {
        StringBuilder sb = new StringBuilder("[CASCADEWAVECONVEXDAY]级联MACD凸波段日突破");
        CascadeWaveConvexDayStrategyParams p = params != null ? params : CascadeWaveConvexDayStrategyParams.defaults();
        if (p.isEnableAllYangGate() || p.isEnableMin30Gate() || p.isEnableBandLastYangLowGate()
                || p.isEnableYangBandTrendGate()) {
            sb.append("|可选门");
        }
        appendTierTrendLabel(sb, eval.getMonthHit());
        appendTierTrendLabel(sb, eval.getWeekHit());
        appendTierTrendLabel(sb, eval.getDayHit());
        return sb.toString();
    }

    public static String buildSignalMessage(CascadeWaveBreakoutTools.TierHit hit) {
        if (hit == null || hit.getBand() == null || hit.getSignalBar() == null) {
            return "";
        }
        Trade last = hit.getLastYangBar();
        Trade signal = hit.getSignalBar();
        Trade prev = hit.getPrevBar();
        Trade prevLastYang = hit.getPrevBand() != null ? hit.getPrevBand().getLastYang() : null;
        double prevLastHigh = prevLastYang != null && prevLastYang.getHigh() != null
                ? prevLastYang.getHigh() : 0;
        double lastHigh = last != null && last.getHigh() != null ? last.getHigh() : 0;
        PeriodTypeEnum cascade = CascadeWaveMacdTools.cascadePeriod(hit.getSignalTier());
        return String.format(
                "级联MACD凸波段日突破,bandShape=CONVEX,edgeMode=DAY,breakPath=%s,signalTier=%s,refPeriod=day,"
                        + "cascadePeriod=%s,lastYangDay=%s,lastHigh=%.2f,"
                        + "prevLastYangHigh=%.2f,breakLine=%.2f,"
                        + "bandHigh=%.2f,bandLow=%.2f,"
                        + "sigDay=%s,sigClose=%.2f,prevDay=%s,prevClose=%.2f",
                hit.getBreakPath().name(),
                hit.getSignalTier().getCode(),
                cascade != null ? cascade.getCode() : "",
                dayOf(last),
                lastHigh,
                prevLastHigh,
                hit.getBreakLine(),
                hit.getBand().getBandHigh(),
                hit.getBand().getBandLow(),
                dayOf(signal),
                signal.getClose() != null ? signal.getClose() : 0,
                dayOf(prev),
                prev != null && prev.getClose() != null ? prev.getClose() : 0);
    }

    private static void appendTierTrendLabel(StringBuilder sb, CascadeWaveBreakoutTools.TierHit hit) {
        if (hit != null) {
            sb.append('|').append(periodLabel(hit.getSignalTier())).append("级联凸日突破");
        }
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
