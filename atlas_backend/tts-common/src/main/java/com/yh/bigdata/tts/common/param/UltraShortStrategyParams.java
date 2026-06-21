package com.yh.bigdata.tts.common.param;



import lombok.AllArgsConstructor;

import lombok.Builder;

import lombok.Data;

import lombok.NoArgsConstructor;



/**

 * 超短线策略（ultra）参数

 */

@Data

@Builder

@NoArgsConstructor

@AllArgsConstructor

public class UltraShortStrategyParams {



    public static final double DEFAULT_MIN_AVG_AMOUNT = 50_000_000D;

    public static final double DEFAULT_MIN_STRONG_PCT = 0.02;

    public static final int DEFAULT_PREV_DAYS = 2;

    public static final int DEFAULT_MAX_BARS_PER_DAY = 8;



    /** 近6日日均成交额下限（元），默认 5000 万 */

    @Builder.Default

    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;



    /** 强K：实体或涨幅阈值（0.02 = 2%） */

    @Builder.Default

    private double minStrongPct = DEFAULT_MIN_STRONG_PCT;



    /** 基准窗口：信号日前 N 个交易日 */

    @Builder.Default

    private int prevDays = DEFAULT_PREV_DAYS;



    @Builder.Default

    private int maxBarsPerDay = DEFAULT_MAX_BARS_PER_DAY;



    /** 可选：月 MACD &gt; 0 */

    @Builder.Default

    private boolean requireMonthMacd = false;



    /** 可选：周 MACD &gt; 0 */

    @Builder.Default

    private boolean requireWeekMacd = false;



    /** 可选：日 MACD &gt; 0 */

    @Builder.Default

    private boolean requireDayMacd = false;



    /** 可选：日 MACD &lt; 0 */

    @Builder.Default

    private boolean requireDayMacdNegative = false;



    /** 可选：周 MACD &lt; 0（大周期仍处零轴下） */

    @Builder.Default

    private boolean requireWeekMacdNegative = false;



    /** 可选：月 MACD &lt; 0 */

    @Builder.Default

    private boolean requireMonthMacdNegative = false;



    /** 突破K：true=须当前（最新）30m K 满足突破条件；false=当日任一根满足即可（多根取最后一根） */

    @Builder.Default

    private boolean requireCurrentBreakout = false;



    public static UltraShortStrategyParams defaults() {

        return UltraShortStrategyParams.builder().build();

    }



    public static UltraShortStrategyParams merge(UltraShortStrategyParams incoming) {

        if (incoming == null) {

            return defaults();

        }

        UltraShortStrategyParams d = defaults();

        if (incoming.minAvgAmount > 0) {

            d.minAvgAmount = incoming.minAvgAmount;

        }

        if (incoming.minStrongPct > 0) {

            d.minStrongPct = incoming.minStrongPct;

        }

        if (incoming.prevDays >= 1) {

            d.prevDays = incoming.prevDays;

        }

        if (incoming.maxBarsPerDay >= 1) {

            d.maxBarsPerDay = incoming.maxBarsPerDay;

        }

        d.requireMonthMacd = incoming.requireMonthMacd;

        d.requireWeekMacd = incoming.requireWeekMacd;

        d.requireDayMacd = incoming.requireDayMacd;

        d.requireDayMacdNegative = incoming.requireDayMacdNegative;

        d.requireWeekMacdNegative = incoming.requireWeekMacdNegative;

        d.requireMonthMacdNegative = incoming.requireMonthMacdNegative;

        d.requireCurrentBreakout = incoming.requireCurrentBreakout;

        return d;

    }

}

