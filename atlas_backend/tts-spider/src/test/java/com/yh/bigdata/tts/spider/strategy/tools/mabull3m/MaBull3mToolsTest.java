package com.yh.bigdata.tts.spider.strategy.tools.mabull3m;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class MaBull3mToolsTest {

    @Test
    public void findHitOnBars_hitsWhenEdgeBreaksGoldenAfterCritical_ma20() {
        // d2=关键K；d3 短暂 MA5<MA10；d4=金叉(在关键K之后)；d5 边沿破 d4.high
        List<Trade> bars = Arrays.asList(
                barMa20("d1", 10.0, 10.2, 9.8, 10.0, 9.0, 9.5, 11.0),
                barMa20("d2", 10.0, 10.3, 9.9, 10.2, 10.2, 10.0, 9.5),
                barMa20("d3", 10.2, 10.4, 10.0, 10.1, 9.7, 10.0, 9.5),
                barMa20("d4", 10.1, 10.5, 10.0, 10.4, 10.1, 10.0, 9.6),
                barMa20("d5", 10.4, 10.7, 10.2, 10.6, 10.3, 10.1, 9.7)
        );
        MaBull3mTools.Hit hit = MaBull3mTools.findHitOnBars(bars, PeriodTypeEnum.DAY);
        Assert.assertNotNull(hit);
        Assert.assertEquals(MaBull3mTools.AlignKind.MA20, hit.getAlignKind());
        Assert.assertEquals(MaBull3mTools.RefKind.GOLDEN, hit.getRefKind());
        Assert.assertEquals("d4", hit.getRefBar().getDay());
        Assert.assertEquals(10.5, hit.getBreakLine(), 1e-6);
    }

    @Test
    public void findHitOnBars_ignoresGoldenBeforeCritical_fallsBackToCritical() {
        List<Trade> bars = Arrays.asList(
                barMa20("d1", 10.0, 10.2, 9.8, 10.0, 9.0, 9.5, 10.0),
                barMa20("d2", 10.0, 10.5, 9.9, 10.3, 9.6, 9.5, 9.8),
                barMa20("d3", 10.3, 10.4, 10.0, 10.2, 10.0, 9.7, 9.5),
                barMa20("d4", 10.2, 10.8, 10.1, 10.6, 10.2, 9.9, 9.4)
        );
        MaBull3mTools.Hit hit = MaBull3mTools.findHitOnBars(bars, PeriodTypeEnum.DAY);
        Assert.assertNotNull(hit);
        Assert.assertEquals(MaBull3mTools.AlignKind.MA20, hit.getAlignKind());
        Assert.assertEquals(MaBull3mTools.RefKind.CRITICAL, hit.getRefKind());
        Assert.assertEquals("d3", hit.getRefBar().getDay());
    }

    @Test
    public void findHitOnBars_hitsWhenEdgeBreaksCriticalK_ma20() {
        List<Trade> bars = Arrays.asList(
                barMa20("d1", 10.0, 10.2, 9.8, 10.0, 10.2, 10.1, 11.0),
                barMa20("d2", 10.0, 10.4, 9.9, 10.3, 10.2, 10.1, 9.5),
                barMa20("d3", 10.3, 10.35, 10.0, 10.2, 10.3, 10.2, 9.6),
                barMa20("d4", 10.2, 10.6, 10.1, 10.5, 10.4, 10.3, 9.7)
        );
        MaBull3mTools.Hit hit = MaBull3mTools.findHitOnBars(bars, PeriodTypeEnum.DAY);
        Assert.assertNotNull(hit);
        Assert.assertEquals(MaBull3mTools.AlignKind.MA20, hit.getAlignKind());
        Assert.assertEquals(MaBull3mTools.RefKind.CRITICAL, hit.getRefKind());
        Assert.assertEquals("d2", hit.getRefBar().getDay());
        Assert.assertEquals(10.4, hit.getBreakLine(), 1e-6);
    }

    @Test
    public void findHitOnBars_hitsMa30PathWhenMa20NotAlign() {
        // MA20 不满足多头；MA30 满足且边沿破关键K
        List<Trade> bars = Arrays.asList(
                barMa30("d1", 10.0, 10.2, 9.8, 10.0, 10.2, 10.1, 11.0, 11.0),
                barMa30("d2", 10.0, 10.4, 9.9, 10.3, 10.2, 10.1, 11.0, 9.5),
                barMa30("d3", 10.3, 10.35, 10.0, 10.2, 10.3, 10.2, 11.0, 9.6),
                barMa30("d4", 10.2, 10.6, 10.1, 10.5, 10.4, 10.3, 11.0, 9.7)
        );
        MaBull3mTools.Hit hit = MaBull3mTools.findHitOnBars(bars, PeriodTypeEnum.DAY);
        Assert.assertNotNull(hit);
        Assert.assertEquals(MaBull3mTools.AlignKind.MA30, hit.getAlignKind());
        Assert.assertEquals(MaBull3mTools.RefKind.CRITICAL, hit.getRefKind());
        Assert.assertEquals("d2", hit.getRefBar().getDay());
    }

    @Test
    public void findHitOnBars_prefersMa20WhenBothAlign() {
        List<Trade> bars = Arrays.asList(
                barBoth("d1", 10.0, 10.2, 9.8, 10.0, 10.2, 10.1, 11.0, 11.5),
                barBoth("d2", 10.0, 10.4, 9.9, 10.3, 10.2, 10.1, 9.5, 9.4),
                barBoth("d3", 10.3, 10.35, 10.0, 10.2, 10.3, 10.2, 9.6, 9.5),
                barBoth("d4", 10.2, 10.6, 10.1, 10.5, 10.4, 10.3, 9.7, 9.6)
        );
        MaBull3mTools.Hit hit = MaBull3mTools.findHitOnBars(bars, PeriodTypeEnum.DAY);
        Assert.assertNotNull(hit);
        Assert.assertEquals(MaBull3mTools.AlignKind.MA20, hit.getAlignKind());
    }

    @Test
    public void findHitOnBars_missWhenNeitherBullAlign() {
        List<Trade> bars = Arrays.asList(
                barBoth("d1", 10.0, 10.2, 9.8, 10.0, 9.0, 9.5, 10.0, 10.0),
                barBoth("d2", 10.0, 10.5, 9.9, 10.3, 9.6, 9.5, 9.8, 9.8),
                barBoth("d3", 10.3, 10.4, 10.0, 10.2, 9.0, 9.1, 10.0, 10.0),
                barBoth("d4", 10.2, 10.8, 10.1, 10.6, 9.0, 9.1, 10.0, 10.0)
        );
        Assert.assertNull(MaBull3mTools.findHitOnBars(bars, PeriodTypeEnum.DAY));
    }

    @Test
    public void findCriticalBullAlignIndex_matchesDocExample() {
        List<Trade> bars = Arrays.asList(
                barMa20("k1", 10, 10, 10, 10, 9.0, 9.1, 10.0),
                barMa20("k2", 10, 10, 10, 10, 10.2, 10.1, 9.5),
                barMa20("k3", 10, 10, 10, 10, 10.3, 10.2, 9.6)
        );
        Assert.assertEquals(1, MaBull3mTools.findCriticalBullAlignIndex(bars, 2, MaBull3mTools.AlignKind.MA20));
    }

    private static Trade barMa20(String day, double open, double high, double low, double close,
                                 double ma5, double ma10, double ma20) {
        Trade t = new Trade();
        t.setDay(day);
        t.setOpen(open);
        t.setHigh(high);
        t.setLow(low);
        t.setClose(close);
        t.setMa5(ma5);
        t.setMa10(ma10);
        t.setMa20(ma20);
        return t;
    }

    private static Trade barMa30(String day, double open, double high, double low, double close,
                                 double ma5, double ma10, double ma20, double ma30) {
        Trade t = barMa20(day, open, high, low, close, ma5, ma10, ma20);
        t.setMa30(ma30);
        return t;
    }

    private static Trade barBoth(String day, double open, double high, double low, double close,
                                 double ma5, double ma10, double ma20, double ma30) {
        return barMa30(day, open, high, low, close, ma5, ma10, ma20, ma30);
    }
}
