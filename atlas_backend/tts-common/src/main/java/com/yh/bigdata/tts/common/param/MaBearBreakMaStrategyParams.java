package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MA空头破MA（mabearbreakma）：日线 3M 空头 + close&gt;max(MA5,MA10)，
 * 且 30 分钟 3M 多头 + 边沿/开盘突破均线 MAX。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaBearBreakMaStrategyParams {

    public static final int DEFAULT_LOOKBACK_DAY = 120;
    public static final int DEFAULT_LOOKBACK_MIN30 = 200;
    public static final double DEFAULT_MIN_AVG_AMOUNT = 3000D * 10_000D;

    @Builder.Default
    private int lookbackDay = DEFAULT_LOOKBACK_DAY;

    @Builder.Default
    private int lookbackMin30 = DEFAULT_LOOKBACK_MIN30;

    @Builder.Default
    private boolean enableMinAmountFilter = true;

    @Builder.Default
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    public static MaBearBreakMaStrategyParams defaults() {
        return MaBearBreakMaStrategyParams.builder().build();
    }

    public static MaBearBreakMaStrategyParams merge(MaBearBreakMaStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        MaBearBreakMaStrategyParams d = defaults();
        if (incoming.lookbackDay >= 10) {
            d.lookbackDay = incoming.lookbackDay;
        }
        if (incoming.lookbackMin30 >= 40) {
            d.lookbackMin30 = incoming.lookbackMin30;
        }
        d.enableMinAmountFilter = incoming.enableMinAmountFilter;
        if (incoming.minAvgAmount > 0) {
            d.minAvgAmount = incoming.minAvgAmount;
        }
        return d;
    }
}
