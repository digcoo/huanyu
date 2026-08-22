package com.yh.bigdata.tts.spider.strategy.tools.macrosspoint;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class MaCrossPointToolsTest {

    @Test
    public void findHitOnBars_goldenYang_usesContainingBandHigh() {
        // d1-d2 阳段，d3 阴完结 → 完整波段 high=max(10.5,10.8,10.6)=10.8
        // d4 金叉且阳，落在未完结段？需要金叉在完整波段阳段内
        // 重建：d1阳 high10.5, d2阳 high10.8 金叉, d3阴完结 high10.6；d4阴 close低；d5破 10.8
        List<Trade> bars = Arrays.asList(
                ohlc("d1", 10.0, 10.5, 9.9, 10.2, 9.5, 10.0),   // 阳
                ohlc("d2", 10.2, 10.8, 10.1, 10.5, 10.2, 10.0), // 阳 + 金叉
                ohlc("d3", 10.5, 10.6, 10.0, 10.1, 10.3, 10.1), // 阴完结
                ohlc("d4", 10.1, 10.3, 9.8, 10.0, 10.4, 10.2),  // prev close<=10.8
                ohlc("d5", 10.0, 11.0, 9.9, 10.9, 10.5, 10.3)   // edge break 10.8
        );
        MaCrossPointTools.Hit hit = MaCrossPointTools.findHitOnBars(
                bars, PeriodTypeEnum.DAY, MaCrossPointCore.CrossKind.GOLDEN);
        Assert.assertNotNull(hit);
        Assert.assertEquals(10.8, hit.getBreakLine(), 1e-6);
        Assert.assertEquals("d2", hit.getCrossBar().getDay());
    }

    @Test
    public void findHitOnBars_goldenYang_unfinishedUsesPrevCompleteBand() {
        // 波段1完结 high=10.4；d3 阳线金叉落在未完结段 → 取前一波段 10.4
        List<Trade> bars = Arrays.asList(
                ohlc("d1", 10.0, 10.4, 9.9, 10.2, 9.5, 10.0),
                ohlc("d2", 10.2, 10.3, 10.0, 10.05, 9.8, 10.0),
                ohlc("d3", 10.1, 10.2, 10.0, 10.15, 10.2, 10.0),
                ohlc("d4", 10.15, 10.3, 10.0, 10.2, 10.3, 10.1),
                ohlc("d5", 10.2, 10.8, 10.1, 10.5, 10.5, 10.3)
        );
        MaCrossPointTools.Hit hit = MaCrossPointTools.findHitOnBars(
                bars, PeriodTypeEnum.DAY, MaCrossPointCore.CrossKind.GOLDEN);
        Assert.assertNotNull(hit);
        Assert.assertEquals(10.4, hit.getBreakLine(), 1e-6);
        Assert.assertEquals("d3", hit.getCrossBar().getDay());
    }

    @Test
    public void findHitOnBars_goldenYang_missWhenUnfinishedAndNoPrevBand() {
        List<Trade> bars = Arrays.asList(
                ohlc("d1", 10.0, 10.5, 9.9, 10.2, 9.5, 10.0),
                ohlc("d2", 10.2, 10.8, 10.1, 10.5, 10.2, 10.0),
                ohlc("d3", 10.5, 10.7, 10.2, 10.6, 10.3, 10.1),
                ohlc("d4", 10.6, 10.9, 10.3, 10.8, 10.4, 10.2)
        );
        Assert.assertNull(MaCrossPointTools.findHitOnBars(
                bars, PeriodTypeEnum.DAY, MaCrossPointCore.CrossKind.GOLDEN));
    }

    @Test
    public void findHitOnBars_goldenYin_usesNearestCompleteBandBefore() {
        // 波段1：d1阳 d2阴完结 high=10.4
        // d3 阴金叉 → 往前第一个完整波段 = 波段1，breakLine=10.4
        // d4 prev<=10.4, d5 破
        List<Trade> bars = Arrays.asList(
                ohlc("d1", 10.0, 10.4, 9.9, 10.2, 9.5, 10.0),   // 阳
                ohlc("d2", 10.2, 10.3, 10.0, 10.05, 9.8, 10.0), // 阴完结
                ohlc("d3", 10.0, 10.2, 9.8, 9.9, 10.2, 10.0),   // 阴金叉（ma5上穿）
                ohlc("d4", 9.9, 10.3, 9.7, 10.1, 10.3, 10.1),
                ohlc("d5", 10.1, 10.8, 10.0, 10.5, 10.4, 10.2)
        );
        // d2: ma5=9.8 < ma10=10；d3: ma5=10.2 >= ma10=10 → golden at d3 yin
        MaCrossPointTools.Hit hit = MaCrossPointTools.findHitOnBars(
                bars, PeriodTypeEnum.DAY, MaCrossPointCore.CrossKind.GOLDEN);
        Assert.assertNotNull(hit);
        Assert.assertEquals(10.4, hit.getBreakLine(), 1e-6);
        Assert.assertEquals("d3", hit.getCrossBar().getDay());
    }

    @Test
    public void findHitOnBars_golden_missWhenSignalMa5NotAboveMa10() {
        List<Trade> bars = Arrays.asList(
                ohlc("d1", 10.0, 10.5, 9.9, 10.2, 9.5, 10.0),
                ohlc("d2", 10.2, 10.8, 10.1, 10.5, 10.2, 10.0),
                ohlc("d3", 10.5, 10.6, 10.0, 10.1, 10.3, 10.1),
                ohlc("d4", 10.1, 10.3, 9.8, 10.0, 10.4, 10.2),
                ohlc("d5", 10.0, 11.0, 9.9, 10.9, 10.0, 10.3) // 边沿破成立但 MA5<=MA10
        );
        Assert.assertNull(MaCrossPointTools.findHitOnBars(
                bars, PeriodTypeEnum.DAY, MaCrossPointCore.CrossKind.GOLDEN));
    }

    @Test
    public void findHitOnBars_hitsDeathCrossEdgeBreak() {
        List<Trade> bars = Arrays.asList(
                flat("d1", 10.2, 10.0, 10.1),
                flat("d2", 10.1, 10.0, 10.05),
                flat("d3", 9.8, 10.0, 9.9),
                flat("d4", 9.9, 9.95, 9.85),
                flat("d5", 10.1, 10.0, 10.05)
        );
        MaCrossPointTools.Hit hit = MaCrossPointTools.findHitOnBars(
                bars, PeriodTypeEnum.DAY, MaCrossPointCore.CrossKind.DEATH);
        Assert.assertNotNull(hit);
        Assert.assertEquals(MaCrossPointCore.CrossKind.DEATH, hit.getCrossKind());
        Assert.assertEquals(10.0, hit.getBreakLine(), 1e-6);
    }

    @Test
    public void passesMaBull_requiresMa5AboveMa60() {
        Trade ok = flat("d1", 10.5, 10.0, 10.2);
        ok.setMa60(10.0);
        Assert.assertTrue(MaCrossPointCore.passesMaBull(ok));
        Trade bad = flat("d1", 9.5, 10.0, 9.8);
        bad.setMa60(10.0);
        Assert.assertFalse(MaCrossPointCore.passesMaBull(bad));
    }

    @Test
    public void passesAboveMa_requiresCloseAboveMaxMa5Ma10() {
        Trade ok = flat("d1", 10.0, 10.2, 10.5);
        Assert.assertTrue(MaCrossPointCore.passesAboveMa(ok));
        Trade bad = flat("d1", 10.5, 10.2, 10.3);
        Assert.assertFalse(MaCrossPointCore.passesAboveMa(bad));
    }

    @Test
    public void resolveParentPeriod_followsDocPairs() {
        Assert.assertEquals(PeriodTypeEnum.DAY, MaCrossPointTools.resolveParentPeriod(PeriodTypeEnum.MIN30));
        Assert.assertEquals(PeriodTypeEnum.WEEK, MaCrossPointTools.resolveParentPeriod(PeriodTypeEnum.DAY));
        Assert.assertEquals(PeriodTypeEnum.MONTH, MaCrossPointTools.resolveParentPeriod(PeriodTypeEnum.WEEK));
        Assert.assertEquals(PeriodTypeEnum.QUARTER, MaCrossPointTools.resolveParentPeriod(PeriodTypeEnum.MONTH));
        Assert.assertEquals(PeriodTypeEnum.YEAR, MaCrossPointTools.resolveParentPeriod(PeriodTypeEnum.QUARTER));
    }

    private static Trade flat(String day, double ma5, double ma10, double close) {
        return ohlc(day, close, close + 0.2, close - 0.2, close, ma5, ma10);
    }

    private static Trade ohlc(String day, double open, double high, double low, double close,
                              double ma5, double ma10) {
        Trade t = new Trade();
        t.setDay(day);
        t.setOpen(open);
        t.setHigh(high);
        t.setLow(low);
        t.setClose(close);
        t.setMa5(ma5);
        t.setMa10(ma10);
        return t;
    }
}
