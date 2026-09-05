package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MA 金叉/死叉点突破、交叉点/波段顶突破共用参数
 * （madcbreak / magcbreak / maghbreak / mayangpierce）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaCrossPointStrategyParams {

    public enum Tier {
        MIN30, DAY, WEEK, MONTH, QUARTER
    }

    public static final int DEFAULT_LOOKBACK_MIN30 = 200;
    public static final int DEFAULT_LOOKBACK_DAY = 120;
    public static final int DEFAULT_LOOKBACK_WEEK = 52;
    public static final int DEFAULT_LOOKBACK_MONTH = 36;
    public static final int DEFAULT_LOOKBACK_QUARTER = 24;
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
    private int lookbackQuarter = DEFAULT_LOOKBACK_QUARTER;

    @Builder.Default
    private boolean enableMinAmountFilter = true;

    @Builder.Default
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    /** 可选均线多头（多选取交集）：策略1/2 为 MA5&gt;MA60；策略3/4 为 MA10≥MA60 */
    @Builder.Default
    private boolean requireDayMaBull = false;
    @Builder.Default
    private boolean requireWeekMaBull = false;
    @Builder.Default
    private boolean requireMonthMaBull = false;
    @Builder.Default
    private boolean requireQuarterMaBull = false;
    @Builder.Default
    private boolean requireYearMaBull = false;

    /** 可选均线空头（多选取交集，仅策略3/4）：该周期 MA10&lt;MA60 */
    @Builder.Default
    private boolean requireDayMaBear = false;
    @Builder.Default
    private boolean requireWeekMaBear = false;
    @Builder.Default
    private boolean requireMonthMaBear = false;
    @Builder.Default
    private boolean requireQuarterMaBear = false;
    @Builder.Default
    private boolean requireYearMaBear = false;

    public static MaCrossPointStrategyParams defaults() {
        return MaCrossPointStrategyParams.builder().build();
    }

    public static MaCrossPointStrategyParams merge(MaCrossPointStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        MaCrossPointStrategyParams d = defaults();
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
        if (incoming.lookbackQuarter >= 6) {
            d.lookbackQuarter = incoming.lookbackQuarter;
        }
        d.enableMinAmountFilter = incoming.enableMinAmountFilter;
        if (incoming.minAvgAmount > 0) {
            d.minAvgAmount = incoming.minAvgAmount;
        }
        d.requireDayMaBull = incoming.requireDayMaBull;
        d.requireWeekMaBull = incoming.requireWeekMaBull;
        d.requireMonthMaBull = incoming.requireMonthMaBull;
        d.requireQuarterMaBull = incoming.requireQuarterMaBull;
        d.requireYearMaBull = incoming.requireYearMaBull;
        d.requireDayMaBear = incoming.requireDayMaBear;
        d.requireWeekMaBear = incoming.requireWeekMaBear;
        d.requireMonthMaBear = incoming.requireMonthMaBear;
        d.requireQuarterMaBear = incoming.requireQuarterMaBear;
        d.requireYearMaBear = incoming.requireYearMaBear;
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
        if ("quarter".equals(s) || "season".equals(s) || "q".equals(s)) {
            return Tier.QUARTER;
        }
        return Tier.DAY;
    }
}
