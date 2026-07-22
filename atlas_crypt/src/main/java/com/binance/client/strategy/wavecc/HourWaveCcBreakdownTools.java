package com.binance.client.strategy.wavecc;

import com.binance.client.model.market.Candlestick;
import com.binance.client.utils.indicator.MACDIndicator;
import com.binance.client.utils.indicator.Ticker;

import java.util.List;

/**
 * Crypto 1小时凹凸跌破（空）：1h/4h/日 MACD 至少 2 个 &lt;0；末根 1h 凸凹边沿跌破。
 */
public final class HourWaveCcBreakdownTools {

    private static final double EPS = 1e-6;
    private static final int DEFAULT_LOOKBACK = 60;

    private HourWaveCcBreakdownTools() {
    }

    public enum BandShape {
        CONVEX, CONCAVE
    }

    public static final class Hit {
        private final BandShape shape;
        private final YinBandTools.CompleteYinBand lastBand;
        private final YinBandTools.CompleteYinBand prevBand;
        private final YinBandTools.CompleteYinBand referenceBand;
        private final Ticker signalBar;
        private final Ticker prevBar;
        private final double breakLine;

        Hit(BandShape shape,
            YinBandTools.CompleteYinBand lastBand,
            YinBandTools.CompleteYinBand prevBand,
            YinBandTools.CompleteYinBand referenceBand,
            Ticker signalBar, Ticker prevBar, double breakLine) {
            this.shape = shape;
            this.lastBand = lastBand;
            this.prevBand = prevBand;
            this.referenceBand = referenceBand;
            this.signalBar = signalBar;
            this.prevBar = prevBar;
            this.breakLine = breakLine;
        }

        public BandShape getShape() {
            return shape;
        }

        public YinBandTools.CompleteYinBand getLastBand() {
            return lastBand;
        }

        public YinBandTools.CompleteYinBand getPrevBand() {
            return prevBand;
        }

        public YinBandTools.CompleteYinBand getReferenceBand() {
            return referenceBand;
        }

        public Ticker getSignalBar() {
            return signalBar;
        }

        public Ticker getPrevBar() {
            return prevBar;
        }

        public double getBreakLine() {
            return breakLine;
        }
    }

    public static Hit findHit(List<Candlestick> hour1Bars,
                              List<Candlestick> hour4Bars,
                              List<Candlestick> dayBars) {
        if (!passesMultiPeriodMacdGate(hour1Bars, hour4Bars, dayBars)) {
            return null;
        }
        if (hour1Bars == null || hour1Bars.size() < 3) {
            return null;
        }
        List<Ticker> tickers = Ticker.from(hour1Bars);
        return findHitOnTickers(tickers, DEFAULT_LOOKBACK);
    }

    static Hit findHitOnTickers(List<Ticker> tickers, int lookback) {
        if (tickers == null || tickers.size() < 3) {
            return null;
        }
        int signalIdx = tickers.size() - 1;
        return resolveHitAtIndex(tickers, signalIdx, lookback);
    }

    static Hit resolveHitAtIndex(List<Ticker> tickers, int signalIdx, int lookback) {
        if (signalIdx <= 0) {
            return null;
        }
        List<Ticker> prefix = tickers.subList(0, signalIdx + 1);
        List<YinBandTools.CompleteYinBand> bands = YinBandTools.findCompleteBands(prefix, lookback);
        if (bands.size() < 2) {
            return null;
        }
        Ticker signalBar = tickers.get(signalIdx);
        Ticker prevBar = tickers.get(signalIdx - 1);
        YinBandTools.CompleteYinBand lastBand = bands.get(bands.size() - 1);
        YinBandTools.CompleteYinBand prevBand = bands.get(bands.size() - 2);
        boolean convex = WaveShapeTools.isConvexYin(lastBand, prevBand);
        BandShape shape = convex ? BandShape.CONVEX : BandShape.CONCAVE;

        YinBandTools.CompleteYinBand referenceBand;
        double breakLine;
        if (convex) {
            referenceBand = prevBand;
            breakLine = prevBand.getBandLow();
        } else {
            referenceBand = lastBand;
            breakLine = lastBand.getBandLow();
        }
        if (Double.isNaN(breakLine)) {
            return null;
        }
        if (!passesBandLowEdge(prevBar, signalBar, breakLine)) {
            return null;
        }
        return new Hit(shape, lastBand, prevBand, referenceBand, signalBar, prevBar, breakLine);
    }

    public static boolean passesMultiPeriodMacdGate(List<Candlestick> hour1Bars,
                                                    List<Candlestick> hour4Bars,
                                                    List<Candlestick> dayBars) {
        int negative = 0;
        if (lastMacd(hour1Bars) < 0) {
            negative++;
        }
        if (lastMacd(hour4Bars) < 0) {
            negative++;
        }
        if (lastMacd(dayBars) < 0) {
            negative++;
        }
        return negative >= 2;
    }

    static double lastMacd(List<Candlestick> bars) {
        if (bars == null || bars.size() < 26) {
            return Double.NaN;
        }
        List<MACDIndicator.MACDPoint> points = MACDIndicator.calculateMACD(Ticker.from(bars));
        if (points.isEmpty()) {
            return Double.NaN;
        }
        MACDIndicator.MACDPoint last = points.get(points.size() - 1);
        return last != null ? last.getMacd() : Double.NaN;
    }

    static boolean passesBandLowEdge(Ticker prevBar, Ticker signalBar, double bandLow) {
        if (prevBar == null || signalBar == null || Double.isNaN(bandLow)) {
            return false;
        }
        return prevBar.getClose() >= bandLow - EPS && signalBar.getClose() < bandLow - EPS;
    }

    public static String buildSignalMessage(Hit hit) {
        if (hit == null) {
            return "";
        }
        Ticker signalBar = hit.getSignalBar();
        Ticker prevBar = hit.getPrevBar();
        return String.format(
                "1h凹凸跌破,strategyTag=H1WCCS,shape=%s,breakLine=%.8f,"
                        + "sigTime=%s,sigClose=%.8f,prevTime=%s,prevClose=%.8f",
                hit.getShape().name(),
                hit.getBreakLine(),
                signalBar != null ? signalBar.getTimestampStr() : "",
                signalBar != null ? signalBar.getClose() : 0,
                prevBar != null ? prevBar.getTimestampStr() : "",
                prevBar != null ? prevBar.getClose() : 0);
    }

    public static String buildTrendMessage(Hit hit) {
        if (hit == null) {
            return "[H1WCCS]1小时凹凸跌破";
        }
        String shapeLabel = hit.getShape() == BandShape.CONVEX ? "凸" : "凹";
        return String.format("[H1WCCS]1小时凹凸跌破|1h/4h/日MACD≥2<0,末1h%s破波段Low|breakLine=%.8f",
                shapeLabel, hit.getBreakLine());
    }
}
