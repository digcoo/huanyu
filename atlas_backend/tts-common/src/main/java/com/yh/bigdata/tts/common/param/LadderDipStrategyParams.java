package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 级联梯子探底回升（ldip）· 可选四门 + 日/周/月档位可选 + 基准大阳 + 中间破 low + 回升 K
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LadderDipStrategyParams {

    public static final int DEFAULT_LOOKBACK_DAY = 60;
    public static final int DEFAULT_LOOKBACK_WEEK = 52;
    public static final int DEFAULT_LOOKBACK_MONTH = 36;
    public static final double DEFAULT_MIN_AVG_AMOUNT = 30_000_000D;

    @Builder.Default
    private boolean enableDualLowGate = false;

    @Builder.Default
    private boolean enableMacdGate = false;

    @Builder.Default
    private boolean enableMacdDcHighGate = true;

    @Builder.Default
    private boolean enableCrossLowGate = false;

    @Builder.Default
    private boolean enableBarHighGate = false;

    @Builder.Default
    private boolean enableAllYangGate = true;

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
    private boolean requireUltra = false;

    @Builder.Default
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    public static LadderDipStrategyParams defaults() {
        return LadderDipStrategyParams.builder().build();
    }

    public static LadderDipStrategyParams merge(LadderDipStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        LadderDipStrategyParams d = defaults();
        d.enableDualLowGate = incoming.enableDualLowGate;
        d.enableMacdGate = incoming.enableMacdGate;
        d.enableMacdDcHighGate = incoming.enableMacdDcHighGate;
        d.enableCrossLowGate = incoming.enableCrossLowGate;
        d.enableBarHighGate = incoming.enableBarHighGate;
        d.enableAllYangGate = incoming.enableAllYangGate;
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
