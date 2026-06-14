package com.yh.bigdata.tts.spider.strategy.tools.pregolden;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;

/**
 * 预判金叉 · K 线突破前一根 High / 实体上沿
 */
public final class PreGoldenBreakoutTools {

    private PreGoldenBreakoutTools() {
    }

    /**
     * high &gt; 前1根 high，且 close &gt; max(前1根 open, 前1根 close)
     */
    public static boolean checkBreakout(StockBase stock, PeriodTypeEnum period) {
        Trade current = RealtimeStockCache.getLastTrade(stock, period, 0);
        Trade prev = RealtimeStockCache.getLastTrade(stock, period, 1);
        if (current == null || prev == null
                || current.getClose() == null || current.getHigh() == null
                || prev.getOpen() == null || prev.getClose() == null || prev.getHigh() == null) {
            return false;
        }
        double prevBodyTop = Math.max(prev.getOpen(), prev.getClose());
        return current.getHigh() > prev.getHigh() && current.getClose() > prevBodyTop;
    }
}
