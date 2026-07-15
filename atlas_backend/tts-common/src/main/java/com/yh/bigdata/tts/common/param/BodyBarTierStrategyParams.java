package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 柱子策略（bodybar）：日/周/月单档实体柱突破 + 信号 K 上涨率。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BodyBarTierStrategyParams {

    public enum Tier {
        DAY, WEEK, MONTH
    }

    public static final int DEFAULT_LOOKBACK_BARS = 10;
    public static final double DEFAULT_HISTORY_BODY_PCT_DAY = 0.04;
    public static final double DEFAULT_SIGNAL_RISE_PCT_DAY = 0.03;
    public static final double DEFAULT_HISTORY_BODY_PCT_WEEK = 0.06;
    public static final double DEFAULT_SIGNAL_RISE_PCT_WEEK = 0.05;
    public static final double DEFAULT_HISTORY_BODY_PCT_MONTH = 0.08;
    public static final double DEFAULT_SIGNAL_RISE_PCT_MONTH = 0.06;

    @Builder.Default
    private Tier tier = Tier.DAY;

    /** 向前检视的 K 根数（不含信号 K） */
    @Builder.Default
    private int lookbackBars = DEFAULT_LOOKBACK_BARS;

    @Builder.Default
    private double historyBodyPctDay = DEFAULT_HISTORY_BODY_PCT_DAY;

    @Builder.Default
    private double signalRisePctDay = DEFAULT_SIGNAL_RISE_PCT_DAY;

    @Builder.Default
    private double historyBodyPctWeek = DEFAULT_HISTORY_BODY_PCT_WEEK;

    @Builder.Default
    private double signalRisePctWeek = DEFAULT_SIGNAL_RISE_PCT_WEEK;

    @Builder.Default
    private double historyBodyPctMonth = DEFAULT_HISTORY_BODY_PCT_MONTH;

    @Builder.Default
    private double signalRisePctMonth = DEFAULT_SIGNAL_RISE_PCT_MONTH;

    public static BodyBarTierStrategyParams defaults() {
        return BodyBarTierStrategyParams.builder().build();
    }

    public static BodyBarTierStrategyParams merge(BodyBarTierStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        BodyBarTierStrategyParams d = defaults();
        if (incoming.tier != null) {
            d.tier = incoming.tier;
        }
        if (incoming.lookbackBars >= 3) {
            d.lookbackBars = incoming.lookbackBars;
        }
        if (incoming.historyBodyPctDay > 0) {
            d.historyBodyPctDay = incoming.historyBodyPctDay;
        }
        if (incoming.signalRisePctDay > 0) {
            d.signalRisePctDay = incoming.signalRisePctDay;
        }
        if (incoming.historyBodyPctWeek > 0) {
            d.historyBodyPctWeek = incoming.historyBodyPctWeek;
        }
        if (incoming.signalRisePctWeek > 0) {
            d.signalRisePctWeek = incoming.signalRisePctWeek;
        }
        if (incoming.historyBodyPctMonth > 0) {
            d.historyBodyPctMonth = incoming.historyBodyPctMonth;
        }
        if (incoming.signalRisePctMonth > 0) {
            d.signalRisePctMonth = incoming.signalRisePctMonth;
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

    public double resolveHistoryBodyPct() {
        if (tier == Tier.WEEK) {
            return historyBodyPctWeek;
        }
        if (tier == Tier.MONTH) {
            return historyBodyPctMonth;
        }
        return historyBodyPctDay;
    }

    public double resolveSignalRisePct() {
        if (tier == Tier.WEEK) {
            return signalRisePctWeek;
        }
        if (tier == Tier.MONTH) {
            return signalRisePctMonth;
        }
        return signalRisePctDay;
    }
}
