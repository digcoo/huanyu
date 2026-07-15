package com.yh.bigdata.tts.common.param;



import lombok.AllArgsConstructor;

import lombok.Builder;

import lombok.Data;

import lombok.NoArgsConstructor;



/**

 * 波段策略参数：短线（周形态门）/ 中线（月形态门）+ 日收阳。

 */

@Data

@Builder

@NoArgsConstructor

@AllArgsConstructor

public class WaveBandStrategyParams {



    public static final int DEFAULT_LOOKBACK_DAY = 60;

    public static final int DEFAULT_LOOKBACK_WEEK = 52;

    public static final int DEFAULT_LOOKBACK_MONTH = 36;



    @Builder.Default

    private int lookbackDay = DEFAULT_LOOKBACK_DAY;



    @Builder.Default

    private int lookbackWeek = DEFAULT_LOOKBACK_WEEK;



    @Builder.Default

    private int lookbackMonth = DEFAULT_LOOKBACK_MONTH;



    @Builder.Default

    private double minAvgAmount = 0D;



    public static WaveBandStrategyParams defaults() {

        return WaveBandStrategyParams.builder().build();

    }



    public static WaveBandStrategyParams merge(WaveBandStrategyParams incoming) {

        if (incoming == null) {

            return defaults();

        }

        WaveBandStrategyParams d = defaults();

        if (incoming.lookbackDay >= 10) {

            d.lookbackDay = incoming.lookbackDay;

        }

        if (incoming.lookbackWeek >= 10) {

            d.lookbackWeek = incoming.lookbackWeek;

        }

        if (incoming.lookbackMonth >= 6) {

            d.lookbackMonth = incoming.lookbackMonth;

        }

        if (incoming.minAvgAmount >= 0) {

            d.minAvgAmount = incoming.minAvgAmount;

        }

        return d;

    }

}


