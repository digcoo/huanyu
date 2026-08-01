package com.yh.bigdata.tts.spider.strategy.tools.mabearbreak;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class MaBearBreakToolsTest {

    @Test
    public void findHitOnBars_hitsWhenMaBearAndEdgeBreaksMaxMa() {
        List<Trade> bars = Arrays.asList(
                bar("2026-01-01", 10.0, 10.5, 9.8, 10.2, 9.5, 9.8, 10.0, 10.2),
                bar("2026-01-02", 10.2, 10.5, 10.0, 10.0, 10.0, 9.8, 10.0, 10.2),
                bar("2026-01-03", 10.0, 11.0, 9.9, 10.95, 10.2, 9.8, 10.0, 10.3)
        );
        MaBearBreakTools.Hit hit = MaBearBreakTools.findHitOnBars(bars, PeriodTypeEnum.DAY);
        Assert.assertNotNull(hit);
        Assert.assertEquals("2026-01-03", hit.getSignalBar().getDay());
        Assert.assertEquals(MaBearBreakTools.BreakoutMode.EDGE, hit.getBreakoutMode());
    }

    @Test
    public void findHitOnBars_hitsWhenOpenBreaksMaxMaEvenIfPrevAbove() {
        // 前一根已在均线MAX之上（边沿不成立），末K开盘在下、收盘在上
        List<Trade> bars = Arrays.asList(
                bar("2026-01-01", 10.0, 10.5, 9.8, 10.2, 9.5, 9.8, 10.0, 10.2),
                bar("2026-01-02", 10.2, 10.8, 10.0, 10.5, 10.0, 9.8, 10.0, 10.2),
                bar("2026-01-03", 10.0, 11.0, 9.9, 10.95, 10.2, 9.8, 10.0, 10.3)
        );
        MaBearBreakTools.Hit hit = MaBearBreakTools.findHitOnBars(bars, PeriodTypeEnum.DAY);
        Assert.assertNotNull(hit);
        Assert.assertEquals(MaBearBreakTools.BreakoutMode.OPEN, hit.getBreakoutMode());
    }

    @Test
    public void findHitOnBars_missWhenMaNotBear() {
        List<Trade> bars = Arrays.asList(
                bar("2026-01-01", 10.0, 10.5, 9.8, 10.2, 9.5, 10.0, 9.7, 9.4),
                bar("2026-01-02", 10.2, 11.0, 10.0, 10.9, 10.2, 10.0, 9.7, 9.4)
        );
        Assert.assertNull(MaBearBreakTools.findHitOnBars(bars, PeriodTypeEnum.DAY));
    }

    @Test
    public void findHitOnBars_missWhenNeitherEdgeNorOpen() {
        List<Trade> bars = Arrays.asList(
                bar("2026-01-01", 10.0, 10.5, 9.8, 10.2, 9.5, 9.8, 10.0, 10.2),
                bar("2026-01-02", 10.2, 10.8, 10.0, 10.5, 10.0, 9.8, 10.0, 10.2),
                bar("2026-01-03", 10.5, 11.0, 10.3, 10.95, 10.2, 9.8, 10.0, 10.3)
        );
        Assert.assertNull(MaBearBreakTools.findHitOnBars(bars, PeriodTypeEnum.DAY));
    }

    @Test
    public void passesMaBearOrder_requiresMa10LessThanMa20LessThanMa30() {
        Trade t = bar("d", 10, 11, 9, 10.5, 10, 9.8, 10.0, 10.3);
        Assert.assertTrue(MaBearBreakTools.passesMaBearOrder(t));
        t.setMa10(10.0);
        t.setMa20(9.9);
        Assert.assertFalse(MaBearBreakTools.passesMaBearOrder(t));
    }

    private static Trade bar(String day, double open, double high, double low, double close,
                             double ma5, double ma10, double ma20, double ma30) {
        Trade trade = new Trade();
        trade.setDay(day);
        trade.setOpen(open);
        trade.setHigh(high);
        trade.setLow(low);
        trade.setClose(close);
        trade.setMa5(ma5);
        trade.setMa10(ma10);
        trade.setMa20(ma20);
        trade.setMa30(ma30);
        return trade;
    }
}
