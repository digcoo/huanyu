package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 趋势上移（trendlift）：日/周/月 MACD 全 &gt;0 + 日 close &gt; 前一日 low。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendLiftStrategyParams {

    public static final double DEFAULT_MIN_AVG_AMOUNT = 3000D * 10_000D;

    @Builder.Default
    private boolean enableMinAmountFilter = true;

    @Builder.Default
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    public static TrendLiftStrategyParams defaults() {
        return TrendLiftStrategyParams.builder().build();
    }

    public static TrendLiftStrategyParams merge(TrendLiftStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        TrendLiftStrategyParams d = defaults();
        d.enableMinAmountFilter = incoming.enableMinAmountFilter;
        if (incoming.minAvgAmount > 0) {
            d.minAvgAmount = incoming.minAvgAmount;
        }
        return d;
    }

    public MacdPositiveGateParams toMacdPositiveGateParams() {
        return MacdPositiveGateParams.builder()
                .requireDayMacd(true)
                .requireWeekMacd(true)
                .requireMonthMacd(true)
                .build();
    }
}
