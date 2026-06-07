package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 多周期强势（multi）可调参数 · v1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiStrategyParams {

    public static final int DEFAULT_MIN_RESONANCE_PERIODS = 4;
    public static final double DEFAULT_MIN_AVG_AMOUNT = 50_000_000D;
    /** 默认仅 S/A 进池 */
    public static final String DEFAULT_TIER_MIN = "A";

    @Builder.Default
    private int minResonancePeriods = DEFAULT_MIN_RESONANCE_PERIODS;

    @Builder.Default
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    @Builder.Default
    private boolean enableModeA = true;

    @Builder.Default
    private boolean enableModeB = true;

    @Builder.Default
    private boolean enableModeC = true;

    /** B 档：有 Context、无 Trigger */
    @Builder.Default
    private boolean enableWeakContext = false;

    /** C 档：单周期 Context + Trigger */
    @Builder.Default
    private boolean enableWeakSinglePeriod = false;

    @Builder.Default
    private String tierMin = DEFAULT_TIER_MIN;

    public static MultiStrategyParams defaults() {
        return MultiStrategyParams.builder().build();
    }

    public static MultiStrategyParams merge(MultiStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        MultiStrategyParams d = defaults();
        if (incoming.minResonancePeriods > 0) {
            d.minResonancePeriods = Math.min(4, Math.max(1, incoming.minResonancePeriods));
        }
        if (incoming.minAvgAmount > 0) {
            d.minAvgAmount = incoming.minAvgAmount;
        }
        d.enableModeA = incoming.enableModeA;
        d.enableModeB = incoming.enableModeB;
        d.enableModeC = incoming.enableModeC;
        d.enableWeakContext = incoming.enableWeakContext;
        d.enableWeakSinglePeriod = incoming.enableWeakSinglePeriod;
        if (incoming.tierMin != null && !incoming.tierMin.isEmpty()) {
            d.tierMin = incoming.tierMin.toUpperCase();
        }
        return d;
    }

    public static int tierRank(char tier) {
        switch (tier) {
            case 'S':
                return 4;
            case 'A':
                return 3;
            case 'B':
                return 2;
            case 'C':
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
