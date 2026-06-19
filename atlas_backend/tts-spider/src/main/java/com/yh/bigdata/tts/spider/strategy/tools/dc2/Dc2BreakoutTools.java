package com.yh.bigdata.tts.spider.strategy.tools.dc2;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.Dc2StrategyParams;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 死叉突破 · ref 死叉 K + 当前 K 突破 ref.high，前一根 K 在 ref.high 下
 */
public final class Dc2BreakoutTools {

    private static final double HIGH_EPS = 1e-6;

    private Dc2BreakoutTools() {
    }

    public enum Dc2TierKind {
        SHORT, LONG
    }

    @Getter
    public static final class TierHit {
        private final Dc2TierKind kind;
        private final Trade referenceBar;
        private final Trade signalBar;

        TierHit(Dc2TierKind kind, Trade referenceBar, Trade signalBar) {
            this.kind = kind;
            this.referenceBar = referenceBar;
            this.signalBar = signalBar;
        }
    }

    public static TierHit findShortHit(StockBase stock, Dc2StrategyParams p) {
        Dc2StrategyParams params = p != null ? p : Dc2StrategyParams.defaults();
        return findHit(stock, Dc2TierKind.SHORT, PeriodTypeEnum.DAY, params.getLookbackShort(), params);
    }

    public static TierHit findLongHit(StockBase stock, Dc2StrategyParams p) {
        Dc2StrategyParams params = p != null ? p : Dc2StrategyParams.defaults();
        return findHit(stock, Dc2TierKind.LONG, PeriodTypeEnum.WEEK, params.getLookbackLong(), params);
    }

    private static TierHit findHit(StockBase stock, Dc2TierKind kind, PeriodTypeEnum period,
                                   int lookback, Dc2StrategyParams params) {
        int fetchBars = lookback + 40;
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        if (CollectionUtils.isEmpty(trades) || trades.size() < 2) {
            return null;
        }

        Trade ref = Dc2StructureTools.findLatestDeathCrossBar(trades, lookback);
        if (ref == null || ref.getHigh() == null) {
            return null;
        }

        Trade current = trades.get(trades.size() - 1);
        Trade prev = trades.get(trades.size() - 2);

        if (current.getClose() == null || current.getClose() <= ref.getHigh() + HIGH_EPS) {
            return null;
        }
        if (prev.getClose() == null || prev.getClose() > ref.getHigh() + HIGH_EPS) {
            return null;
        }

        int refIdx = Dc2StructureTools.indexOfBar(trades, ref);
        int sigIdx = trades.size() - 1;
        int barsBetween = sigIdx - refIdx - 1;
        if (refIdx < 0 || barsBetween < params.getMinBarsAfterRef()) {
            return null;
        }
        if (sameBar(ref, current) || sameBar(ref, prev)) {
            return null;
        }

        if (!isCurrentCloseAboveRefHigh(stock, ref)) {
            return null;
        }

        return new TierHit(kind, ref, current);
    }

    private static boolean isCurrentCloseAboveRefHigh(StockBase stock, Trade refBar) {
        if (stock == null || refBar == null || refBar.getHigh() == null) {
            return false;
        }
        Double close = stock.getClose();
        return close != null && close > refBar.getHigh() + HIGH_EPS;
    }

    private static boolean sameBar(Trade a, Trade b) {
        return a != null && b != null && a.getDay() != null && a.getDay().equals(b.getDay());
    }
}
