package com.yh.bigdata.tts.spider.strategy.tools.wavecc;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 凸/凹波段 · 末两波段形态 + 同档/日 K 边沿突破
 */
public final class WaveShapeTools {

    private static final double LINE_EPS = 1e-6;

    private WaveShapeTools() {
    }

    public enum BandShape {
        CONVEX,
        CONCAVE
    }

    public enum BreakPath {
        LAST_MEDIAN,
        LAST_HIGH,
        LAST_LOW
    }

    public enum EdgeMode {
        /** 同档 K 边沿 */
        TIER,
        /** 统一日 K 边沿 */
        DAY
    }

    @Getter
    public static final class BreakLineConfig {
        private final boolean enableLastHighBreak;
        private final boolean enableLastMedianBreak;
        private final boolean enableLastLowBreak;

        public BreakLineConfig(boolean enableLastHighBreak, boolean enableLastMedianBreak,
                               boolean enableLastLowBreak) {
            this.enableLastHighBreak = enableLastHighBreak;
            this.enableLastMedianBreak = enableLastMedianBreak;
            this.enableLastLowBreak = enableLastLowBreak;
        }

        public static BreakLineConfig of(boolean enableLastHighBreak, boolean enableLastMedianBreak,
                                         boolean enableLastLowBreak) {
            return new BreakLineConfig(enableLastHighBreak, enableLastMedianBreak, enableLastLowBreak);
        }
    }

    @Getter
    public static final class TierHit {
        private final PeriodTypeEnum signalTier;
        private final BandShape bandShape;
        private final YangBandTools.CompleteYangBand band;
        private final YangBandTools.CompleteYangBand prevBand;
        private final Trade signalBar;
        private final Trade prevBar;
        private final BreakPath breakPath;
        private final double breakLine;

        TierHit(PeriodTypeEnum signalTier, BandShape bandShape,
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
                                      BandShape requiredShape, BreakLineConfig breakConfig) {
        return findTierHit(stock, period, lookback, requiredShape, breakConfig, EdgeMode.TIER);
    }

    public static TierHit findTierHit(StockBase stock, PeriodTypeEnum period, int lookback,
                                      BandShape requiredShape, BreakLineConfig breakConfig,
                                      EdgeMode edgeMode) {
        if (stock == null || period == null || lookback < 3 || requiredShape == null) {
            return null;
        }
        BreakLineConfig cfg = breakConfig != null ? breakConfig : BreakLineConfig.of(false, false, false);
        EdgeMode mode = edgeMode != null ? edgeMode : EdgeMode.TIER;
        int fetchBars = Math.max(lookback + 2, lookback);
        List<Trade> periodBars = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(periodBars, lookback);
        if (bands.size() < 2) {
            return null;
        }
        YangBandTools.CompleteYangBand lastBand = bands.get(bands.size() - 1);
        YangBandTools.CompleteYangBand prevBand = bands.get(bands.size() - 2);
        if (lastBand == null || prevBand == null || lastBand.getLastYang() == null) {
            return null;
        }
        boolean convex = isConvex(lastBand, prevBand);
        if (requiredShape == BandShape.CONVEX && !convex) {
            return null;
        }
        if (requiredShape == BandShape.CONCAVE && convex) {
            return null;
        }
        BreakPath breakPath = resolveBreakPath(requiredShape, cfg);
        double breakLine = resolveBreakLine(lastBand, requiredShape, cfg);
        if (Double.isNaN(breakLine)) {
            return null;
        }
        if (mode == EdgeMode.DAY) {
            DayEdge dayEdge = resolveDayEdge(stock);
            if (dayEdge == null) {
                return null;
            }
            if (sameBar(lastBand.getLastYang(), dayEdge.signalBar)) {
                return null;
            }
            if (!passesEdge(dayEdge.prevClose, dayEdge.signalClose, breakLine)) {
                return null;
            }
            return new TierHit(period, requiredShape, lastBand, prevBand,
                    dayEdge.signalBar, dayEdge.prevBar, breakPath, breakLine);
        }
        PeriodEdge edge = resolvePeriodEdge(stock, period);
        if (edge == null) {
            return null;
        }
        if (sameBar(lastBand.getLastYang(), edge.signalBar)) {
            return null;
        }
        if (!passesEdge(edge.prevClose, edge.signalClose, breakLine)) {
            return null;
        }
        return new TierHit(period, requiredShape, lastBand, prevBand,
                edge.signalBar, edge.prevBar, breakPath, breakLine);
    }

    public static boolean isConvex(YangBandTools.CompleteYangBand lastBand,
                                   YangBandTools.CompleteYangBand prevBand) {
        if (lastBand == null || prevBand == null) {
            return false;
        }
        double lastHigh = lastBand.getBandHigh();
        double prevHigh = prevBand.getBandHigh();
        if (Double.isNaN(lastHigh) || Double.isNaN(prevHigh)) {
            return false;
        }
        return lastHigh > prevHigh + LINE_EPS;
    }

    static BreakPath resolveBreakPath(BandShape shape, BreakLineConfig config) {
        if (config.isEnableLastLowBreak()) {
            return BreakPath.LAST_LOW;
        }
        if (config.isEnableLastMedianBreak()) {
            return BreakPath.LAST_MEDIAN;
        }
        if (config.isEnableLastHighBreak()) {
            return BreakPath.LAST_HIGH;
        }
        return shape == BandShape.CONVEX ? BreakPath.LAST_MEDIAN : BreakPath.LAST_HIGH;
    }

    static double resolveBreakLine(YangBandTools.CompleteYangBand band, BandShape shape,
                                   BreakLineConfig config) {
        if (band == null || band.getLastYang() == null) {
            return Double.NaN;
        }
        BreakPath path = resolveBreakPath(shape, config);
        if (path == BreakPath.LAST_LOW) {
            return band.getLastLow();
        }
        Trade lastYang = band.getLastYang();
        if (path == BreakPath.LAST_MEDIAN) {
            return band.getLastMedian();
        }
        return band.getBandHigh();
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
