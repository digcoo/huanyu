package com.yh.bigdata.tts.spider.strategy.tools.wavecc;

import com.yh.bigdata.tts.common.model.Trade;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class WaveCcMin60BreakoutCoreTest {

    @Test
    public void findHitOnBars_currentBarOnly_checksLastBar() {
        List<Trade> bars = buildBreakoutSeries(false);
        Assert.assertNotNull(WaveCcMin60BreakoutCore.findHitOnBars(bars, 0, 8, 20, true));
        bars.get(bars.size() - 1).setClose(10.2);
        Assert.assertNull(WaveCcMin60BreakoutCore.findHitOnBars(bars, 0, 8, 20, true));
    }

    @Test
    public void findHitOnBars_intradayAny_findsEarlierBreakoutWhenLastBarMisses() {
        List<Trade> bars = buildBreakoutSeries(true);
        WaveCcMin60BreakoutCore.Hit hit = WaveCcMin60BreakoutCore.findHitOnBars(bars, 0, 8, 20, false);
        Assert.assertNotNull(hit);
        Assert.assertEquals("2026-01-02 11:30:00", hit.getSignalBar().getDay());
    }

    private static List<Trade> buildBreakoutSeries(boolean lastBarMisses) {
        List<Trade> bars = new ArrayList<>();
        bars.add(trade("2026-01-02 10:30:00", 10.0, 10.2, 9.9, 10.1));
        bars.add(trade("2026-01-02 11:30:00", 10.1, 11.5, 10.0, 11.2));
        if (lastBarMisses) {
            bars.add(trade("2026-01-02 14:00:00", 11.2, 11.3, 11.0, 11.15));
        } else {
            bars.add(trade("2026-01-02 14:00:00", 11.2, 12.0, 11.1, 11.9));
        }
        return bars;
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
