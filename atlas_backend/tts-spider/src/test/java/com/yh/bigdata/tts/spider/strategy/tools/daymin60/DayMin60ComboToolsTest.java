package com.yh.bigdata.tts.spider.strategy.tools.daymin60;

import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.DayMin60ComboStrategyParams;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DayMin60ComboToolsTest {

    @Test
    public void passesDayGateNeedsEnoughBarsForMacd() {
        List<Trade> days = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            days.add(bar("2026-01-" + String.format("%02d", (i % 28) + 1),
                    10 + i * 0.01, 10.5 + i * 0.01, 11 + i * 0.01, 9.5 + i * 0.01));
        }
        days.get(days.size() - 1).setClose(12.0);
        days.get(days.size() - 2).setLow(10.0);
        days.get(days.size() - 3).setLow(10.0);

        com.yh.bigdata.tts.common.model.StockBase stock = new com.yh.bigdata.tts.common.model.StockBase();
        stock.setCode("sztest001");
        RealtimeStockCache.dayMap.put(stock.getCode(), new ArrayList<>(days));
        try {
            Assert.assertTrue(DayMin60ComboTools.passesDayGate(stock));
        } finally {
            RealtimeStockCache.dayMap.remove(stock.getCode());
        }
    }

    @Test
    public void passesDayGateFailsWhenMacdInputTooShort() {
        List<Trade> days = Arrays.asList(
                bar("2026-07-15", 2.0, 2.1, 2.2, 1.9),
                bar("2026-07-16", 2.1, 2.0, 2.15, 1.95),
                bar("2026-07-17", 2.05, 2.3, 2.35, 2.0),
                bar("2026-07-18", 2.1, 2.2, 2.25, 2.05),
                bar("2026-07-20", 2.2, 2.4, 2.45, 2.15));
        com.yh.bigdata.tts.common.model.StockBase stock = new com.yh.bigdata.tts.common.model.StockBase();
        stock.setCode("sztest002");
        RealtimeStockCache.dayMap.put(stock.getCode(), new ArrayList<>(days));
        try {
            Assert.assertFalse(DayMin60ComboTools.passesDayGate(stock));
        } finally {
            RealtimeStockCache.dayMap.remove(stock.getCode());
        }
    }

    @Test
    public void passesDayCloseAboveRecentLows() {
        List<Trade> days = Arrays.asList(
                bar("2026-07-15", 2.0, 2.1, 2.2, 1.9),
                bar("2026-07-16", 2.1, 2.0, 2.15, 1.95),
                bar("2026-07-17", 2.05, 2.3, 2.35, 2.0));
        Assert.assertTrue(DayMin60ComboTools.passesDayCloseAboveRecentLows(days));
        days.get(2).setClose(1.96);
        Assert.assertFalse(DayMin60ComboTools.passesDayCloseAboveRecentLows(days));
    }

    @Test
    public void hitsWhenMin60BreaksBandHighInLastDayWithAmplitudeExpand() {
        List<Trade> trades = buildMin60Series();
        List<MACDIndicatorUtils.MACDPoint> points = points(trades,
                false, false,
                false, false,
                false, false,
                false, false,
                true, false,
                false, false,
                false, false,
                false, false,
                false, false,
                0.5, false);
        DayMin60ComboTools.Hit hit = DayMin60ComboTools.findHitOnBars(
                trades, points, DayMin60ComboStrategyParams.builder()
                        .prevDays(0)
                        .maxBarsPerDay(4)
                        .gcLookbackBars(20)
                        .enableSignalRiseGate(false)
                        .build());
        Assert.assertNotNull(hit);
        Assert.assertEquals("d4", hit.getCrossBar().getBar().getDay());
        Assert.assertEquals(13.0, hit.getReferenceBand().getBandHigh(), 1e-6);
        Assert.assertEquals("d6", hit.getSignalBar().getDay());
    }

    @Test
    public void rejectsWhenAmplitudeNotExpand() {
        List<Trade> trades = Arrays.asList(
                bar("d1", 10, 11, 11.5, 10),
                bar("d2", 11, 12, 12.5, 11),
                bar("d3", 12, 11.5, 13, 11),
                bar("d4", 11.5, 11.2, 11.5, 11),
                bar("d5", 11.0, 11.1, 13.5, 11),
                bar("d6", 13.05, 13.08, 13.12, 13.04));
        List<MACDIndicatorUtils.MACDPoint> points = points(trades,
                false, false,
                false, false,
                false, false,
                true, false,
                false, false,
                false, false,
                0.5, false);
        Assert.assertNull(DayMin60ComboTools.findHitOnBars(
                trades, points, DayMin60ComboStrategyParams.builder()
                        .prevDays(0)
                        .maxBarsPerDay(4)
                        .gcLookbackBars(20)
                        .build()));
    }

    @Test
    public void passesAmplitudeExpand() {
        Trade prev = bar("d5", 11.0, 11.1, 13.5, 11);
        Trade sig = bar("d6", 13.05, 13.08, 13.12, 13.04);
        Assert.assertTrue(DayMin60ComboTools.passesAmplitudeExpand(sig, prev));
    }

    private static List<Trade> buildMin60Series() {
        return Arrays.asList(
                bar("d1", 10, 11, 11.5, 10),
                bar("d2", 11, 12, 12.5, 11),
                bar("d3", 12, 11.5, 13, 11),
                bar("d4", 11.5, 11.2, 11.5, 11),
                bar("d5", 11.0, 11.05, 11.05, 11.0),
                bar("d6", 13.05, 13.08, 13.12, 13.04));
    }

    private static Trade bar(String day, double open, double close, double high, double low) {
        Trade t = new Trade();
        t.setDay(day);
        t.setOpen(open);
        t.setClose(close);
        t.setHigh(high);
        t.setLow(low);
        return t;
    }

    private static List<MACDIndicatorUtils.MACDPoint> points(List<Trade> trades, Object... flags) {
        List<MACDIndicatorUtils.MACDPoint> list = new ArrayList<>();
        for (int i = 0; i < trades.size(); i++) {
            Object f0 = flags[i * 2];
            Object f1 = flags[i * 2 + 1];
            if (f0 instanceof Number) {
                list.add(point(trades.get(i), ((Number) f0).doubleValue(), false, false));
            } else {
                list.add(point(trades.get(i), 0, (Boolean) f0, (Boolean) f1));
            }
        }
        return list;
    }

    private static MACDIndicatorUtils.MACDPoint point(Trade trade, double macd,
                                                      boolean redGold, boolean greenGold) {
        Ticker ticker = Ticker.from(trade);
        MACDIndicatorUtils.MACDPoint pt = new MACDIndicatorUtils.MACDPoint(
                ticker, macd, 0, 0, false, false, false);
        if (redGold) {
            pt.setIfGoldCross(true);
            pt.setIfGreenGoldCross(false);
        } else if (greenGold) {
            pt.setIfGoldCross(true);
            pt.setIfGreenGoldCross(true);
        }
        return pt;
    }
}
