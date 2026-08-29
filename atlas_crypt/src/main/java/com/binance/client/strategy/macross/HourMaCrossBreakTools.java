package com.binance.client.strategy.macross;

import com.binance.client.dto.LongCandlestickDTO;
import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.model.market.Candlestick;
import com.binance.client.model.market.LongCandlestickMA;
import com.binance.client.strategy.wavecc.YangBandTools;
import com.binance.client.strategy.wavecc.YinBandTools;
import com.binance.client.utils.LongStrategyUtil;
import com.binance.client.utils.indicator.Ticker;

import java.util.ArrayList;
import java.util.List;

/**
 * 策略3：1H MA 交叉突破（多）/ 跌破（空），父级 4H。
 * <ul>
 *   <li>多：边沿破金叉波段顶（且 MA5&gt;MA10）或死叉交叉点；4H close &gt; max(MA5, MA10)</li>
 *   <li>空：边沿破死叉波段底（且 MA5&lt;MA10）或金叉交叉点；4H close &lt; min(MA5, MA10)</li>
 * </ul>
 */
public final class HourMaCrossBreakTools {

    private HourMaCrossBreakTools() {
    }

    public static final class Hit {
        private final MaCrossPointCore.CrossKind crossKind;
        private final LongCandlestickMA signalBar;
        private final LongCandlestickMA prevBar;
        private final LongCandlestickMA crossBar;
        private final double breakLine;
        private final LongCandlestickMA bandRefBar;
        private final boolean shortSide;

        Hit(MaCrossPointCore.CrossKind crossKind,
            LongCandlestickMA signalBar, LongCandlestickMA prevBar, LongCandlestickMA crossBar,
            double breakLine, LongCandlestickMA bandRefBar, boolean shortSide) {
            this.crossKind = crossKind;
            this.signalBar = signalBar;
            this.prevBar = prevBar;
            this.crossBar = crossBar;
            this.breakLine = breakLine;
            this.bandRefBar = bandRefBar;
            this.shortSide = shortSide;
        }

        public MaCrossPointCore.CrossKind getCrossKind() {
            return crossKind;
        }

        public LongCandlestickMA getSignalBar() {
            return signalBar;
        }

        public LongCandlestickMA getPrevBar() {
            return prevBar;
        }

        public LongCandlestickMA getCrossBar() {
            return crossBar;
        }

        public double getBreakLine() {
            return breakLine;
        }

        public LongCandlestickMA getBandHighBar() {
            return bandRefBar;
        }

        public LongCandlestickMA getBandRefBar() {
            return bandRefBar;
        }

        public boolean isShortSide() {
            return shortSide;
        }
    }

    public static Hit findHit(List<Candlestick> hour1Bars, List<Candlestick> hour4Bars) {
        if (hour1Bars == null || hour1Bars.size() < 12 || hour4Bars == null || hour4Bars.size() < 12) {
            return null;
        }
        LongCandlestickDTO hour1 = LongStrategyUtil.buildLongCandlestickDTO(hour1Bars, PeriodTypeEnum.HOUR1);
        LongCandlestickDTO hour4 = LongStrategyUtil.buildLongCandlestickDTO(hour4Bars, PeriodTypeEnum.HOUR4);
        if (hour1 == null || hour4 == null) {
            return null;
        }
        return findHitOnBars(hour1.getCandlestickMAS(), hour4.getCurrentCandlestick());
    }

    public static Hit findShortHit(List<Candlestick> hour1Bars, List<Candlestick> hour4Bars) {
        if (hour1Bars == null || hour1Bars.size() < 12 || hour4Bars == null || hour4Bars.size() < 12) {
            return null;
        }
        LongCandlestickDTO hour1 = LongStrategyUtil.buildLongCandlestickDTO(hour1Bars, PeriodTypeEnum.HOUR1);
        LongCandlestickDTO hour4 = LongStrategyUtil.buildLongCandlestickDTO(hour4Bars, PeriodTypeEnum.HOUR4);
        if (hour1 == null || hour4 == null) {
            return null;
        }
        return findShortHitOnBars(hour1.getCandlestickMAS(), hour4.getCurrentCandlestick());
    }

    static Hit findHitOnBars(List<LongCandlestickMA> hour1Bars, LongCandlestickMA parent4h) {
        if (hour1Bars == null || hour1Bars.size() < 3 || parent4h == null) {
            return null;
        }
        if (!MaCrossPointCore.passesAboveMa(parent4h)) {
            return null;
        }
        Hit golden = findGoldenBandTopHit(hour1Bars);
        if (golden != null) {
            return golden;
        }
        return findDeathCrossPointHit(hour1Bars);
    }

    static Hit findShortHitOnBars(List<LongCandlestickMA> hour1Bars, LongCandlestickMA parent4h) {
        if (hour1Bars == null || hour1Bars.size() < 3 || parent4h == null) {
            return null;
        }
        if (!MaCrossPointCore.passesBelowMa(parent4h)) {
            return null;
        }
        Hit deathBand = findDeathBandLowHit(hour1Bars);
        if (deathBand != null) {
            return deathBand;
        }
        return findGoldenCrossPointBreakdownHit(hour1Bars);
    }

