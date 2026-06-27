package com.yh.bigdata.tts.spider.strategy.tools.pillar;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.PillarStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;

/**
 * 柱子内上移 · 可选成交额过滤
 */
public final class PillarFilterTools {

    private PillarFilterTools() {
    }

    public static boolean passFilters(StockBase stock, CheckResult checkResult, PillarStrategyParams params) {
        if (stock == null) {
            return false;
        }
        PillarStrategyParams p = params != null ? params : PillarStrategyParams.defaults();
        return MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount());
    }
}
