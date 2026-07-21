package com.yh.bigdata.tts.spider.strategy.tools.monthwavecc;

import com.yh.bigdata.tts.common.model.Trade;
import org.junit.Assert;
import org.junit.Test;

public class MonthWaveCcBreakoutToolsTest {

    @Test
    public void passesLowRetestBandHighWithinTolerance() {
        Trade bar = bar("2026-01", 12, 13, 10.05, 12.5);
        Assert.assertTrue(MonthWaveCcBreakoutTools.passesLowRetestBandHigh(bar, 10.0, 0.01));
    }

    @Test
    public void tryConvexRetestRequiresYangLastBar() {
        Trade prev = bar("2025-12", 11, 12, 10.0, 11.5);
        Trade signal = bar("2026-01", 11.2, 11.3, 10.05, 11.0);
        Assert.assertNull(MonthWaveCcBreakoutTools.tryConvexRetest(null, null, signal, prev, 0.01));
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
