package com.yh.bigdata.tts.spider.strategy.tools.cascade;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.CascadeStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.AllYangGateTools;
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.BarHighLadderGateTools;
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.DualLowSupportGateTools;
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.FrictionlessMacdGateTools;
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.MacdDcHighGateTools;
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.MacdCrossLowGateTools;

/**
 * 级联交叉突破 · 可选六门
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
        if (p.isEnableMacdDcHighGate()
                && !MacdDcHighGateTools.passesAll(stock, checkResult)) {
            return false;
        }
        if (p.isEnableCrossLowGate()
                && !MacdCrossLowGateTools.passesAll(stock, checkResult)) {
            return false;
        }
        if (p.isEnableBarHighGate()
                && !BarHighLadderGateTools.passesAll(stock, checkResult)) {
            return false;
        }
        if (p.isEnableAllYangGate()
                && !AllYangGateTools.passesAll(stock, checkResult)) {
            return false;
        }
        return true;
    }
}
