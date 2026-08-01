package com.yh.bigdata.tts.spider.strategy.tools.wavecc;

import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.strategy.tools.bodybar.BodyBarTierTools;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.BreakoutBucketTools;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.BreakoutScanWindowTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Min60 凸凹边沿突破 · 共用信号扫描（末根 K 或当日任一根满足）。
 */
public final class WaveCcMin60BreakoutCore {

    private static final double EPS = 1e-6;
    private static final double INTRINSIC_BREAKOUT_RISE_PCT = 0.015;

    private WaveCcMin60BreakoutCore() {
    }

    public enum BandShape {
        CONVEX, CONCAVE
    }

    @Getter
    public static final class Hit {
        private final BandShape shape;
        private final YangBandTools.CompleteYangBand lastBand;
        private final YangBandTools.CompleteYangBand prevBand;
        private final YangBandTools.CompleteYangBand referenceBand;
        private final Trade signalBar;
        private final Trade prevBar;
        private final Trade prevPrevBar;
        private final double breakLine;

        Hit(BandShape shape,
            YangBandTools.CompleteYangBand lastBand,
            YangBandTools.CompleteYangBand prevBand,
            YangBandTools.CompleteYangBand referenceBand,
            Trade signalBar, Trade prevBar, Trade prevPrevBar, double breakLine) {
            this.shape = shape;
            this.lastBand = lastBand;
            this.prevBand = prevBand;
            this.referenceBand = referenceBand;
            this.signalBar = signalBar;
            this.prevBar = prevBar;
            this.prevPrevBar = prevPrevBar;
            this.breakLine = breakLine;
        }
    }

    public static Hit findHitOnBars(List<Trade> allBars, int prevDays, int maxBarsPerDay,
                                    int lookback, boolean requireCurrentBreakout) {
        if (CollectionUtils.isEmpty(allBars) || allBars.size() < 3) {
            return null;
        }
        int lb = Math.max(lookback, 10);
        int prev = Math.max(prevDays, 0);
        int maxPerDay = Math.max(maxBarsPerDay, 1);

        if (requireCurrentBreakout) {
            return findHitAtIndex(allBars, allBars.size() - 1, lb, prev, maxPerDay, true);
        }

        BreakoutScanWindowTools.ScanWindow window = BreakoutScanWindowTools.buildScanWindow(
                allBars, BreakoutBucketTools::dayKey, prev, maxPerDay);
        List<Trade> todayBars = window.getSignalBars();
        if (CollectionUtils.isEmpty(todayBars)) {
            return null;
        }
        Hit lastHit = null;
        for (Trade candidate : todayBars) {
            int idx = indexOfBar(allBars, candidate);
            if (idx <= 0) {
                continue;
            }
            Hit hit = findHitAtIndex(allBars, idx, lb, prev, maxPerDay, false);
            if (hit != null) {
                lastHit = hit;
            }
        }
        return lastHit;
    }

    private static Hit findHitAtIndex(List<Trade> allBars, int signalIdx, int lookback,
                                      int prevDays, int maxBarsPerDay, boolean requireCurrentBreakout) {
        if (requireCurrentBreakout) {
            BreakoutScanWindowTools.ScanWindow window = BreakoutScanWindowTools.buildScanWindow(
                    allBars, BreakoutBucketTools::dayKey, prevDays, maxBarsPerDay);
            Trade signalBar = allBars.get(signalIdx);
            if (!containsBar(window.getSignalBars(), signalBar)) {
                return null;
            }
        }
        Hit hit = resolveHitAtIndex(allBars, signalIdx, lookback);
        if (hit == null) {
            return null;
        }
        Trade signalBar = allBars.get(signalIdx);
        Trade prevBar = allBars.get(signalIdx - 1);
        Trade prevPrevBar = signalIdx >= 2 ? allBars.get(signalIdx - 2) : null;
        if (!passesBreakoutStrength(signalBar, prevBar, prevPrevBar)) {
            return null;
        }
        return new Hit(hit.getShape(), hit.getLastBand(), hit.getPrevBand(), hit.getReferenceBand(),
                signalBar, prevBar, prevPrevBar, hit.getBreakLine());
    }

