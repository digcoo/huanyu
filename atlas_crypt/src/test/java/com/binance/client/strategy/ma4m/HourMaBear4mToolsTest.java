package com.binance.client.strategy.ma4m;

import com.binance.client.model.market.LongCandlestickMA;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class HourMaBear4mToolsTest {

    @Test
    public void findHitOnBars_hitsWhenEdgeBreaksCriticalLow() {
        List<LongCandlestickMA> bars = Arrays.asList(
                bar(1L, 10.0, 10.2, 9.8, 10.0, 10.5, 10.4, 10.3, 10.2),
                bar(2L, 10.0, 10.1, 9.5, 9.7, 9.6, 9.8, 10.0, 10.2),
                bar(3L, 9.7, 9.8, 9.4, 9.6, 9.5, 9.7, 9.9, 10.1),
                bar(4L, 9.6, 9.7, 9.0, 9.2, 9.4, 9.6, 9.8, 10.0)
        );
        HourMaBear4mTools.Hit hit = HourMaBear4mTools.findHitOnBars(bars);
        Assert.assertNotNull(hit);
        Assert.assertEquals(HourMaBear4mTools.RefKind.CRITICAL, hit.getRefKind());
        Assert.assertEquals(9.5, hit.getBreakLine(), 1e-9);
        Assert.assertEquals(Long.valueOf(2L), hit.getRefBar().getOpenTime());
    }

    @Test
    public void findHitOnBars_missWhenNotBearAlign() {
        List<LongCandlestickMA> bars = Arrays.asList(
                bar(1L, 10.0, 10.2, 9.8, 10.0, 10.5, 10.4, 10.3, 10.2),
                bar(2L, 10.0, 10.1, 9.5, 9.7, 10.5, 10.4, 10.3, 10.2),
                bar(3L, 9.7, 9.8, 9.4, 9.6, 10.5, 10.4, 10.3, 10.2),
                bar(4L, 9.6, 9.7, 9.0, 9.2, 10.5, 10.4, 10.3, 10.2)
        );
        Assert.assertNull(HourMaBear4mTools.findHitOnBars(bars));
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
