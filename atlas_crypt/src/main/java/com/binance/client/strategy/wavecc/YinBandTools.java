package com.binance.client.strategy.wavecc;

import com.binance.client.utils.indicator.Ticker;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 连续阴 K 完整波段：严格 close≤open，以严格阳 K（close&gt;open）完结，且完结 K 不能是序列最后一根。
 */
public final class YinBandTools {

    private static final double EPS = 1e-6;

    private YinBandTools() {
    }

    @Getter
    public static final class CompleteYinBand {
        private final Ticker firstYin;
        private final Ticker lastYin;
        private final Ticker terminatorBar;
        private final Ticker bandLowBar;
        private final double bandHigh;
        private final double bandLow;

        CompleteYinBand(Ticker firstYin, Ticker lastYin, Ticker terminatorBar, Ticker bandLowBar,
                        double bandHigh, double bandLow) {
            this.firstYin = firstYin;
            this.lastYin = lastYin;
            this.terminatorBar = terminatorBar;
            this.bandLowBar = bandLowBar;
            this.bandHigh = bandHigh;
            this.bandLow = bandLow;
        }
    }

    public static boolean isStrictYin(Ticker bar) {
        return bar != null && bar.getClose() <= bar.getOpen() + EPS;
    }

    public static boolean isStrictYang(Ticker bar) {
        return bar != null && bar.getClose() > bar.getOpen() + EPS;
    }

    public static List<CompleteYinBand> findCompleteBands(List<Ticker> bars, int lookback) {
        List<CompleteYinBand> bands = new ArrayList<>();
        if (bars == null || bars.isEmpty() || lookback < 3) {
            return bands;
        }
        int n = bars.size();
        int start = Math.max(0, n - lookback);
        int i = start;
        while (i < n) {
            if (!isStrictYin(bars.get(i))) {
                i++;
                continue;
            }
            int j = i;
            while (j + 1 < n && isStrictYin(bars.get(j + 1))) {
                j++;
            }
            int terminatorIdx = j + 1;
            if (terminatorIdx <= n - 2 && isStrictYang(bars.get(terminatorIdx))) {
                bands.add(buildBand(bars, i, j, terminatorIdx));
            }
            i = j + 1;
        }
        return bands;
    }

    public static CompleteYinBand findBandContainingYinBar(List<Ticker> bars, int yinIdx) {
        if (bars == null || yinIdx < 0 || yinIdx >= bars.size()) {
            return null;
        }
        if (!isStrictYin(bars.get(yinIdx))) {
            return null;
        }
        List<CompleteYinBand> bands = findCompleteBands(bars, bars.size());
        for (CompleteYinBand band : bands) {
            int from = indexOfBar(bars, band.getFirstYin());
            int to = indexOfBar(bars, band.getLastYin());
            if (from >= 0 && to >= from && yinIdx >= from && yinIdx <= to) {
                return band;
            }
        }
        return null;
    }

    /** 从 index 往前找最近一个完整阴波段（完结阳的下标 ≤ index）。 */
    public static CompleteYinBand findNearestCompleteBandAtOrBefore(List<Ticker> bars, int index) {
        if (bars == null || index < 0) {
            return null;
        }
        List<CompleteYinBand> bands = findCompleteBands(bars, bars.size());
        CompleteYinBand best = null;
        int bestTerm = -1;
        for (CompleteYinBand band : bands) {
            int termIdx = indexOfBar(bars, band.getTerminatorBar());
            if (termIdx >= 0 && termIdx <= index && termIdx > bestTerm) {
                best = band;
                bestTerm = termIdx;
            }
        }
        return best;
    }

    private static int indexOfBar(List<Ticker> bars, Ticker target) {
        if (target == null) {
            return -1;
        }
        for (int i = 0; i < bars.size(); i++) {
            Ticker bar = bars.get(i);
            if (bar != null && bar.getTimestamp() == target.getTimestamp()) {
                return i;
            }
        }
        return -1;
    }

    private static CompleteYinBand buildBand(List<Ticker> bars, int from, int to, int terminatorIdx) {
        Ticker first = bars.get(from);
        Ticker last = bars.get(to);
        Ticker terminator = bars.get(terminatorIdx);
        double low = Double.POSITIVE_INFINITY;
        for (int k = from; k <= to; k++) {
            low = Math.min(low, bars.get(k).getLow());
        }
        if (terminator != null) {
            low = Math.min(low, terminator.getLow());
        }
        double high = first != null ? first.getHigh() : Double.NaN;
        Ticker bandLowBar = resolveBandLowBar(bars, from, to, terminator, low);
        return new CompleteYinBand(first, last, terminator, bandLowBar, high, low);
    }

    private static Ticker resolveBandLowBar(List<Ticker> bars, int from, int to,
                                            Ticker terminator, double bandLow) {
        if (Double.isNaN(bandLow) || bandLow == Double.POSITIVE_INFINITY) {
            return null;
        }
        Ticker best = null;
        for (int k = from; k <= to; k++) {
            Ticker bar = bars.get(k);
            if (!isStrictYin(bar)) {
                continue;
            }
            if (Math.abs(bar.getLow() - bandLow) <= EPS) {
                best = bar;
            }
        }
        if (terminator != null && Math.abs(terminator.getLow() - bandLow) <= EPS) {
            return terminator;
        }
        return best != null ? best : bars.get(to);
    }
}
