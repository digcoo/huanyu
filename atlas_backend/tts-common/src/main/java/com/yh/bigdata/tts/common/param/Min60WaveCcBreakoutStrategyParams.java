package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分时凹凸突破（min60wavecc）：Min60/日/周 MACD 至少 2 个 &gt;0 + Min60 凸凹边沿突破。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Min60WaveCcBreakoutStrategyParams {

    public static final int DEFAULT_PREV_DAYS = 2;
    public static final int DEFAULT_MAX_BARS_PER_DAY = 4;
    public static final int DEFAULT_LOOKBACK_BARS = 120;
    public static final double DEFAULT_MIN_AVG_AMOUNT = 3000D * 10_000D;
    public static final double DEFAULT_SIGNAL_RISE_PCT = 0.015;

    @Builder.Default
    private int prevDays = DEFAULT_PREV_DAYS;

    @Builder.Default
    private int maxBarsPerDay = DEFAULT_MAX_BARS_PER_DAY;

    @Builder.Default
    private int lookbackBars = DEFAULT_LOOKBACK_BARS;

    @Builder.Default
    private boolean enableMinAmountFilter = true;

    @Builder.Default
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    @Builder.Default
    private boolean enableSignalRiseGate = true;

    @Builder.Default
    private double signalRisePct = DEFAULT_SIGNAL_RISE_PCT;

    /** true=须末根 Min60 K 突破；false=当日任一根 Min60 满足即可（多根取最后一根） */
    @Builder.Default
    private boolean requireCurrentBreakout = true;

    public static Min60WaveCcBreakoutStrategyParams defaults() {
        return Min60WaveCcBreakoutStrategyParams.builder().build();
    }

    public static Min60WaveCcBreakoutStrategyParams merge(Min60WaveCcBreakoutStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        Min60WaveCcBreakoutStrategyParams d = defaults();
        if (incoming.prevDays >= 0) {
            d.prevDays = incoming.prevDays;
        }
        if (incoming.maxBarsPerDay >= 1) {
            d.maxBarsPerDay = incoming.maxBarsPerDay;
        }
        if (incoming.lookbackBars >= 10) {
            d.lookbackBars = incoming.lookbackBars;
        }
        d.enableMinAmountFilter = incoming.enableMinAmountFilter;
        if (incoming.minAvgAmount > 0) {
            d.minAvgAmount = incoming.minAvgAmount;
        }
        d.enableSignalRiseGate = incoming.enableSignalRiseGate;
        if (incoming.signalRisePct > 0) {
            d.signalRisePct = incoming.signalRisePct;
        }
        d.requireCurrentBreakout = incoming.requireCurrentBreakout;
        return d;
    }
}
