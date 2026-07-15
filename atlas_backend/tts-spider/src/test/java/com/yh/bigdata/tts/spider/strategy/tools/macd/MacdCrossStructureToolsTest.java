package com.yh.bigdata.tts.spider.strategy.tools.macd;

import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.model.Trade;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class MacdCrossStructureToolsTest {

    @Test
    public void goldenOnly_findsEarlierGoldenWhenRecentCrossIsDeath() {
        List<Trade> trades = bars("d1", "d2", "d3", "d4");
        List<MACDIndicatorUtils.MACDPoint> points = new ArrayList<>();
        points.add(point(trades.get(0), false, false));
        points.add(point(trades.get(1), true, false));
        points.add(point(trades.get(2), false, true));
        points.add(point(trades.get(3), false, false));

        MacdCrossStructureTools.CrossBar hit = MacdCrossStructureTools.findLatestCrossBar(
                trades, points, 10, true, false);

        Assert.assertNotNull(hit);
        Assert.assertEquals(MacdCrossStructureTools.CrossKind.GOLDEN, hit.getKind());
        Assert.assertEquals("d2", hit.getBar().getDay());
    }

    @Test
    public void goldenOnly_acceptsWhenLastCrossIsGolden() {
        List<Trade> trades = bars("d1", "d2", "d3", "d4");
        List<MACDIndicatorUtils.MACDPoint> points = new ArrayList<>();
        points.add(point(trades.get(0), false, false));
        points.add(point(trades.get(1), false, true));
        points.add(point(trades.get(2), true, false));
        points.add(point(trades.get(3), false, false));

        MacdCrossStructureTools.CrossBar hit = MacdCrossStructureTools.findLatestCrossBar(
                trades, points, 10, true, false);

        Assert.assertNotNull(hit);
        Assert.assertEquals(MacdCrossStructureTools.CrossKind.GOLDEN, hit.getKind());
        Assert.assertEquals("d3", hit.getBar().getDay());
    }

    @Test
    public void deathOnly_acceptsWhenLastCrossIsDeath() {
        List<Trade> trades = bars("d1", "d2", "d3", "d4");
        List<MACDIndicatorUtils.MACDPoint> points = new ArrayList<>();
        points.add(point(trades.get(0), true, false));
        points.add(point(trades.get(1), false, false));
        points.add(point(trades.get(2), false, true));
        points.add(point(trades.get(3), false, false));

        MacdCrossStructureTools.CrossBar hit = MacdCrossStructureTools.findLatestCrossBar(
                trades, points, 10, false, true);

        Assert.assertNotNull(hit);
        Assert.assertEquals(MacdCrossStructureTools.CrossKind.DEATH, hit.getKind());
        Assert.assertEquals("d3", hit.getBar().getDay());
    }

    @Test
    public void deathOnly_findsEarlierDeathWhenRecentCrossIsGolden() {
        List<Trade> trades = bars("d1", "d2", "d3", "d4");
        List<MACDIndicatorUtils.MACDPoint> points = new ArrayList<>();
        points.add(point(trades.get(0), false, false));
        points.add(point(trades.get(1), false, true));
        points.add(point(trades.get(2), true, false));
        points.add(point(trades.get(3), false, false));

        MacdCrossStructureTools.CrossBar hit = MacdCrossStructureTools.findLatestCrossBar(
                trades, points, 10, false, true);

        Assert.assertNotNull(hit);
        Assert.assertEquals(MacdCrossStructureTools.CrossKind.DEATH, hit.getKind());
        Assert.assertEquals("d2", hit.getBar().getDay());
    }

    @Test
    public void bothEnabled_usesLastCrossRegardlessOfKind() {
        List<Trade> trades = bars("d1", "d2", "d3", "d4");
        List<MACDIndicatorUtils.MACDPoint> points = new ArrayList<>();
        points.add(point(trades.get(0), true, false));
        points.add(point(trades.get(1), false, false));
        points.add(point(trades.get(2), false, true));
        points.add(point(trades.get(3), false, false));

        MacdCrossStructureTools.CrossBar hit = MacdCrossStructureTools.findLatestCrossBar(
                trades, points, 10, true, true);

        Assert.assertNotNull(hit);
        Assert.assertEquals(MacdCrossStructureTools.CrossKind.DEATH, hit.getKind());
        Assert.assertEquals("d3", hit.getBar().getDay());
    }

    @Test
    public void skipsCurrentBarWhenSearchingLastCross() {
        List<Trade> trades = bars("d1", "d2", "d3");
        List<MACDIndicatorUtils.MACDPoint> points = new ArrayList<>();
        points.add(point(trades.get(0), false, false));
        points.add(point(trades.get(1), true, false));
        points.add(point(trades.get(2), true, false));

        MacdCrossStructureTools.CrossBar hit = MacdCrossStructureTools.findLatestCrossBar(
                trades, points, 10, true, true);

        Assert.assertNotNull(hit);
        Assert.assertEquals("d2", hit.getBar().getDay());
    }

    private static List<Trade> bars(String... days) {
        List<Trade> list = new ArrayList<>();
        for (String day : days) {
            Trade t = new Trade();
            t.setDay(day);
            t.setHigh(10.0);
            t.setClose(9.0);
            list.add(t);
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
