package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MACD金叉（macdgc）：日/周/月单档当前 MACD 金叉 + 可选成交额/上涨率门。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MacdGoldenCrossStrategyParams {

    public enum Tier {
        DAY, WEEK, MONTH
    }

    public static final double DEFAULT_MIN_AVG_AMOUNT = 3000D * 10_000D;
    public static final double DEFAULT_SIGNAL_RISE_PCT = 0.03;
    public static final double DEFAULT_HISTORY_RISE_PCT = 0.03;
    public static final int DEFAULT_HISTORY_LOOKBACK_BARS = 5;

    @Builder.Default
    private Tier tier = Tier.DAY;

    @Builder.Default
    private boolean enableMinAmountFilter = true;

    @Builder.Default
    private double minAvgAmount = DEFAULT_MIN_AVG_AMOUNT;

    @Builder.Default
    private boolean enableSignalRiseGate = true;

    @Builder.Default
    private double signalRisePct = DEFAULT_SIGNAL_RISE_PCT;

    @Builder.Default
    private boolean enableHistoryRiseGate = true;

    @Builder.Default
    private int historyLookbackBars = DEFAULT_HISTORY_LOOKBACK_BARS;

    @Builder.Default
    private double historyRisePct = DEFAULT_HISTORY_RISE_PCT;

    public static MacdGoldenCrossStrategyParams defaults() {
        return MacdGoldenCrossStrategyParams.builder().build();
    }

    public static MacdGoldenCrossStrategyParams merge(MacdGoldenCrossStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        MacdGoldenCrossStrategyParams d = defaults();
        if (incoming.tier != null) {
            d.tier = incoming.tier;
        }
        d.enableMinAmountFilter = incoming.enableMinAmountFilter;
        if (incoming.minAvgAmount > 0) {
            d.minAvgAmount = incoming.minAvgAmount;
        }
        d.enableSignalRiseGate = incoming.enableSignalRiseGate;
        if (incoming.signalRisePct > 0) {
            d.signalRisePct = incoming.signalRisePct;
        }
        d.enableHistoryRiseGate = incoming.enableHistoryRiseGate;
        if (incoming.historyLookbackBars >= 1) {
            d.historyLookbackBars = incoming.historyLookbackBars;
        }
        if (incoming.historyRisePct > 0) {
            d.historyRisePct = incoming.historyRisePct;
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
