package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 超短线 · MACD金叉K突破（ultragc）：Min30 最近金叉 K high 边沿突破。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UltraGcBreakoutStrategyParams {

    public static final int DEFAULT_PREV_DAYS = 2;
    public static final int DEFAULT_MAX_BARS_PER_DAY = 8;
    public static final int DEFAULT_GC_LOOKBACK_BARS = 24;
    public static final double DEFAULT_SIGNAL_RISE_PCT = 0.01;

    @Builder.Default
    private int prevDays = DEFAULT_PREV_DAYS;

    @Builder.Default
    private int maxBarsPerDay = DEFAULT_MAX_BARS_PER_DAY;

    @Builder.Default
    private int gcLookbackBars = DEFAULT_GC_LOOKBACK_BARS;

    @Builder.Default
    private double signalRisePct = DEFAULT_SIGNAL_RISE_PCT;

    public static UltraGcBreakoutStrategyParams defaults() {
        return UltraGcBreakoutStrategyParams.builder().build();
    }

    public static UltraGcBreakoutStrategyParams merge(UltraGcBreakoutStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        UltraGcBreakoutStrategyParams d = defaults();
        if (incoming.prevDays >= 0) {
            d.prevDays = incoming.prevDays;
        }
        if (incoming.maxBarsPerDay >= 1) {
            d.maxBarsPerDay = incoming.maxBarsPerDay;
        }
        if (incoming.gcLookbackBars >= 5) {
            d.gcLookbackBars = incoming.gcLookbackBars;
        }
        if (incoming.signalRisePct > 0) {
            d.signalRisePct = incoming.signalRisePct;
        }
        return d;
    }
}
