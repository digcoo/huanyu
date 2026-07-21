package com.yh.bigdata.tts.spider.strategy.tools.waveccbreak;

import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.WaveCcBreakoutStrategyParams;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class WaveCcBreakoutToolsTest {

    @Test
    public void hitsConcaveWhenMacdPositive() {
        List<Trade> trades = buildConcaveDayEdgeBars();
        List<MACDIndicatorUtils.MACDPoint> points = macdPoints(trades, 0.5);
        WaveCcBreakoutTools.TierHit hit = WaveCcBreakoutTools.resolveHitOnBars(
                trades, points, WaveCcBreakoutStrategyParams.builder()
                        .enableMinAmountFilter(false)
                        .enableSignalRiseGate(false)
                        .build());
        Assert.assertNotNull(hit);
        Assert.assertEquals(WaveCcBreakoutTools.BandShape.CONCAVE, hit.getShape());
        Assert.assertTrue(hit.isMacdPositive());
        Assert.assertEquals(hit.getBreakLine(), hit.getLastBand().getBandHigh(), 1e-6);
    }

    @Test
    public void rejectsConcaveWhenMacdNonPositive() {
        List<Trade> trades = buildConcaveDayEdgeBars();
        List<MACDIndicatorUtils.MACDPoint> points = macdPoints(trades, -0.5);
        Assert.assertNull(WaveCcBreakoutTools.resolveHitOnBars(
                trades, points, WaveCcBreakoutStrategyParams.builder().build()));
    }

    @Test
    public void hitsConvexWhenMacdNonPositive() {
        List<Trade> trades = buildConvexDayEdgeBars();
        List<MACDIndicatorUtils.MACDPoint> points = macdPoints(trades, -0.3);
        WaveCcBreakoutTools.TierHit hit = WaveCcBreakoutTools.resolveHitOnBars(
                trades, points, WaveCcBreakoutStrategyParams.builder()
                        .enableMinAmountFilter(false)
                        .enableSignalRiseGate(false)
                        .build());
        Assert.assertNotNull(hit);
        Assert.assertEquals(WaveCcBreakoutTools.BandShape.CONVEX, hit.getShape());
        Assert.assertFalse(hit.isMacdPositive());
        Assert.assertEquals(hit.getBreakLine(), hit.getPrevBand().getBandHigh(), 1e-6);
    }

    @Test
    public void convexUsesPrevBandHighNotPrevKHigh() {
        List<Trade> trades = buildConvexDayEdgeBars();
        List<YangBandTools.CompleteYangBand> bands =
                YangBandTools.findCompleteBands(trades, 120);
        YangBandTools.CompleteYangBand prevBand = bands.get(bands.size() - 2);
        Trade prev = trades.get(trades.size() - 2);
        Trade signal = trades.get(trades.size() - 1);
        double bandHigh = prevBand.getBandHigh();
        Assert.assertTrue(bandHigh > prev.getHigh() + 0.01);
        prev.setClose(bandHigh - 0.2);
        signal.setClose(prev.getHigh() + 0.1);
        Assert.assertTrue(prev.getClose() <= prev.getHigh() + 1e-6);
        Assert.assertTrue(signal.getClose() > prev.getHigh() + 1e-6);
        Assert.assertFalse(WaveCcBreakoutTools.passesBandHighEdge(prev, signal, bandHigh));

        signal.setClose(bandHigh + 0.5);
        Assert.assertTrue(WaveCcBreakoutTools.passesBandHighEdge(prev, signal, bandHigh));
        List<MACDIndicatorUtils.MACDPoint> points = macdPoints(trades, 0.2);
        WaveCcBreakoutTools.TierHit hit = WaveCcBreakoutTools.resolveHitOnBars(
                trades, points, WaveCcBreakoutStrategyParams.builder()
                        .enableMinAmountFilter(false)
                        .enableSignalRiseGate(false)
                        .build());
        Assert.assertNotNull(hit);
        Assert.assertEquals(WaveCcBreakoutTools.BandShape.CONVEX, hit.getShape());
    }

    @Test
    public void passesBandHighEdge() {
        Trade prev = bar("d1", 10, 10.4, 10.45, 10);
        Trade signal = bar("d2", 10.5, 10.8, 10.85, 10.5);
        Assert.assertTrue(WaveCcBreakoutTools.passesBandHighEdge(prev, signal, 10.5));
        Trade prevBroken = bar("d1", 10, 10.6, 10.7, 10);
        Assert.assertFalse(WaveCcBreakoutTools.passesBandHighEdge(prevBroken, signal, 10.5));
    }

    private static List<Trade> buildConcaveDayEdgeBars() {
        List<Trade> bars = buildConcaveDayBars();
        Trade prev = bars.get(bars.size() - 2);
        Trade signal = bars.get(bars.size() - 1);
        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(bars, 120);
        Assert.assertTrue(bands.size() >= 2);
        YangBandTools.CompleteYangBand lastBand = bands.get(bands.size() - 1);
        YangBandTools.CompleteYangBand prevBand = bands.get(bands.size() - 2);
        Assert.assertFalse(WaveShapeTools.isConvex(lastBand, prevBand));
        double line = lastBand.getBandHigh();
        prev.setClose(line - 0.5);
        signal.setClose(line + 1.0);
        return bars;
    }

    private static List<Trade> buildConvexDayEdgeBars() {
        List<Trade> bars = new ArrayList<>();
        bars.add(bar("2026-07-01", 10, 12, 9, 11));
        bars.add(bar("2026-07-02", 11, 13, 10, 10.5));
        bars.add(bar("2026-07-03", 10.5, 14, 10, 13.5));
        bars.add(bar("2026-07-04", 13.5, 15, 13, 14.5));
        bars.add(bar("2026-07-05", 14.5, 16.5, 14, 16.0));
        bars.add(bar("2026-07-06", 16.0, 18.0, 15.5, 17.5));
        bars.add(bar("2026-07-07", 17.5, 19.0, 17.0, 18.5));
        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(bars, 120);
        Assert.assertTrue(bands.size() >= 2);
        YangBandTools.CompleteYangBand lastBand = bands.get(bands.size() - 1);
        YangBandTools.CompleteYangBand prevBand = bands.get(bands.size() - 2);
        Assert.assertTrue(WaveShapeTools.isConvex(lastBand, prevBand));
        Trade prev = bars.get(bars.size() - 2);
        Trade signal = bars.get(bars.size() - 1);
        double line = prevBand.getBandHigh();
        prev.setClose(line - 0.5);
        signal.setClose(line + 1.0);
        return bars;
    }

    private static List<Trade> buildConcaveDayBars() {
        List<Trade> bars = new ArrayList<>();
        bars.add(bar("2026-01-01", 10, 12, 9, 11));
        bars.add(bar("2026-01-02", 11, 13, 10, 10.5));
        bars.add(bar("2026-01-03", 10.5, 14, 10, 13.5));
        bars.add(bar("2026-01-06", 13.5, 15, 13, 14.5));
        bars.add(bar("2026-01-07", 14.5, 16, 14, 15.5));
        bars.add(bar("2026-01-08", 15.5, 17, 15, 16.5));
        bars.add(bar("2026-01-09", 16.5, 18, 16, 17.5));
        bars.add(bar("2026-01-10", 17.5, 20, 17, 19.5));
        bars.add(bar("2026-01-13", 19.5, 21, 19, 20.5));
        bars.add(bar("2026-01-14", 20.5, 22, 20, 21.5));
        bars.add(bar("2026-01-15", 21.5, 23, 21, 22.5));
        bars.add(bar("2026-01-16", 22.5, 24, 22, 23.5));
        bars.add(bar("2026-01-17", 23.5, 25, 23, 24.5));
        bars.add(bar("2026-01-20", 24.5, 26, 24, 25.5));
        bars.add(bar("2026-01-21", 25.5, 27, 25, 26.5));
        bars.add(bar("2026-01-22", 26.5, 28, 26, 27.5));
        bars.add(bar("2026-01-23", 27.5, 29, 27, 28.5));
        bars.add(bar("2026-01-24", 28.5, 30, 28, 29.5));
        bars.add(bar("2026-01-27", 29.5, 31, 29, 30.5));
        bars.add(bar("2026-01-28", 30.5, 32, 30, 31.5));
        bars.add(bar("2026-01-29", 31.5, 33, 31, 32.5));
        bars.add(bar("2026-01-30", 32.5, 34, 32, 33.5));
        bars.add(bar("2026-01-31", 33.5, 35, 33, 34.5));
        bars.add(bar("2026-02-03", 34.5, 36, 34, 35.5));
        bars.add(bar("2026-02-04", 35.5, 37, 35, 36.5));
        bars.add(bar("2026-02-05", 36.5, 38, 36, 37.5));
        bars.add(bar("2026-02-06", 37.5, 39, 37, 38.5));
        bars.add(bar("2026-02-07", 38.5, 40, 38, 39.5));
        bars.add(bar("2026-02-10", 39.5, 41, 39, 40.5));
        bars.add(bar("2026-02-11", 40.5, 42, 40, 41.5));
        return bars;
    }

    private static List<MACDIndicatorUtils.MACDPoint> macdPoints(List<Trade> trades, double macd) {
        List<MACDIndicatorUtils.MACDPoint> list = new ArrayList<>();
        for (int i = 0; i < trades.size(); i++) {
            double value = i == trades.size() - 1 ? macd : macd * 0.5;
            list.add(point(trades.get(i), value));
        }
        return list;
    }

    private static MACDIndicatorUtils.MACDPoint point(Trade trade, double macd) {
        Ticker ticker = Ticker.from(trade);
        return new MACDIndicatorUtils.MACDPoint(ticker, macd, 0, 0, false, false, false);
    }

    private static Trade bar(String day, double open, double high, double low, double close) {
        Trade t = new Trade();
        t.setDay(day);
        t.setOpen(open);
        t.setHigh(high);
        t.setLow(low);
        t.setClose(close);
        return t;
    }
}
