package com.binance.client.strategy.ma4m;

import com.binance.client.model.market.LongCandlestickMA;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class HourMaBull4mToolsTest {

    @Test
    public void findHitOnBars_hitsWhenEdgeBreaksCriticalHigh() {
        List<LongCandlestickMA> bars = Arrays.asList(
                bar(1L, 10.0, 10.2, 9.8, 10.0, 9.0, 9.1, 9.2, 9.3),
                bar(2L, 10.0, 10.5, 9.9, 10.3, 10.4, 10.2, 10.0, 9.8),
                bar(3L, 10.3, 10.4, 10.0, 10.2, 10.3, 10.2, 10.0, 9.8),
                bar(4L, 10.2, 10.8, 10.1, 10.6, 10.5, 10.3, 10.1, 9.9)
        );
        HourMaBull4mTools.Hit hit = HourMaBull4mTools.findHitOnBars(bars);
        Assert.assertNotNull(hit);
        Assert.assertEquals(HourMaBull4mTools.RefKind.CRITICAL, hit.getRefKind());
        Assert.assertEquals(10.5, hit.getBreakLine(), 1e-9);
        Assert.assertEquals(Long.valueOf(2L), hit.getRefBar().getOpenTime());
    }

    @Test
    public void findHitOnBars_missWhenNotBullAlign() {
        List<LongCandlestickMA> bars = Arrays.asList(
                bar(1L, 10.0, 10.2, 9.8, 10.0, 10.0, 9.5, 9.6, 9.7),
                bar(2L, 10.0, 10.5, 9.9, 10.3, 10.0, 9.5, 9.6, 9.7),
                bar(3L, 10.3, 10.4, 10.0, 10.2, 10.0, 9.5, 9.6, 9.7),
                bar(4L, 10.2, 10.8, 10.1, 10.6, 10.0, 9.5, 9.6, 9.7)
        );
        Assert.assertNull(HourMaBull4mTools.findHitOnBars(bars));
    }

    @Test
    public void findCriticalBullAlignIndex_matchesDocExample() {
        List<LongCandlestickMA> bars = Arrays.asList(
                bar(1L, 10, 10, 10, 10, 9.0, 9.1, 9.2, 9.3),
                bar(2L, 10, 10, 10, 10, 10.3, 10.2, 10.1, 10.0),
                bar(3L, 10, 10, 10, 10, 10.4, 10.3, 10.2, 10.1)
        );
        Assert.assertEquals(1, HourMaBull4mTools.findCriticalBullAlignIndex(bars, 2));
    }

    private static LongCandlestickMA bar(long openTime, double open, double high, double low, double close,
                                         double ma7, double ma14, double ma28, double ma42) {
        return LongCandlestickMA.builder()
                .openTime(openTime)
                .open(BigDecimal.valueOf(open))
                .high(BigDecimal.valueOf(high))
                .low(BigDecimal.valueOf(low))
                .close(BigDecimal.valueOf(close))
                .ma7(BigDecimal.valueOf(ma7))
                .ma14(BigDecimal.valueOf(ma14))
                .ma28(BigDecimal.valueOf(ma28))
                .ma42(BigDecimal.valueOf(ma42))
                .build();
    }
}
