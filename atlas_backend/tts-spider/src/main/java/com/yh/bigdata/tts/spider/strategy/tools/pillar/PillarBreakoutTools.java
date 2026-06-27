package com.yh.bigdata.tts.spider.strategy.tools.pillar;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.BreakoutBarTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 柱子内上移 · 强柱基准 K（任一 ref 命中，展示取最近一根）+ 当前 K 突破
 */
public final class PillarBreakoutTools {

    private static final double HIGH_EPS = 1e-6;

    private PillarBreakoutTools() {
    }

    @Getter
    public static final class PeriodHit {
        private final PeriodTypeEnum period;
        private final Trade referenceBar;
        private final Trade signalBar;

        PeriodHit(PeriodTypeEnum period, Trade referenceBar, Trade signalBar) {
            this.period = period;
            this.referenceBar = referenceBar;
            this.signalBar = signalBar;
        }
    }

    public static PeriodHit findHit(StockBase stock, PeriodTypeEnum period, int lookback) {
        if (stock == null || period == null || lookback < 1) {
            return null;
        }
        double refBodyPct = PillarThresholdTools.refBodyPct(period);
        double signalStrongPct = PillarThresholdTools.signalStrongPct(period);

        int fetchBars = lookback + 5;
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        if (CollectionUtils.isEmpty(trades) || trades.size() < 3) {
            return null;
        }

        int sigIdx = trades.size() - 1;
        Trade current = trades.get(sigIdx);
        Trade prev = trades.get(sigIdx - 1);
        Double currentClose = resolveCurrentClose(stock, current);
        if (currentClose == null || prev == null || prev.getHigh() == null || prev.getClose() == null) {
            return null;
        }

        int searchStart = Math.max(0, sigIdx - lookback);
        PeriodHit best = null;
        int bestRefIdx = -1;

        for (int refIdx = sigIdx - 2; refIdx >= searchStart; refIdx--) {
            Trade ref = trades.get(refIdx);
            if (!isRefBar(ref, refBodyPct) || ref.getHigh() == null || ref.getLow() == null) {
                continue;
            }
            double refHigh = ref.getHigh();
            if (prev.getClose() > refHigh + HIGH_EPS) {
                continue;
            }
            if (!intermediateClosesStrictlyBelow(trades, refIdx, sigIdx, refHigh)) {
                continue;
            }
            if (!matchesSignal(current, prev, ref, currentClose, signalStrongPct)) {
                continue;
            }
            if (refIdx > bestRefIdx) {
                bestRefIdx = refIdx;
                best = new PeriodHit(period, ref, current);
            }
        }
        return best;
    }

    static boolean isRefBar(Trade bar, double refBodyPct) {
        return BreakoutBarTools.isLargeYang(bar, refBodyPct);
    }

    static boolean matchesSignal(Trade current, Trade prev, Trade ref, double currentClose,
                                 double signalStrongPct) {
        if (!BreakoutBarTools.isStrongBar(current, signalStrongPct)) {
            return false;
        }
        double refLow = ref.getLow();
        double refHigh = ref.getHigh();
        if (currentClose <= refLow + HIGH_EPS) {
            return false;
        }
        if (currentClose <= prev.getHigh() + HIGH_EPS) {
            return false;
        }
        if (prev.getClose() > refHigh + HIGH_EPS) {
            return false;
        }
        return belowRefHigh(current, prev, refHigh);
    }

    static boolean belowRefHigh(Trade signalBar, Trade prevBar, double refHigh) {
        Double low = signalBar.getLow();
        Double prevClose = prevBar.getClose();
        boolean lowBelow = low != null && low < refHigh - HIGH_EPS;
        boolean prevBelow = prevClose != null && prevClose < refHigh - HIGH_EPS;
        return lowBelow || prevBelow;
    }

    private static boolean intermediateClosesStrictlyBelow(List<Trade> trades, int refIdx, int sigIdx,
                                                          double refHigh) {
        for (int i = refIdx + 1; i < sigIdx; i++) {
            Double close = trades.get(i).getClose();
            if (close == null || close >= refHigh - HIGH_EPS) {
                return false;
            }
        }
        return true;
    }

    private static Double resolveCurrentClose(StockBase stock, Trade currentBar) {
        if (currentBar != null && currentBar.getClose() != null) {
            return currentBar.getClose();
        }
        return stock != null ? stock.getClose() : null;
    }
}
