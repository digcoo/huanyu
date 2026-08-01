package com.yh.bigdata.tts.spider.strategy.tools.trendretesthigh;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TrendRetestHighToolsTest {

    @Test
    public void findHitOnBars_retestAndFirstBreakBandHigh() {
        List<Trade> bars = Arrays.asList(
                yang("2026-01-01", 10.0, 11.0, 9.5, 10.8),
                yin("2026-01-02", 10.8, 10.5, 11.0, 10.4),
                yin("2026-01-03", 10.4, 10.3, 10.5, 10.2),
                yang("2026-01-04", 10.2, 10.4, 9.4, 10.3),
                yang("2026-01-05", 10.3, 11.2, 10.2, 11.1)
        );
        TrendRetestHighTools.Hit hit = TrendRetestHighTools.findHitOnBars(bars, PeriodTypeEnum.DAY, 20);
        Assert.assertNotNull(hit);
        Assert.assertEquals(11.0, hit.getBandHigh(), 1e-6);
        Assert.assertEquals("2026-01-05", hit.getSignalBar().getDay());
    }

    @Test
    public void findHitOnBars_missWhenHighBrokenBeforeLastBar() {
        List<Trade> bars = Arrays.asList(
                yang("2026-01-01", 10.0, 11.0, 9.5, 10.8),
                yin("2026-01-02", 10.8, 10.5, 11.0, 10.4),
                yang("2026-01-03", 10.2, 11.5, 9.4, 11.2),
                yang("2026-01-04", 11.2, 11.3, 11.0, 11.25)
        );
        Assert.assertNull(TrendRetestHighTools.findHitOnBars(bars, PeriodTypeEnum.DAY, 20));
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
