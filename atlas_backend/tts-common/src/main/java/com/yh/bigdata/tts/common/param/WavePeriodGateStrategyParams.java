package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 形态门（waveperiodgate）：日/周/月档凹/凸边沿突破 + 本档 MACD&gt;0。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WavePeriodGateStrategyParams {

    public enum Tier {
        DAY, WEEK, MONTH
    }

    public static final int DEFAULT_LOOKBACK_DAY = 120;
    public static final int DEFAULT_LOOKBACK_WEEK = 52;
    public static final int DEFAULT_LOOKBACK_MONTH = 36;
    public static final int DEFAULT_LOOKBACK_YEAR = 20;
    public static final double DEFAULT_MIN_AVG_AMOUNT = 3000D * 10_000D;

    @Builder.Default
    private Tier tier = Tier.DAY;

    @Builder.Default
    private int lookbackDay = DEFAULT_LOOKBACK_DAY;

    @Builder.Default
    private int lookbackWeek = DEFAULT_LOOKBACK_WEEK;

    @Builder.Default
    private int lookbackMonth = DEFAULT_LOOKBACK_MONTH;

    /** 可选：近 6 日日均成交额门槛 */
    @Builder.Default
    private boolean enableMinAmountFilter = false;

    @Builder.Default
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    /** 收盘价 &gt; max(末波段 low, 次波段 low) */
    @Builder.Default
    private boolean enableMaxBandLowGate = false;

    /** 凹波段：前 K 收盘 ≤ 末波段 high，信号 K 收盘 &gt; 末波段 high */
    @Builder.Default
    private boolean enableConcaveBreakout = true;

    /** 凸波段：前 K 收盘 ≤ 前 K high，信号 K 收盘 &gt; 前 K high */
    @Builder.Default
    private boolean enableConvexBreakout = true;

    /**
     * 上级周期 min 波段 low 门：日档查周、周档查月、月档查年；
     * 无二波段取末波段 low，无波段则上级末 K 须为阳 K。
     */
    @Builder.Default
    private boolean enableUpperPeriodMinBandLowGate = false;

    /** 本档 MACD 柱 &gt; 0 */
    @Builder.Default
    private boolean enableTierMacdPositiveGate = true;

    /** 周、月须同时满足凹凸形态门（凹收阳破末波段顶，凸收阳破前 K 高） */
    @Builder.Default
    private boolean enableWeekMonthBandShapeGate = true;

    /** 周、月收盘价须同时 &gt; 各周期末波段底 */
    @Builder.Default
    private boolean enableWeekMonthBandLowGate = true;

    /** 年、周、月最后一根 K 须全部收阳 */
    @Builder.Default
    private boolean enableYearWeekMonthYangGate = true;

    @Builder.Default
    private int lookbackYear = DEFAULT_LOOKBACK_YEAR;

    public static WavePeriodGateStrategyParams defaults() {
        return WavePeriodGateStrategyParams.builder().build();
    }

    public static WavePeriodGateStrategyParams merge(WavePeriodGateStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        WavePeriodGateStrategyParams d = defaults();
        if (incoming.tier != null) {
            d.tier = incoming.tier;
        }
        if (incoming.lookbackDay >= 20) {
            d.lookbackDay = incoming.lookbackDay;
        }
        if (incoming.lookbackWeek >= 10) {
            d.lookbackWeek = incoming.lookbackWeek;
        }
        if (incoming.lookbackMonth >= 6) {
            d.lookbackMonth = incoming.lookbackMonth;
        }
        d.enableMinAmountFilter = incoming.enableMinAmountFilter;
        d.enableMaxBandLowGate = incoming.enableMaxBandLowGate;
        d.enableConcaveBreakout = incoming.enableConcaveBreakout;
        d.enableConvexBreakout = incoming.enableConvexBreakout;
        d.enableUpperPeriodMinBandLowGate = incoming.enableUpperPeriodMinBandLowGate;
        d.enableTierMacdPositiveGate = incoming.enableTierMacdPositiveGate;
        d.enableWeekMonthBandShapeGate = incoming.enableWeekMonthBandShapeGate;
        d.enableWeekMonthBandLowGate = incoming.enableWeekMonthBandLowGate;
        d.enableYearWeekMonthYangGate = incoming.enableYearWeekMonthYangGate;
        if (incoming.lookbackYear >= 4) {
            d.lookbackYear = incoming.lookbackYear;
        }
        if (incoming.minAvgAmount >= 0) {
            d.minAvgAmount = incoming.minAvgAmount;
        }
        if (!d.enableConcaveBreakout && !d.enableConvexBreakout) {
            d.enableConcaveBreakout = true;
            d.enableConvexBreakout = true;
        }
        return d;
    }

    public static Tier parseTier(String raw) {
        if (raw == null || raw.isEmpty()) {
            return Tier.DAY;
        }
        String s = raw.trim().toLowerCase();
        if ("week".equals(s) || "medium".equals(s)) {
            return Tier.WEEK;
        }
        if ("month".equals(s) || "long".equals(s)) {
            return Tier.MONTH;
        }
        return Tier.DAY;
    }
}
