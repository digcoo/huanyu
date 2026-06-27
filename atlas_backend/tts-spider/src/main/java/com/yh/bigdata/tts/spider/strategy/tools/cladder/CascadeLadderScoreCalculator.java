package com.yh.bigdata.tts.spider.strategy.tools.cladder;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.CascadeLadderStrategyParams;

public final class CascadeLadderScoreCalculator {

    private CascadeLadderScoreCalculator() {
    }

    public static int computeScore() {
        return 55;
    }

    public static String buildTrendMessage(CascadeLadderStrategyParams params) {
        StringBuilder sb = new StringBuilder("[CLADDER]级联梯子突破|日周月交集");
        CascadeLadderStrategyParams p = params != null ? params : CascadeLadderStrategyParams.defaults();
        if (p.isEnableDualLowGate() || p.isEnableMacdGate() || p.isEnableCrossLowGate()
                || p.isEnableBarHighGate()) {
            sb.append("|可选四门");
        }
        sb.append("|基准压顶+双阳柱");
        return sb.toString();
    }

    public static String buildSignalMessage(CascadeLadderBreakoutTools.PeriodHit hit) {
        if (hit == null || hit.getReferenceBar() == null || hit.getLastBar() == null) {
            return "";
        }
        Trade ref = hit.getReferenceBar();
        Trade last = hit.getLastBar();
        Trade prev = hit.getPrevBar();
        return String.format(
                "级联梯子突破,period=%s,refDay=%s,refHigh=%.2f,refLow=%.2f,"
                        + "sigDay=%s,sigClose=%.2f,prevDay=%s,prevClose=%.2f",
                hit.getPeriod().getCode(),
                dayOf(ref),
                ref.getHigh() != null ? ref.getHigh() : 0,
                ref.getLow() != null ? ref.getLow() : 0,
                dayOf(last),
                last.getClose() != null ? last.getClose() : 0,
                dayOf(prev),
                prev != null && prev.getClose() != null ? prev.getClose() : 0);
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }
}
