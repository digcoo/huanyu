package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MACD金叉波段High回踩（macdgcwhr）：MACD&gt;0、价在基准波段 High 上、末 K 振幅≤1%。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MacdGcWaveHighRetestStrategyParams {

    public enum Tier {
        DAY, WEEK, MONTH
    }

    public static final int DEFAULT_LOOKBACK_DAY = 60;
    public static final int DEFAULT_LOOKBACK_WEEK = 52;
    public static final int DEFAULT_LOOKBACK_MONTH = 36;
    public static final double DEFAULT_MIN_AVG_AMOUNT = 3000D * 10_000D;
    public static final double DEFAULT_MAX_BAR_RANGE_PCT = 0.01;

    @Builder.Default
    private Tier tier = Tier.DAY;

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

    @Builder.Default
    private double maxBarRangePct = DEFAULT_MAX_BAR_RANGE_PCT;

    public static MacdGcWaveHighRetestStrategyParams defaults() {
        return MacdGcWaveHighRetestStrategyParams.builder().build();
    }

    public static MacdGcWaveHighRetestStrategyParams merge(MacdGcWaveHighRetestStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        MacdGcWaveHighRetestStrategyParams d = defaults();
        if (incoming.tier != null) {
            d.tier = incoming.tier;
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
        if (incoming.maxBarRangePct > 0) {
            d.maxBarRangePct = incoming.maxBarRangePct;
        }
        return d;
    }

    public static Tier parseTier(String raw) {
        if (raw == null || raw.isEmpty()) {
            return Tier.DAY;
        }
        String s = raw.trim().toLowerCase();
        if ("week".equals(s) || "medium".equals(s)) {
            return Tier.WEEK;
        }
        if ("month".equals(s) || "long".equals(s)) {
            return Tier.MONTH;
        }
        return Tier.DAY;
    }
}
