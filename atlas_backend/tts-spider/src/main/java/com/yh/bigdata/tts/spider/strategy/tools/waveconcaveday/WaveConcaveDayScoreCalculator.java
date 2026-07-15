package com.yh.bigdata.tts.spider.strategy.tools.waveconcaveday;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.WaveConcaveDayStrategyParams;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeTools;

public final class WaveConcaveDayScoreCalculator {

    private WaveConcaveDayScoreCalculator() {
    }

    public static int computeScore(WaveConcaveDayEvaluator.WaveConcaveDayEvaluation eval) {
        int score = 32;
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

    public static WaveShapeTools.TierHit primaryHit(WaveConcaveDayEvaluator.WaveConcaveDayEvaluation eval) {
        if (eval.getMonthHit() != null) {
            return eval.getMonthHit();
        }
        if (eval.getWeekHit() != null) {
            return eval.getWeekHit();
        }
        return eval.getDayHit();
    }

    public static PeriodTypeEnum primaryPeriod(WaveConcaveDayEvaluator.WaveConcaveDayEvaluation eval) {
        WaveShapeTools.TierHit hit = primaryHit(eval);
        return hit != null ? hit.getSignalTier() : PeriodTypeEnum.DAY;
    }

    public static String buildTrendMessage(WaveConcaveDayEvaluator.WaveConcaveDayEvaluation eval,
                                           WaveConcaveDayStrategyParams params) {
        StringBuilder sb = new StringBuilder("[WAVECONCAVEDAY]凹波段日突破|日周月末阳底上方");
        WaveConcaveDayStrategyParams p = params != null ? params : WaveConcaveDayStrategyParams.defaults();
        if (p.isEnableAllYangGate() || p.isEnableMin30Gate()) {
            sb.append("|可选门");
        }
        appendTierTrendLabel(sb, eval.getMonthHit());
        appendTierTrendLabel(sb, eval.getWeekHit());
        appendTierTrendLabel(sb, eval.getDayHit());
        return sb.toString();
    }

    public static String buildSignalMessage(WaveShapeTools.TierHit hit) {
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
        return String.format(
                "凹波段日突破,bandShape=CONCAVE,breakPath=%s,signalTier=%s,refPeriod=%s,"
                        + "lastYangDay=%s,lastMedian=%.2f,lastHigh=%.2f,lastLow=%.2f,"
                        + "prevLastYangHigh=%.2f,breakLine=%.2f,"
                        + "bandHigh=%.2f,bandLow=%.2f,"
                        + "sigDay=%s,sigClose=%.2f,prevDay=%s,prevClose=%.2f",
                hit.getBreakPath().name(),
                hit.getSignalTier().getCode(),
                hit.getSignalTier().getCode(),
                dayOf(last),
                hit.getBand().getLastMedian(),
                lastHigh,
                hit.getBand().getLastLow(),
                prevLastHigh,
                hit.getBreakLine(),
                hit.getBand().getBandHigh(),
                hit.getBand().getBandLow(),
                dayOf(signal),
                signal.getClose() != null ? signal.getClose() : 0,
                dayOf(prev),
                prev != null && prev.getClose() != null ? prev.getClose() : 0);
    }

    private static void appendTierTrendLabel(StringBuilder sb, WaveShapeTools.TierHit hit) {
        if (hit != null) {
            sb.append('|').append(periodLabel(hit.getSignalTier())).append("凹波段日K突破");
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
