package com.yh.bigdata.tts.spider.strategy.tools.wavecc;

import com.yh.bigdata.tts.common.model.Trade;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

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
        private final Trade firstYang;
        private final Trade lastYang;
        /** 阳段后第一根 K（完结波段的严格阴 K） */
        private final Trade terminatorBar;
        /** 波段顶价格对应 K（阳段或完结阴 K 中 high 最高者） */
        private final Trade bandHighBar;
        private final double bandHigh;
        private final double bandLow;
        private final double firstMedian;
        private final double lastMedian;
        private final double lastLow;

        CompleteYangBand(Trade firstYang, Trade lastYang, Trade terminatorBar, Trade bandHighBar,
                         double bandHigh, double bandLow) {
            this.firstYang = firstYang;
            this.lastYang = lastYang;
            this.terminatorBar = terminatorBar;
            this.bandHighBar = bandHighBar;
            this.bandHigh = bandHigh;
            this.bandLow = bandLow;
            this.firstMedian = medianPrice(firstYang);
            this.lastMedian = medianPrice(lastYang);
            this.lastLow = lowPrice(lastYang);
        }
    }

    public static boolean isStrictYang(Trade bar) {
        return bar != null && bar.getOpen() != null && bar.getClose() != null
                && bar.getClose() > bar.getOpen() + EPS;
    }

    public static boolean isStrictYin(Trade bar) {
        return bar != null && bar.getOpen() != null && bar.getClose() != null
                && bar.getClose() <= bar.getOpen() + EPS;
    }

    public static double medianPrice(Trade bar) {
        if (bar == null || bar.getHigh() == null || bar.getLow() == null) {
            return Double.NaN;
        }
        return (bar.getHigh() + bar.getLow()) / 2.0;
    }

    public static double lowPrice(Trade bar) {
        if (bar == null || bar.getLow() == null) {
            return Double.NaN;
        }
        return bar.getLow();
    }

    public static List<CompleteYangBand> findCompleteBands(List<Trade> trades, int lookback) {
        List<CompleteYangBand> bands = new ArrayList<>();
        if (CollectionUtils.isEmpty(trades) || lookback < 3) {
            return bands;
        }
        int n = trades.size();
        int start = Math.max(0, n - lookback);
        int i = start;
        while (i < n) {
            if (!isStrictYang(trades.get(i))) {
                i++;
                continue;
            }
            int j = i;
            while (j + 1 < n && isStrictYang(trades.get(j + 1))) {
                j++;
            }
            int terminatorIdx = j + 1;
            if (terminatorIdx <= n - 2 && isStrictYin(trades.get(terminatorIdx))) {
                bands.add(buildBand(trades, i, j, terminatorIdx));
            }
            i = j + 1;
        }
        return bands;
    }

    public static CompleteYangBand findLastCompleteBand(List<Trade> trades, int lookback) {
        List<CompleteYangBand> bands = findCompleteBands(trades, lookback);
        if (bands.isEmpty()) {
            return null;
        }
        return bands.get(bands.size() - 1);
    }

    /** 波段内 High 最高的阳 K；并列取靠前一根。 */
    public static Trade findHighestHighYangInBand(List<Trade> trades, CompleteYangBand band) {
        if (band == null || CollectionUtils.isEmpty(trades) || band.getFirstYang() == null) {
            return null;
        }
        int from = indexOfBar(trades, band.getFirstYang());
        int to = indexOfBar(trades, band.getLastYang());
        if (from < 0 || to < 0 || from > to) {
            return null;
        }
        Trade best = null;
        double bestHigh = Double.NEGATIVE_INFINITY;
        for (int i = from; i <= to; i++) {
            Trade bar = trades.get(i);
            if (!isStrictYang(bar) || bar.getHigh() == null) {
                continue;
            }
            if (bar.getHigh() > bestHigh + EPS) {
                bestHigh = bar.getHigh();
                best = bar;
            }
        }
        return best;
    }

    /** 波段顶对应 K：阳段与完结阴 K 中 high 最高者；并列时取靠后一根。 */
    public static Trade findBandHighBar(List<Trade> trades, CompleteYangBand band) {
        if (band == null || CollectionUtils.isEmpty(trades) || band.getFirstYang() == null) {
            return null;
        }
        int from = indexOfBar(trades, band.getFirstYang());
        int to = indexOfBar(trades, band.getLastYang());
        if (from < 0 || to < 0 || from > to) {
            return null;
        }
        Trade best = null;
        double bestHigh = Double.NEGATIVE_INFINITY;
        for (int i = from; i <= to; i++) {
            Trade bar = trades.get(i);
            if (!isStrictYang(bar) || bar.getHigh() == null) {
                continue;
            }
            if (bar.getHigh() > bestHigh + EPS) {
                bestHigh = bar.getHigh();
                best = bar;
            }
        }
        Trade terminator = band.getTerminatorBar();
        if (terminator != null && terminator.getHigh() != null
                && terminator.getHigh() >= bestHigh - EPS) {
            return terminator;
        }
        return best;
    }

    private static int indexOfBar(List<Trade> trades, Trade target) {
        if (target == null || target.getDay() == null) {
            return -1;
        }
        for (int i = 0; i < trades.size(); i++) {
            Trade bar = trades.get(i);
            if (bar != null && target.getDay().equals(bar.getDay())) {
                return i;
            }
        }
        return -1;
    }

    private static CompleteYangBand buildBand(List<Trade> trades, int from, int to, int terminatorIdx) {
        Trade first = trades.get(from);
        Trade last = trades.get(to);
        Trade terminator = trades.get(terminatorIdx);
        double high = Double.NEGATIVE_INFINITY;
        for (int k = from; k <= to; k++) {
            Trade bar = trades.get(k);
            if (bar.getHigh() != null) {
                high = Math.max(high, bar.getHigh());
            }
        }
        if (terminator != null && terminator.getHigh() != null) {
            high = Math.max(high, terminator.getHigh());
        }
        double low = Double.POSITIVE_INFINITY;
        for (int k = from; k <= to; k++) {
            Trade bar = trades.get(k);
            if (isStrictYang(bar) && bar.getLow() != null) {
                low = Math.min(low, bar.getLow());
            }
        }
        if (low == Double.POSITIVE_INFINITY) {
            low = Double.NaN;
        }
        Trade bandHighBar = resolveBandHighBar(trades, from, to, terminator, high);
        return new CompleteYangBand(first, last, terminator, bandHighBar, high, low);
    }

    private static Trade resolveBandHighBar(List<Trade> trades, int from, int to,
                                            Trade terminator, double bandHigh) {
        if (Double.isNaN(bandHigh) || bandHigh == Double.NEGATIVE_INFINITY) {
            return null;
        }
        Trade best = null;
        for (int k = from; k <= to; k++) {
            Trade bar = trades.get(k);
            if (!isStrictYang(bar) || bar.getHigh() == null) {
                continue;
            }
            if (Math.abs(bar.getHigh() - bandHigh) <= EPS) {
                best = bar;
            }
        }
        if (terminator != null && terminator.getHigh() != null
                && Math.abs(terminator.getHigh() - bandHigh) <= EPS) {
            return terminator;
        }
        return best != null ? best : trades.get(to);
    }
}
