package com.yh.bigdata.tts.spider.strategy.tools.cascadewave;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 级联 MACD 波段同档突破：凸波段突破次波段顶，凹波段突破末波段顶。
 */
public final class CascadeWaveBreakoutTools {

    private static final double LINE_EPS = 1e-6;

    private CascadeWaveBreakoutTools() {
    }

    public enum BreakPath {
        LAST_BAND_HIGH,
        PREV_BAND_HIGH,
        LAST_YANG_LOW
    }

    @Getter
    public static final class TierHit {
        private final PeriodTypeEnum signalTier;
        private final WaveShapeTools.BandShape bandShape;
        private final YangBandTools.CompleteYangBand band;
        private final YangBandTools.CompleteYangBand prevBand;
        private final Trade signalBar;
        private final Trade prevBar;
        private final BreakPath breakPath;
        private final double breakLine;

        TierHit(PeriodTypeEnum signalTier, WaveShapeTools.BandShape bandShape,
                YangBandTools.CompleteYangBand band, YangBandTools.CompleteYangBand prevBand,
                Trade signalBar, Trade prevBar, BreakPath breakPath, double breakLine) {
            this.signalTier = signalTier;
            this.bandShape = bandShape;
            this.band = band;
            this.prevBand = prevBand;
            this.signalBar = signalBar;
            this.prevBar = prevBar;
            this.breakPath = breakPath;
            this.breakLine = breakLine;
        }

        public Trade getFirstYangBar() {
            return band != null ? band.getFirstYang() : null;
        }

        public Trade getLastYangBar() {
            return band != null ? band.getLastYang() : null;
        }
    }

    public static TierHit findTierHit(StockBase stock, PeriodTypeEnum period, int lookback,
                                      WaveShapeTools.BandShape requiredShape,
                                      boolean enablePrevBandBreak) {
        return findTierHit(stock, period, lookback, requiredShape, enablePrevBandBreak, true);
    }

    public static TierHit findTierHit(StockBase stock, PeriodTypeEnum period, int lookback,
                                      WaveShapeTools.BandShape requiredShape,
                                      boolean enablePrevBandBreak, boolean requireCascadeMacd) {
        if (stock == null || period == null || lookback < 3 || requiredShape == null) {
            return null;
        }
        if (requireCascadeMacd && !CascadeWaveMacdTools.passesCascade(stock, period)) {
            return null;
        }
        int fetchBars = Math.max(lookback + 2, lookback);
        List<Trade> periodBars = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(periodBars, lookback);
        if (bands.size() < 2) {
            return null;
        }
        YangBandTools.CompleteYangBand lastBand = bands.get(bands.size() - 1);
        YangBandTools.CompleteYangBand prevBand = bands.get(bands.size() - 2);
        if (!matchesBandShape(requiredShape, lastBand, prevBand)) {
            return null;
        }
        PeriodEdge edge = resolvePeriodEdge(stock, period);
        if (edge == null) {
            return null;
        }
        return evaluateBreakout(period, requiredShape, lastBand, prevBand,
                edge.prevBar, edge.signalBar, edge.prevClose, edge.signalClose);
    }

    /** 统一日 K 边沿突破（信号档波段形态 + 级联 MACD 门不变） */
    public static TierHit findDayEdgeTierHit(StockBase stock, PeriodTypeEnum period, int lookback,
                                             WaveShapeTools.BandShape requiredShape,
                                             boolean enablePrevBandBreak) {
        if (stock == null || period == null || lookback < 3 || requiredShape == null) {
            return null;
        }
        if (!CascadeWaveMacdTools.passesCascade(stock, period)) {
            return null;
        }
        int fetchBars = Math.max(lookback + 2, lookback);
        List<Trade> periodBars = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(periodBars, lookback);
        if (bands.size() < 2) {
            return null;
        }
        YangBandTools.CompleteYangBand lastBand = bands.get(bands.size() - 1);
        YangBandTools.CompleteYangBand prevBand = bands.get(bands.size() - 2);
        if (!matchesBandShape(requiredShape, lastBand, prevBand)) {
            return null;
        }
        DayEdge dayEdge = resolveDayEdge(stock);
        if (dayEdge == null) {
            return null;
        }
        return evaluateBreakout(period, requiredShape, lastBand, prevBand,
                dayEdge.prevBar, dayEdge.signalBar, dayEdge.prevClose, dayEdge.signalClose);
    }

    private static boolean matchesBandShape(WaveShapeTools.BandShape requiredShape,
                                            YangBandTools.CompleteYangBand lastBand,
                                            YangBandTools.CompleteYangBand prevBand) {
        if (lastBand == null || prevBand == null) {
            return false;
        }
        boolean convex = WaveShapeTools.isConvex(lastBand, prevBand);
        if (requiredShape == WaveShapeTools.BandShape.CONVEX) {
            return convex && prevBand.getLastYang() != null;
        }
        if (requiredShape == WaveShapeTools.BandShape.CONCAVE) {
            return !convex && lastBand.getLastYang() != null;
        }
        return false;
    }

