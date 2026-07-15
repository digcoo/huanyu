package com.yh.bigdata.tts.spider.strategy.tools.macedge;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.strategy.tools.macd.MacdCrossBarResolver;
import com.yh.bigdata.tts.spider.strategy.tools.macd.MacdCrossStructureTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * MACD 交叉边沿突破 · 各档 MACD 交叉基准 + 同档 K 边沿破基准 high
 */
public final class MacdEdgeBreakoutTools {

    private static final double HIGH_EPS = 1e-6;

    private MacdEdgeBreakoutTools() {
    }

    @Getter
    public static final class TierHit {
        private final PeriodTypeEnum signalTier;
        private final MacdCrossStructureTools.CrossBar crossBar;
        private final Trade signalBar;
        private final Trade prevBar;

        TierHit(PeriodTypeEnum signalTier, MacdCrossStructureTools.CrossBar crossBar,
                Trade signalBar, Trade prevBar) {
            this.signalTier = signalTier;
            this.crossBar = crossBar;
            this.signalBar = signalBar;
            this.prevBar = prevBar;
        }

        public Trade getReferenceBar() {
            return crossBar != null ? crossBar.getBar() : null;
        }

        public MacdCrossStructureTools.CrossKind getCrossKind() {
            return crossBar != null ? crossBar.getKind() : null;
        }
    }

    public static TierHit findTierHit(StockBase stock, PeriodTypeEnum refPeriod, int refLookback) {
        return findTierHit(stock, refPeriod, refLookback, true, true);
    }

    public static TierHit findTierHit(StockBase stock, PeriodTypeEnum refPeriod, int refLookback,
                                      boolean enableGoldenCross, boolean enableDeathCross) {
        if (stock == null || refPeriod == null || refLookback < 1) {
            return null;
        }
        MacdCrossStructureTools.CrossBar cross = MacdCrossBarResolver.findLatestCrossBar(
                stock, refPeriod, refLookback, enableGoldenCross, enableDeathCross);
        if (cross == null || cross.getBar() == null || cross.getBar().getHigh() == null) {
            return null;
        }
        Trade ref = cross.getBar();
        PeriodEdge edge = resolvePeriodEdge(stock, refPeriod);
        if (edge == null || sameBar(ref, edge.signalBar)) {
            return null;
        }
        if (!passesRefHighEdge(edge, ref)) {
            return null;
        }
        return new TierHit(refPeriod, cross, edge.signalBar, edge.prevBar);
    }

    private static boolean passesRefHighEdge(PeriodEdge edge, Trade ref) {
        if (edge == null || ref == null || ref.getHigh() == null) {
            return false;
        }
        double refHigh = ref.getHigh();
        return edge.prevClose <= refHigh + HIGH_EPS && edge.signalClose > refHigh + HIGH_EPS;
    }

    private static PeriodEdge resolvePeriodEdge(StockBase stock, PeriodTypeEnum period) {
        List<Trade> bars = RealtimeStockCache.getLastTrades(stock, period, 2);
        if (CollectionUtils.isEmpty(bars) || bars.size() < 2) {
            return null;
        }
        Trade prev = bars.get(bars.size() - 2);
        Trade signal = bars.get(bars.size() - 1);
        Double prevClose = prev != null ? prev.getClose() : null;
        Double signalClose = resolveSignalClose(stock, signal);
        if (prevClose == null || signalClose == null) {
            return null;
        }
        return new PeriodEdge(prev, signal, prevClose, signalClose);
    }

    private static Double resolveSignalClose(StockBase stock, Trade signalBar) {
        if (signalBar != null && signalBar.getClose() != null) {
            return signalBar.getClose();
        }
        return stock != null ? stock.getClose() : null;
    }

    private static boolean sameBar(Trade a, Trade b) {
        return a != null && b != null && a.getDay() != null && a.getDay().equals(b.getDay());
    }

    private static final class PeriodEdge {
        private final Trade prevBar;
        private final Trade signalBar;
        private final double prevClose;
        private final double signalClose;

        PeriodEdge(Trade prevBar, Trade signalBar, double prevClose, double signalClose) {
            this.prevBar = prevBar;
            this.signalBar = signalBar;
            this.prevClose = prevClose;
            this.signalClose = signalClose;
        }
    }
}
