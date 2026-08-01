package com.yh.bigdata.tts.spider.strategy.tools.trendlift;

import com.yh.bigdata.tts.common.model.Trade;
import org.junit.Assert;
import org.junit.Test;

public class TrendLiftToolsTest {

    @Test
    public void passesDayCloseAbovePrevLow_requiresCloseAbovePrevLow() {
        Trade prev = trade("2026-01-01", 10.0, 10.5, 9.8, 10.2);
        Trade signal = trade("2026-01-02", 10.2, 10.8, 10.0, 10.6);
        Assert.assertTrue(TrendLiftTools.passesDayCloseAbovePrevLow(signal, prev));

        signal.setClose(9.7);
        Assert.assertFalse(TrendLiftTools.passesDayCloseAbovePrevLow(signal, prev));
    }

    private static Trade trade(String day, double open, double high, double low, double close) {
        Trade t = new Trade();
        t.setDay(day);
        t.setOpen(open);
        t.setHigh(high);
        t.setLow(low);
        t.setClose(close);
        return t;
    }
}
