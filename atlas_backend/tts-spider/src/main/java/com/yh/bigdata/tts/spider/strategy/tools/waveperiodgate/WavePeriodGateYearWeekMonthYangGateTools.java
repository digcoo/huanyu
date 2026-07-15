package com.yh.bigdata.tts.spider.strategy.tools.waveperiodgate;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.WavePeriodGateStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YearWeekMonthYangGateTools;

/**
 * 凹凸波段突破 · 年/周/月须全部收阳。
 */
public final class WavePeriodGateYearWeekMonthYangGateTools {

    private static final String LOG_PREFIX = "[WPG]";

    private WavePeriodGateYearWeekMonthYangGateTools() {
    }

    public static boolean passesYearWeekMonthYangGate(StockBase stock, CheckResult checkResult,
                                                      WavePeriodGateStrategyParams params) {
        WavePeriodGateStrategyParams p = params != null ? params : WavePeriodGateStrategyParams.defaults();
        if (!p.isEnableYearWeekMonthYangGate()) {
            return true;
        }
        return YearWeekMonthYangGateTools.passesGate(stock, checkResult, LOG_PREFIX);
    }
}
