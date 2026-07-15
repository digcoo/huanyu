package com.yh.bigdata.tts.spider.strategy.tools.waveperiodgate;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.WavePeriodGateStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WeekMonthBandShapeGateTools;

/**
 * 凹凸波段突破 · 周/月凹凸形态门（委托 {@link WeekMonthBandShapeGateTools}）。
 */
public final class WavePeriodGateBandShapeGateTools {

    private static final String LOG_PREFIX = "[WPG]";

    private WavePeriodGateBandShapeGateTools() {
    }

    public static boolean passesWeekMonthShapeGate(StockBase stock, CheckResult checkResult,
                                                   WavePeriodGateStrategyParams params) {
        WavePeriodGateStrategyParams p = params != null ? params : WavePeriodGateStrategyParams.defaults();
        if (!p.isEnableWeekMonthBandShapeGate()) {
            return true;
        }
        return WeekMonthBandShapeGateTools.passesWeekMonthGate(stock, checkResult,
                p.getLookbackWeek(), p.getLookbackMonth(), LOG_PREFIX);
    }
}
