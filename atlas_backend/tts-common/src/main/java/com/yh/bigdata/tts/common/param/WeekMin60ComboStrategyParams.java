package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 小时周组合（weekmin60）：周 MACD&gt;0 + Min60 金叉波段 High 边沿突破（信号限末周线 Min60）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeekMin60ComboStrategyParams {

    public static final int DEFAULT_PREV_WEEKS = 2;
    public static final int DEFAULT_MAX_BARS_PER_WEEK = 20;
    public static final int DEFAULT_GC_LOOKBACK_BARS = 60;
    public static final double DEFAULT_MIN_AVG_AMOUNT = 3000D * 10_000D;
    public static final double DEFAULT_SIGNAL_RISE_PCT = 0.01;

    @Builder.Default
    private int prevWeeks = DEFAULT_PREV_WEEKS;

    @Builder.Default
    private int maxBarsPerWeek = DEFAULT_MAX_BARS_PER_WEEK;

    @Builder.Default
    private int gcLookbackBars = DEFAULT_GC_LOOKBACK_BARS;

    @Builder.Default
    private boolean enableMinAmountFilter = true;

    @Builder.Default
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    @Builder.Default
    private boolean enableSignalRiseGate = true;

    @Builder.Default
    private double signalRisePct = DEFAULT_SIGNAL_RISE_PCT;

    public static WeekMin60ComboStrategyParams defaults() {
        return WeekMin60ComboStrategyParams.builder().build();
    }

    public static WeekMin60ComboStrategyParams merge(WeekMin60ComboStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        WeekMin60ComboStrategyParams d = defaults();
        if (incoming.prevWeeks >= 0) {
            d.prevWeeks = incoming.prevWeeks;
        }
        if (incoming.maxBarsPerWeek >= 1) {
            d.maxBarsPerWeek = incoming.maxBarsPerWeek;
        }
        if (incoming.gcLookbackBars >= 5) {
            d.gcLookbackBars = incoming.gcLookbackBars;
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
