package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单边趋势（qsn）可自定义参数 · v1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnilateralStrategyParams {

    public static final int DEFAULT_DAY_LOOKBACK = 8;
    public static final double DEFAULT_STRONG_YANG_RATE = 0.03;
    public static final int DEFAULT_WEEK_CONTEXT_MIN = 2;
    /** 最低日均成交额，元（默认 5000 万） */
    public static final double DEFAULT_MIN_AVG_AMOUNT = 50_000_000D;
    public static final String DEFAULT_TIER_MIN = "ALL";

    @Builder.Default
    private int dayPlatformLookback = DEFAULT_DAY_LOOKBACK;

    @Builder.Default
    private double strongYangRate = DEFAULT_STRONG_YANG_RATE;

    @Builder.Default
    private int weekContextMin = DEFAULT_WEEK_CONTEXT_MIN;

    @Builder.Default
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    @Builder.Default
    private boolean enableModeB = true;

    @Builder.Default
    private boolean enableModeA = true;

    @Builder.Default
    private boolean enableModeBWeak = true;

    /** ALL / S / A / B / C — 最低展示档位 */
    @Builder.Default
    private String tierMin = DEFAULT_TIER_MIN;

    public static UnilateralStrategyParams defaults() {
        return UnilateralStrategyParams.builder().build();
    }

    public static UnilateralStrategyParams merge(UnilateralStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        UnilateralStrategyParams d = defaults();
        if (incoming.dayPlatformLookback > 0) {
            d.dayPlatformLookback = Math.min(30, Math.max(3, incoming.dayPlatformLookback));
        }
        if (incoming.strongYangRate > 0) {
            d.strongYangRate = Math.min(0.15, Math.max(0.005, incoming.strongYangRate));
        }
        if (incoming.weekContextMin > 0) {
            d.weekContextMin = Math.min(3, Math.max(1, incoming.weekContextMin));
        }
        if (incoming.minAvgAmount > 0) {
            d.minAvgAmount = incoming.minAvgAmount;
        }
        d.enableModeB = incoming.enableModeB;
        d.enableModeA = incoming.enableModeA;
        d.enableModeBWeak = incoming.enableModeBWeak;
        if (incoming.tierMin != null && !incoming.tierMin.isEmpty()) {
            d.tierMin = incoming.tierMin.toUpperCase();
        }
        return d;
    }

    public static int tierRank(char tier) {
        switch (tier) {
            case 'S': return 4;
            case 'A': return 3;
            case 'B': return 2;
            case 'C': return 1;
            default: return 0;
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
