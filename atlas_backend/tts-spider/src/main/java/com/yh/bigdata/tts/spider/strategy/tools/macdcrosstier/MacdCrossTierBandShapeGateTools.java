package com.yh.bigdata.tts.spider.strategy.tools.macdcrosstier;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MacdCrossTierStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WeekMonthBandShapeGateTools;

/**
 * MACD 交叉突破 · 周/月凹凸形态门（委托 {@link WeekMonthBandShapeGateTools}）。
 */
public final class MacdCrossTierBandShapeGateTools {

    private static final String LOG_PREFIX = "[MCT]";

    private MacdCrossTierBandShapeGateTools() {
    }

    public static boolean passesWeekMonthShapeGate(StockBase stock, CheckResult checkResult,
                                                   MacdCrossTierStrategyParams params) {
        MacdCrossTierStrategyParams p = params != null ? params : MacdCrossTierStrategyParams.defaults();
        return WeekMonthBandShapeGateTools.passesWeekMonthGate(stock, checkResult,
                p.getLookbackWeek(), p.getLookbackMonth(), LOG_PREFIX);
    }
}
