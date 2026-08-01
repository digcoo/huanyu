package com.yh.bigdata.tts.spider.strategy.tools.trendretestlow;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TrendRetestLowToolsTest {

    @Test
    public void findHitOnBars_retestLowAndLastYangCloseAboveBandLow() {
        List<Trade> bars = Arrays.asList(
                yang("2026-01-01", 10.0, 10.5, 9.8, 10.4),
                yang("2026-01-02", 10.4, 10.8, 9.5, 10.7),
                yin("2026-01-03", 10.7, 10.4, 10.9, 10.3),
                yin("2026-01-04", 10.3, 10.2, 10.4, 10.1),
                yang("2026-01-05", 10.1, 10.3, 9.4, 10.2)
        );
        TrendRetestLowTools.Hit hit = TrendRetestLowTools.findHitOnBars(bars, PeriodTypeEnum.DAY, 20);
        Assert.assertNotNull(hit);
        Assert.assertEquals(9.5, hit.getBandLow(), 1e-6);
        Assert.assertEquals("2026-01-05", hit.getSignalBar().getDay());
    }

    @Test
    public void findHitOnBars_missWhenCloseAboveBandHigh() {
        List<Trade> bars = Arrays.asList(
                yang("2026-01-01", 10.0, 10.5, 9.8, 10.4),
                yang("2026-01-02", 10.4, 10.8, 9.5, 10.7),
                yin("2026-01-03", 10.7, 10.4, 10.9, 10.3),
                yin("2026-01-04", 10.3, 10.2, 10.4, 10.1),
                yang("2026-01-05", 10.1, 11.0, 9.4, 10.95)
        );
        Assert.assertNull(TrendRetestLowTools.findHitOnBars(bars, PeriodTypeEnum.DAY, 20));
    }

    @Test
    public void findHitOnBars_missWhenNoRetest() {
        List<Trade> bars = Arrays.asList(
                yang("2026-01-01", 10.0, 10.5, 9.8, 10.4),
                yin("2026-01-02", 10.4, 10.2, 10.5, 10.1),
                yang("2026-01-03", 10.1, 10.4, 9.81, 10.3),
                yang("2026-01-04", 10.3, 10.5, 9.82, 10.4)
        );
        Assert.assertNull(TrendRetestLowTools.findHitOnBars(bars, PeriodTypeEnum.DAY, 20));
    }

    private static Trade yang(String day, double open, double high, double low, double close) {
        Trade t = new Trade();
        t.setDay(day);
        t.setOpen(open);
        t.setHigh(high);
        t.setLow(low);
        t.setClose(close);
        return t;
    }

    private static Trade yin(String day, double open, double high, double low, double close) {
        return yang(day, open, high, low, close);
    }
}
