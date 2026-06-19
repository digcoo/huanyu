package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 中线策略（medium）参数 · 周K + 自然月桶突破
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediumStrategyParams {

    public static final double DEFAULT_MIN_AVG_AMOUNT = 50_000_000D;
    public static final double DEFAULT_MIN_STRONG_PCT = 0.05;
    public static final int DEFAULT_PREV_MONTHS = 2;
    public static final int DEFAULT_MAX_WEEKS_PER_MONTH = 5;

    @Builder.Default
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    @Builder.Default
    private double minStrongPct = DEFAULT_MIN_STRONG_PCT;

    /** 基准窗口：信号自然月之前 N 个完整月 */
    @Builder.Default
    private int prevMonths = DEFAULT_PREV_MONTHS;

    @Builder.Default
    private int maxWeeksPerMonth = DEFAULT_MAX_WEEKS_PER_MONTH;

    @Builder.Default
    private boolean requireCurrentBreakout = false;

    @Builder.Default
    private boolean requireMonthMacd = false;

    @Builder.Default
    private boolean requireYearMacd = false;

    @Builder.Default
    private boolean requireMonthGoldenCross = false;

    public static MediumStrategyParams defaults() {
        return MediumStrategyParams.builder().build();
    }

    public static MediumStrategyParams merge(MediumStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        MediumStrategyParams d = defaults();
        if (incoming.minAvgAmount > 0) {
            d.minAvgAmount = incoming.minAvgAmount;
        }
        if (incoming.minStrongPct > 0) {
            d.minStrongPct = incoming.minStrongPct;
        }
        if (incoming.prevMonths >= 1) {
            d.prevMonths = incoming.prevMonths;
        }
        if (incoming.maxWeeksPerMonth >= 1) {
            d.maxWeeksPerMonth = incoming.maxWeeksPerMonth;
        }
        d.requireCurrentBreakout = incoming.requireCurrentBreakout;
        d.requireMonthMacd = incoming.requireMonthMacd;
        d.requireYearMacd = incoming.requireYearMacd;
        d.requireMonthGoldenCross = incoming.requireMonthGoldenCross;
        return d;
    }
}
