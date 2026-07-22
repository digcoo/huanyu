package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 日凹凸突破（daywavecc）：日/周/月 MACD 至少 2 个 &gt;0 + 日 K 凸凹边沿突破（信号限末根日 K）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayWaveCcBreakoutStrategyParams {

    public static final int DEFAULT_LOOKBACK_BARS = 120;
    public static final double DEFAULT_MIN_AVG_AMOUNT = 3000D * 10_000D;
    public static final double DEFAULT_SIGNAL_RISE_PCT = 0.015;

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

    public static DayWaveCcBreakoutStrategyParams defaults() {
        return DayWaveCcBreakoutStrategyParams.builder().build();
    }

    public static DayWaveCcBreakoutStrategyParams merge(DayWaveCcBreakoutStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        DayWaveCcBreakoutStrategyParams d = defaults();
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
        return d;
    }
}
