package com.yh.bigdata.tts.spider.strategy.tools.macedge;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MacdEdgeStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;

public final class MacdEdgeFilterTools {

    private MacdEdgeFilterTools() {
    }

    public static boolean passFilters(StockBase stock, CheckResult checkResult, MacdEdgeStrategyParams params) {
        if (stock == null) {
            return false;
        }
        MacdEdgeStrategyParams p = params != null ? params : MacdEdgeStrategyParams.defaults();
        return MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount());
    }
}
