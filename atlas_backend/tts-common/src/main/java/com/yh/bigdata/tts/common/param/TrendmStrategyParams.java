package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 趋势策略（trendm）· 无阻力门 + 日/周/月同时基准突破 + min30 梯子
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendmStrategyParams {

    public static final int DEFAULT_LOOKBACK_DAY = 60;
    public static final int DEFAULT_LOOKBACK_WEEK = 52;
    public static final int DEFAULT_LOOKBACK_MONTH = 36;

    @Builder.Default
    private int lookbackDay = DEFAULT_LOOKBACK_DAY;

    @Builder.Default
    private int lookbackWeek = DEFAULT_LOOKBACK_WEEK;

    @Builder.Default
    private int lookbackMonth = DEFAULT_LOOKBACK_MONTH;

    public static TrendmStrategyParams defaults() {
        return TrendmStrategyParams.builder().build();
    }

    public static TrendmStrategyParams merge(TrendmStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        TrendmStrategyParams d = defaults();
        if (incoming.lookbackDay >= 10) {
            d.lookbackDay = incoming.lookbackDay;
        }
        if (incoming.lookbackWeek >= 10) {
            d.lookbackWeek = incoming.lookbackWeek;
        }
        if (incoming.lookbackMonth >= 6) {
            d.lookbackMonth = incoming.lookbackMonth;
        }
        return d;
    }
}
