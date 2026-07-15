package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MACD 交叉边沿突破（macedge）· 可选五门 + Min30/日/周/月/年基准档 + 同档 K 边沿突破
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MacdEdgeStrategyParams {

    public static final int DEFAULT_LOOKBACK_MIN30 = 50;
    public static final int DEFAULT_LOOKBACK_DAY = 60;
    public static final int DEFAULT_LOOKBACK_WEEK = 52;
    public static final int DEFAULT_LOOKBACK_MONTH = 36;
    public static final int DEFAULT_LOOKBACK_YEAR = 20;
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
    private boolean enableMin30 = false;

    @Builder.Default
    private boolean enableDay = true;

    @Builder.Default
    private boolean enableWeek = false;

    @Builder.Default
    private boolean enableMonth = false;

    @Builder.Default
    private boolean enableYear = false;

    @Builder.Default
    private int lookbackMin30 = DEFAULT_LOOKBACK_MIN30;

    @Builder.Default
    private int lookbackDay = DEFAULT_LOOKBACK_DAY;

    @Builder.Default
    private int lookbackWeek = DEFAULT_LOOKBACK_WEEK;

    @Builder.Default
    private int lookbackMonth = DEFAULT_LOOKBACK_MONTH;

    @Builder.Default
    private int lookbackYear = DEFAULT_LOOKBACK_YEAR;

    @Builder.Default
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    public static MacdEdgeStrategyParams defaults() {
        return MacdEdgeStrategyParams.builder().build();
    }

    public static MacdEdgeStrategyParams merge(MacdEdgeStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        MacdEdgeStrategyParams d = defaults();
        d.enableDualLowGate = incoming.enableDualLowGate;
        d.enableMacdGate = incoming.enableMacdGate;
        d.enableMacdDcHighGate = incoming.enableMacdDcHighGate;
        d.enableCrossLowGate = incoming.enableCrossLowGate;
        d.enableBarHighGate = incoming.enableBarHighGate;
        d.enableAllYangGate = incoming.enableAllYangGate;
        d.enableMin30 = incoming.enableMin30;
        d.enableDay = incoming.enableDay;
        d.enableWeek = incoming.enableWeek;
        d.enableMonth = incoming.enableMonth;
        d.enableYear = incoming.enableYear;
        if (incoming.lookbackMin30 >= 10) {
            d.lookbackMin30 = incoming.lookbackMin30;
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
        if (incoming.lookbackYear >= 4) {
            d.lookbackYear = incoming.lookbackYear;
        }
        if (incoming.minAvgAmount >= 0) {
            d.minAvgAmount = incoming.minAvgAmount;
        }
        if (!d.enableMin30 && !d.enableDay && !d.enableWeek && !d.enableMonth && !d.enableYear) {
            d.enableDay = true;
        }
        return d;
    }
}
