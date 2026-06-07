package com.yh.bigdata.tts.spider.strategy.tools;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralPivotTools;

/**
 * @deprecated 使用 {@link UnilateralPivotTools}
 */
@Deprecated
public final class UnilateralTrendSignalTools {

    private UnilateralTrendSignalTools() {
    }

    public static boolean checkMonthUnilateralSignal(StockBase stock, CheckResult checkResult) {
        return UnilateralPivotTools.checkMonthPivot(stock, checkResult);
    }
}
