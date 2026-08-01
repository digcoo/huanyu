package com.binance.client.strategy.wavecc;

import com.binance.client.utils.indicator.Ticker;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 连续阳 K 完整波段：严格 close&gt;open，以严格阴 K（close≤open）完结，且完结 K 不能是序列最后一根。
 * <p>
 * 波段底 = min(波段内所有阳 K low)；波段顶 = max(波段内所有阳 K high, 紧跟阳段后的完结阴 K high)。
 */
public final class YangBandTools {

    private static final double EPS = 1e-6;

    private YangBandTools() {
    }

    @Getter
    public static final class CompleteYangBand {
        private final Ticker firstYang;
        private final Ticker lastYang;
        private final Ticker terminatorBar;
        private final Ticker bandHighBar;
        private final double bandHigh;
        private final double bandLow;

        CompleteYangBand(Ticker firstYang, Ticker lastYang, Ticker terminatorBar, Ticker bandHighBar,
                         double bandHigh, double bandLow) {
            this.firstYang = firstYang;
            this.lastYang = lastYang;
            this.terminatorBar = terminatorBar;
            this.bandHighBar = bandHighBar;
            this.bandHigh = bandHigh;
            this.bandLow = bandLow;
        }
    }

    public static boolean isStrictYang(Ticker bar) {
        return bar != null && bar.getClose() > bar.getOpen() + EPS;
    }

    public static boolean isStrictYin(Ticker bar) {
        return bar != null && bar.getClose() <= bar.getOpen() + EPS;
    }

    public static List<CompleteYangBand> findCompleteBands(List<Ticker> bars, int lookback) {
        List<CompleteYangBand> bands = new ArrayList<>();
        if (bars == null || bars.isEmpty() || lookback < 3) {
            return bands;
        }
        int n = bars.size();
        int start = Math.max(0, n - lookback);
        int i = start;
        while (i < n) {
            if (!isStrictYang(bars.get(i))) {
                i++;
                continue;
            }
            int j = i;
            while (j + 1 < n && isStrictYang(bars.get(j + 1))) {
                j++;
            }
            int terminatorIdx = j + 1;
            if (terminatorIdx <= n - 2 && isStrictYin(bars.get(terminatorIdx))) {
                bands.add(buildBand(bars, i, j, terminatorIdx));
            }
            i = j + 1;
        }
        return bands;
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

    private static CompleteYangBand buildBand(List<Ticker> bars, int from, int to, int terminatorIdx) {
        Ticker first = bars.get(from);
        Ticker last = bars.get(to);
        Ticker terminator = bars.get(terminatorIdx);
        double high = Double.NEGATIVE_INFINITY;
        for (int k = from; k <= to; k++) {
            high = Math.max(high, bars.get(k).getHigh());
        }
        if (terminator != null) {
            high = Math.max(high, terminator.getHigh());
        }
        double low = Double.POSITIVE_INFINITY;
        for (int k = from; k <= to; k++) {
            Ticker bar = bars.get(k);
            if (isStrictYang(bar)) {
                low = Math.min(low, bar.getLow());
            }
        }
        if (low == Double.POSITIVE_INFINITY) {
            low = Double.NaN;
        }
        Ticker bandHighBar = resolveBandHighBar(bars, from, to, terminator, high);
        return new CompleteYangBand(first, last, terminator, bandHighBar, high, low);
    }

    private static Ticker resolveBandHighBar(List<Ticker> bars, int from, int to,
                                               Ticker terminator, double bandHigh) {
        if (Double.isNaN(bandHigh) || bandHigh == Double.NEGATIVE_INFINITY) {
            return null;
        }
        Ticker best = null;
        for (int k = from; k <= to; k++) {
            Ticker bar = bars.get(k);
            if (!isStrictYang(bar)) {
                continue;
            }
            if (Math.abs(bar.getHigh() - bandHigh) <= EPS) {
                best = bar;
            }
        }
        if (terminator != null && Math.abs(terminator.getHigh() - bandHigh) <= EPS) {
            return terminator;
        }
        return best != null ? best : bars.get(to);
    }
}
