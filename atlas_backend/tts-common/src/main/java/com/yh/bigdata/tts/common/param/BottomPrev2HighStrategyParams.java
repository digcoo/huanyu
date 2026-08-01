package com.yh.bigdata.tts.common.param;



import lombok.AllArgsConstructor;

import lombok.Builder;

import lombok.Data;

import lombok.NoArgsConstructor;



/**

 * 底部 High 突破（bottomprev2high）：MACD&lt;0 + 末 K 边沿突破末前 2 根 K 的 max(high)。

 */

@Data

@Builder

@NoArgsConstructor

@AllArgsConstructor

public class BottomPrev2HighStrategyParams {



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



    public static BottomPrev2HighStrategyParams defaults() {

        return BottomPrev2HighStrategyParams.builder().build();

    }



    public static BottomPrev2HighStrategyParams merge(BottomPrev2HighStrategyParams incoming) {

        if (incoming == null) {

            return defaults();

        }

        BottomPrev2HighStrategyParams d = defaults();

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

