package com.yh.bigdata.tts.spider.strategy.tools.cladder;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.CascadeLadderStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;

public final class CascadeLadderFilterTools {

    private CascadeLadderFilterTools() {
    }

    public static boolean passFilters(StockBase stock, CheckResult checkResult, CascadeLadderStrategyParams params) {
        if (stock == null) {
            return false;
        }
        CascadeLadderStrategyParams p = params != null ? params : CascadeLadderStrategyParams.defaults();
        return MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount());
    }
}
