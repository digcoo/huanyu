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

    private static LongCandlestickMA parentAboveMa() {
        return ohlc(99L, 10.0, 11.0, 9.8, 10.8, 10.0, 10.2);
    }

    private static LongCandlestickMA flat(long openTime, double ma5, double ma10, double close) {
        return ohlc(openTime, close, close + 0.2, close - 0.2, close, ma5, ma10);
    }

    private static LongCandlestickMA ohlc(long openTime, double open, double high, double low, double close,
                                          double ma5, double ma10) {
        return LongCandlestickMA.builder()
                .openTime(openTime)
                .open(BigDecimal.valueOf(open))
                .high(BigDecimal.valueOf(high))
                .low(BigDecimal.valueOf(low))
                .close(BigDecimal.valueOf(close))
                .ma5(BigDecimal.valueOf(ma5))
                .ma10(BigDecimal.valueOf(ma10))
                .build();
    }
}
