package com.yh.bigdata.tts.spider.strategy.tools.cascade;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.CascadeStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.DualLowSupportGateTools;
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.FrictionlessMacdGateTools;
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.MacdCrossLowGateTools;

/**
 * 级联交叉突破 · 可选三门（默认均关闭）
 */
public final class CascadeOptionalGateTools {

    private CascadeOptionalGateTools() {
    }

    public static boolean passGate(StockBase stock, CheckResult checkResult, CascadeStrategyParams params) {
        CascadeStrategyParams p = params != null ? params : CascadeStrategyParams.defaults();
        if (p.isEnableDualLowGate()
                && !DualLowSupportGateTools.passesAll(stock, checkResult)) {
            return false;
        }
        if (p.isEnableMacdGate()
                && !FrictionlessMacdGateTools.passesMacdAll(stock, checkResult)) {
            return false;
        }
        if (p.isEnableCrossLowGate()
                && !MacdCrossLowGateTools.passesAll(stock, checkResult)) {
            return false;
        }
        return true;
    }
}
