package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 级联交叉突破（cascade）· 可选三门 + 日/周/月基准档 + 日 K 边沿 + 级联确认
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CascadeStrategyParams {

    public static final int DEFAULT_LOOKBACK_DAY = 60;
    public static final int DEFAULT_LOOKBACK_WEEK = 52;
    public static final int DEFAULT_LOOKBACK_MONTH = 36;
    public static final double DEFAULT_MIN_AVG_AMOUNT = 30_000_000D;

    @Builder.Default
    private boolean enableDualLowGate = false;

    @Builder.Default
    private boolean enableMacdGate = false;

    @Builder.Default
    private boolean enableCrossLowGate = false;

    /** 突破日基准 K.high */
    @Builder.Default
    private boolean enableDay = true;

    /** 突破周基准 K.high */
    @Builder.Default
    private boolean enableWeek = false;

    /** 突破月基准 K.high */
    @Builder.Default
    private boolean enableMonth = false;

    @Builder.Default
    private int lookbackDay = DEFAULT_LOOKBACK_DAY;

    @Builder.Default
    private int lookbackWeek = DEFAULT_LOOKBACK_WEEK;

    @Builder.Default
    private int lookbackMonth = DEFAULT_LOOKBACK_MONTH;

    /** 须同时满足 30m 跨日桶柱内突破 */
    @Builder.Default
    private boolean requireUltra = false;

    /** 近 6 日日均成交额门槛（元）；0 表示不启用 */
    @Builder.Default
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    public static CascadeStrategyParams defaults() {
        return CascadeStrategyParams.builder().build();
    }

    public static CascadeStrategyParams merge(CascadeStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        CascadeStrategyParams d = defaults();
        d.enableDualLowGate = incoming.enableDualLowGate;
        d.enableMacdGate = incoming.enableMacdGate;
        d.enableCrossLowGate = incoming.enableCrossLowGate;
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
        d.requireUltra = incoming.requireUltra;
        if (incoming.minAvgAmount >= 0) {
            d.minAvgAmount = incoming.minAvgAmount;
        }
        if (!d.enableDay && !d.enableWeek && !d.enableMonth) {
            d.enableDay = true;
        }
        return d;
    }
}
