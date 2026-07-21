package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 日小时组合（daymin60）：日 MACD&gt;0 + Min60 金叉波段 High 边沿突破（信号限末交易日 Min60）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayMin60ComboStrategyParams {

    public static final int DEFAULT_PREV_DAYS = 2;
    public static final int DEFAULT_MAX_BARS_PER_DAY = 4;
    public static final int DEFAULT_GC_LOOKBACK_BARS = 60;
    public static final double DEFAULT_MIN_AVG_AMOUNT = 3000D * 10_000D;
    public static final double DEFAULT_SIGNAL_RISE_PCT = 0.01;

    @Builder.Default
    private int prevDays = DEFAULT_PREV_DAYS;

    @Builder.Default
    private int maxBarsPerDay = DEFAULT_MAX_BARS_PER_DAY;

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

    public static DayMin60ComboStrategyParams defaults() {
        return DayMin60ComboStrategyParams.builder().build();
    }

    public static DayMin60ComboStrategyParams merge(DayMin60ComboStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        DayMin60ComboStrategyParams d = defaults();
        if (incoming.prevDays >= 0) {
            d.prevDays = incoming.prevDays;
        }
        if (incoming.maxBarsPerDay >= 1) {
            d.maxBarsPerDay = incoming.maxBarsPerDay;
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
