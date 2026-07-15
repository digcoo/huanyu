package com.yh.bigdata.tts.spider.strategy.tools.macdgcwhr;

import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MacdGcWaveHighRetestStrategyParams;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MacdGcWaveHighRetestToolsTest {

    @Test
    public void hitsWhenAboveBandHighWithTightBar() {
        List<Trade> trades = Arrays.asList(
                bar("d1", 10, 11, 11.5, 10),
                bar("d2", 11, 12, 12.5, 11),
                bar("d3", 12, 11.5, 13, 11),
                bar("d4", 11.5, 11.2, 11.5, 11),
                bar("d5", 13.05, 13.08, 13.12, 13.04));
        List<MACDIndicatorUtils.MACDPoint> points = points(trades,
                false, false,
                false, false,
                false, false,
                true, false,
                false, false,
                0.5, false);
        MacdGcWaveHighRetestStrategyParams p = MacdGcWaveHighRetestStrategyParams.builder().build();
        MacdGcWaveHighRetestTools.TierHit hit = MacdGcWaveHighRetestTools.resolveHitOnBars(trades, points, p);
        Assert.assertNotNull(hit);
        Assert.assertEquals("d4", hit.getCrossBar().getBar().getDay());
        Assert.assertEquals(13.0, hit.getReferenceBand().getBandHigh(), 1e-6);
    }

    @Test
    public void rejectsWhenBarRangeTooWide() {
        List<Trade> trades = Arrays.asList(
                bar("d1", 10, 11, 11.5, 10),
                bar("d2", 11, 12, 12.5, 11),
                bar("d3", 12, 11.5, 13, 11),
                bar("d4", 11.5, 11.2, 11.5, 11),
                bar("d5", 13.0, 13.2, 13.5, 13.0));
        List<MACDIndicatorUtils.MACDPoint> points = points(trades,
                false, false,
                false, false,
                false, false,
                true, false,
                false, false,
                0.5, false);
        Assert.assertNull(MacdGcWaveHighRetestTools.resolveHitOnBars(
                trades, points, MacdGcWaveHighRetestStrategyParams.builder().build()));
    }

    @Test
    public void rejectsWhenCloseBelowBandHigh() {
        List<Trade> trades = Arrays.asList(
                bar("d1", 10, 11, 11.5, 10),
                bar("d2", 11, 12, 12.5, 11),
                bar("d3", 12, 11.5, 13, 11),
                bar("d4", 11.5, 11.2, 11.5, 11),
                bar("d5", 12.9, 12.95, 12.99, 12.9));
        List<MACDIndicatorUtils.MACDPoint> points = points(trades,
                false, false,
                false, false,
                false, false,
                true, false,
                false, false,
                0.5, false);
        Assert.assertNull(MacdGcWaveHighRetestTools.resolveHitOnBars(
                trades, points, MacdGcWaveHighRetestStrategyParams.builder().build()));
    }

    @Test
    public void rejectsWhenMacdNotPositive() {
        List<Trade> trades = Arrays.asList(
                bar("d1", 10, 11, 11.5, 10),
                bar("d2", 11, 12, 12.5, 11),
                bar("d3", 12, 11.5, 13, 11),
                bar("d4", 11.5, 11.2, 11.5, 11),
                bar("d5", 13.05, 13.08, 13.12, 13.04));
        List<MACDIndicatorUtils.MACDPoint> points = points(trades,
                false, false,
                false, false,
                false, false,
                true, false,
                false, false,
                -0.1, false);
        Assert.assertNull(MacdGcWaveHighRetestTools.resolveHitOnBars(
                trades, points, MacdGcWaveHighRetestStrategyParams.builder().build()));
    }

    @Test
    public void passesTightBarRangeAtOnePercent() {
        Trade bar = bar("d5", 10, 10.05, 10.099, 10);
        Assert.assertTrue(MacdGcWaveHighRetestTools.passesTightBarRange(bar, 0.01));
        Trade wide = bar("d6", 10, 10.2, 10.3, 10);
        Assert.assertFalse(MacdGcWaveHighRetestTools.passesTightBarRange(wide, 0.01));
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
