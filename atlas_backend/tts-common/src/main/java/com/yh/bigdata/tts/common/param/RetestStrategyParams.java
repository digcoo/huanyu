package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 回踩抬升策略（retest）· 超短/短/中/长 + bear/bull
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetestStrategyParams {

    public static final double DEFAULT_MIN_AVG_AMOUNT = 30_000_000D;
    public static final String DEFAULT_TIER_MIN = "ALL";

    public static final double DEFAULT_IMPULSE_MIN_GAIN_PCT = 0.08D;
    public static final int DEFAULT_IMPULSE_MIN_BARS = 3;
    public static final int DEFAULT_IMPULSE_MAX_BARS = 12;
    public static final int DEFAULT_IMPULSE_MIN_STRONG_BARS = 2;
    public static final double DEFAULT_PULLBACK_MIN_RATIO = 0.35D;
    public static final double DEFAULT_PULLBACK_MAX_RATIO = 0.65D;
    public static final double DEFAULT_LOW_EQUAL_TOLERANCE = 0.005D;
    public static final int DEFAULT_FRACTAL_BARS = 2;
    public static final int DEFAULT_STABILIZE_BARS = 3;

    @Builder.Default
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    @Builder.Default
    private String tierMin = DEFAULT_TIER_MIN;

    @Builder.Default
    private boolean enableBear = true;

    @Builder.Default
    private boolean enableBull = true;

    @Builder.Default
    private boolean enableUltra = true;

    @Builder.Default
    private boolean enableShort = true;

    @Builder.Default
    private boolean enableMedium = true;

    @Builder.Default
    private boolean enableLong = true;

    @Builder.Default
    private double impulseMinGainPct = DEFAULT_IMPULSE_MIN_GAIN_PCT;

    @Builder.Default
    private int impulseMinBars = DEFAULT_IMPULSE_MIN_BARS;

    @Builder.Default
    private int impulseMaxBars = DEFAULT_IMPULSE_MAX_BARS;

    @Builder.Default
    private int impulseMinStrongBars = DEFAULT_IMPULSE_MIN_STRONG_BARS;

    @Builder.Default
    private double pullbackMinRatio = DEFAULT_PULLBACK_MIN_RATIO;

    @Builder.Default
    private double pullbackMaxRatio = DEFAULT_PULLBACK_MAX_RATIO;

    @Builder.Default
    private double lowEqualTolerance = DEFAULT_LOW_EQUAL_TOLERANCE;

    @Builder.Default
    private int fractalBars = DEFAULT_FRACTAL_BARS;

    @Builder.Default
    private int stabilizeBars = DEFAULT_STABILIZE_BARS;

    public static RetestStrategyParams defaults() {
        return RetestStrategyParams.builder().build();
    }

    public static RetestStrategyParams merge(RetestStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        RetestStrategyParams d = defaults();
        if (incoming.minAvgAmount > 0) {
            d.minAvgAmount = incoming.minAvgAmount;
        }
        if (incoming.tierMin != null && !incoming.tierMin.isEmpty()) {
            d.tierMin = incoming.tierMin.toUpperCase();
        }
        d.enableBear = incoming.enableBear;
        d.enableBull = incoming.enableBull;
        d.enableUltra = incoming.enableUltra;
        d.enableShort = incoming.enableShort;
        d.enableMedium = incoming.enableMedium;
        d.enableLong = incoming.enableLong;
        if (incoming.impulseMinGainPct > 0) {
            d.impulseMinGainPct = incoming.impulseMinGainPct;
        }
        if (incoming.impulseMinBars >= 1) {
            d.impulseMinBars = incoming.impulseMinBars;
        }
        if (incoming.impulseMaxBars >= incoming.impulseMinBars) {
            d.impulseMaxBars = incoming.impulseMaxBars;
        }
        if (incoming.impulseMinStrongBars >= 1) {
            d.impulseMinStrongBars = incoming.impulseMinStrongBars;
        }
        if (incoming.pullbackMinRatio > 0) {
            d.pullbackMinRatio = incoming.pullbackMinRatio;
        }
        if (incoming.pullbackMaxRatio > incoming.pullbackMinRatio) {
            d.pullbackMaxRatio = incoming.pullbackMaxRatio;
        }
        if (incoming.lowEqualTolerance >= 0) {
            d.lowEqualTolerance = incoming.lowEqualTolerance;
        }
        if (incoming.fractalBars >= 1) {
            d.fractalBars = incoming.fractalBars;
        }
        if (incoming.stabilizeBars >= 1) {
            d.stabilizeBars = incoming.stabilizeBars;
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
