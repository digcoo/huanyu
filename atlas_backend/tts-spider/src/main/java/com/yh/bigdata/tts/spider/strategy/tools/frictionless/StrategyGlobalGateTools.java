package com.yh.bigdata.tts.spider.strategy.tools.frictionless;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.spider.response.CheckResult;

/**
 * 策略全局门控入口：双低支撑门 → 无阻力 MACD 门。
 */
public final class StrategyGlobalGateTools {

    public static final String FULL_GATE_LABEL =
            DualLowSupportGateTools.GATE_LABEL + "|" + FrictionlessMacdGateTools.GATE_LABEL;

    private StrategyGlobalGateTools() {
    }

    public static boolean passGate(StockBase stock, CheckResult checkResult) {
        if (!DualLowSupportGateTools.passesAll(stock, checkResult)) {
            return false;
        }
        return FrictionlessMacdGateTools.passesMacdAll(stock, checkResult);
    }
}
