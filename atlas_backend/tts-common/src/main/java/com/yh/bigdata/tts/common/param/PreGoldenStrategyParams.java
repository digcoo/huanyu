package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 预判金叉策略（preqsn）可自定义参数 · v2.0
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

    /** 短线：（周 or 月 MACD&gt;0）+ 日 MACD&lt;0 + 日 close&gt;前日 high */
    @Builder.Default
    private boolean enableShort = true;

    /** @deprecated v2.0 起忽略（原中线档已取消） */
    @Builder.Default
    private boolean enableMedium = false;

    /** 长线：（月 or 年 MACD&gt;0）+ 周 MACD&lt;0 + 周 close&gt;前周 high */
    @Builder.Default
    private boolean enableLong = true;

    /** ALL / S / B — 最低展示档位（S=短线 B=长线） */
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
            case 'S': return 2;
            case 'B': return 1;
            case 'A': return 1;
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