    static Hit resolveHitAtIndex(List<Trade> allBars, int signalIdx, int lookback) {
        if (signalIdx <= 0 || CollectionUtils.isEmpty(allBars)) {
            return null;
        }
        List<Trade> prefix = allBars.subList(0, signalIdx + 1);
        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(prefix, lookback);
        if (bands.size() < 2) {
            return null;
        }
        Trade signalBar = allBars.get(signalIdx);
        Trade prevBar = allBars.get(signalIdx - 1);
        YangBandTools.CompleteYangBand lastBand = bands.get(bands.size() - 1);
        YangBandTools.CompleteYangBand prevBand = bands.get(bands.size() - 2);
        boolean convex = WaveShapeTools.isConvex(lastBand, prevBand);
        BandShape shape = convex ? BandShape.CONVEX : BandShape.CONCAVE;

        YangBandTools.CompleteYangBand referenceBand;
        double breakLine;
        if (convex) {
            referenceBand = prevBand;
            breakLine = prevBand.getBandHigh();
        } else {
            referenceBand = lastBand;
            breakLine = lastBand.getBandHigh();
        }
        if (Double.isNaN(breakLine)) {
            return null;
        }
        if (!passesBandHighEdge(prevBar, signalBar, breakLine)) {
            return null;
        }
        return new Hit(shape, lastBand, prevBand, referenceBand, signalBar, prevBar, null, breakLine);
    }

    public static boolean passesBandHighEdge(Trade prevBar, Trade signalBar, double bandHigh) {
        if (prevBar == null || signalBar == null || Double.isNaN(bandHigh)) {
            return false;
        }
        Double prevClose = prevBar.getClose();
        Double signalClose = signalBar.getClose();
        if (prevClose == null || signalClose == null) {
            return false;
        }
        return prevClose <= bandHigh + EPS && signalClose > bandHigh + EPS;
    }

    public static double barAmplitudeRate(Trade bar, Trade prevBar) {
        if (bar == null || bar.getLow() == null || bar.getHigh() == null || bar.getLow() <= 0) {
            return Double.NaN;
        }
        double rangeRate = (bar.getHigh() - bar.getLow()) / bar.getLow();
        if (bar.getOpen() == null || prevBar == null || prevBar.getClose() == null || prevBar.getClose() <= 0) {
            return rangeRate;
        }
        double gapRate = (bar.getOpen() - prevBar.getClose()) / prevBar.getClose();
        return Math.max(rangeRate, gapRate);
    }

    public static boolean passesBreakoutStrength(Trade signalBar, Trade prevBar, Trade prevPrevBar) {
        if (passesAmplitudeExpand(signalBar, prevBar, prevPrevBar)) {
            return true;
        }
        double rise = BodyBarTierTools.risePct(signalBar, prevBar);
        return !Double.isNaN(rise) && rise > INTRINSIC_BREAKOUT_RISE_PCT + EPS;
    }

    public static boolean passesAmplitudeExpand(Trade signalBar, Trade prevBar, Trade prevPrevBar) {
        double sigRate = barAmplitudeRate(signalBar, prevBar);
        double prevRate = barAmplitudeRate(prevBar, prevPrevBar);
        if (Double.isNaN(sigRate) || Double.isNaN(prevRate)) {
            return false;
        }
        return sigRate > prevRate + EPS;
    }

    private static int indexOfBar(List<Trade> allBars, Trade bar) {
        if (bar == null || bar.getDay() == null) {
            return -1;
        }
        for (int i = 0; i < allBars.size(); i++) {
            if (sameBar(allBars.get(i), bar)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean sameBar(Trade a, Trade b) {
        return a != null && b != null && a.getDay() != null && a.getDay().equals(b.getDay());
    }

    private static boolean containsBar(List<Trade> bars, Trade target) {
        if (CollectionUtils.isEmpty(bars) || target == null) {
            return false;
        }
        for (Trade bar : bars) {
            if (sameBar(bar, target)) {
                return true;
            }
        }
        return false;
    }
}
