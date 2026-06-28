package com.yh.bigdata.tts.spider.strategy.tools.ldip;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.LadderDipStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;

public final class LadderDipFilterTools {

    private LadderDipFilterTools() {
    }

    public static boolean passFilters(StockBase stock, CheckResult checkResult, LadderDipStrategyParams params) {
        if (stock == null) {
            return false;
        }
        LadderDipStrategyParams p = params != null ? params : LadderDipStrategyParams.defaults();
        return MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount());
    }
}
