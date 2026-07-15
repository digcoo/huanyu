package com.yh.bigdata.tts.spider.strategy.tools.macdcrosstier;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.macedge.MacdEdgeBreakoutTools;

/**
 * 三档共用门：本档末 K 的 close &gt; 前 K 的 high。
 */
public final class MacdCrossTierPrevHighGateTools {

    private static final double EPS = 1e-6;

    private MacdCrossTierPrevHighGateTools() {
    }

    public static boolean passesTierCloseAbovePrevHigh(StockBase stock, CheckResult checkResult,
                                                       MacdEdgeBreakoutTools.TierHit hit) {
        if (hit == null || hit.getSignalBar() == null || hit.getPrevBar() == null) {
            return false;
        }
        return passesCloseAbovePrevHigh(stock, checkResult, hit.getSignalTier(),
                hit.getPrevBar(), hit.getSignalBar());
    }

    static boolean passesCloseAbovePrevHigh(StockBase stock, CheckResult checkResult, PeriodTypeEnum period,
                                            Trade prevBar, Trade signalBar) {
        Double close = resolveClose(stock, period, signalBar);
        Double prevHigh = prevBar != null ? prevBar.getHigh() : null;
        if (close == null || prevHigh == null || close <= prevHigh + EPS) {
            appendMessage(checkResult, period, periodLabel(period) + "未破前K高");
            return false;
        }
        appendMessage(checkResult, period, periodLabel(period) + "破前K高");
        return true;
    }

    private static Double resolveClose(StockBase stock, PeriodTypeEnum period, Trade signalBar) {
        if (signalBar != null && signalBar.getClose() != null) {
            return signalBar.getClose();
        }
        if (period == PeriodTypeEnum.DAY && stock != null && stock.getClose() != null) {
            return stock.getClose();
        }
        return null;
    }

    private static void appendMessage(CheckResult checkResult, PeriodTypeEnum period, String msg) {
        if (checkResult != null && period != null) {
            checkResult.addTrendPeriod(period, "[MCT]" + msg);
        }
    }

    private static String periodLabel(PeriodTypeEnum period) {
        if (period == PeriodTypeEnum.MONTH) {
            return "月";
        }
        if (period == PeriodTypeEnum.WEEK) {
            return "周";
        }
        return "日";
    }
}
