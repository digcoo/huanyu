package com.yh.bigdata.tts.common.param;



import lombok.AllArgsConstructor;

import lombok.Builder;

import lombok.Data;

import lombok.NoArgsConstructor;



/**

 * 级联 MACD 凹波段日突破（cascadewaveconcaveday）

 */

@Data

@Builder

@NoArgsConstructor

@AllArgsConstructor

public class CascadeWaveConcaveDayStrategyParams {



    public static final int DEFAULT_LOOKBACK_DAY = 60;

    public static final int DEFAULT_LOOKBACK_WEEK = 52;

    public static final int DEFAULT_LOOKBACK_MONTH = 36;

    public static final int DEFAULT_LOOKBACK_YEAR = 20;



    @Builder.Default

    private boolean enableAllYangGate = false;



    @Builder.Default

    private boolean enableMin30Gate = false;



    @Builder.Default

    private boolean enableBandLastYangLowGate = true;



    @Builder.Default

    private boolean enableYangBandTrendGate = false;



    @Builder.Default

    private boolean enablePrevBandBreak = false;



    @Builder.Default

    private boolean enableDay = true;



    @Builder.Default

    private boolean enableWeek = false;



    @Builder.Default

    private boolean enableMonth = false;



    @Builder.Default

    private int lookbackDay = DEFAULT_LOOKBACK_DAY;



    @Builder.Default

    private int lookbackWeek = DEFAULT_LOOKBACK_WEEK;



    @Builder.Default

    private int lookbackMonth = DEFAULT_LOOKBACK_MONTH;



    @Builder.Default

    private int lookbackYear = DEFAULT_LOOKBACK_YEAR;



    @Builder.Default

    private double minAvgAmount = 0D;



    public static CascadeWaveConcaveDayStrategyParams defaults() {

        return CascadeWaveConcaveDayStrategyParams.builder().build();

    }



    public static CascadeWaveConcaveDayStrategyParams merge(CascadeWaveConcaveDayStrategyParams incoming) {

        if (incoming == null) {

            return defaults();

        }

        CascadeWaveConcaveDayStrategyParams d = defaults();

        d.enableAllYangGate = incoming.enableAllYangGate;

        d.enableMin30Gate = incoming.enableMin30Gate;

        d.enableBandLastYangLowGate = incoming.enableBandLastYangLowGate;

        d.enableYangBandTrendGate = incoming.enableYangBandTrendGate;

        d.enablePrevBandBreak = incoming.enablePrevBandBreak;

        d.enableDay = incoming.enableDay;

        d.enableWeek = incoming.enableWeek;

        d.enableMonth = incoming.enableMonth;

        if (incoming.lookbackDay >= 10) {

            d.lookbackDay = incoming.lookbackDay;

        }

        if (incoming.lookbackWeek >= 10) {

            d.lookbackWeek = incoming.lookbackWeek;

        }

        if (incoming.lookbackMonth >= 6) {

            d.lookbackMonth = incoming.lookbackMonth;

        }

        if (incoming.lookbackYear >= 4) {

            d.lookbackYear = incoming.lookbackYear;

        }

        if (incoming.minAvgAmount >= 0) {

            d.minAvgAmount = incoming.minAvgAmount;

        }

        if (!d.enableDay && !d.enableWeek && !d.enableMonth) {

            d.enableDay = true;

        }

        return d;

    }

}