    static Hit findGoldenBandTopHit(List<LongCandlestickMA> bars) {
        int lastIdx = bars.size() - 1;
        LongCandlestickMA signalBar = bars.get(lastIdx);
        LongCandlestickMA prevBar = bars.get(lastIdx - 1);
        if (!MaCrossPointCore.passesMa5AboveMa10(signalBar)) {
            return null;
        }
        int crossIdx = MaCrossPointCore.findLatestCrossIndex(bars, lastIdx, MaCrossPointCore.CrossKind.GOLDEN);
        if (crossIdx < 1) {
            return null;
        }
        LongCandlestickMA crossBar = bars.get(crossIdx);
        YangBandTools.CompleteYangBand band = resolveGoldenBand(bars, crossIdx);
        if (band == null || Double.isNaN(band.getBandHigh())) {
            return null;
        }
        double breakLine = band.getBandHigh();
        if (!MaCrossPointCore.passesEdgeBreak(prevBar, signalBar, breakLine)) {
            return null;
        }
        return new Hit(MaCrossPointCore.CrossKind.GOLDEN, signalBar, prevBar, crossBar,
                breakLine, findByTime(bars, band.getBandHighBar()), false);
    }

    static YangBandTools.CompleteYangBand resolveGoldenBand(List<LongCandlestickMA> bars, int crossIdx) {
        List<Ticker> tickers = toTickers(bars);
        Ticker crossTicker = tickers.get(crossIdx);
        if (YangBandTools.isStrictYang(crossTicker)) {
            YangBandTools.CompleteYangBand containing = YangBandTools.findBandContainingYangBar(tickers, crossIdx);
            if (containing != null) {
                return containing;
            }
            return YangBandTools.findNearestCompleteBandAtOrBefore(tickers, crossIdx);
        }
        if (YangBandTools.isStrictYin(crossTicker)) {
            return YangBandTools.findNearestCompleteBandAtOrBefore(tickers, crossIdx);
        }
        return null;
    }

    static Hit findDeathCrossPointHit(List<LongCandlestickMA> bars) {
        int lastIdx = bars.size() - 1;
        LongCandlestickMA signalBar = bars.get(lastIdx);
        LongCandlestickMA prevBar = bars.get(lastIdx - 1);
        int crossIdx = MaCrossPointCore.findLatestCrossIndex(bars, lastIdx, MaCrossPointCore.CrossKind.DEATH);
        if (crossIdx < 1) {
            return null;
        }
        Double breakLine = MaCrossPointCore.crossPriceAt(bars, crossIdx);
        if (breakLine == null) {
            return null;
        }
        if (!MaCrossPointCore.passesEdgeBreak(prevBar, signalBar, breakLine)) {
            return null;
        }
        return new Hit(MaCrossPointCore.CrossKind.DEATH, signalBar, prevBar, bars.get(crossIdx), breakLine, null, false);
    }

    static Hit findDeathBandLowHit(List<LongCandlestickMA> bars) {
        int lastIdx = bars.size() - 1;
        LongCandlestickMA signalBar = bars.get(lastIdx);
        LongCandlestickMA prevBar = bars.get(lastIdx - 1);
        if (!MaCrossPointCore.passesMa5BelowMa10(signalBar)) {
            return null;
        }
        int crossIdx = MaCrossPointCore.findLatestCrossIndex(bars, lastIdx, MaCrossPointCore.CrossKind.DEATH);
        if (crossIdx < 1) {
            return null;
        }
        LongCandlestickMA crossBar = bars.get(crossIdx);
        YinBandTools.CompleteYinBand band = resolveDeathBand(bars, crossIdx);
        if (band == null || Double.isNaN(band.getBandLow())) {
            return null;
        }
        double breakLine = band.getBandLow();
        if (!MaCrossPointCore.passesEdgeBreakDown(prevBar, signalBar, breakLine)) {
            return null;
        }
        return new Hit(MaCrossPointCore.CrossKind.DEATH, signalBar, prevBar, crossBar,
                breakLine, findByTime(bars, band.getBandLowBar()), true);
    }

    static YinBandTools.CompleteYinBand resolveDeathBand(List<LongCandlestickMA> bars, int crossIdx) {
        List<Ticker> tickers = toTickers(bars);
        Ticker crossTicker = tickers.get(crossIdx);
        if (YinBandTools.isStrictYin(crossTicker)) {
            YinBandTools.CompleteYinBand containing = YinBandTools.findBandContainingYinBar(tickers, crossIdx);
            if (containing != null) {
                return containing;
            }
            return YinBandTools.findNearestCompleteBandAtOrBefore(tickers, crossIdx);
        }
        if (YinBandTools.isStrictYang(crossTicker)) {
            return YinBandTools.findNearestCompleteBandAtOrBefore(tickers, crossIdx);
        }
        return null;
    }

