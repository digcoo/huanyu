package com.yh.bigdata.tts.spider.strategy.tools.trendwavecc;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.TrendWaveCcBreakoutStrategyParams;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TrendWaveCcBreakoutToolsTest {

    @Test
    public void resolveSignalPeriod_parsesDayAndWeek() {
        Assert.assertEquals(PeriodTypeEnum.MIN60, TrendWaveCcBreakoutTools.resolveSignalPeriod("min60"));
        Assert.assertEquals(PeriodTypeEnum.DAY, TrendWaveCcBreakoutTools.resolveSignalPeriod("day"));
        Assert.assertEquals(PeriodTypeEnum.WEEK, TrendWaveCcBreakoutTools.resolveSignalPeriod("week"));
    }

    @Test
    public void passesBreakoutStrength_acceptsAmplitudeExpand() {
        Trade prevPrev = trade("2026-01-01", 10.0, 10.1, 9.9, 10.0);
        Trade prev = trade("2026-01-02", 10.0, 10.2, 9.95, 10.1);
        Trade signal = trade("2026-01-03", 10.1, 11.0, 10.0, 10.9);
        Assert.assertTrue(TrendWaveCcBreakoutTools.passesBreakoutStrength(signal, prev, prevPrev));
    }

    @Test
    public void resolvePrimaryTrendPeriod_usesHighestSelectedMacdGate() {
        TrendWaveCcBreakoutStrategyParams weekOnly = TrendWaveCcBreakoutStrategyParams.builder()
                .requireDayMacd(false)
                .requireWeekMacd(true)
                .build();
        Assert.assertEquals(PeriodTypeEnum.WEEK,
                TrendWaveCcBreakoutTools.resolvePrimaryTrendPeriod(weekOnly));

        TrendWaveCcBreakoutStrategyParams dayAndWeek = TrendWaveCcBreakoutStrategyParams.builder()
                .requireDayMacd(true)
                .requireWeekMacd(true)
                .build();
        Assert.assertEquals(PeriodTypeEnum.WEEK,
                TrendWaveCcBreakoutTools.resolvePrimaryTrendPeriod(dayAndWeek));
    }

    @Test
    public void resolveHitOnBars_returnsNullWhenNoBandBreakout() {
        List<Trade> bars = new ArrayList<>();
        bars.add(trade("d1", 10.0, 10.5, 9.8, 10.4));
        bars.add(trade("d2", 10.4, 10.6, 10.2, 10.3));
        bars.add(trade("d3", 10.3, 10.8, 10.1, 10.7));
        bars.add(trade("d4", 10.7, 11.2, 10.5, 11.0));
        bars.add(trade("d5", 11.0, 11.1, 10.9, 11.05));
        bars.add(trade("d6", 11.05, 11.3, 10.7, 11.2));
        TrendWaveCcBreakoutStrategyParams params = TrendWaveCcBreakoutStrategyParams.builder()
                .lookbackBars(20)
                .build();
        Assert.assertNull(TrendWaveCcBreakoutTools.resolveHitOnBars(bars, params, PeriodTypeEnum.DAY));
    }

    private static Trade trade(String day, double open, double high, double low, double close) {
        Trade t = new Trade();
        t.setDay(day);
        t.setOpen(open);
        t.setHigh(high);
        t.setLow(low);
        t.setClose(close);
        return t;
    }
}
