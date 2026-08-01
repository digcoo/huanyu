package com.yh.bigdata.tts.spider.strategy.tools.maalignlift;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class MaAlignLiftToolsTest {

    @Test
    public void findHitOnBars_hitsWhenMaBullAndEdgeBreaksMaxMa() {
        List<Trade> bars = Arrays.asList(
                bar("2026-01-01", 10.0, 10.5, 9.8, 10.2, 9.5, 9.3, 9.0, 8.8),
                bar("2026-01-02", 10.2, 10.5, 10.0, 10.0, 10.0, 9.7, 9.4, 9.1),
                bar("2026-01-03", 10.0, 11.0, 9.9, 10.95, 10.2, 10.0, 9.7, 9.4)
        );
        MaAlignLiftTools.Hit hit = MaAlignLiftTools.findHitOnBars(bars, PeriodTypeEnum.DAY);
        Assert.assertNotNull(hit);
        Assert.assertEquals("2026-01-03", hit.getSignalBar().getDay());
        Assert.assertEquals(10.2, hit.getSignalMaxMa(), 1e-6);
        Assert.assertEquals(MaAlignLiftTools.BreakoutMode.EDGE, hit.getBreakoutMode());
    }

    @Test
    public void findHitOnBars_hitsWhenOpenBreaksMaxMaEvenIfPrevAbove() {
        // 前一根已在均线MAX之上（边沿不成立），末K开盘在下、收盘在上
        List<Trade> bars = Arrays.asList(
                bar("2026-01-01", 10.0, 10.5, 9.8, 10.2, 9.5, 9.3, 9.0, 8.8),
                bar("2026-01-02", 10.2, 10.8, 10.0, 10.75, 10.0, 9.7, 9.4, 9.1),
                bar("2026-01-03", 10.0, 11.0, 9.9, 10.95, 10.2, 10.0, 9.7, 9.4)
        );
        MaAlignLiftTools.Hit hit = MaAlignLiftTools.findHitOnBars(bars, PeriodTypeEnum.DAY);
        Assert.assertNotNull(hit);
        Assert.assertEquals(MaAlignLiftTools.BreakoutMode.OPEN, hit.getBreakoutMode());
    }

    @Test
    public void findHitOnBars_missWhenMaNotBull() {
        List<Trade> bars = Arrays.asList(
                bar("2026-01-01", 10.0, 10.5, 9.8, 10.2, 9.0, 9.5, 9.3, 9.0),
                bar("2026-01-02", 10.2, 11.0, 10.0, 10.9, 9.0, 9.5, 9.3, 9.0)
        );
        Assert.assertNull(MaAlignLiftTools.findHitOnBars(bars, PeriodTypeEnum.DAY));
    }

    @Test
    public void findHitOnBars_missWhenNeitherEdgeNorOpen() {
        // 前一根已在MAX之上，且末K开盘也在MAX之上
        List<Trade> bars = Arrays.asList(
                bar("2026-01-01", 10.0, 10.5, 9.8, 10.2, 9.5, 9.3, 9.0, 8.8),
                bar("2026-01-02", 10.2, 10.8, 10.0, 10.75, 10.0, 9.7, 9.4, 9.1),
                bar("2026-01-03", 10.75, 11.0, 10.6, 10.95, 10.2, 10.0, 9.7, 9.4)
        );
        Assert.assertNull(MaAlignLiftTools.findHitOnBars(bars, PeriodTypeEnum.DAY));
    }

    @Test
    public void passesMaxMaOpenBreakout_requiresOpenAtOrBelowMaxMa() {
        Trade sig = bar("s", 10.0, 11, 9.9, 10.5, 10.2, 10.0, 9.7, 9.4);
        Assert.assertTrue(MaAlignLiftTools.passesMaxMaOpenBreakout(sig));
        sig.setOpen(10.21);
        Assert.assertFalse(MaAlignLiftTools.passesMaxMaOpenBreakout(sig));
    }

    private static Trade bar(String day, double open, double high, double low, double close,
                             double ma5, double ma10, double ma20, double ma30) {
        Trade t = new Trade();
        t.setDay(day);
        t.setOpen(open);
        t.setHigh(high);
        t.setLow(low);
        t.setClose(close);
        t.setMa5(ma5);
        t.setMa10(ma10);
        t.setMa20(ma20);
        t.setMa30(ma30);
        return t;
    }
}