    static Hit findGoldenCrossPointBreakdownHit(List<LongCandlestickMA> bars) {
        int lastIdx = bars.size() - 1;
        LongCandlestickMA signalBar = bars.get(lastIdx);
        LongCandlestickMA prevBar = bars.get(lastIdx - 1);
        int crossIdx = MaCrossPointCore.findLatestCrossIndex(bars, lastIdx, MaCrossPointCore.CrossKind.GOLDEN);
        if (crossIdx < 1) {
            return null;
        }
        Double breakLine = MaCrossPointCore.crossPriceAt(bars, crossIdx);
        if (breakLine == null) {
            return null;
        }
        if (!MaCrossPointCore.passesEdgeBreakDown(prevBar, signalBar, breakLine)) {
            return null;
        }
        return new Hit(MaCrossPointCore.CrossKind.GOLDEN, signalBar, prevBar, bars.get(crossIdx), breakLine, null, true);
    }

    public static String buildSignalMessage(Hit hit) {
        if (hit == null) {
            return "";
        }
        boolean shortSide = hit.isShortSide();
        boolean death = hit.getCrossKind() == MaCrossPointCore.CrossKind.DEATH;
        LongCandlestickMA signalBar = hit.getSignalBar();
        String name;
        String tag;
        String target;
        if (shortSide) {
            name = death ? "MA死叉波段跌破" : "MA金叉点跌破";
            tag = death ? "MDS" : "MGS";
            target = death ? "BAND_LOW" : "CROSS_POINT";
        } else {
            name = death ? "MA死叉点突破" : "MA金叉波段突破";
            tag = death ? "MDB" : "MGB";
            target = death ? "CROSS_POINT" : "BAND_TOP";
        }
        return String.format(
                "%s,strategyTag=%s,period=1h,parent=4h,side=%s,cross=%s,target=%s,sigTime=%s,sigClose=%.8f,breakLine=%.8f,crossTime=%s",
                name, tag, shortSide ? "SHORT" : "LONG",
                hit.getCrossKind() != null ? hit.getCrossKind().name() : "",
                target,
                timeOf(signalBar),
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose().doubleValue() : 0,
                hit.getBreakLine(),
                timeOf(hit.getCrossBar()));
    }

    public static String buildTrendMessage(Hit hit) {
        if (hit == null) {
            return "[MXB]1小时MA交叉突破";
        }
        boolean shortSide = hit.isShortSide();
        boolean death = hit.getCrossKind() == MaCrossPointCore.CrossKind.DEATH;
        if (shortSide) {
            String name = death ? "MA死叉波段跌破" : "MA金叉点跌破";
            String tag = death ? "MDS" : "MGS";
            String target = death ? "死叉波段底" : "金叉交叉点";
            String maGate = death ? ",MA5<MA10" : "";
            return String.format("[%s]%s|1H边沿破%s%s,4H父级均价之下|breakLine=%.8f,crossTime=%s",
                    tag, name, target, maGate, hit.getBreakLine(), timeOf(hit.getCrossBar()));
        }
        String name = death ? "MA死叉点突破" : "MA金叉波段突破";
        String tag = death ? "MDB" : "MGB";
        String target = death ? "死叉交叉点" : "金叉波段顶";
        String maGate = death ? "" : ",MA5>MA10";
        return String.format("[%s]%s|1H边沿破%s%s,4H父级均价之上|breakLine=%.8f,crossTime=%s",
                tag, name, target, maGate, hit.getBreakLine(), timeOf(hit.getCrossBar()));
    }

    static List<Ticker> toTickers(List<LongCandlestickMA> bars) {
        List<Ticker> tickers = new ArrayList<>(bars.size());
        for (LongCandlestickMA bar : bars) {
            tickers.add(Ticker.builder()
                    .timestamp(bar != null && bar.getOpenTime() != null ? bar.getOpenTime() : 0L)
                    .open(MaCrossPointCore.bd(bar != null ? bar.getOpen() : null))
                    .high(MaCrossPointCore.bd(bar != null ? bar.getHigh() : null))
                    .low(MaCrossPointCore.bd(bar != null ? bar.getLow() : null))
                    .close(MaCrossPointCore.bd(bar != null ? bar.getClose() : null))
                    .build());
        }
        return tickers;
    }

    private static LongCandlestickMA findByTime(List<LongCandlestickMA> bars, Ticker ticker) {
        if (ticker == null || bars == null) {
            return null;
        }
        for (LongCandlestickMA bar : bars) {
            if (bar != null && bar.getOpenTime() != null && bar.getOpenTime() == ticker.getTimestamp()) {
                return bar;
            }
        }
        return null;
    }

    private static String timeOf(LongCandlestickMA bar) {
        if (bar == null || bar.getOpenTime() == null) {
            return "";
        }
        return String.valueOf(bar.getOpenTime());
    }
}
