package com.binance.client.strategy.macross;

import com.binance.client.model.market.LongCandlestickMA;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class HourMaCrossBreakToolsTest {

    @Test
    public void goldenYang_usesContainingBandHigh() {
        List<LongCandlestickMA> bars = Arrays.asList(
                ohlc(1L, 10.0, 10.5, 9.9, 10.2, 9.5, 10.0),
                ohlc(2L, 10.2, 10.8, 10.1, 10.5, 10.2, 10.0),
                ohlc(3L, 10.5, 10.6, 10.0, 10.1, 10.3, 10.1),
                ohlc(4L, 10.1, 10.3, 9.8, 10.0, 10.4, 10.2),
                ohlc(5L, 10.0, 11.0, 9.9, 10.9, 10.5, 10.3)
        );
        HourMaCrossBreakTools.Hit hit = HourMaCrossBreakTools.findHitOnBars(bars, parentAboveMa());
        Assert.assertNotNull(hit);
        Assert.assertEquals(MaCrossPointCore.CrossKind.GOLDEN, hit.getCrossKind());
        Assert.assertEquals(10.8, hit.getBreakLine(), 1e-6);
        Assert.assertEquals(Long.valueOf(2L), hit.getCrossBar().getOpenTime());
    }

    @Test
    public void goldenYang_unfinishedUsesPrevCompleteBand() {
        List<LongCandlestickMA> bars = Arrays.asList(
                ohlc(1L, 10.0, 10.4, 9.9, 10.2, 9.5, 10.0),
                ohlc(2L, 10.2, 10.3, 10.0, 10.05, 9.8, 10.0),
                ohlc(3L, 10.1, 10.2, 10.0, 10.15, 10.2, 10.0),
                ohlc(4L, 10.15, 10.3, 10.0, 10.2, 10.3, 10.1),
                ohlc(5L, 10.2, 10.8, 10.1, 10.5, 10.5, 10.3)
        );
        HourMaCrossBreakTools.Hit hit = HourMaCrossBreakTools.findHitOnBars(bars, parentAboveMa());
        Assert.assertNotNull(hit);
        Assert.assertEquals(10.4, hit.getBreakLine(), 1e-6);
        Assert.assertEquals(Long.valueOf(3L), hit.getCrossBar().getOpenTime());
    }

    @Test
    public void goldenYin_usesNearestCompleteBandBefore() {
        List<LongCandlestickMA> bars = Arrays.asList(
                ohlc(1L, 10.0, 10.4, 9.9, 10.2, 9.5, 10.0),
                ohlc(2L, 10.2, 10.3, 10.0, 10.05, 9.8, 10.0),
                ohlc(3L, 10.0, 10.2, 9.8, 9.9, 10.2, 10.0),
                ohlc(4L, 9.9, 10.3, 9.7, 10.1, 10.3, 10.1),
                ohlc(5L, 10.1, 10.8, 10.0, 10.5, 10.4, 10.2)
        );
        HourMaCrossBreakTools.Hit hit = HourMaCrossBreakTools.findHitOnBars(bars, parentAboveMa());
        Assert.assertNotNull(hit);
        Assert.assertEquals(10.4, hit.getBreakLine(), 1e-6);
        Assert.assertEquals(Long.valueOf(3L), hit.getCrossBar().getOpenTime());
    }

    @Test
    public void golden_missWhenSignalMa5NotAboveMa10() {
        List<LongCandlestickMA> bars = Arrays.asList(
                ohlc(1L, 10.0, 10.5, 9.9, 10.2, 9.5, 10.0),
                ohlc(2L, 10.2, 10.8, 10.1, 10.5, 10.2, 10.0),
                ohlc(3L, 10.5, 10.6, 10.0, 10.1, 10.3, 10.1),
                ohlc(4L, 10.1, 10.3, 9.8, 10.0, 10.4, 10.2),
                ohlc(5L, 10.0, 11.0, 9.9, 10.9, 10.0, 10.3)
        );
        Assert.assertNull(HourMaCrossBreakTools.findHitOnBars(bars, parentAboveMa()));
    }

    @Test
    public void death_hitsCrossPointEdgeBreak() {
        List<LongCandlestickMA> bars = Arrays.asList(
                flat(1L, 10.2, 10.0, 10.1),
                flat(2L, 10.1, 10.0, 10.05),
                flat(3L, 9.8, 10.0, 9.9),
                flat(4L, 9.9, 9.95, 9.85),
                flat(5L, 10.1, 10.0, 10.05)
        );
        HourMaCrossBreakTools.Hit hit = HourMaCrossBreakTools.findHitOnBars(bars, parentAboveMa());
        Assert.assertNotNull(hit);
        Assert.assertEquals(MaCrossPointCore.CrossKind.DEATH, hit.getCrossKind());
        Assert.assertEquals(10.0, hit.getBreakLine(), 1e-6);
    }

    @Test
    public void missWhenParentMa10BelowMa60() {
        List<LongCandlestickMA> bars = Arrays.asList(
                ohlc(1L, 10.0, 10.5, 9.9, 10.2, 9.5, 10.0),
                ohlc(2L, 10.2, 10.8, 10.1, 10.5, 10.2, 10.0),
                ohlc(3L, 10.5, 10.6, 10.0, 10.1, 10.3, 10.1),
                ohlc(4L, 10.1, 10.3, 9.8, 10.0, 10.4, 10.2),
                ohlc(5L, 10.0, 11.0, 9.9, 10.9, 10.5, 10.3)
        );
        Assert.assertNull(HourMaCrossBreakTools.findHitOnBars(bars, parentAboveMaButNotBull()));
    }

    @Test
    public void missWhenParentNotAboveMa() {
        List<LongCandlestickMA> bars = Arrays.asList(
                ohlc(1L, 10.0, 10.5, 9.9, 10.2, 9.5, 10.0),
                ohlc(2L, 10.2, 10.8, 10.1, 10.5, 10.2, 10.0),
                ohlc(3L, 10.5, 10.6, 10.0, 10.1, 10.3, 10.1),
                ohlc(4L, 10.1, 10.3, 9.8, 10.0, 10.4, 10.2),
                ohlc(5L, 10.0, 11.0, 9.9, 10.9, 10.5, 10.3)
        );
        LongCandlestickMA parent = ohlc(100L, 10.0, 10.2, 9.8, 10.0, 10.5, 10.2);
        Assert.assertNull(HourMaCrossBreakTools.findHitOnBars(bars, parent));
    }

    @Test
    public void passesAboveMa_requiresCloseAboveMaxMa5Ma10() {
        Assert.assertTrue(MaCrossPointCore.passesAboveMa(parentAboveMa()));
        Assert.assertFalse(MaCrossPointCore.passesAboveMa(ohlc(1L, 10.0, 10.2, 9.8, 10.3, 10.5, 10.2)));
    }

    @Test
    public void shortDeathYin_usesContainingBandLow() {
        List<LongCandlestickMA> bars = Arrays.asList(
                ohlc(1L, 10.2, 10.3, 9.9, 10.0, 10.2, 10.0),
                ohlc(2L, 10.0, 10.1, 9.2, 9.7, 9.8, 10.0),
                ohlc(3L, 9.7, 10.0, 9.4, 9.9, 9.7, 9.9),
                ohlc(4L, 9.9, 10.0, 9.3, 9.4, 9.6, 9.9),
                ohlc(5L, 9.4, 9.5, 8.8, 9.0, 9.4, 9.8)
        );
        HourMaCrossBreakTools.Hit hit = HourMaCrossBreakTools.findShortHitOnBars(bars, parentBelowMa());
        Assert.assertNotNull(hit);
        Assert.assertTrue(hit.isShortSide());
        Assert.assertEquals(MaCrossPointCore.CrossKind.DEATH, hit.getCrossKind());
        Assert.assertEquals(9.2, hit.getBreakLine(), 1e-6);
        Assert.assertEquals(Long.valueOf(2L), hit.getCrossBar().getOpenTime());
    }

    @Test
    public void shortDeathYin_unfinishedUsesPrevCompleteBand() {
        List<LongCandlestickMA> bars = Arrays.asList(
                ohlc(1L, 10.2, 10.3, 9.6, 10.0, 10.2, 10.0),
                ohlc(2L, 10.0, 10.3, 9.8, 10.2, 10.1, 10.0),
                ohlc(3L, 10.1, 10.2, 9.7, 10.0, 9.8, 10.0),
                ohlc(4L, 10.0, 10.1, 9.6, 9.7, 9.7, 9.9),
                ohlc(5L, 9.7, 9.8, 9.3, 9.4, 9.5, 9.9)
        );
        HourMaCrossBreakTools.Hit hit = HourMaCrossBreakTools.findShortHitOnBars(bars, parentBelowMa());
        Assert.assertNotNull(hit);
        Assert.assertEquals(9.6, hit.getBreakLine(), 1e-6);
        Assert.assertEquals(Long.valueOf(3L), hit.getCrossBar().getOpenTime());
    }

    @Test
    public void shortDeathYang_usesNearestCompleteBandBefore() {
        List<LongCandlestickMA> bars = Arrays.asList(
                ohlc(1L, 10.2, 10.3, 9.6, 10.0, 10.2, 10.0),
                ohlc(2L, 10.0, 10.3, 9.8, 10.2, 10.1, 10.0),
                ohlc(3L, 10.0, 10.3, 9.9, 10.2, 9.8, 10.0),
                ohlc(4L, 10.2, 10.3, 9.7, 9.8, 9.7, 9.9),
                ohlc(5L, 9.8, 9.9, 9.3, 9.4, 9.5, 9.9)
        );
        HourMaCrossBreakTools.Hit hit = HourMaCrossBreakTools.findShortHitOnBars(bars, parentBelowMa());
        Assert.assertNotNull(hit);
        Assert.assertEquals(9.6, hit.getBreakLine(), 1e-6);
        Assert.assertEquals(Long.valueOf(3L), hit.getCrossBar().getOpenTime());
    }

    @Test
    public void shortDeath_missWhenSignalMa5NotBelowMa10() {
        List<LongCandlestickMA> bars = Arrays.asList(
                ohlc(1L, 10.2, 10.3, 9.9, 10.0, 10.2, 10.0),
                ohlc(2L, 10.0, 10.1, 9.2, 9.7, 9.8, 10.0),
                ohlc(3L, 9.7, 10.0, 9.4, 9.9, 9.7, 9.9),
                ohlc(4L, 9.9, 10.0, 9.3, 9.4, 9.6, 9.9),
                ohlc(5L, 9.4, 9.5, 8.8, 9.0, 10.2, 9.8)
        );
        Assert.assertNull(HourMaCrossBreakTools.findShortHitOnBars(bars, parentBelowMa()));
    }

    @Test
    public void shortGolden_hitsCrossPointEdgeBreakDown() {
        List<LongCandlestickMA> bars = Arrays.asList(
                flat(1L, 9.8, 10.0, 9.9),
                flat(2L, 9.9, 10.0, 9.95),
                flat(3L, 10.2, 10.0, 10.1),
                flat(4L, 10.1, 10.05, 10.15),
                flat(5L, 9.9, 10.0, 9.95)
        );
        HourMaCrossBreakTools.Hit hit = HourMaCrossBreakTools.findShortHitOnBars(bars, parentBelowMa());
        Assert.assertNotNull(hit);
        Assert.assertTrue(hit.isShortSide());
        Assert.assertEquals(MaCrossPointCore.CrossKind.GOLDEN, hit.getCrossKind());
        Assert.assertEquals(10.0, hit.getBreakLine(), 1e-6);
    }

    @Test
    public void short_missWhenParentMa10BelowMa60() {
        List<LongCandlestickMA> bars = Arrays.asList(
                ohlc(1L, 10.2, 10.3, 9.9, 10.0, 10.2, 10.0),
                ohlc(2L, 10.0, 10.1, 9.2, 9.7, 9.8, 10.0),
                ohlc(3L, 9.7, 10.0, 9.4, 9.9, 9.7, 9.9),
                ohlc(4L, 9.9, 10.0, 9.3, 9.4, 9.6, 9.9),
                ohlc(5L, 9.4, 9.5, 8.8, 9.0, 9.4, 9.8)
        );
        Assert.assertNull(HourMaCrossBreakTools.findShortHitOnBars(bars, parentBelowMaButNotBull()));
    }

    @Test
    public void short_missWhenParentNotBelowMa() {
        List<LongCandlestickMA> bars = Arrays.asList(
                ohlc(1L, 10.2, 10.3, 9.9, 10.0, 10.2, 10.0),
                ohlc(2L, 10.0, 10.1, 9.2, 9.7, 9.8, 10.0),
                ohlc(3L, 9.7, 10.0, 9.4, 9.9, 9.7, 9.9),
                ohlc(4L, 9.9, 10.0, 9.3, 9.4, 9.6, 9.9),
                ohlc(5L, 9.4, 9.5, 8.8, 9.0, 9.4, 9.8)
        );
        Assert.assertNull(HourMaCrossBreakTools.findShortHitOnBars(bars, parentAboveMa()));
    }

    @Test
    public void passesBelowMa_requiresCloseBelowMinMa5Ma10() {
        Assert.assertTrue(MaCrossPointCore.passesBelowMa(parentBelowMa()));
        Assert.assertFalse(MaCrossPointCore.passesBelowMa(parentAboveMa()));
    }

    @Test
    public void passesMa10GeMa60_allowsEqual() {
        Assert.assertTrue(MaCrossPointCore.passesMa10GeMa60(parentAboveMa()));
        Assert.assertFalse(MaCrossPointCore.passesMa10GeMa60(parentAboveMaButNotBull()));
        LongCandlestickMA eq = parentAboveMa();
        eq.setMa60(eq.getMa10());
        Assert.assertTrue(MaCrossPointCore.passesMa10GeMa60(eq));
    }

    private static LongCandlestickMA parentAboveMa() {
        return ohlc(99L, 10.0, 11.0, 9.8, 10.8, 10.0, 10.2, 10.0);
    }

    private static LongCandlestickMA parentBelowMa() {
        return ohlc(99L, 10.0, 10.2, 9.0, 9.5, 10.0, 10.2, 10.0);
    }

    private static LongCandlestickMA parentAboveMaButNotBull() {
        return ohlc(99L, 10.0, 11.0, 9.8, 10.8, 10.0, 10.2, 10.5);
    }

    private static LongCandlestickMA parentBelowMaButNotBull() {
        return ohlc(99L, 10.0, 10.2, 9.0, 9.5, 10.0, 10.2, 10.5);
    }

    private static LongCandlestickMA flat(long openTime, double ma5, double ma10, double close) {
        return ohlc(openTime, close, close + 0.2, close - 0.2, close, ma5, ma10);
    }

    private static LongCandlestickMA ohlc(long openTime, double open, double high, double low, double close,
                                          double ma5, double ma10) {
        return ohlc(openTime, open, high, low, close, ma5, ma10, ma10);
    }

    private static LongCandlestickMA ohlc(long openTime, double open, double high, double low, double close,
                                          double ma5, double ma10, double ma60) {
        return LongCandlestickMA.builder()
                .openTime(openTime)
                .open(BigDecimal.valueOf(open))
                .high(BigDecimal.valueOf(high))
                .low(BigDecimal.valueOf(low))
                .close(BigDecimal.valueOf(close))
                .ma5(BigDecimal.valueOf(ma5))
                .ma10(BigDecimal.valueOf(ma10))
                .ma60(BigDecimal.valueOf(ma60))
                .build();
    }
}
