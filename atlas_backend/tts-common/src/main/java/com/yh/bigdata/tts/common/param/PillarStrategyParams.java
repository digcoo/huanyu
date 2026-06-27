package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 柱子内上移（pillar）· 全局门控 + 强柱基准 K 突破
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PillarStrategyParams {

    public static final int DEFAULT_LOOKBACK_DAY = 60;
    public static final int DEFAULT_LOOKBACK_WEEK = 52;
    public static final int DEFAULT_LOOKBACK_MONTH = 36;

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

    /** 须同时满足 30m 超短梯子 */
    @Builder.Default
    private boolean requireUltra = false;

    /** 近 6 日日均成交额门槛（元）；0 表示不启用 */
    @Builder.Default
    private double minAvgAmount = 0;

    public static PillarStrategyParams defaults() {
        return PillarStrategyParams.builder().build();
    }

    public static PillarStrategyParams merge(PillarStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        PillarStrategyParams d = defaults();
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
