package com.yh.bigdata.tts.common.param;



import lombok.AllArgsConstructor;

import lombok.Builder;

import lombok.Data;

import lombok.NoArgsConstructor;



/**

 * 底部波段突破（bottombandhigh）：MACD&lt;0 + 末 K 首次边沿突破末波段 bandHigh。

 */

@Data

@Builder

@NoArgsConstructor

@AllArgsConstructor

public class BottomBandHighStrategyParams {



    public enum Tier {

        DAY, WEEK, MONTH

    }



    public static final int DEFAULT_LOOKBACK_DAY = 120;

    public static final int DEFAULT_LOOKBACK_WEEK = 52;

    public static final int DEFAULT_LOOKBACK_MONTH = 36;

    public static final double DEFAULT_MIN_AVG_AMOUNT = 3000D * 10_000D;



    @Builder.Default

    private Tier tier = Tier.DAY;



    @Builder.Default

    private int lookbackDay = DEFAULT_LOOKBACK_DAY;



    @Builder.Default

    private int lookbackWeek = DEFAULT_LOOKBACK_WEEK;



    @Builder.Default

    private int lookbackMonth = DEFAULT_LOOKBACK_MONTH;



    @Builder.Default

    private boolean enableMinAmountFilter = true;



    @Builder.Default

    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;



    public static BottomBandHighStrategyParams defaults() {

        return BottomBandHighStrategyParams.builder().build();

    }



    public static BottomBandHighStrategyParams merge(BottomBandHighStrategyParams incoming) {

        if (incoming == null) {

            return defaults();

        }

        BottomBandHighStrategyParams d = defaults();

        if (incoming.tier != null) {

            d.tier = incoming.tier;

        }

        if (incoming.lookbackDay >= 10) {

            d.lookbackDay = incoming.lookbackDay;

        }

        if (incoming.lookbackWeek >= 10) {

            d.lookbackWeek = incoming.lookbackWeek;

        }

        if (incoming.lookbackMonth >= 6) {

            d.lookbackMonth = incoming.lookbackMonth;

        }

        d.enableMinAmountFilter = incoming.enableMinAmountFilter;

        if (incoming.minAvgAmount > 0) {

            d.minAvgAmount = incoming.minAvgAmount;

        }

        return d;

    }



    public static Tier parseTier(String raw) {

        if (raw == null || raw.isEmpty()) {

            return Tier.DAY;

        }

        String s = raw.trim().toLowerCase();

        if ("week".equals(s) || "medium".equals(s)) {

            return Tier.WEEK;

        }

        if ("month".equals(s) || "long".equals(s)) {

            return Tier.MONTH;

        }

        return Tier.DAY;

    }

}

