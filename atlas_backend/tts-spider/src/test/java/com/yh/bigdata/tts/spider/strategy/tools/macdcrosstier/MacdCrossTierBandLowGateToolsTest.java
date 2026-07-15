package com.yh.bigdata.tts.spider.strategy.tools.macdcrosstier;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MacdCrossTierStrategyParams;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class MacdCrossTierBandLowGateToolsTest {

    @Test
    public void weekMonthBothPassWhenCloseAboveLastBandLow() {
        List<Trade> weekBars = buildOneBandBars(10.0, 11.0);
        List<Trade> monthBars = buildOneBandBars(20.0, 21.0);
        Assert.assertTrue(MacdCrossTierBandLowGateTools.passesPeriodBandLowOnBars(
                weekBars, null, PeriodTypeEnum.WEEK, 20));
        Assert.assertTrue(MacdCrossTierBandLowGateTools.passesPeriodBandLowOnBars(
                monthBars, null, PeriodTypeEnum.MONTH, 20));
    }

    @Test
    public void failsWhenCloseAtOrBelowLastBandLow() {
        List<Trade> weekBars = buildOneBandBars(10.0, 10.0);
        Assert.assertFalse(MacdCrossTierBandLowGateTools.passesPeriodBandLowOnBars(
                weekBars, null, PeriodTypeEnum.WEEK, 20));
        weekBars.get(weekBars.size() - 1).setClose(9.5);
        Assert.assertFalse(MacdCrossTierBandLowGateTools.passesPeriodBandLowOnBars(
                weekBars, null, PeriodTypeEnum.WEEK, 20));
    }

    @Test
    public void failsWhenNoCompleteBand() {
        List<Trade> bars = new ArrayList<>();
        bars.add(bar("2026-01-01", 10, 11, 9, 9.5));
        bars.add(bar("2026-01-08", 9.5, 10, 9, 9.2));
        bars.add(bar("2026-01-15", 9.2, 10.5, 9, 10.0));
        Assert.assertFalse(MacdCrossTierBandLowGateTools.passesPeriodBandLowOnBars(
                bars, null, PeriodTypeEnum.WEEK, 20));
    }

    @Test
    public void dayTierGateSkipsForWeekAndMonthTiers() {
        MacdCrossTierStrategyParams week = MacdCrossTierStrategyParams.builder()
                .tier(MacdCrossTierStrategyParams.Tier.WEEK)
                .build();
        MacdCrossTierStrategyParams month = MacdCrossTierStrategyParams.builder()
                .tier(MacdCrossTierStrategyParams.Tier.MONTH)
                .build();
        Assert.assertTrue(MacdCrossTierBandLowGateTools.passesDayTierWeekMonthGate(null, null, week));
        Assert.assertTrue(MacdCrossTierBandLowGateTools.passesDayTierWeekMonthGate(null, null, month));
    }

    private static List<Trade> buildOneBandBars(double bandLow, double lastClose) {
        List<Trade> bars = new ArrayList<>();
        bars.add(bar("2026-01-01", bandLow + 1, bandLow + 3, bandLow, bandLow + 2));
        bars.add(bar("2026-01-08", bandLow + 2, bandLow + 2.5, bandLow, bandLow));
        bars.add(bar("2026-01-15", lastClose, lastClose + 1, lastClose - 1, lastClose));
        return bars;
    }

    private static Trade bar(String date, double open, double high, double low, double close) {
        Trade t = new Trade();
        t.setDay(date);
        t.setOpen(open);
        t.setHigh(high);
        t.setLow(low);
        t.setClose(close);
        return t;
    }
}
