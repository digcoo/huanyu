package com.yh.bigdata.tts.spider.strategy.tools.ultralow;

import com.yh.bigdata.tts.common.model.Trade;

/**
 * K 线强度判定（大阳线 / 大涨幅），各周期通用
 */
public final class BreakoutBarTools {

    private BreakoutBarTools() {
    }

    public static boolean isLargeYang(Trade bar, double minPct) {
        if (bar == null || bar.getOpen() == null || bar.getClose() == null || bar.getOpen() <= 0) {
            return false;
        }
        return bar.getClose() > bar.getOpen() && bar.getShitiRate() != null && bar.getShitiRate() >= minPct;
    }

    public static boolean isLargeGain(Trade bar, double minPct) {
        if (bar == null || bar.getOpen() == null || bar.getClose() == null) {
            return false;
        }
        Double block = bar.getBlockRate();
        if (block != null && block >= minPct) {
            return true;
        }
        Double change = bar.getChangeRate();
        return change != null && change >= minPct;
    }

    public static boolean isStrongBar(Trade bar, double minPct) {
        return isLargeYang(bar, minPct) || isLargeGain(bar, minPct);
    }
}
