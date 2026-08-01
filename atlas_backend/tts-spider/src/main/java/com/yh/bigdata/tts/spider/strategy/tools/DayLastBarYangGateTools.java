package com.yh.bigdata.tts.spider.strategy.tools;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.response.CheckResult;

/**
 * 任意信号档位均要求日线末 K 收阳（close ≥ open）。
 */
public final class DayLastBarYangGateTools {

    private static final double EPS = 1e-6;

    private DayLastBarYangGateTools() {
    }

    public static boolean isDayLastBarYang(Trade dayBar) {
        if (dayBar == null || dayBar.getOpen() == null || dayBar.getClose() == null) {
            return false;
        }
        return dayBar.getClose() >= dayBar.getOpen() - EPS;
    }

    public static boolean passes(StockBase stock) {
        if (stock == null) {
            return false;
        }
        Trade dayBar = RealtimeStockCache.getLastTrade(stock, PeriodTypeEnum.DAY, 0);
        return isDayLastBarYang(dayBar);
    }

    public static boolean passWithMessage(StockBase stock, CheckResult checkResult) {
        if (passes(stock)) {
            return true;
        }
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.DAY, "[DAY]日K末K非阳(close<open)");
        }
        return false;
    }
}
