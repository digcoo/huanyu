package com.yh.bigdata.tts.spider.strategy.tools.ldip;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.LadderDipStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.BarHighLadderGateTools;
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.DualLowSupportGateTools;
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.FrictionlessMacdGateTools;
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.MacdCrossLowGateTools;

public final class LadderDipOptionalGateTools {

    private LadderDipOptionalGateTools() {
    }

    public static boolean passGate(StockBase stock, CheckResult checkResult, LadderDipStrategyParams params) {
        LadderDipStrategyParams p = params != null ? params : LadderDipStrategyParams.defaults();
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
        if (p.isEnableBarHighGate()
                && !BarHighLadderGateTools.passesAll(stock, checkResult)) {
            return false;
        }
        return true;
    }
}