    private static TierHit evaluateBreakout(PeriodTypeEnum period, WaveShapeTools.BandShape requiredShape,
                                            YangBandTools.CompleteYangBand lastBand,
                                            YangBandTools.CompleteYangBand prevBand,
                                            Trade prevBar, Trade signalBar,
                                            double prevClose, double signalClose) {
        BreakTarget target = resolveBreakTarget(requiredShape, lastBand, prevBand);
        if (target == null) {
            return null;
        }
        if (sameBar(target.refYangBar, signalBar)) {
            return null;
        }
        if (!passesEdge(prevClose, signalClose, target.breakLine)) {
            return null;
        }
        return new TierHit(period, requiredShape, lastBand, prevBand,
                signalBar, prevBar, target.breakPath, target.breakLine);
    }

    private static BreakTarget resolveBreakTarget(WaveShapeTools.BandShape requiredShape,
                                                  YangBandTools.CompleteYangBand lastBand,
                                                  YangBandTools.CompleteYangBand prevBand) {
        if (requiredShape == WaveShapeTools.BandShape.CONVEX) {
            if (prevBand == null || Double.isNaN(prevBand.getBandHigh())) {
                return null;
            }
            Trade ref = prevBand.getBandHighBar() != null ? prevBand.getBandHighBar() : prevBand.getLastYang();
            return new BreakTarget(BreakPath.PREV_BAND_HIGH, prevBand.getBandHigh(), ref);
        }
        if (requiredShape == WaveShapeTools.BandShape.CONCAVE) {
            if (lastBand == null || Double.isNaN(lastBand.getBandHigh())) {
                return null;
            }
            Trade ref = lastBand.getBandHighBar() != null ? lastBand.getBandHighBar() : lastBand.getLastYang();
            return new BreakTarget(BreakPath.LAST_BAND_HIGH, lastBand.getBandHigh(), ref);
        }
        return null;
    }

    private static boolean passesEdge(double prevClose, double signalClose, double line) {
        if (Double.isNaN(line)) {
            return false;
        }
        return prevClose <= line + LINE_EPS && signalClose > line + LINE_EPS;
    }

    private static DayEdge resolveDayEdge(StockBase stock) {
        List<Trade> dayBars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.DAY, 2);
        if (CollectionUtils.isEmpty(dayBars) || dayBars.size() < 2) {
            return null;
        }
        Trade prev = dayBars.get(dayBars.size() - 2);
        Trade today = dayBars.get(dayBars.size() - 1);
        Double prevClose = prev != null ? prev.getClose() : null;
        Double todayClose = resolveTodayClose(stock, today);
        if (prevClose == null || todayClose == null) {
            return null;
        }
        return new DayEdge(prev, today, prevClose, todayClose);
    }

    private static Double resolveTodayClose(StockBase stock, Trade todayBar) {
        if (todayBar != null && todayBar.getClose() != null) {
            return todayBar.getClose();
        }
        return stock != null ? stock.getClose() : null;
    }

    private static PeriodEdge resolvePeriodEdge(StockBase stock, PeriodTypeEnum period) {
        List<Trade> bars = RealtimeStockCache.getLastTrades(stock, period, 2);
        if (CollectionUtils.isEmpty(bars) || bars.size() < 2) {
            return null;
        }
        Trade prev = bars.get(bars.size() - 2);
        Trade signal = bars.get(bars.size() - 1);
        Double prevClose = prev != null ? prev.getClose() : null;
        Double signalClose = resolveSignalClose(stock, period, signal);
        if (prevClose == null || signalClose == null) {
            return null;
        }
        return new PeriodEdge(prev, signal, prevClose, signalClose);
    }

    private static Double resolveSignalClose(StockBase stock, PeriodTypeEnum period, Trade signalBar) {
        if (signalBar != null && signalBar.getClose() != null) {
            return signalBar.getClose();
        }
        if (period == PeriodTypeEnum.DAY && stock != null) {
            return stock.getClose();
        }
        return null;
    }

    private static boolean sameBar(Trade a, Trade b) {
        return a != null && b != null && a.getDay() != null && a.getDay().equals(b.getDay());
    }

    private static final class BreakTarget {
        private final BreakPath breakPath;
        private final double breakLine;
        private final Trade refYangBar;

        BreakTarget(BreakPath breakPath, double breakLine, Trade refYangBar) {
            this.breakPath = breakPath;
            this.breakLine = breakLine;
            this.refYangBar = refYangBar;
        }
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

    private static final class DayEdge {
        private final Trade prevBar;
        private final Trade signalBar;
        private final double prevClose;
        private final double signalClose;

        DayEdge(Trade prevBar, Trade signalBar, double prevClose, double signalClose) {
            this.prevBar = prevBar;
            this.signalBar = signalBar;
            this.prevClose = prevClose;
            this.signalClose = signalClose;
        }
    }
}
