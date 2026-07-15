package com.yh.bigdata.tts.spider.strategy.tools.macddcb;

import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MacdDcBreakoutStrategyParams;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MacdDcBreakoutToolsTest {

    @Test
    public void hitsDeathCrossPreBarHighEdgeBreakout() {
        List<Trade> trades = Arrays.asList(
                bar("d0", 10, 10.1, 10.2, 9.9),
                bar("d1", 10.1, 10.2, 10.5, 10.0),
                bar("d2", 10.2, 10.0, 10.3, 9.9),
                bar("d3", 10.0, 9.9, 10.1, 9.8),
                bar("d4", 9.9, 10.0, 10.2, 9.9),
                bar("d5", 10.0, 10.4, 10.45, 10.0),
                bar("d6", 10.45, 10.8, 10.85, 10.4));
        List<MACDIndicatorUtils.MACDPoint> points = points(trades,
                false, false,
                false, false,
                false, true,
                false, false,
                false, false,
                false, false,
                -0.5, false);
        MacdDcBreakoutTools.TierHit hit = MacdDcBreakoutTools.resolveHitOnBars(
                trades, points, MacdDcBreakoutStrategyParams.builder()
                        .enableMinAmountFilter(false)
                        .enableSignalRiseGate(false)
                        .build());
        Assert.assertNotNull(hit);
        Assert.assertEquals(10.5, hit.getRefHigh(), 1e-6);
        Assert.assertEquals("d1", hit.getReferenceBar().getDay());
    }

    @Test
    public void rejectsWhenMacdPositive() {
        List<Trade> trades = Arrays.asList(
                bar("d0", 10, 10.1, 10.2, 9.9),
                bar("d1", 10.1, 10.2, 10.5, 10.0),
                bar("d2", 10.2, 10.0, 10.3, 9.9),
                bar("d3", 10.0, 9.9, 10.1, 9.8),
                bar("d4", 9.9, 10.0, 10.2, 9.9),
                bar("d5", 10.0, 10.4, 10.45, 10.0),
                bar("d6", 10.45, 10.8, 10.85, 10.4));
        List<MACDIndicatorUtils.MACDPoint> points = points(trades,
                false, false,
                false, false,
                false, true,
                false, false,
                false, false,
                false, false,
                0.5, false);
        Assert.assertNull(MacdDcBreakoutTools.resolveHitOnBars(
                trades, points, MacdDcBreakoutStrategyParams.builder().build()));
    }

    @Test
    public void rejectsWhenLatestCrossIsGolden() {
        List<Trade> trades = Arrays.asList(
                bar("d0", 10, 10.1, 10.2, 9.9),
                bar("d1", 10.1, 10.2, 10.5, 10.0),
                bar("d2", 10.2, 10.0, 10.3, 9.9),
                bar("d3", 10.0, 9.9, 10.1, 9.8),
                bar("d4", 9.9, 10.0, 10.2, 9.9),
                bar("d5", 10.0, 10.4, 10.45, 10.0),
                bar("d6", 10.45, 10.8, 10.85, 10.4));
        List<MACDIndicatorUtils.MACDPoint> points = points(trades,
                false, false,
                false, false,
                true, false,
                false, false,
                false, false,
                false, false,
                -0.5, false);
        Assert.assertNull(MacdDcBreakoutTools.resolveHitOnBars(
                trades, points, MacdDcBreakoutStrategyParams.builder().build()));
    }

    @Test
    public void passesRefHighEdge() {
        Trade prev = bar("d1", 10, 10.4, 10.45, 10);
        Trade signal = bar("d2", 10.5, 10.8, 10.85, 10.5);
        Assert.assertTrue(MacdDcBreakoutTools.passesRefHighEdge(signal, prev, 10.5));
        Trade prevBroken = bar("d1", 10, 10.6, 10.7, 10);
        Assert.assertFalse(MacdDcBreakoutTools.passesRefHighEdge(signal, prevBroken, 10.5));
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
