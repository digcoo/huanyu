package com.yh.bigdata.tts.common.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 多周期 MACD&gt;0 可选门（小程序各策略共用）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MacdPositiveGateParams {

    @Builder.Default
    private boolean requireDayMacd = false;

    @Builder.Default
    private boolean requireWeekMacd = false;

    @Builder.Default
    private boolean requireMonthMacd = false;

    public static MacdPositiveGateParams defaults() {
        return MacdPositiveGateParams.builder().build();
    }

    public static MacdPositiveGateParams merge(MacdPositiveGateParams incoming) {
        if (incoming == null) {
            return defaults();
        }
        MacdPositiveGateParams d = defaults();
        d.requireDayMacd = incoming.requireDayMacd;
        d.requireWeekMacd = incoming.requireWeekMacd;
        d.requireMonthMacd = incoming.requireMonthMacd;
        return d;
    }
}
