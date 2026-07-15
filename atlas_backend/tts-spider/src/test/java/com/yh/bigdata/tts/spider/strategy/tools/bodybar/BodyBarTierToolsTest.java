package com.yh.bigdata.tts.spider.strategy.tools.bodybar;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.BodyBarTierStrategyParams;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BodyBarTierToolsTest {

    @Test
    public void dayTierPassesWhenAllConditionsMet() {
        List<Trade> bars = buildDayPassBars();
        BodyBarTierStrategyParams params = BodyBarTierStrategyParams.builder()
                .tier(BodyBarTierStrategyParams.Tier.DAY)
                .lookbackBars(10)
                .build();
        Assert.assertTrue(BodyBarTierTools.passesTierGateOnBars(
                bars, null, null, PeriodTypeEnum.DAY, params));
    }

    @Test
    public void failsWhenNoHistoryStrongBody() {
        List<Trade> bars = buildDayPassBars();
        for (int i = bars.size() - 11; i <= bars.size() - 2; i++) {
            Trade bar = bars.get(i);
            bar.setOpen(10.0);
            bar.setClose(10.2);
        }
        BodyBarTierStrategyParams params = BodyBarTierStrategyParams.builder()
                .tier(BodyBarTierStrategyParams.Tier.DAY)
                .build();
        Assert.assertFalse(BodyBarTierTools.passesTierGateOnBars(
                bars, null, null, PeriodTypeEnum.DAY, params));
    }

    @Test
    public void failsWhenSignalRiseTooSmall() {
        List<Trade> bars = buildDayPassBars();
        Trade prev = bars.get(bars.size() - 2);
        Trade signal = bars.get(bars.size() - 1);
        prev.setClose(10.0);
        signal.setOpen(10.0);
        signal.setClose(10.25);
        BodyBarTierStrategyParams params = BodyBarTierStrategyParams.builder()
                .tier(BodyBarTierStrategyParams.Tier.DAY)
                .build();
        Assert.assertFalse(BodyBarTierTools.passesTierGateOnBars(
                bars, null, null, PeriodTypeEnum.DAY, params));
    }

    @Test
    public void signalRiseCanPassWhenBodySmallButGapUp() {
        List<Trade> bars = buildDayPassBars();
        Trade prev = bars.get(bars.size() - 2);
        Trade signal = bars.get(bars.size() - 1);
        prev.setClose(10.0);
        prev.setHigh(10.55);
        signal.setOpen(10.5);
        signal.setClose(10.55);
        BodyBarTierStrategyParams params = BodyBarTierStrategyParams.builder()
                .tier(BodyBarTierStrategyParams.Tier.DAY)
                .build();
        Assert.assertTrue(BodyBarTierTools.passesTierGateOnBars(
                bars, null, null, PeriodTypeEnum.DAY, params));
    }

    @Test
    public void failsWhenCloseNotAbovePrevHigh() {
        List<Trade> bars = buildDayPassBars();
        Trade prev = bars.get(bars.size() - 2);
        Trade signal = bars.get(bars.size() - 1);
        signal.setClose(prev.getHigh());
        BodyBarTierStrategyParams params = BodyBarTierStrategyParams.builder()
                .tier(BodyBarTierStrategyParams.Tier.DAY)
                .build();
        Assert.assertFalse(BodyBarTierTools.passesTierGateOnBars(
                bars, null, null, PeriodTypeEnum.DAY, params));
    }

    @Test
    public void weekTierUsesHigherThresholds() {
        List<Trade> bars = buildDayPassBars();
        BodyBarTierStrategyParams params = BodyBarTierStrategyParams.builder()
                .tier(BodyBarTierStrategyParams.Tier.WEEK)
                .build();
        Assert.assertFalse(BodyBarTierTools.passesTierGateOnBars(
                bars, null, null, PeriodTypeEnum.WEEK, params));
    }

    @Test
    public void weekTierPassesWithStrongerBodiesAndRise() {
        List<Trade> bars = buildWeekPassBars();
        BodyBarTierStrategyParams params = BodyBarTierStrategyParams.builder()
                .tier(BodyBarTierStrategyParams.Tier.WEEK)
                .build();
        Assert.assertTrue(BodyBarTierTools.passesTierGateOnBars(
                bars, null, null, PeriodTypeEnum.WEEK, params));
    }

    @Test
    public void bodyPctUsesSignedEntityRatio() {
        Trade yang = bar(10.0, 10.5);
        Trade yin = bar(10.0, 9.5);
        Assert.assertEquals(0.05, BodyBarTierTools.bodyPct(yang), 1e-6);
        Assert.assertEquals(-0.05, BodyBarTierTools.bodyPct(yin), 1e-6);
    }

    @Test
    public void risePctUsesPrevClose() {
        Trade prev = bar(10.0, 10.0);
        Trade signal = bar(10.2, 10.4);
        Assert.assertEquals(0.04, BodyBarTierTools.risePct(signal, prev), 1e-6);
    }

    private static List<Trade> buildDayPassBars() {
        List<Trade> bars = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            bars.add(bar(10.0, 10.1));
        }
        bars.get(0).setOpen(10.0);
        bars.get(0).setClose(10.5);
        bars.add(bar(10.0, 10.8));
        Trade prev = bars.get(bars.size() - 2);
        prev.setClose(10.1);
        prev.setHigh(10.6);
        Trade signal = bars.get(bars.size() - 1);
        signal.setOpen(10.2);
        signal.setClose(10.7);
        return bars;
    }

    private static List<Trade> buildWeekPassBars() {
        List<Trade> bars = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            bars.add(bar(10.0, 10.2));
        }
        bars.get(0).setOpen(10.0);
        bars.get(0).setClose(10.7);
        bars.add(bar(10.0, 11.0));
        Trade prev = bars.get(bars.size() - 2);
        prev.setClose(10.0);
        prev.setHigh(10.5);
        Trade signal = bars.get(bars.size() - 1);
        signal.setOpen(10.0);
        signal.setClose(10.6);
        return bars;
    }

    private static Trade bar(double open, double close) {
        Trade t = new Trade();
        t.setOpen(open);
        t.setClose(close);
        t.setHigh(Math.max(open, close) + 0.1);
        t.setLow(Math.min(open, close) - 0.1);
        return t;
    }
}
