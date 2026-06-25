package com.yh.bigdata.tts.spider.strategy.tools.bogo;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 底部机会 · 基准 K（最近金叉/死叉，且非当前 K）+ 前 K 未突破、当前 K 突破 ref.high
 */
public final class BogoBreakoutTools {

    private static final double HIGH_EPS = 1e-6;

    private BogoBreakoutTools() {
    }

    @Getter
    public static final class PeriodHit {
        private final PeriodTypeEnum period;
        private final BogoStructureTools.CrossBar crossBar;
        private final Trade signalBar;

        PeriodHit(PeriodTypeEnum period, BogoStructureTools.CrossBar crossBar, Trade signalBar) {
            this.period = period;
            this.crossBar = crossBar;
            this.signalBar = signalBar;
        }

        public Trade getReferenceBar() {
            return crossBar != null ? crossBar.getBar() : null;
        }

        public BogoStructureTools.CrossKind getCrossKind() {
            return crossBar != null ? crossBar.getKind() : null;
        }
    }

    public static PeriodHit findHit(StockBase stock, PeriodTypeEnum period, int lookback) {
        if (stock == null || period == null || lookback < 1) {
            return null;
        }
        int fetchBars = lookback + 40;
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        if (CollectionUtils.isEmpty(trades) || trades.size() < 3) {
            return null;
        }

        BogoStructureTools.CrossBar cross = BogoStructureTools.findLatestCrossBar(trades, lookback);
        if (cross == null || cross.getBar() == null || cross.getBar().getHigh() == null) {
            return null;
        }

        Trade ref = cross.getBar();
        Trade prev = trades.get(trades.size() - 2);
        Trade current = trades.get(trades.size() - 1);

        if (sameBar(ref, current)) {
            return null;
        }

        double refHigh = ref.getHigh();
        Double prevClose = prev.getClose();
        Double currentClose = resolveCurrentClose(stock, current);
        if (prevClose == null || currentClose == null) {
            return null;
        }

        // 前 K 未突破基准 high
        if (prevClose > refHigh + HIGH_EPS) {
            return null;
        }
        // 当前 K 突破基准 high
        if (currentClose <= refHigh + HIGH_EPS) {
            return null;
        }

        return new PeriodHit(period, cross, current);
    }

    /** 当前 K 收盘；未收完时可用现价（与最后一根 K 同源） */
    private static Double resolveCurrentClose(StockBase stock, Trade currentBar) {
        if (currentBar != null && currentBar.getClose() != null) {
            return currentBar.getClose();
        }
        return stock != null ? stock.getClose() : null;
    }

    private static boolean sameBar(Trade a, Trade b) {
        return a != null && b != null && a.getDay() != null && a.getDay().equals(b.getDay());
    }
}
