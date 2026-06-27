package com.yh.bigdata.tts.spider.strategy.tools.cascade;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.CascadeStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;

public final class CascadeFilterTools {

    private CascadeFilterTools() {
    }

    public static boolean passFilters(StockBase stock, CheckResult checkResult, CascadeStrategyParams params) {
        if (stock == null) {
            return false;
        }
        CascadeStrategyParams p = params != null ? params : CascadeStrategyParams.defaults();
        return MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount());
    }
}
