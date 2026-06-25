package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 底部机会（bogo）· 无阻力 MACD 门 + 金叉/死叉基准 K 突破
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BogoStrategyParams {

    public static final int DEFAULT_LOOKBACK_DAY = 60;
    public static final int DEFAULT_LOOKBACK_WEEK = 52;
    public static final int DEFAULT_LOOKBACK_MONTH = 36;

    /** 日 K 联检 */
    @Builder.Default
    private boolean enableDay = true;

    /** 周 K 联检 */
    @Builder.Default
    private boolean enableWeek = false;

    /** 月 K 联检 */
    @Builder.Default
    private boolean enableMonth = false;

    @Builder.Default
    private int lookbackDay = DEFAULT_LOOKBACK_DAY;

    @Builder.Default
    private int lookbackWeek = DEFAULT_LOOKBACK_WEEK;

    @Builder.Default
    private int lookbackMonth = DEFAULT_LOOKBACK_MONTH;

    public static BogoStrategyParams defaults() {
        return BogoStrategyParams.builder().build();
    }

    public static BogoStrategyParams merge(BogoStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        BogoStrategyParams d = defaults();
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
        if (!d.enableDay && !d.enableWeek && !d.enableMonth) {
            d.enableDay = true;
        }
        return d;
    }
}
