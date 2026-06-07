package com.yh.bigdata.tts.common.utils;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;

/**
 * 用日 K 最新一根覆盖 base 表可能过期的 close / prev_close（首页现价、涨跌幅）
 */
public final class StockQuoteUtils {

    private StockQuoteUtils() {
    }

    public static void overlayLatestDayQuote(StockBase stock) {
        if (stock == null || stock.getCode() == null) {
            return;
        }
        Trade latest = RealtimeStockCache.getLastTrade(stock, PeriodTypeEnum.DAY, 0);
        if (latest == null || latest.getClose() == null || latest.getClose() <= 0) {
            return;
        }
        stock.setClose(latest.getClose());
        if (latest.getPrevClose() != null && latest.getPrevClose() > 0) {
            stock.setPrevClose(latest.getPrevClose());
        }
        if (latest.getDay() != null) {
            stock.setDay(latest.getDay());
        }
    }
}
