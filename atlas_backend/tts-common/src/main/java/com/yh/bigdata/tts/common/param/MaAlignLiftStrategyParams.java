package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 均线多头突破（maalignlift）：MA10&gt;MA20&gt;MA30，末 K close&gt;均线MAX，边沿或开盘突破均线MAX。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaAlignLiftStrategyParams {

    public enum Tier {
        DAY, WEEK, MONTH
    }

    public static final double DEFAULT_MIN_AVG_AMOUNT = 3000D * 10_000D;

    @Builder.Default
    private Tier tier = Tier.DAY;

    @Builder.Default
    private boolean enableMinAmountFilter = true;

    @Builder.Default
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    public static MaAlignLiftStrategyParams defaults() {
        return MaAlignLiftStrategyParams.builder().build();
    }

    public static MaAlignLiftStrategyParams merge(MaAlignLiftStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        MaAlignLiftStrategyParams d = defaults();
        if (incoming.tier != null) {
            d.tier = incoming.tier;
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
        if ("week".equals(s) || "medium".equals(s)) {
            return Tier.WEEK;
        }
        if ("month".equals(s) || "long".equals(s)) {
            return Tier.MONTH;
        }
        return Tier.DAY;
    }
}
