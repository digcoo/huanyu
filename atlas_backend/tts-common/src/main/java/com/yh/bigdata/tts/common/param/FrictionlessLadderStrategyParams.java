package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 无阻力梯子（nrf）· 与梯子策略相同档位，固定日/周/月 MACD 门
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrictionlessLadderStrategyParams {

    public enum ActiveTier {
        /** 短线 · 日K 梯子（trend） */
        SHORT,
        /** 中线 · 周K 梯子（medium） */
        MEDIUM,
        /** 长线 · 月K 梯子（long） */
        LONG
    }

    @Builder.Default
    private ActiveTier activeTier = ActiveTier.SHORT;

    public static FrictionlessLadderStrategyParams defaults() {
        return FrictionlessLadderStrategyParams.builder().build();
    }

    public static FrictionlessLadderStrategyParams merge(FrictionlessLadderStrategyParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        FrictionlessLadderStrategyParams d = defaults();
        if (incoming.activeTier != null) {
            d.activeTier = incoming.activeTier;
        }
        return d;
    }

    public static ActiveTier parseActiveTier(String code) {
        if (code == null || code.isEmpty()) {
            return ActiveTier.SHORT;
        }
        switch (code.trim().toLowerCase()) {
            case "medium":
            case "week":
                return ActiveTier.MEDIUM;
            case "long":
            case "month":
                return ActiveTier.LONG;
            case "short":
            case "day":
            default:
                return ActiveTier.SHORT;
        }
    }
}
