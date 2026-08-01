package com.yh.bigdata.tts.spider.strategy.tools.macdcrosstier;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MacdCrossTierStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WeekMonthBandLowGateTools;

import java.util.List;

/**
 * 日档附加门：周、月收盘价须同时 &gt; 各周期末完整波段的波段底（波段内阳 K low 最小值）。
 */
public final class MacdCrossTierBandLowGateTools {

    private static final String LOG_PREFIX = "[MCT]";

    private MacdCrossTierBandLowGateTools() {
    }

    public static boolean passesDayTierWeekMonthGate(StockBase stock, CheckResult checkResult,
                                                     MacdCrossTierStrategyParams params) {
        MacdCrossTierStrategyParams p = params != null ? params : MacdCrossTierStrategyParams.defaults();
        if (p.getTier() != MacdCrossTierStrategyParams.Tier.DAY) {
            return true;
        }
        return WeekMonthBandLowGateTools.passesWeekMonthGate(stock, checkResult,
                p.getLookbackWeek(), p.getLookbackMonth(), LOG_PREFIX);
    }

    static boolean passesPeriodBandLowOnBars(List<Trade> periodBars, CheckResult checkResult,
                                             PeriodTypeEnum period, int lookback) {
        return WeekMonthBandLowGateTools.passesPeriodBandLowOnBars(
                periodBars, checkResult, period, lookback, LOG_PREFIX);
    }
}
