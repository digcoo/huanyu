package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 死叉突破（dc2）· 短/长两档，大周期 MACD&gt;0 + 突破死叉柱 high
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dc2StrategyParams {

    public static final double DEFAULT_MIN_AVG_AMOUNT = 30_000_000D;
    public static final String DEFAULT_TIER_MIN = "ALL";
    public static final int DEFAULT_LOOKBACK_SHORT = 60;
    public static final int DEFAULT_LOOKBACK_LONG = 52;
    public static final int DEFAULT_MIN_BARS_AFTER_REF = 2;

    @Builder.Default
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    @Builder.Default
    private String tierMin = DEFAULT_TIER_MIN;

    @Builder.Default
    private boolean enableShort = true;

    @Builder.Default
    private boolean enableLong = true;

    @Builder.Default
    private int lookbackShort = DEFAULT_LOOKBACK_SHORT;

    @Builder.Default
    private int lookbackLong = DEFAULT_LOOKBACK_LONG;

    /** ref 与 signal 之间至少 N 根完整 K（不含 ref / signal） */
    @Builder.Default
    private int minBarsAfterRef = DEFAULT_MIN_BARS_AFTER_REF;

    public static Dc2StrategyParams defaults() {
        return Dc2StrategyParams.builder().build();
    }

    public static Dc2StrategyParams merge(Dc2StrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        Dc2StrategyParams d = defaults();
        if (incoming.minAvgAmount > 0) {
            d.minAvgAmount = incoming.minAvgAmount;
        }
        if (incoming.tierMin != null && !incoming.tierMin.isEmpty()) {
            d.tierMin = incoming.tierMin.toUpperCase();
        }
        d.enableShort = incoming.enableShort;
        d.enableLong = incoming.enableLong;
        if (incoming.lookbackShort >= 10) {
            d.lookbackShort = incoming.lookbackShort;
        }
        if (incoming.lookbackLong >= 10) {
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
                return 2;
            case 'B':
                return 1;
            case 'A':
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
