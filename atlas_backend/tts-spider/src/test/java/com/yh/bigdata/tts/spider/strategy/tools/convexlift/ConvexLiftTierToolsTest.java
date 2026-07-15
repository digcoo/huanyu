package com.yh.bigdata.tts.spider.strategy.tools.convexlift;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.ConvexLiftTierStrategyParams;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ConvexLiftTierToolsTest {

    @Test
    public void resolvePeriodAndTierLabel() {
        Assert.assertEquals(PeriodTypeEnum.WEEK,
                ConvexLiftTierTools.resolvePeriod(ConvexLiftTierStrategyParams.Tier.WEEK));
        Assert.assertEquals(PeriodTypeEnum.MONTH,
                ConvexLiftTierTools.resolvePeriod(ConvexLiftTierStrategyParams.Tier.MONTH));
        Assert.assertEquals(PeriodTypeEnum.YEAR,
                ConvexLiftTierTools.resolvePeriod(ConvexLiftTierStrategyParams.Tier.YEAR));
        Assert.assertEquals("周", ConvexLiftTierTools.buildTierLabel(ConvexLiftTierStrategyParams.defaults()));
        Assert.assertEquals("月", ConvexLiftTierTools.buildTierLabel(
                ConvexLiftTierStrategyParams.builder().tier(ConvexLiftTierStrategyParams.Tier.MONTH).build()));
        Assert.assertEquals("年", ConvexLiftTierTools.buildTierLabel(
                ConvexLiftTierStrategyParams.builder().tier(ConvexLiftTierStrategyParams.Tier.YEAR).build()));
    }

    @Test
    public void passesCloseAbovePrevHigh() {
        List<Trade> bars = new ArrayList<>();
        bars.add(bar("2026-07-09", 13.5, 15, 13, 14.5));
        bars.add(bar("2026-07-10", 14.5, 16.5, 14, 16.0));
        Assert.assertTrue(ConvexLiftTierTools.passesCloseAbovePrevHigh(bars, null, PeriodTypeEnum.DAY));

        bars.get(1).setClose(15.0);
        Assert.assertFalse(ConvexLiftTierTools.passesCloseAbovePrevHigh(bars, null, PeriodTypeEnum.DAY));
    }

    @Test
    public void passesPeriodCloseAbovePrevHighOnBars() {
        List<Trade> bars = new ArrayList<>();
        bars.add(bar("2026-07-04", 38.5, 40, 38, 39.5));
        bars.add(bar("2026-07-11", 39.5, 42, 39, 41.5));
        Assert.assertTrue(ConvexLiftTierTools.passesPeriodCloseAbovePrevHighOnBars(
                bars, null, null, PeriodTypeEnum.WEEK));

        bars.get(1).setClose(39.0);
        Assert.assertFalse(ConvexLiftTierTools.passesPeriodCloseAbovePrevHighOnBars(
                bars, null, null, PeriodTypeEnum.WEEK));
    }

    @Test
    public void passesDayConvexLiftOnBarsWhenConvexAndBreakout() {
        List<Trade> bars = buildConvexDayBars();
        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(bars, 120);
        Assert.assertTrue(bands.size() >= 2);
        YangBandTools.CompleteYangBand lastBand = bands.get(bands.size() - 1);
        YangBandTools.CompleteYangBand prevBand = bands.get(bands.size() - 2);
        Assert.assertTrue(WaveShapeTools.isConvex(lastBand, prevBand));

        Trade prev = bars.get(bars.size() - 2);
        Trade signal = bars.get(bars.size() - 1);
        signal.setClose(prev.getHigh() + 1.0);

        Assert.assertTrue(ConvexLiftTierTools.passesDayConvexLiftOnBars(bars, null, null, 120));
    }

    @Test
    public void failsDayConvexLiftWhenCloseNotAbovePrevHigh() {
        List<Trade> bars = buildConvexDayBars();
        Trade prev = bars.get(bars.size() - 2);
        Trade signal = bars.get(bars.size() - 1);
        signal.setClose(prev.getHigh() - 0.5);
        Assert.assertFalse(ConvexLiftTierTools.passesDayConvexLiftOnBars(bars, null, null, 120));
    }

    private static List<Trade> buildConvexDayBars() {
        List<Trade> bars = new ArrayList<>();
        bars.add(bar("2026-07-06", 10, 12, 9, 11));
        bars.add(bar("2026-07-07", 11, 13, 10, 10.5));
        bars.add(bar("2026-07-08", 10.5, 14, 10, 13.5));
        bars.add(bar("2026-07-09", 13.5, 15, 13, 14.5));
        bars.add(bar("2026-07-10", 14.5, 16.5, 14, 16.0));
        return bars;
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
