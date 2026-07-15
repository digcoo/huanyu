package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 凸波段日突破（waveconvexday）· 末阳中位价默认可选末阳顶 · 日 K 边沿
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaveConvexDayStrategyParams {

    public static final int DEFAULT_LOOKBACK_DAY = 60;
    public static final int DEFAULT_LOOKBACK_WEEK = 52;
    public static final int DEFAULT_LOOKBACK_MONTH = 36;

    @Builder.Default
    private boolean enableAllYangGate = false;

    @Builder.Default
    private boolean enableMin30Gate = false;

    @Builder.Default
    private boolean enableLastHighBreak = false;

    @Builder.Default
    private boolean enableLastMedianBreak = false;

    @Builder.Default
    private boolean enableLastLowBreak = false;

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
    private double minAvgAmount = 0D;

    public static WaveConvexDayStrategyParams defaults() {
        return WaveConvexDayStrategyParams.builder().build();
    }

    public static WaveConvexDayStrategyParams merge(WaveConvexDayStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        WaveConvexDayStrategyParams d = defaults();
        d.enableAllYangGate = incoming.enableAllYangGate;
        d.enableMin30Gate = incoming.enableMin30Gate;
        d.enableLastHighBreak = incoming.enableLastHighBreak;
        d.enableLastMedianBreak = incoming.enableLastMedianBreak;
        d.enableLastLowBreak = incoming.enableLastLowBreak;
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
        if (incoming.minAvgAmount >= 0) {
            d.minAvgAmount = incoming.minAvgAmount;
        }
        if (!d.enableDay && !d.enableWeek && !d.enableMonth) {
            d.enableDay = true;
        }
        return d;
    }
}
