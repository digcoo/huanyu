package com.yh.bigdata.tts.common.param;

import lombok.Data;

/**
 * 深坑反弹策略（default）可调参数 · v1.0
 */
@Data
public class ReboundStrategyParams {

    public static final double DEFAULT_MIN_AVG_AMOUNT = 30_000_000D;
    public static final int DEFAULT_PIT_CONTEXT_MIN = 2;
    public static final String DEFAULT_TIER_MIN = "ALL";

    /** 日 K 恐慌大跌阈值（涨跌幅，默认 6%） */
    public static final double DEFAULT_CAPITULATION_DAY_RATE = 0.06;
    /** 周 K 恐慌大跌阈值（默认 5%） */
    public static final double DEFAULT_CAPITULATION_WEEK_RATE = 0.05;
    /** 相对 52 周高点最小回撤（默认 20%） */
    public static final double DEFAULT_MIN_DRAWDOWN_FROM_52W = 0.20;
    public static final int DEFAULT_CAPITULATION_LOOKBACK_DAYS = 15;
    public static final int DEFAULT_CAPITULATION_LOOKBACK_WEEKS = 6;

    /** 近 6 日平均成交额下限（元） */
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    /** @deprecated v1.1 改用三段式叙事，保留兼容 */
    private int pitContextMin = DEFAULT_PIT_CONTEXT_MIN;

    private double capitulationDayRate = DEFAULT_CAPITULATION_DAY_RATE;
    private double capitulationWeekRate = DEFAULT_CAPITULATION_WEEK_RATE;
    private double minDrawdownFrom52w = DEFAULT_MIN_DRAWDOWN_FROM_52W;
    private int capitulationLookbackDays = DEFAULT_CAPITULATION_LOOKBACK_DAYS;
    private int capitulationLookbackWeeks = DEFAULT_CAPITULATION_LOOKBACK_WEEKS;

    private boolean enableModeA = true;
    private boolean enableModeB = true;
    private boolean enableModeC = true;

    /** 最低档位 ALL/S/A/B/C */
    private String tierMin = DEFAULT_TIER_MIN;

    public static ReboundStrategyParams defaults() {
        return new ReboundStrategyParams();
    }

    public static ReboundStrategyParams merge(ReboundStrategyParams override) {
        if (override == null) {
            return defaults();
        }
        ReboundStrategyParams base = defaults();
        if (override.minAvgAmount > 0) {
            base.minAvgAmount = override.minAvgAmount;
        }
        if (override.pitContextMin >= 1 && override.pitContextMin <= 3) {
            base.pitContextMin = override.pitContextMin;
        }
        if (override.capitulationDayRate > 0) {
            base.capitulationDayRate = override.capitulationDayRate;
        }
        if (override.capitulationWeekRate > 0) {
            base.capitulationWeekRate = override.capitulationWeekRate;
        }
        if (override.minDrawdownFrom52w > 0) {
            base.minDrawdownFrom52w = override.minDrawdownFrom52w;
        }
        if (override.capitulationLookbackDays > 0) {
            base.capitulationLookbackDays = override.capitulationLookbackDays;
        }
        if (override.capitulationLookbackWeeks > 0) {
            base.capitulationLookbackWeeks = override.capitulationLookbackWeeks;
        }
        base.enableModeA = override.enableModeA;
        base.enableModeB = override.enableModeB;
        base.enableModeC = override.enableModeC;
        if (override.tierMin != null && !override.tierMin.isEmpty()) {
            base.tierMin = override.tierMin;
        }
        return base;
    }

    public boolean passTierFilter(char tier) {
        if (tierMin == null || "ALL".equalsIgnoreCase(tierMin)) {
            return true;
        }
        int min = tierRank(tierMin.charAt(0));
        return tierRank(tier) >= min;
    }

    private static int tierRank(char tier) {
        switch (tier) {
            case 'S': return 4;
            case 'A': return 3;
            case 'B': return 2;
            case 'C': return 1;
            default: return 0;
        }
    }
}
