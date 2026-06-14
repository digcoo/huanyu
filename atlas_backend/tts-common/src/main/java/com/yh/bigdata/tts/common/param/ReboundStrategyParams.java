package com.yh.bigdata.tts.common.param;

import lombok.Data;

/**
 * 深跌反弹策略（default）可调参数 · v3.0
 */
@Data
public class ReboundStrategyParams {

    public static final double DEFAULT_MIN_AVG_AMOUNT = 30_000_000D;
    public static final String DEFAULT_TIER_MIN = "ALL";

    /** 近 6 日平均成交额下限（元） */
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    private boolean enableShort = true;
    private boolean enableMedium = true;
    private boolean enableLong = true;

    /** 最低档位 ALL/S/A/B */
    private String tierMin = DEFAULT_TIER_MIN;

    public static ReboundStrategyParams defaults() {
        return new ReboundStrategyParams();
    }

    public static ReboundStrategyParams merge(ReboundStrategyParams override) {
        if (override == null) {
            return defaults();
        }
        ReboundStrategyParams base = defaults();
        if (override.minAvgAmount > 0) {
            base.minAvgAmount = override.minAvgAmount;
        }
        base.enableShort = override.enableShort;
        base.enableMedium = override.enableMedium;
        base.enableLong = override.enableLong;
        if (override.tierMin != null && !override.tierMin.isEmpty()) {
            base.tierMin = override.tierMin.toUpperCase();
        }
        return base;
    }

    public static int tierRank(char tier) {
        switch (tier) {
            case 'S':
                return 3;
            case 'A':
                return 2;
            case 'B':
                return 1;
            default:
                return 0;
        }
    }

    public boolean passTierFilter(char tier) {
        if (tierMin == null || "ALL".equalsIgnoreCase(tierMin)) {
            return true;
        }
        char required = tierMin.charAt(0);
        return tierRank(tier) >= tierRank(required);
    }
}
