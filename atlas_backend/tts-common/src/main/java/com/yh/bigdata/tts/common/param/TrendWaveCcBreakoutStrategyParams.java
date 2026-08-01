package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 趋势内凹凸突破（trendwavecc）：趋势 MACD&gt;0 交集 + 信号周期凸凹边沿突破。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendWaveCcBreakoutStrategyParams {

    public static final String DEFAULT_SIGNAL_PERIOD = "min60";
    public static final int DEFAULT_PREV_DAYS = 2;
    public static final int DEFAULT_MAX_BARS_PER_DAY = 4;
    public static final int DEFAULT_LOOKBACK_BARS = 120;
    public static final double DEFAULT_MIN_AVG_AMOUNT = 3000D * 10_000D;
    public static final double DEFAULT_SIGNAL_RISE_PCT = 0.015;

    /** 信号周期：min60 / day / week */
    @Builder.Default
    private String signalPeriod = DEFAULT_SIGNAL_PERIOD;

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

    @Builder.Default
    private boolean requireDayMacd = true;

    @Builder.Default
    private boolean requireWeekMacd = false;

    @Builder.Default
    private boolean requireMonthMacd = false;

    public static TrendWaveCcBreakoutStrategyParams defaults() {
        return TrendWaveCcBreakoutStrategyParams.builder().build();
    }

    public static TrendWaveCcBreakoutStrategyParams merge(TrendWaveCcBreakoutStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        TrendWaveCcBreakoutStrategyParams d = defaults();
        if (incoming.signalPeriod != null && !incoming.signalPeriod.isEmpty()) {
            d.signalPeriod = incoming.signalPeriod;
        }
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
        d.requireDayMacd = incoming.requireDayMacd;
        d.requireWeekMacd = incoming.requireWeekMacd;
        d.requireMonthMacd = incoming.requireMonthMacd;
        return d;
    }

    public MacdPositiveGateParams toMacdPositiveGateParams() {
        return MacdPositiveGateParams.builder()
                .requireDayMacd(requireDayMacd)
                .requireWeekMacd(requireWeekMacd)
                .requireMonthMacd(requireMonthMacd)
                .build();
    }
}
