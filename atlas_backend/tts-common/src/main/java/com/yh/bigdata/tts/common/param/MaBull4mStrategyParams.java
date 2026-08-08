package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MA多头4M排列（mabull4m）：MA5≥MA10≥MA20≥MA30，收阳，末K close &gt; 前一根 Low。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaBull4mStrategyParams {

    public enum Tier {
        MIN30, DAY, WEEK, MONTH
    }

    public static final int DEFAULT_LOOKBACK_MIN30 = 200;
    public static final int DEFAULT_LOOKBACK_DAY = 120;
    public static final int DEFAULT_LOOKBACK_WEEK = 52;
    public static final int DEFAULT_LOOKBACK_MONTH = 36;
    public static final double DEFAULT_MIN_AVG_AMOUNT = 3000D * 10_000D;

    @Builder.Default
    private Tier tier = Tier.DAY;

    @Builder.Default
    private int lookbackMin30 = DEFAULT_LOOKBACK_MIN30;

    @Builder.Default
    private int lookbackDay = DEFAULT_LOOKBACK_DAY;

    @Builder.Default
    private int lookbackWeek = DEFAULT_LOOKBACK_WEEK;

    @Builder.Default
    private int lookbackMonth = DEFAULT_LOOKBACK_MONTH;

    @Builder.Default
    private boolean enableMinAmountFilter = true;

    @Builder.Default
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    public static MaBull4mStrategyParams defaults() {
        return MaBull4mStrategyParams.builder().build();
    }

    public static MaBull4mStrategyParams merge(MaBull4mStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        MaBull4mStrategyParams d = defaults();
        if (incoming.tier != null) {
            d.tier = incoming.tier;
        }
        if (incoming.lookbackMin30 >= 40) {
            d.lookbackMin30 = incoming.lookbackMin30;
        }
        if (incoming.lookbackDay >= 10) {
            d.lookbackDay = incoming.lookbackDay;
        }
        if (incoming.lookbackWeek >= 10) {
            d.lookbackWeek = incoming.lookbackWeek;
        }
        if (incoming.lookbackMonth >= 6) {
            d.lookbackMonth = incoming.lookbackMonth;
        }
        d.enableMinAmountFilter = incoming.enableMinAmountFilter;
        if (incoming.minAvgAmount > 0) {
            d.minAvgAmount = incoming.minAvgAmount;
        }
        return d;
    }

    public static Tier parseTier(String raw) {
        if (raw == null || raw.isEmpty()) {
            return Tier.DAY;
        }
        String s = raw.trim().toLowerCase();
        if ("min30".equals(s) || "flash".equals(s) || "30".equals(s) || "30m".equals(s)) {
            return Tier.MIN30;
        }
        if ("week".equals(s) || "medium".equals(s)) {
            return Tier.WEEK;
        }
        if ("month".equals(s) || "long".equals(s)) {
            return Tier.MONTH;
        }
        return Tier.DAY;
    }
}
