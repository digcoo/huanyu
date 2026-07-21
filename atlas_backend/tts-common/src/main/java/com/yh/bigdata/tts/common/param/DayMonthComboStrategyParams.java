package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 日月组合（daymonth）：月+日 MACD&gt;0、月 close&gt;近2月 low 高值；日金叉波段 High 边沿突破（信号限末自然月）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayMonthComboStrategyParams {

    public static final int DEFAULT_PREV_MONTHS = 2;
    public static final int DEFAULT_MAX_BARS_PER_MONTH = 22;
    public static final int DEFAULT_GC_LOOKBACK_BARS = 40;
    public static final double DEFAULT_MIN_AVG_AMOUNT = 3000D * 10_000D;
    public static final double DEFAULT_SIGNAL_RISE_PCT = 0.01;

    @Builder.Default
    private int prevMonths = DEFAULT_PREV_MONTHS;

    @Builder.Default
    private int maxBarsPerMonth = DEFAULT_MAX_BARS_PER_MONTH;

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

    public static DayMonthComboStrategyParams defaults() {
        return DayMonthComboStrategyParams.builder().build();
    }

    public static DayMonthComboStrategyParams merge(DayMonthComboStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        DayMonthComboStrategyParams d = defaults();
        if (incoming.prevMonths >= 0) {
            d.prevMonths = incoming.prevMonths;
        }
        if (incoming.maxBarsPerMonth >= 1) {
            d.maxBarsPerMonth = incoming.maxBarsPerMonth;
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
