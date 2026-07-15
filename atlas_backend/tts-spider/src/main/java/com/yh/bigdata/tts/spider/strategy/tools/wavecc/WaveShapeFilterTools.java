package com.yh.bigdata.tts.spider.strategy.tools.wavecc;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;

public final class WaveShapeFilterTools {

    private WaveShapeFilterTools() {
    }

    public static boolean passFilters(StockBase stock, CheckResult checkResult, double minAvgAmount) {
        if (stock == null) {
            return false;
        }
        return MinAvgAmountFilterTools.passWithMessage(stock, checkResult, minAvgAmount);
    }
}
