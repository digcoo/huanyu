package com.yh.bigdata.tts.spider.strategy.tools.ultralow;

import com.yh.bigdata.tts.common.model.Trade;

/**
 * 30m K 线强度判定（大阳线 / 大涨幅）
 */
public final class Min30BarTools {

    private Min30BarTools() {
    }

    /** 大阳线：阳线且实体涨幅 ≥ minPct */
    public static boolean isLargeYang(Trade bar, double minPct) {
        if (bar == null || bar.getOpen() == null || bar.getClose() == null || bar.getOpen() <= 0) {
            return false;
        }
        return bar.getClose() > bar.getOpen() && bar.getShitiRate() != null && bar.getShitiRate() >= minPct;
    }

    /** 大涨幅：实体涨幅或相对前收涨幅 ≥ minPct */
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

    /** 大阳线 或 大涨幅 */
    public static boolean isStrongBar(Trade bar, double minPct) {
        return isLargeYang(bar, minPct) || isLargeGain(bar, minPct);
    }
}
