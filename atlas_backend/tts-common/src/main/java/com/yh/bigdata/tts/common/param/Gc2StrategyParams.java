package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 金叉二次突破（gc2）· 日/周/月三档
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Gc2StrategyParams {

    public static final double DEFAULT_MIN_AVG_AMOUNT = 30_000_000D;
    public static final String DEFAULT_TIER_MIN = "ALL";
    public static final int DEFAULT_LOOKBACK_SHORT = 60;
    public static final int DEFAULT_LOOKBACK_MEDIUM = 52;
    public static final int DEFAULT_LOOKBACK_LONG = 24;
    public static final int DEFAULT_MIN_BARS_AFTER_REF = 1;

    @Builder.Default
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    @Builder.Default
    private String tierMin = DEFAULT_TIER_MIN;

    @Builder.Default
    private boolean enableShort = true;

    @Builder.Default
    private boolean enableMedium = false;

    @Builder.Default
    private boolean enableLong = false;

    @Builder.Default
    private int lookbackShort = DEFAULT_LOOKBACK_SHORT;

    @Builder.Default
    private int lookbackMedium = DEFAULT_LOOKBACK_MEDIUM;

    @Builder.Default
    private int lookbackLong = DEFAULT_LOOKBACK_LONG;

    @Builder.Default
    private int minBarsAfterRef = DEFAULT_MIN_BARS_AFTER_REF;

    public static Gc2StrategyParams defaults() {
        return Gc2StrategyParams.builder().build();
    }

    public static Gc2StrategyParams merge(Gc2StrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        Gc2StrategyParams d = defaults();
        if (incoming.minAvgAmount > 0) {
            d.minAvgAmount = incoming.minAvgAmount;
        }
        if (incoming.tierMin != null && !incoming.tierMin.isEmpty()) {
            d.tierMin = incoming.tierMin.toUpperCase();
        }
        d.enableShort = incoming.enableShort;
        d.enableMedium = incoming.enableMedium;
        d.enableLong = incoming.enableLong;
        if (incoming.lookbackShort >= 10) {
            d.lookbackShort = incoming.lookbackShort;
        }
        if (incoming.lookbackMedium >= 10) {
            d.lookbackMedium = incoming.lookbackMedium;
        }
        if (incoming.lookbackLong >= 6) {
            d.lookbackLong = incoming.lookbackLong;
        }
        if (incoming.minBarsAfterRef >= 1) {
            d.minBarsAfterRef = incoming.minBarsAfterRef;
        }
        return d;
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
