package com.yh.bigdata.tts.spider.strategy.tools.ultragc;

import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.UltraGcBreakoutStrategyParams;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UltraGcBreakoutToolsTest {

    @Test
    public void hitsGoldenCrossRefHighEdgeOnSignalDay() {
        List<Trade> trades = Arrays.asList(
                bar("2026-01-14 10:00", 10.0, 10.1, 10.2, 9.9),
                bar("2026-01-14 10:30", 10.1, 10.2, 10.5, 10.0),
                bar("2026-01-14 11:00", 10.2, 10.0, 10.3, 9.9),
                bar("2026-01-15 10:00", 10.0, 10.4, 10.45, 10.0),
                bar("2026-01-15 10:30", 10.45, 10.8, 10.85, 10.4));
        List<MACDIndicatorUtils.MACDPoint> points = points(trades,
                false, false,
                true, false,
                false, false,
                false, false,
                0.5, false);
        UltraGcBreakoutTools.Hit hit = UltraGcBreakoutTools.findHitOnBars(
                trades, points, UltraGcBreakoutStrategyParams.builder()
                        .prevDays(1)
                        .maxBarsPerDay(8)
                        .gcLookbackBars(10)
                        .signalRisePct(0.01)
                        .build());
        Assert.assertNotNull(hit);
        Assert.assertEquals("2026-01-14 10:30", hit.getReferenceBar().getDay());
        Assert.assertEquals(10.5, hit.getReferenceBar().getHigh(), 1e-6);
        Assert.assertEquals("2026-01-15 10:30", hit.getSignalBar().getDay());
    }

    @Test
    public void rejectsWhenLastMacdNotPositive() {
        List<Trade> trades = Arrays.asList(
                bar("2026-01-14 10:00", 10.0, 10.1, 10.2, 9.9),
                bar("2026-01-14 10:30", 10.1, 10.2, 10.5, 10.0),
                bar("2026-01-15 10:00", 10.0, 10.4, 10.45, 10.0),
                bar("2026-01-15 10:30", 10.45, 10.8, 10.85, 10.4));
        List<MACDIndicatorUtils.MACDPoint> points = points(trades,
                false, false,
                true, false,
                false, false,
                -0.2, false);
        Assert.assertNull(UltraGcBreakoutTools.findHitOnBars(
                trades, points, UltraGcBreakoutStrategyParams.builder().build()));
    }

    @Test
    public void rejectsWhenRiseBelowThreshold() {
        List<Trade> trades = Arrays.asList(
                bar("2026-01-14 10:00", 10.0, 10.1, 10.2, 9.9),
                bar("2026-01-14 10:30", 10.1, 10.2, 10.5, 10.0),
                bar("2026-01-15 10:00", 10.0, 10.4, 10.45, 10.0),
                bar("2026-01-15 10:30", 10.45, 10.51, 10.52, 10.4));
        List<MACDIndicatorUtils.MACDPoint> points = points(trades,
                false, false,
                true, false,
                false, false,
                0.5, false);
        Assert.assertNull(UltraGcBreakoutTools.findHitOnBars(
                trades, points, UltraGcBreakoutStrategyParams.builder()
                        .signalRisePct(0.01)
                        .build()));
    }

    @Test
    public void passesRefHighEdge() {
        Trade prev = bar("2026-01-15 10:00", 10.0, 10.4, 10.45, 10.0);
        Trade signal = bar("2026-01-15 10:30", 10.45, 10.8, 10.85, 10.4);
        Assert.assertTrue(UltraGcBreakoutTools.passesRefHighEdge(signal, prev, 10.5));
        Trade prevBroken = bar("2026-01-15 10:00", 10.0, 10.6, 10.7, 10.0);
        Assert.assertFalse(UltraGcBreakoutTools.passesRefHighEdge(signal, prevBroken, 10.5));
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
