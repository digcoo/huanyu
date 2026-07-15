package com.yh.bigdata.tts.spider.strategy.tools.waveperiodgate;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.WavePeriodGateStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WeekMonthBandLowGateTools;

/**
 * 凹凸波段突破 · 周/月收盘价须同时 &gt; 各周期末波段底。
 */
public final class WavePeriodGateBandLowGateTools {

    private static final String LOG_PREFIX = "[WPG]";

    private WavePeriodGateBandLowGateTools() {
    }

    public static boolean passesWeekMonthBandLowGate(StockBase stock, CheckResult checkResult,
                                                     WavePeriodGateStrategyParams params) {
        WavePeriodGateStrategyParams p = params != null ? params : WavePeriodGateStrategyParams.defaults();
        if (!p.isEnableWeekMonthBandLowGate()) {
            return true;
        }
        return WeekMonthBandLowGateTools.passesWeekMonthGate(stock, checkResult,
                p.getLookbackWeek(), p.getLookbackMonth(), LOG_PREFIX);
    }
}
