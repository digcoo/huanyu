package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 预判金叉策略（preqsn）可自定义参数 · v1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreGoldenStrategyParams {

    public static final double DEFAULT_MIN_AVG_AMOUNT = 50_000_000D;
    public static final String DEFAULT_TIER_MIN = "ALL";

    @Builder.Default
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    @Builder.Default
    private boolean enableShort = true;

    @Builder.Default
    private boolean enableMedium = true;

    @Builder.Default
    private boolean enableLong = true;

    @Builder.Default
    private String tierMin = DEFAULT_TIER_MIN;

    public static PreGoldenStrategyParams defaults() {
        return PreGoldenStrategyParams.builder().build();
    }

    public static PreGoldenStrategyParams merge(PreGoldenStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        PreGoldenStrategyParams d = defaults();
        if (incoming.minAvgAmount > 0) {
            d.minAvgAmount = incoming.minAvgAmount;
        }
        d.enableShort = incoming.enableShort;
        d.enableMedium = incoming.enableMedium;
        d.enableLong = incoming.enableLong;
        if (incoming.tierMin != null && !incoming.tierMin.isEmpty()) {
            d.tierMin = incoming.tierMin.toUpperCase();
        }
        return d;
    }

    public static int tierRank(char tier) {
        switch (tier) {
            case 'S': return 3;
            case 'A': return 2;
            case 'B': return 1;
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
