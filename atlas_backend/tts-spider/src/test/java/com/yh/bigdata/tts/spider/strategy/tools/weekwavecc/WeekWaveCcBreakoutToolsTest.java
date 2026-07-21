package com.yh.bigdata.tts.spider.strategy.tools.weekwavecc;

import com.yh.bigdata.tts.common.model.Trade;
import org.junit.Assert;
import org.junit.Test;

public class WeekWaveCcBreakoutToolsTest {

    @Test
    public void passesLowRetestBandHighWithinTolerance() {
        Trade bar = bar("2026-W01", 12, 13, 10.05, 12.5);
        Assert.assertTrue(WeekWaveCcBreakoutTools.passesLowRetestBandHigh(bar, 10.0, 0.01));
    }

    @Test
    public void isYangBarWhenCloseNotBelowOpen() {
        Trade yang = bar("2026-W02", 10, 11, 9.8, 10.5);
        Trade flat = bar("2026-W03", 10, 10.2, 9.9, 10);
        Assert.assertTrue(WeekWaveCcBreakoutTools.isYangBar(yang));
        Assert.assertTrue(WeekWaveCcBreakoutTools.isYangBar(flat));
    }

    @Test
    public void tryConvexRetestRequiresYangLastBar() {
        Trade prev = bar("2026-W01", 11, 12, 10.0, 11.5);
        Trade signal = bar("2026-W02", 11.2, 11.3, 10.05, 11.0);
        Assert.assertNull(WeekWaveCcBreakoutTools.tryConvexRetest(null, null, signal, prev, 0.01));
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
