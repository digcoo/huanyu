package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 趋势MA（trendma）：日/周/月 MACD&gt;0 且 close&gt;max(MA5~30) 且收阳，至少 2 档满足。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendMaStrategyParams {

    public static final double DEFAULT_MIN_AVG_AMOUNT = 3000D * 10_000D;

    @Builder.Default
    private boolean enableMinAmountFilter = true;

    @Builder.Default
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    public static TrendMaStrategyParams defaults() {
        return TrendMaStrategyParams.builder().build();
    }

    public static TrendMaStrategyParams merge(TrendMaStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        TrendMaStrategyParams d = defaults();
        d.enableMinAmountFilter = incoming.enableMinAmountFilter;
        if (incoming.minAvgAmount > 0) {
            d.minAvgAmount = incoming.minAvgAmount;
        }
        return d;
    }
}
