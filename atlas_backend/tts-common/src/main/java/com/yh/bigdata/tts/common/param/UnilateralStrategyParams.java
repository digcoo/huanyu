package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 金叉策略（qsn）可自定义参数 · v3.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnilateralStrategyParams {

    /** 最低日均成交额，元（默认 5000 万） */
    public static final double DEFAULT_MIN_AVG_AMOUNT = 50_000_000D;
    public static final String DEFAULT_TIER_MIN = "ALL";

    @Builder.Default
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    /** 短线：周 MACD&gt;0 + 日 MACD 金叉 */
    @Builder.Default
    private boolean enableShort = true;

    /** 中线：月 MACD&gt;0 + 周 MACD 金叉 */
    @Builder.Default
    private boolean enableMedium = true;

    /** 长线：年 MACD&gt;0 + 月 MACD 金叉 */
    @Builder.Default
    private boolean enableLong = true;

    /** ALL / S / A / B — 最低展示档位（S=短线 A=中线 B=长线） */
    @Builder.Default
    private String tierMin = DEFAULT_TIER_MIN;

    /** @deprecated v3.0 起忽略 */
    @Builder.Default
    private int dayPlatformLookback = 8;

    /** @deprecated v3.0 起忽略 */
    @Builder.Default
    private double strongYangRate = 0.03;

    /** @deprecated v3.0 起忽略 */
    @Builder.Default
    private int weekContextMin = 2;

    /** @deprecated v3.0 起忽略，映射 enableShort */
    @Builder.Default
    private boolean enableModeB = true;

    /** @deprecated v3.0 起忽略，映射 enableMedium */
    @Builder.Default
    private boolean enableModeA = true;

    /** @deprecated v3.0 起忽略 */
    @Builder.Default
    private boolean enableModeBWeak = true;

    public static UnilateralStrategyParams defaults() {
        return UnilateralStrategyParams.builder().build();
    }

    public static UnilateralStrategyParams merge(UnilateralStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        UnilateralStrategyParams d = defaults();
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
