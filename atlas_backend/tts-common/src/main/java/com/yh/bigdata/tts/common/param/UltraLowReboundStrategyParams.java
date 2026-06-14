package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 梯子突破策略（ladder）· 超短/短/中/长 四档突破阶梯
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UltraLowReboundStrategyParams {

    public static final double DEFAULT_MIN_AVG_AMOUNT = 30_000_000D;
    public static final String DEFAULT_TIER_MIN = "ALL";

    public static final double DEFAULT_MIN30_BODY_GAIN_PCT = 0.02D;
    public static final int DEFAULT_MIN30_PREV_DAYS = 2;
    public static final int DEFAULT_MIN30_BARS_PER_DAY = 8;

    public static final double DEFAULT_DAY_BODY_GAIN_PCT = 0.02D;
    public static final int DEFAULT_DAY_PREV_WEEKS = 2;
    public static final int DEFAULT_DAY_BARS_PER_WEEK = 5;

    public static final double DEFAULT_WEEK_BODY_GAIN_PCT = 0.025D;
    public static final int DEFAULT_WEEK_PREV_MONTHS = 2;
    public static final int DEFAULT_WEEK_BARS_PER_MONTH = 6;

    public static final double DEFAULT_MONTH_BODY_GAIN_PCT = 0.045D;
    public static final int DEFAULT_MONTH_PREV_YEARS = 2;
    public static final int DEFAULT_MONTH_BARS_PER_YEAR = 12;

    @Builder.Default
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    @Builder.Default
    private String tierMin = DEFAULT_TIER_MIN;

    @Builder.Default
    private boolean enableUltra = true;

    @Builder.Default
    private boolean enableShort = true;

    @Builder.Default
    private boolean enableMedium = true;

    @Builder.Default
    private boolean enableLong = true;

    @Builder.Default
    private double min30BodyGainPct = DEFAULT_MIN30_BODY_GAIN_PCT;

    @Builder.Default
    private int min30PrevDays = DEFAULT_MIN30_PREV_DAYS;

    @Builder.Default
    private int min30BarsPerDay = DEFAULT_MIN30_BARS_PER_DAY;

    @Builder.Default
    private double dayBodyGainPct = DEFAULT_DAY_BODY_GAIN_PCT;

    @Builder.Default
    private int dayPrevWeeks = DEFAULT_DAY_PREV_WEEKS;

    @Builder.Default
    private int dayBarsPerWeek = DEFAULT_DAY_BARS_PER_WEEK;

    @Builder.Default
    private double weekBodyGainPct = DEFAULT_WEEK_BODY_GAIN_PCT;

    @Builder.Default
    private int weekPrevMonths = DEFAULT_WEEK_PREV_MONTHS;

    @Builder.Default
    private int weekBarsPerMonth = DEFAULT_WEEK_BARS_PER_MONTH;

    @Builder.Default
    private double monthBodyGainPct = DEFAULT_MONTH_BODY_GAIN_PCT;

    @Builder.Default
    private int monthPrevYears = DEFAULT_MONTH_PREV_YEARS;

    @Builder.Default
    private int monthBarsPerYear = DEFAULT_MONTH_BARS_PER_YEAR;

    /** 长线是否启用现价 ≤ 突破K.high 过滤 */
    @Builder.Default
    private boolean enableLongPriceFilter = true;

    public static UltraLowReboundStrategyParams defaults() {
        return UltraLowReboundStrategyParams.builder().build();
    }

    public static UltraLowReboundStrategyParams merge(UltraLowReboundStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        UltraLowReboundStrategyParams d = defaults();
        if (incoming.minAvgAmount > 0) {
            d.minAvgAmount = incoming.minAvgAmount;
        }
        if (incoming.tierMin != null && !incoming.tierMin.isEmpty()) {
            d.tierMin = incoming.tierMin.toUpperCase();
        }
        d.enableUltra = incoming.enableUltra;
        d.enableShort = incoming.enableShort;
        d.enableMedium = incoming.enableMedium;
        d.enableLong = incoming.enableLong;
        if (incoming.min30BodyGainPct > 0) {
            d.min30BodyGainPct = incoming.min30BodyGainPct;
        }
        if (incoming.min30PrevDays >= 1) {
            d.min30PrevDays = incoming.min30PrevDays;
        }
        if (incoming.min30BarsPerDay > 0) {
            d.min30BarsPerDay = incoming.min30BarsPerDay;
        }
        if (incoming.dayBodyGainPct > 0) {
            d.dayBodyGainPct = incoming.dayBodyGainPct;
        }
        if (incoming.dayPrevWeeks >= 1) {
            d.dayPrevWeeks = incoming.dayPrevWeeks;
        }
        if (incoming.dayBarsPerWeek > 0) {
            d.dayBarsPerWeek = incoming.dayBarsPerWeek;
        }
        if (incoming.weekBodyGainPct > 0) {
            d.weekBodyGainPct = incoming.weekBodyGainPct;
        }
        if (incoming.weekPrevMonths >= 1) {
            d.weekPrevMonths = incoming.weekPrevMonths;
        }
        if (incoming.weekBarsPerMonth > 0) {
            d.weekBarsPerMonth = incoming.weekBarsPerMonth;
        }
        if (incoming.monthBodyGainPct > 0) {
            d.monthBodyGainPct = incoming.monthBodyGainPct;
        }
        if (incoming.monthPrevYears >= 1) {
            d.monthPrevYears = incoming.monthPrevYears;
        }
        if (incoming.monthBarsPerYear > 0) {
            d.monthBarsPerYear = incoming.monthBarsPerYear;
        }
        d.enableLongPriceFilter = incoming.enableLongPriceFilter;
        return d;
    }

    /** S=超短 A=短 B=中 C=长 */
    public static int tierRank(char tier) {
        switch (tier) {
            case 'S':
                return 4;
            case 'A':
                return 3;
            case 'B':
                return 2;
            case 'C':
                return 1;
            default:
                return 0;
        }
    }

    public boolean passTierFilter(char tier) {
        if (tierMin == null || "ALL".equalsIgnoreCase(tierMin)) {
            return true;
        }
        char required = tierMin.charAt(0);
        return tierRank(tier) >= tierRank(required);
    }
}
