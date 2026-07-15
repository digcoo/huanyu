package com.yh.bigdata.tts.spider.strategy.tools.macdgcwh;

import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MacdGcWaveHighStrategyParams;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MacdGcWaveHighToolsTest {

    @Test
    public void rejectsWhenLastCrossIsDeath() {
        List<Trade> trades = Arrays.asList(
                bar("d1", 10, 9, 10, 9),
                bar("d2", 9, 9, 10, 9),
                bar("d3", 9, 9, 10, 9),
                bar("d4", 9, 9, 10, 9),
                bar("d5", 12.9, 12.9, 13, 12.5),
                bar("d6", 13.1, 13.1, 13.2, 13));
        List<MACDIndicatorUtils.MACDPoint> points = points(
                trades,
                false, false,
                true, false,
                false, true,
                false, false,
                false, false,
                false, false);
        MacdGcWaveHighStrategyParams p = MacdGcWaveHighStrategyParams.builder().build();
        Assert.assertNull(MacdGcWaveHighTools.resolveHitOnBars(trades, points, p));
    }

    @Test
    public void rejectsWhenSignalBarIsGoldenCross() {
        List<Trade> trades = Arrays.asList(
                bar("d1", 10, 11, 11.5, 10),
                bar("d2", 11, 12, 12.5, 11),
                bar("d3", 12, 11.5, 13, 11),
                bar("d4", 11.5, 11.2, 11.5, 11),
                bar("d5", 12.9, 12.9, 13, 12.5),
                bar("d6", 13.1, 13.1, 13.2, 13));
        List<MACDIndicatorUtils.MACDPoint> points = points(
                trades,
                false, false,
                false, false,
                false, false,
                false, false,
                false, false,
                false, false,
                true, false);
        MacdGcWaveHighStrategyParams p = MacdGcWaveHighStrategyParams.builder().build();
        Assert.assertNull(MacdGcWaveHighTools.resolveHitOnBars(trades, points, p));
    }

    @Test
    public void rejectsWhenYangGoldenCrossHasNoCompleteBand() {
        List<Trade> trades = Arrays.asList(
                bar("d1", 10, 11, 11.5, 10),
                bar("d2", 11, 12, 12.5, 11),
                bar("d3", 12, 12.9, 13, 11.5),
                bar("d4", 13.1, 13.1, 13.2, 13));
        List<MACDIndicatorUtils.MACDPoint> points = points(
                trades,
                false, false,
                true, false,
                false, false,
                false, false);
        MacdGcWaveHighStrategyParams p = MacdGcWaveHighStrategyParams.builder().build();
        Assert.assertNull(MacdGcWaveHighTools.resolveHitOnBars(trades, points, p));
    }

    @Test
    public void hitsWhenYinGoldenCrossBreaksPreviousBandHigh() {
        List<Trade> trades = Arrays.asList(
                bar("d1", 10, 11, 11.5, 10),
                bar("d2", 11, 12, 12.5, 11),
                bar("d3", 12, 11.5, 13, 11),
                bar("d4", 11.5, 11.2, 11.5, 11),
                bar("d5", 12.9, 12.9, 13, 12.5),
                bar("d6", 13.1, 13.1, 13.2, 13));
        List<MACDIndicatorUtils.MACDPoint> points = points(
                trades,
                false, false,
                false, false,
                false, false,
                true, false,
                false, false,
                false, false);
        MacdGcWaveHighStrategyParams p = MacdGcWaveHighStrategyParams.builder().build();
        MacdGcWaveHighTools.TierHit hit = MacdGcWaveHighTools.resolveHitOnBars(trades, points, p);
        Assert.assertNotNull(hit);
        Assert.assertEquals("d4", hit.getCrossBar().getBar().getDay());
        Assert.assertEquals(13.0, hit.getReferenceBand().getBandHigh(), 1e-6);
        Assert.assertEquals("d6", hit.getSignalBar().getDay());
    }

    @Test
    public void hitsWhenYangGoldenCrossInCompleteBandBreaksBandHigh() {
        List<Trade> trades = Arrays.asList(
                bar("d1", 10, 11, 11.5, 10),
                bar("d2", 11, 12, 12.5, 11),
                bar("d3", 12, 11.5, 13, 11),
                bar("d4", 11.2, 11.5, 11.8, 11),
                bar("d5", 12.9, 12.9, 13, 12.5),
                bar("d6", 13.1, 13.1, 13.2, 13));
        List<MACDIndicatorUtils.MACDPoint> points = points(
                trades,
                false, false,
                true, false,
                false, false,
                false, false,
                false, false,
                false, false);
        MacdGcWaveHighStrategyParams p = MacdGcWaveHighStrategyParams.builder().build();
        MacdGcWaveHighTools.TierHit hit = MacdGcWaveHighTools.resolveHitOnBars(trades, points, p);
        Assert.assertNotNull(hit);
        Assert.assertEquals("d2", hit.getCrossBar().getBar().getDay());
        Assert.assertEquals(13.0, hit.getReferenceBand().getBandHigh(), 1e-6);
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

    private static List<MACDIndicatorUtils.MACDPoint> points(List<Trade> trades, boolean... flags) {
        List<MACDIndicatorUtils.MACDPoint> list = new ArrayList<>();
        for (int i = 0; i < trades.size(); i++) {
            boolean redGold = flags[i * 2];
            boolean greenGold = flags[i * 2 + 1];
            list.add(point(trades.get(i), redGold, greenGold));
        }
        return list;
    }

    private static MACDIndicatorUtils.MACDPoint point(Trade trade, boolean redGold, boolean greenGold) {
        Ticker ticker = Ticker.from(trade);
        MACDIndicatorUtils.MACDPoint pt = new MACDIndicatorUtils.MACDPoint(
                ticker, 0, 0, 0, false, false, false);
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
