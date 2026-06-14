package com.yh.bigdata.tts.spider.strategy.tools.multi;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;

/**
 * 多周期强势 v2.0 · 周/月 MACD + 日 K 突破
 */
public final class MultiMacdBreakoutTools {

    private MultiMacdBreakoutTools() {
    }

    public static MacdBreakoutSnapshot evaluate(StockBase stock) {
        MacdBreakoutSnapshot snap = new MacdBreakoutSnapshot();
        snap.weekMacdPositive = UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.WEEK);
        snap.monthMacdPositive = UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.MONTH);
        snap.higherMacdOk = snap.weekMacdPositive && snap.monthMacdPositive;
        snap.dayBreakout = checkDayBreakout(stock);
        snap.hit = snap.higherMacdOk && snap.dayBreakout;
        return snap;
    }

    /**
     * 日 close &gt; max(前1日 open, 前1日 close)，且 日 high &gt; 前1日 high
     */
    static boolean checkDayBreakout(StockBase stock) {
        Trade current = RealtimeStockCache.getLastTrade(stock, PeriodTypeEnum.DAY, 0);
        Trade prev = RealtimeStockCache.getLastTrade(stock, PeriodTypeEnum.DAY, 1);
        if (current == null || prev == null
                || current.getClose() == null || current.getHigh() == null
                || prev.getOpen() == null || prev.getClose() == null || prev.getHigh() == null) {
            return false;
        }
        double prevBodyTop = Math.max(prev.getOpen(), prev.getClose());
        return current.getClose() > prevBodyTop && current.getHigh() > prev.getHigh();
    }

    public static final class MacdBreakoutSnapshot {
        public boolean weekMacdPositive;
        public boolean monthMacdPositive;
        public boolean higherMacdOk;
        public boolean dayBreakout;
        public boolean hit;
    }
}
