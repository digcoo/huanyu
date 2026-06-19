package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 长线策略（long）参数 · 月K + 自然年桶突破
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LongStrategyParams {

    public static final double DEFAULT_MIN_AVG_AMOUNT = 50_000_000D;
    public static final double DEFAULT_MIN_STRONG_PCT = 0.05;
    public static final int DEFAULT_PREV_YEARS = 2;
    public static final int DEFAULT_MAX_MONTHS_PER_YEAR = 12;

    @Builder.Default
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    @Builder.Default
    private double minStrongPct = DEFAULT_MIN_STRONG_PCT;

    /** 基准窗口：信号自然年之前 N 个完整年 */
    @Builder.Default
    private int prevYears = DEFAULT_PREV_YEARS;

    @Builder.Default
    private int maxMonthsPerYear = DEFAULT_MAX_MONTHS_PER_YEAR;

    @Builder.Default
    private boolean requireCurrentBreakout = false;

    @Builder.Default
    private boolean requireYearMacd = false;

    @Builder.Default
    private boolean requireMonthMacd = false;

    @Builder.Default
    private boolean requireYearGoldenCross = false;

    /** 须同时满足超短 30m 突破 */
    @Builder.Default
    private boolean requireUltra = true;

    public static LongStrategyParams defaults() {
        return LongStrategyParams.builder().build();
    }

    public static LongStrategyParams merge(LongStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        LongStrategyParams d = defaults();
        if (incoming.minAvgAmount > 0) {
            d.minAvgAmount = incoming.minAvgAmount;
        }
        if (incoming.minStrongPct > 0) {
            d.minStrongPct = incoming.minStrongPct;
        }
        if (incoming.prevYears >= 1) {
            d.prevYears = incoming.prevYears;
        }
        if (incoming.maxMonthsPerYear >= 1) {
            d.maxMonthsPerYear = incoming.maxMonthsPerYear;
        }
        d.requireCurrentBreakout = incoming.requireCurrentBreakout;
        d.requireYearMacd = incoming.requireYearMacd;
        d.requireMonthMacd = incoming.requireMonthMacd;
        d.requireYearGoldenCross = incoming.requireYearGoldenCross;
        d.requireUltra = incoming.requireUltra;
        return d;
    }
}
