package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短线策略（trend · v3）参数 · 日K + 自然周桶突破
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendV2StrategyParams {

    public static final double DEFAULT_MIN_AVG_AMOUNT = 50_000_000D;
    public static final double DEFAULT_MIN_STRONG_PCT = 0.03;
    public static final int DEFAULT_PREV_WEEKS = 2;
    public static final int DEFAULT_MAX_DAYS_PER_WEEK = 5;
    public static final double DEFAULT_WEEK_PREV_MAX_BEAR_PCT = 0.05;
    public static final double DEFAULT_MONTH_PREV_MAX_BEAR_PCT = 0.10;

    @Builder.Default
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    /** 强K：实体或涨幅阈值（0.03 = 3%） */
    @Builder.Default
    private double minStrongPct = DEFAULT_MIN_STRONG_PCT;

    /** 基准窗口：信号自然周之前 N 个完整周 */
    @Builder.Default
    private int prevWeeks = DEFAULT_PREV_WEEKS;

    /** 每自然周最多纳入几根日K（背景截取） */
    @Builder.Default
    private int maxDaysPerWeek = DEFAULT_MAX_DAYS_PER_WEEK;

    /** 突破K：true=须当前日K满足；false=本周任一日K满足（多根取最后一根） */
    @Builder.Default
    private boolean requireCurrentBreakout = false;

    @Builder.Default
    private boolean requireMonthMacd = false;

    @Builder.Default
    private boolean requireWeekMacd = false;

    @Builder.Default
    private boolean requireDayMacd = false;

    @Builder.Default
    private boolean requireWeekGoldenCross = false;

    /** 须同时满足超短 30m 突破（参数共用 QueryContextParam.ultraShort） */
    @Builder.Default
    private boolean requireUltra = true;

    /** 前一周 K 实体阴线率上限 */
    @Builder.Default
    private double weekPrevMaxBearPct = DEFAULT_WEEK_PREV_MAX_BEAR_PCT;

    /** 前一月 K 实体阴线率上限 */
    @Builder.Default
    private double monthPrevMaxBearPct = DEFAULT_MONTH_PREV_MAX_BEAR_PCT;

    public static TrendV2StrategyParams defaults() {
        return TrendV2StrategyParams.builder().build();
    }

    public static TrendV2StrategyParams merge(TrendV2StrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        TrendV2StrategyParams d = defaults();
        if (incoming.minAvgAmount > 0) {
            d.minAvgAmount = incoming.minAvgAmount;
        }
        if (incoming.minStrongPct > 0) {
            d.minStrongPct = incoming.minStrongPct;
        }
        if (incoming.prevWeeks >= 1) {
            d.prevWeeks = incoming.prevWeeks;
        }
        if (incoming.maxDaysPerWeek >= 1) {
            d.maxDaysPerWeek = incoming.maxDaysPerWeek;
        }
        d.requireCurrentBreakout = incoming.requireCurrentBreakout;
        d.requireMonthMacd = incoming.requireMonthMacd;
        d.requireWeekMacd = incoming.requireWeekMacd;
        d.requireDayMacd = incoming.requireDayMacd;
        d.requireWeekGoldenCross = incoming.requireWeekGoldenCross;
        d.requireUltra = incoming.requireUltra;
        if (incoming.weekPrevMaxBearPct > 0) {
            d.weekPrevMaxBearPct = incoming.weekPrevMaxBearPct;
        }
        if (incoming.monthPrevMaxBearPct > 0) {
            d.monthPrevMaxBearPct = incoming.monthPrevMaxBearPct;
        }
        return d;
    }
}
