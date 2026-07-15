package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 凸波段上移（convexlifttier）：日凸波段 + 日/周/月/年 close &gt; 前 K high。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvexLiftTierStrategyParams {

    public enum Tier {
        WEEK, MONTH, YEAR
    }

    public static final int DEFAULT_LOOKBACK_DAY = 120;

    @Builder.Default
    private Tier tier = Tier.WEEK;

    @Builder.Default
    private int lookbackDay = DEFAULT_LOOKBACK_DAY;

    public static ConvexLiftTierStrategyParams defaults() {
        return ConvexLiftTierStrategyParams.builder().build();
    }

    public static ConvexLiftTierStrategyParams merge(ConvexLiftTierStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        ConvexLiftTierStrategyParams d = defaults();
        if (incoming.tier != null) {
            d.tier = incoming.tier;
        }
        if (incoming.lookbackDay >= 20) {
            d.lookbackDay = incoming.lookbackDay;
        }
        return d;
    }

    public static Tier parseTier(String raw) {
        if (raw == null || raw.isEmpty()) {
            return Tier.WEEK;
        }
        String s = raw.trim().toLowerCase();
        if ("month".equals(s) || "medium".equals(s)) {
            return Tier.MONTH;
        }
        if ("year".equals(s) || "long".equals(s)) {
            return Tier.YEAR;
        }
        return Tier.WEEK;
    }
}
