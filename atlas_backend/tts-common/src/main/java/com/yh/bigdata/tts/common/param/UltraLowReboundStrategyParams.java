package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 超短线策略（ulow）· 前1~2日 30m 新高强K + 当日突破
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UltraLowReboundStrategyParams {

    public static final double DEFAULT_MIN_AVG_AMOUNT = 30_000_000D;
    public static final String DEFAULT_TIER_MIN = "ALL";
    public static final double DEFAULT_MIN30_BODY_GAIN_PCT = 0.02D;
    public static final int DEFAULT_MIN30_PREV_DAYS = 2;
    public static final int DEFAULT_MIN30_BARS_PER_DAY = 8;

    @Builder.Default
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    @Builder.Default
    private String tierMin = DEFAULT_TIER_MIN;

    /** 大阳线实体 / 大涨幅下限（0.02 = 2%） */
    @Builder.Default
    private double min30BodyGainPct = DEFAULT_MIN30_BODY_GAIN_PCT;

    /** 向前取几个完整交易日的 30m（不含当日，1~2 日） */
    @Builder.Default
    private int min30PrevDays = DEFAULT_MIN30_PREV_DAYS;

    /** 每个历史交易日最多取几根 30m（A 股 1 日 8 根） */
    @Builder.Default
    private int min30BarsPerDay = DEFAULT_MIN30_BARS_PER_DAY;

    public static UltraLowReboundStrategyParams defaults() {
        return UltraLowReboundStrategyParams.builder().build();
    }

    public static UltraLowReboundStrategyParams merge(UltraLowReboundStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        UltraLowReboundStrategyParams d = defaults();
        if (incoming.minAvgAmount > 0) {
            d.minAvgAmount = incoming.minAvgAmount;
        }
        if (incoming.tierMin != null && !incoming.tierMin.isEmpty()) {
            d.tierMin = incoming.tierMin.toUpperCase();
        }
        if (incoming.min30BodyGainPct > 0) {
            d.min30BodyGainPct = incoming.min30BodyGainPct;
        }
        if (incoming.min30PrevDays >= 1) {
            d.min30PrevDays = incoming.min30PrevDays;
        }
        if (incoming.min30BarsPerDay > 0) {
            d.min30BarsPerDay = incoming.min30BarsPerDay;
        }
        return d;
    }

    public static int tierRank(char tier) {
        return tier == 'S' ? 1 : 0;
    }

    public boolean passTierFilter(char tier) {
        if (tierMin == null || "ALL".equalsIgnoreCase(tierMin)) {
            return true;
        }
        char required = tierMin.charAt(0);
        return tierRank(tier) >= tierRank(required);
    }
}
