package com.yh.bigdata.tts.spider.strategy.tools.min60wavecc;

import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.Min60WaveCcBreakoutStrategyParams;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class Min60WaveCcBreakoutToolsTest {

    @Test
    public void barAmplitudeRateUsesMaxOfRangeAndGap() {
        Trade prev = bar("2026-01-02 10:00", 10, 10.2, 9.9, 10);
        Trade signal = bar("2026-01-02 11:00", 10.5, 10.8, 10.4, 10.6);
        double rate = Min60WaveCcBreakoutTools.barAmplitudeRate(signal, prev);
        double rangeRate = (10.8 - 10.4) / 10.4;
        double gapRate = (10.5 - 10) / 10;
        Assert.assertEquals(Math.max(rangeRate, gapRate), rate, 1e-9);
    }

    @Test
    public void passesAmplitudeExpandWhenSignalRateHigher() {
        Trade prevPrev = bar("2026-01-02 09:00", 9.8, 10, 9.7, 9.9);
        Trade prev = bar("2026-01-02 10:00", 10, 10.1, 9.95, 10);
        Trade signal = bar("2026-01-02 11:00", 10.2, 10.9, 10.1, 10.8);
        Assert.assertTrue(Min60WaveCcBreakoutTools.passesAmplitudeExpand(signal, prev, prevPrev));
    }

    @Test
    public void passesBandHighEdge() {
        Trade prev = bar("2026-01-02 10:00", 10, 10.4, 10.45, 10);
        Trade signal = bar("2026-01-02 11:00", 10.5, 10.8, 10.85, 10.55);
        Assert.assertTrue(Min60WaveCcBreakoutTools.passesBandHighEdge(prev, signal, 10.5));
    }

    @Test
    public void findHitOnBarsScansOnlyLastDaySignalBucket() {
        List<Trade> bars = new ArrayList<>();
        bars.add(bar("2026-01-01 10:00", 10, 10.2, 9.9, 10));
        bars.add(bar("2026-01-01 11:00", 10, 10.2, 9.95, 10.05));
        bars.add(bar("2026-01-02 10:00", 10.1, 10.3, 10.05, 10.2));
        bars.add(bar("2026-01-02 11:00", 10.2, 10.4, 10.15, 10.3));
        Min60WaveCcBreakoutStrategyParams params = Min60WaveCcBreakoutStrategyParams.builder()
                .prevDays(1)
                .maxBarsPerDay(4)
                .lookbackBars(20)
                .build();
        Assert.assertNull(Min60WaveCcBreakoutTools.findHitOnBars(bars, params));
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
