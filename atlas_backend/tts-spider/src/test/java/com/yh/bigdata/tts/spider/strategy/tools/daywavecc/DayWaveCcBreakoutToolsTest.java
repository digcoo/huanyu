//package com.yh.bigdata.tts.spider.strategy.tools.daywavecc;
//
//import com.yh.bigdata.tts.common.model.Trade;
//import com.yh.bigdata.tts.common.param.DayWaveCcBreakoutStrategyParams;
//import org.junit.Assert;
//import org.junit.Test;
//
//public class DayWaveCcBreakoutToolsTest {
//
//    @Test
//    public void barAmplitudeRateUsesMaxOfRangeAndGap() {
//        Trade prev = bar("2026-01-01", 10, 10.2, 9.9, 10);
//        Trade signal = bar("2026-01-02", 10.5, 10.8, 10.4, 10.6);
//        double rate = DayWaveCcBreakoutTools.barAmplitudeRate(signal, prev);
//        double rangeRate = (10.8 - 10.4) / 10.4;
//        double gapRate = (10.5 - 10) / 10;
//        Assert.assertEquals(Math.max(rangeRate, gapRate), rate, 1e-9);
//    }
//
//    @Test
//    public void passesAmplitudeExpandWhenSignalRateHigher() {
//        Trade prevPrev = bar("2026-01-01", 9.8, 10, 9.7, 9.9);
//        Trade prev = bar("2026-01-02", 10, 10.1, 9.95, 10);
//        Trade signal = bar("2026-01-03", 10.2, 10.9, 10.1, 10.8);
//        Assert.assertTrue(DayWaveCcBreakoutTools.passesAmplitudeExpand(signal, prev, prevPrev));
//    }
//
//    @Test
//    public void resolveHitOnBarsRequiresLastDaySignal() {
//        Trade b1 = bar("2026-01-01", 10, 10.2, 9.9, 10);
//        Trade b2 = bar("2026-01-02", 10, 10.15, 9.98, 10.05);
//        java.util.List<Trade> bars = java.util.Arrays.asList(b1, b2);
//        DayWaveCcBreakoutStrategyParams params = DayWaveCcBreakoutStrategyParams.builder()
//                .lookbackBars(20)
//                .build();
//        Assert.assertNull(DayWaveCcBreakoutTools.resolveHitOnBars(bars, params));
//    }
//
//    private static Trade bar(String day, double open, double high, double low, double close) {
//        Trade t = new Trade();
//        t.setDay(day);
//        t.setOpen(open);
//        t.setHigh(high);
//        t.setLow(low);
//        t.setClose(close);
//        return t;
//    }
//}
