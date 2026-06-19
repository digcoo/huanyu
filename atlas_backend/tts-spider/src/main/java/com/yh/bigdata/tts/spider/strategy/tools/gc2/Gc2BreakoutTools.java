package com.yh.bigdata.tts.spider.strategy.tools.gc2;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.Gc2StrategyParams;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 金叉二次突破 v2.1 · ref 金叉 K + 当前 K 突破 ref.high；
 * ref 与 signal 之间每根 K 的 close &lt; ref.high
 */
public final class Gc2BreakoutTools {

    private static final double HIGH_EPS = 1e-6;

    private Gc2BreakoutTools() {
    }

    public enum Gc2TierKind {
        SHORT, LONG
    }

    @Getter
    public static final class TierHit {
        private final Gc2TierKind kind;
        private final Trade referenceBar;
        private final Trade signalBar;

        TierHit(Gc2TierKind kind, Trade referenceBar, Trade signalBar) {
            this.kind = kind;
            this.referenceBar = referenceBar;
            this.signalBar = signalBar;
        }
    }

    public static TierHit findShortHit(StockBase stock, Gc2StrategyParams p) {
        Gc2StrategyParams params = p != null ? p : Gc2StrategyParams.defaults();
        return findHit(stock, Gc2TierKind.SHORT, PeriodTypeEnum.DAY, params.getLookbackShort(), params);
    }

    public static TierHit findLongHit(StockBase stock, Gc2StrategyParams p) {
        Gc2StrategyParams params = p != null ? p : Gc2StrategyParams.defaults();
        return findHit(stock, Gc2TierKind.LONG, PeriodTypeEnum.WEEK, params.getLookbackLong(), params);
    }

    private static TierHit findHit(StockBase stock, Gc2TierKind kind, PeriodTypeEnum period,
                                   int lookback, Gc2StrategyParams params) {
        int fetchBars = lookback + 40;
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        if (CollectionUtils.isEmpty(trades) || trades.size() < 2) {
            return null;
        }

        Trade ref = Gc2StructureTools.findLatestGoldenCrossBar(trades, lookback);
        if (ref == null || ref.getHigh() == null) {
            return null;
        }

        Trade current = trades.get(trades.size() - 1);

        if (current.getClose() == null || current.getClose() <= ref.getHigh() + HIGH_EPS) {
            return null;
        }

        int refIdx = Gc2StructureTools.indexOfBar(trades, ref);
        int sigIdx = trades.size() - 1;
        int barsBetween = sigIdx - refIdx - 1;
        if (refIdx < 0 || barsBetween < params.getMinBarsAfterRef()) {
            return null;
        }
        if (sameBar(ref, current)) {
            return null;
        }
        if (!allIntermediateClosesBelowRefHigh(trades, refIdx, sigIdx, ref)) {
            return null;
        }

        if (!isCurrentCloseAboveRefHigh(stock, ref)) {
            return null;
        }

        return new TierHit(kind, ref, current);
    }

    private static boolean allIntermediateClosesBelowRefHigh(List<Trade> trades, int refIdx, int sigIdx,
                                                             Trade ref) {
        if (ref == null || ref.getHigh() == null || refIdx < 0 || sigIdx <= refIdx + 1) {
            return false;
        }
        double refHigh = ref.getHigh();
        for (int i = refIdx + 1; i < sigIdx; i++) {
            Double close = trades.get(i).getClose();
            if (close == null || close >= refHigh) {
                return false;
            }
        }
        return true;
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
