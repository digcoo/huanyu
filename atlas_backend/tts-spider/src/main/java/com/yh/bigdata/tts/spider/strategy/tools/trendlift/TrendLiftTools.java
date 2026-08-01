package com.yh.bigdata.tts.spider.strategy.tools.trendlift;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.TrendLiftStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;
import com.yh.bigdata.tts.spider.strategy.tools.macd.MacdPositiveGateTools;
import lombok.Getter;

/**
 * 趋势上移：日/周/月 MACD 全 &gt;0 + 日 close &gt; 前一日 low。
 */
public final class TrendLiftTools {

    private static final double EPS = 1e-6;

    private TrendLiftTools() {
    }

    @Getter
    public static final class Hit {
        private final Trade signalBar;
        private final Trade prevBar;
        private final double prevLow;

        Hit(Trade signalBar, Trade prevBar, double prevLow) {
            this.signalBar = signalBar;
            this.prevBar = prevBar;
            this.prevLow = prevLow;
        }
    }

    public static Hit findHit(StockBase stock) {
        if (stock == null) {
            return null;
        }
        Trade signalBar = RealtimeStockCache.getLastTrade(stock, PeriodTypeEnum.DAY, 0);
        Trade prevBar = RealtimeStockCache.getLastTrade(stock, PeriodTypeEnum.DAY, 1);
        if (!passesDayCloseAbovePrevLow(signalBar, prevBar)) {
            return null;
        }
        return new Hit(signalBar, prevBar, prevBar.getLow());
    }

    static boolean passesDayCloseAbovePrevLow(Trade signalBar, Trade prevBar) {
        if (signalBar == null || prevBar == null || signalBar.getClose() == null || prevBar.getLow() == null) {
            return false;
        }
        return signalBar.getClose() > prevBar.getLow() + EPS;
    }

    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,
                                              TrendLiftStrategyParams params) {
        TrendLiftStrategyParams p = params != null ? params : TrendLiftStrategyParams.defaults();
        if (p.isEnableMinAmountFilter()
                && !MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount())) {
            appendMessage(checkResult, "成交额不足");
            return false;
        }
        return true;
    }

    public static String buildTrendMessage(Hit hit) {
        if (hit == null) {
            return "[TLIFT]趋势上移";
        }
        return String.format("[TLIFT]趋势上移|日/周/月MACD>0,日close>前日low|prevLow=%.2f",
                hit.getPrevLow());
    }

    public static String buildSignalMessage(Hit hit) {
        if (hit == null) {
            return "";
        }
        Trade signalBar = hit.getSignalBar();
        Trade prevBar = hit.getPrevBar();
        return String.format(
                "趋势上移,strategyTag=TLIFT,period=day,sigDay=%s,sigClose=%.2f,prevDay=%s,prevLow=%.2f",
                dayOf(signalBar),
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,
                dayOf(prevBar),
                hit.getPrevLow());
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }

    private static void appendMessage(CheckResult checkResult, String msg) {
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.DAY, "[TLIFT]" + msg);
        }
    }
}
