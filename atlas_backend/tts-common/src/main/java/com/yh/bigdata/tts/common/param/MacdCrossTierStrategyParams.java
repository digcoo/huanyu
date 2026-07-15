package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 同档 MACD 交叉突破（macdcrosstier）：日/周/月单档评估。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MacdCrossTierStrategyParams {

    public enum Tier {
        DAY, WEEK, MONTH
    }

    public static final int DEFAULT_LOOKBACK_DAY = 60;
    public static final int DEFAULT_LOOKBACK_WEEK = 52;
    public static final int DEFAULT_LOOKBACK_MONTH = 36;
    public static final double DEFAULT_SIGNAL_RISE_PCT = 0.03;

    @Builder.Default
    private Tier tier = Tier.DAY;

    @Builder.Default
    private int lookbackDay = DEFAULT_LOOKBACK_DAY;

    @Builder.Default
    private int lookbackWeek = DEFAULT_LOOKBACK_WEEK;

    @Builder.Default
    private int lookbackMonth = DEFAULT_LOOKBACK_MONTH;

    /** 路径：突破最近金叉 K */
    @Builder.Default
    private boolean enableGoldenCross = true;

    /** 路径：突破最近死叉 K */
    @Builder.Default
    private boolean enableDeathCross = true;

    /** 路径：当前 MACD 金叉且上涨率达标 */
    @Builder.Default
    private boolean enableGoldenCrossRiseGate = true;

    /** 金叉上涨率路径阈值（三档共用） */
    @Builder.Default
    private double signalRisePct = DEFAULT_SIGNAL_RISE_PCT;

    public static MacdCrossTierStrategyParams defaults() {
        return MacdCrossTierStrategyParams.builder().build();
    }

    public static MacdCrossTierStrategyParams merge(MacdCrossTierStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        MacdCrossTierStrategyParams d = defaults();
        if (incoming.tier != null) {
            d.tier = incoming.tier;
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
        d.enableGoldenCross = incoming.enableGoldenCross;
        d.enableDeathCross = incoming.enableDeathCross;
        d.enableGoldenCrossRiseGate = incoming.enableGoldenCrossRiseGate;
        if (incoming.signalRisePct > 0) {
            d.signalRisePct = incoming.signalRisePct;
        }
        if (!d.enableGoldenCross && !d.enableDeathCross && !d.enableGoldenCrossRiseGate) {
            MacdCrossTierStrategyParams def = defaults();
            d.enableGoldenCross = def.isEnableGoldenCross();
            d.enableDeathCross = def.isEnableDeathCross();
            d.enableGoldenCrossRiseGate = def.isEnableGoldenCrossRiseGate();
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
