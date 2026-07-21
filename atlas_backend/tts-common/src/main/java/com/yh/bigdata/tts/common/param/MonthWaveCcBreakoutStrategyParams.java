package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 月凹凸突破（monthwavecc）：月 MACD&gt;0 + 月 K 凸凹边沿突破/凸边沿回踩（信号限末根月 K）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthWaveCcBreakoutStrategyParams {

    public static final int DEFAULT_LOOKBACK_BARS = 36;
    public static final double DEFAULT_MIN_AVG_AMOUNT = 3000D * 10_000D;
    public static final double DEFAULT_SIGNAL_RISE_PCT = 0.01;
    public static final double DEFAULT_MAX_RETEST_GAP_PCT = 0.01;

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

    @Builder.Default
    private double maxRetestGapPct = DEFAULT_MAX_RETEST_GAP_PCT;

    public static MonthWaveCcBreakoutStrategyParams defaults() {
        return MonthWaveCcBreakoutStrategyParams.builder().build();
    }

    public static MonthWaveCcBreakoutStrategyParams merge(MonthWaveCcBreakoutStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        MonthWaveCcBreakoutStrategyParams d = defaults();
        if (incoming.lookbackBars >= 6) {
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
        if (incoming.maxRetestGapPct > 0) {
            d.maxRetestGapPct = incoming.maxRetestGapPct;
        }
        return d;
    }
}
