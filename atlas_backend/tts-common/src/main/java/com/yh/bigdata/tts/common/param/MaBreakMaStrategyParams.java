package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MA破MAX 共用参数（原 mabreakma 已下线；MaBreakMaTools / mabearbreakma 仍使用）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaBreakMaStrategyParams {

    public enum Tier {
        MIN30, DAY, WEEK, MONTH
    }

    public static final int DEFAULT_LOOKBACK_MIN30 = 200;
    public static final int DEFAULT_LOOKBACK_DAY = 120;
    public static final int DEFAULT_LOOKBACK_WEEK = 52;
    public static final int DEFAULT_LOOKBACK_MONTH = 36;
    public static final double DEFAULT_MIN_AVG_AMOUNT = 3000D * 10_000D;

    @Builder.Default
    private Tier tier = Tier.DAY;

    @Builder.Default
    private int lookbackMin30 = DEFAULT_LOOKBACK_MIN30;

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

    /** 可选：额外要求 30 分钟也满足 3M 多头+破 MAX */
    @Builder.Default
    private boolean enableMin30BreakFilter = false;

    /** 可选大周期 3M 多头排列（多选取交集） */
    @Builder.Default
    private boolean requireDayAlign = false;

    @Builder.Default
    private boolean requireWeekAlign = false;

    @Builder.Default
    private boolean requireMonthAlign = false;

    public static MaBreakMaStrategyParams defaults() {
        return MaBreakMaStrategyParams.builder().build();
    }

    public static MaBreakMaStrategyParams merge(MaBreakMaStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        MaBreakMaStrategyParams d = defaults();
        if (incoming.tier != null) {
            d.tier = incoming.tier;
        }
        if (incoming.lookbackMin30 >= 40) {
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
        d.enableMinAmountFilter = incoming.enableMinAmountFilter;
        if (incoming.minAvgAmount > 0) {
            d.minAvgAmount = incoming.minAvgAmount;
        }
        d.enableMin30BreakFilter = incoming.enableMin30BreakFilter;
        d.requireDayAlign = incoming.requireDayAlign;
        d.requireWeekAlign = incoming.requireWeekAlign;
        d.requireMonthAlign = incoming.requireMonthAlign;
        return d;
    }

    public static Tier parseTier(String raw) {
        if (raw == null || raw.isEmpty()) {
            return Tier.DAY;
        }
        String s = raw.trim().toLowerCase();
        if ("min30".equals(s) || "flash".equals(s) || "30".equals(s) || "30m".equals(s)) {
            return Tier.MIN30;
        }
        if ("week".equals(s) || "medium".equals(s)) {
            return Tier.WEEK;
        }
        if ("month".equals(s) || "long".equals(s)) {
            return Tier.MONTH;
        }
        return Tier.DAY;
    }
}
