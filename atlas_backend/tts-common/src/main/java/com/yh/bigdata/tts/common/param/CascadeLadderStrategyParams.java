package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 级联梯子突破（cladder）· 可选四门 + 日/周/月基准压顶 + 前后阳柱（三周期交集）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CascadeLadderStrategyParams {

    public static final int DEFAULT_LOOKBACK_DAY = 60;
    public static final int DEFAULT_LOOKBACK_WEEK = 52;
    public static final int DEFAULT_LOOKBACK_MONTH = 36;

    @Builder.Default
    private boolean enableDualLowGate = true;

    @Builder.Default
    private boolean enableMacdGate = true;

    @Builder.Default
    private boolean enableCrossLowGate = true;

    @Builder.Default
    private boolean enableBarHighGate = true;

    @Builder.Default
    private int lookbackDay = DEFAULT_LOOKBACK_DAY;

    @Builder.Default
    private int lookbackWeek = DEFAULT_LOOKBACK_WEEK;

    @Builder.Default
    private int lookbackMonth = DEFAULT_LOOKBACK_MONTH;

    @Builder.Default
    private boolean requireUltra = true;

    /** 近 6 日日均成交额门槛（元）；0 表示不启用 */
    @Builder.Default
    private double minAvgAmount = 0D;

    public static CascadeLadderStrategyParams defaults() {
        return CascadeLadderStrategyParams.builder().build();
    }

    public static CascadeLadderStrategyParams merge(CascadeLadderStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        CascadeLadderStrategyParams d = defaults();
        d.enableDualLowGate = incoming.enableDualLowGate;
        d.enableMacdGate = incoming.enableMacdGate;
        d.enableCrossLowGate = incoming.enableCrossLowGate;
        d.enableBarHighGate = incoming.enableBarHighGate;
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
        return d;
    }
}
