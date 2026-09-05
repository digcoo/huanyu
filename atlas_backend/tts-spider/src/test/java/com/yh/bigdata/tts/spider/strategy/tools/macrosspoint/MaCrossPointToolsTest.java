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
    public void passesMa10GeMa60_allowsEqual() {
        Trade eq = flat("d1", 10.0, 10.0, 10.2);
        eq.setMa60(10.0);
        Assert.assertTrue(MaCrossPointCore.passesMa10GeMa60(eq));
        Trade below = flat("d1", 10.0, 9.9, 10.2);
        below.setMa60(10.0);
        Assert.assertFalse(MaCrossPointCore.passesMa10GeMa60(below));
    }

    @Test
    public void passesMa10LtMa60_requiresStrictBelow() {
        Trade below = flat("d1", 10.0, 9.9, 10.2);
        below.setMa60(10.0);
        Assert.assertTrue(MaCrossPointCore.passesMa10LtMa60(below));
        Trade eq = flat("d1", 10.0, 10.0, 10.2);
        eq.setMa60(10.0);
        Assert.assertFalse(MaCrossPointCore.passesMa10LtMa60(eq));
    }

    @Test
    public void passesAboveMa_requiresCloseAboveMaxMa5Ma10() {
        Trade ok = flat("d1", 10.0, 10.2, 10.5);
        Assert.assertTrue(MaCrossPointCore.passesAboveMa(ok));
        Trade bad = flat("d1", 10.5, 10.2, 10.3);
        Assert.assertFalse(MaCrossPointCore.passesAboveMa(bad));
    }

    @Test
    public void passesEdgeBreak_allowsGapOpenCross() {
        Trade prev = ohlc("d1", 11.0, 11.2, 10.9, 11.1, 10.5, 10.0);
        Trade signal = ohlc("d2", 9.8, 11.0, 9.7, 10.5, 10.6, 10.1);
        Assert.assertTrue(MaCrossPointCore.passesEdgeBreak(prev, signal, 10.0));
        Trade noGap = ohlc("d2", 11.0, 11.2, 10.9, 11.1, 10.6, 10.1);
        Assert.assertFalse(MaCrossPointCore.passesEdgeBreak(prev, noGap, 10.0));
    }

    @Test
    public void passesMa10VsMa60() {
        Trade ge = flat("d1", 10.5, 10.2, 10.3);
        ge.setMa60(10.2);
        Assert.assertTrue(MaCrossPointCore.passesMa10GeMa60(ge));
        Assert.assertFalse(MaCrossPointCore.passesMa10LtMa60(ge));
        Trade lt = flat("d1", 10.5, 10.0, 10.2);
        lt.setMa60(10.2);
        Assert.assertFalse(MaCrossPointCore.passesMa10GeMa60(lt));
        Assert.assertTrue(MaCrossPointCore.passesMa10LtMa60(lt));
    }

    @Test
    public void findLatestCrossIndex_includesLastBarWhenItIsTheCross() {
        List<Trade> bars = Arrays.asList(
                flat("d1", 9.5, 10.0, 9.8),
                flat("d2", 9.6, 10.0, 9.9),
                flat("d3", 10.2, 10.0, 10.3)
        );
        int last = bars.size() - 1;
        Assert.assertEquals(last, MaCrossPointCore.findLatestCrossIndex(
                bars, last, MaCrossPointCore.CrossKind.GOLDEN));
    }

    @Test
    public void passesMa10GeMa60_computesFromClosesWhenMa60Missing() {
        List<Trade> bars = new java.util.ArrayList<>();
        for (int i = 0; i < 60; i++) {
            double close = i < 50 ? 10.0 : 12.0;
            Trade t = ohlc("d" + i, close, close + 0.1, close - 0.1, close, close, close);
            t.setMa60(null);
            bars.add(t);
        }
        Assert.assertTrue(MaCrossPointCore.passesMa10GeMa60(bars));
        Assert.assertFalse(MaCrossPointCore.passesMa10LtMa60(bars));
    }

    @Test
    public void findAnyDeathCrossBreak_hitsDc10First() {
        List<Trade> bars = Arrays.asList(
                dcBar("d1", 10.2, 10.0, 10.1, 10.3, 10.4, 10.1),
                dcBar("d2", 10.1, 10.0, 10.05, 10.3, 10.4, 10.05),
                dcBar("d3", 9.8, 10.0, 9.9, 10.3, 10.4, 9.9),
                dcBar("d4", 9.9, 9.95, 9.85, 10.3, 10.4, 9.85),
                dcBar("d5", 9.9, 9.95, 9.9, 10.3, 10.4, 10.1)
        );
        MaCrossPointTools.Hit hit = MaCrossPointTools.findAnyDeathCrossBreakHitOnBars(bars, PeriodTypeEnum.DAY);
        Assert.assertNotNull(hit);
        Assert.assertEquals(10, hit.getSlowMa());
        Assert.assertEquals(10.0, hit.getBreakLine(), 1e-6);
    }

    @Test
    public void findAnyDeathCrossBreak_fallsBackToDc20() {
        List<Trade> bars = Arrays.asList(
                dcBar("d1", 10.2, 9.5, 10.0, 10.5, 10.6, 10.1),
                dcBar("d2", 10.05, 9.5, 10.0, 10.5, 10.6, 10.05),
                dcBar("d3", 9.95, 9.5, 10.0, 10.5, 10.6, 9.9),
                dcBar("d4", 9.7, 9.5, 10.0, 10.5, 10.6, 9.85),
                dcBar("d5", 9.7, 9.5, 10.0, 10.5, 10.6, 10.1)
        );
        MaCrossPointTools.Hit hit = MaCrossPointTools.findAnyDeathCrossBreakHitOnBars(bars, PeriodTypeEnum.DAY);
        Assert.assertNotNull(hit);
        Assert.assertEquals(20, hit.getSlowMa());
        Assert.assertEquals(10.0, hit.getBreakLine(), 1e-6);
    }

    @Test
    public void findAnyGoldenCrossBreak_hitsGc10First() {
        List<Trade> bars = Arrays.asList(
                dcBar("d1", 9.8, 10.0, 9.5, 9.4, 9.3, 9.9),
                dcBar("d2", 9.9, 10.0, 9.5, 9.4, 9.3, 9.95),
                dcBar("d3", 10.2, 10.0, 9.5, 9.4, 9.3, 10.1),
                dcBar("d4", 10.1, 10.0, 9.5, 9.4, 9.3, 9.9),
                dcBar("d5", 10.1, 10.0, 9.5, 9.4, 9.3, 10.15)
        );
        MaCrossPointTools.Hit hit = MaCrossPointTools.findAnyGoldenCrossBreakHitOnBars(bars, PeriodTypeEnum.DAY);
        Assert.assertNotNull(hit);
        Assert.assertEquals(10, hit.getSlowMa());
        Assert.assertEquals(MaCrossPointTools.BreakTarget.GOLDEN_CROSS, hit.getBreakTarget());
        Assert.assertEquals(10.0, hit.getBreakLine(), 1e-6);
    }

    @Test
    public void findAnyGoldenCrossBreak_fallsBackToGc20() {
        List<Trade> bars = Arrays.asList(
                dcBar("d1", 10.2, 9.5, 10.5, 10.6, 10.7, 10.1),
                dcBar("d2", 10.3, 9.5, 10.5, 10.6, 10.7, 10.05),
                dcBar("d3", 10.6, 9.5, 10.5, 10.6, 10.7, 10.0),
                dcBar("d4", 10.5, 9.5, 10.5, 10.6, 10.7, 10.4),
                dcBar("d5", 10.5, 9.5, 10.5, 10.6, 10.7, 10.6)
        );
        MaCrossPointTools.Hit hit = MaCrossPointTools.findAnyGoldenCrossBreakHitOnBars(bars, PeriodTypeEnum.DAY);
        Assert.assertNotNull(hit);
        Assert.assertEquals(20, hit.getSlowMa());
        Assert.assertEquals(10.5, hit.getBreakLine(), 1e-6);
    }

    @Test
    public void findAnyGoldenCrossBreak_missWhenNoneEdgeBroken() {
        List<Trade> bars = Arrays.asList(
                dcBar("d1", 9.8, 10.0, 9.5, 9.4, 9.3, 10.3),
                dcBar("d2", 9.9, 10.0, 9.5, 9.4, 9.3, 10.2),
                dcBar("d3", 10.2, 10.0, 9.5, 9.4, 9.3, 10.15),
                dcBar("d4", 10.1, 10.0, 9.5, 9.4, 9.3, 10.2),
                dcBar("d5", 10.1, 10.0, 9.5, 9.4, 9.3, 10.3)
        );
        Assert.assertNull(MaCrossPointTools.findAnyGoldenCrossBreakHitOnBars(bars, PeriodTypeEnum.DAY));
    }

    @Test
    public void findAnyGoldenHighBreak_hitsGh10First() {
        List<Trade> bars = Arrays.asList(
                ghBar("d1", 10.0, 10.5, 9.9, 10.2, 9.5, 10.0, 12.0),
                ghBar("d2", 10.2, 10.8, 10.1, 10.5, 10.2, 10.0, 12.0),
                ghBar("d3", 10.5, 10.6, 10.0, 10.1, 10.3, 10.1, 12.0),
                ghBar("d4", 10.1, 10.3, 9.8, 10.0, 10.4, 10.2, 12.0),
                ghBar("d5", 10.0, 11.0, 9.9, 10.9, 10.5, 10.3, 12.0)
        );
        MaCrossPointTools.Hit hit = MaCrossPointTools.findAnyGoldenHighBreakHitOnBars(bars, PeriodTypeEnum.DAY);
        Assert.assertNotNull(hit);
        Assert.assertEquals(10, hit.getSlowMa());
        Assert.assertEquals(MaCrossPointTools.BreakTarget.GOLDEN_BAND_TOP, hit.getBreakTarget());
        Assert.assertEquals(10.8, hit.getBreakLine(), 1e-6);
        Assert.assertEquals("d2", hit.getCrossBar().getDay());
    }

    @Test
    public void findAnyGoldenHighBreak_fallsBackToGh20() {
        List<Trade> bars = Arrays.asList(
                ghBar("d1", 10.0, 10.5, 9.9, 10.2, 10.5, 10.0, 11.0),
                ghBar("d2", 10.2, 10.8, 10.1, 10.5, 11.2, 10.0, 11.0),
                ghBar("d3", 10.5, 10.6, 10.0, 10.1, 11.0, 10.1, 11.0),
                ghBar("d4", 10.1, 10.3, 9.8, 10.0, 11.1, 10.2, 11.0),
                ghBar("d5", 10.0, 11.0, 9.9, 10.9, 11.3, 10.3, 11.0)
        );
        MaCrossPointTools.Hit hit = MaCrossPointTools.findAnyGoldenHighBreakHitOnBars(bars, PeriodTypeEnum.DAY);
        Assert.assertNotNull(hit);
        Assert.assertEquals(20, hit.getSlowMa());
        Assert.assertEquals(10.8, hit.getBreakLine(), 1e-6);
        Assert.assertEquals("d2", hit.getCrossBar().getDay());
    }

    @Test
    public void findAnyGoldenHighBreak_missWhenNoneEdgeBroken() {
        List<Trade> bars = Arrays.asList(
                ghBar("d1", 10.0, 10.5, 9.9, 10.2, 9.5, 10.0, 12.0),
                ghBar("d2", 10.2, 10.8, 10.1, 10.5, 10.2, 10.0, 12.0),
                ghBar("d3", 10.5, 10.6, 10.0, 10.1, 10.3, 10.1, 12.0),
                ghBar("d4", 10.1, 10.3, 9.8, 10.0, 10.4, 10.2, 12.0),
                ghBar("d5", 10.0, 10.7, 9.9, 10.6, 10.5, 10.3, 12.0)
        );
        Assert.assertNull(MaCrossPointTools.findAnyGoldenHighBreakHitOnBars(bars, PeriodTypeEnum.DAY));
    }

    @Test
    public void findAnyDeathCrossBreak_missWhenNoneEdgeBroken() {
        List<Trade> bars = Arrays.asList(
                dcBar("d1", 10.2, 10.0, 10.1, 10.3, 10.4, 10.3),
                dcBar("d2", 10.1, 10.0, 10.05, 10.3, 10.4, 10.2),
                dcBar("d3", 9.8, 10.0, 9.9, 10.3, 10.4, 10.15),
                dcBar("d4", 9.9, 9.95, 9.85, 10.3, 10.4, 10.2),
                dcBar("d5", 9.9, 9.95, 9.9, 10.3, 10.4, 10.3)
        );
        Assert.assertNull(MaCrossPointTools.findAnyDeathCrossBreakHitOnBars(bars, PeriodTypeEnum.DAY));
    }

    @Test
    public void passesParentMdxGate_ma10GtMa60_passesEvenIfCloseBelowMaxMa() {
        Trade bar = flat("d1", 12.0, 11.0, 10.0);
        bar.setMa60(10.0);
        Assert.assertTrue(MaCrossPointTools.passesParentMdxGateOnParent(bar, null));
    }

    @Test
    public void passesParentMdxGate_ma10LeMa60_requiresCloseAboveMaxMa() {
        Trade above = flat("d1", 10.0, 10.2, 10.5);
        above.setMa60(10.5);
        Assert.assertTrue(MaCrossPointTools.passesParentMdxGateOnParent(above, null));
        Trade below = flat("d1", 10.5, 10.2, 10.3);
        below.setMa60(10.5);
        Assert.assertFalse(MaCrossPointTools.passesParentMdxGateOnParent(below, null));
    }

    @Test
    public void passesParentMdxGate_ma10EqMa60_requiresCloseAboveMaxMa() {
        Trade above = flat("d1", 10.0, 10.0, 10.5);
        above.setMa60(10.0);
        Assert.assertTrue(MaCrossPointTools.passesParentMdxGateOnParent(above, null));
        Trade below = flat("d1", 10.5, 10.0, 10.3);
        below.setMa60(10.0);
        Assert.assertFalse(MaCrossPointTools.passesParentMdxGateOnParent(below, null));
    }

    @Test
    public void passesParentMdxGate_unknownMa_fails() {
        Trade bar = flat("d1", 10.0, 10.2, 10.5);
        bar.setMa60(null);
        Assert.assertFalse(MaCrossPointTools.passesParentMdxGateOnParent(bar, null));
    }

    @Test
    public void findAnyDeathCrossBreak_ignoresDc30Only() {
        List<Trade> bars = Arrays.asList(
                dcBar("d1", 10.2, 11.0, 9.5, 10.0, 9.0, 10.1),
                dcBar("d2", 10.1, 11.0, 9.5, 10.0, 9.0, 10.05),
                dcBar("d3", 9.8, 11.0, 9.5, 10.0, 9.0, 9.9),
                dcBar("d4", 9.7, 11.0, 9.5, 10.0, 9.0, 9.85),
                dcBar("d5", 9.7, 11.0, 9.5, 10.0, 9.0, 10.1)
        );
        Assert.assertNull(MaCrossPointTools.findAnyDeathCrossBreakHitOnBars(bars, PeriodTypeEnum.DAY));
    }

    @Test
    public void passesCloseGeMaxMa_allowsEqual() {
        Trade eq = flat("d1", 10.0, 10.2, 10.2);
        Assert.assertTrue(MaCrossPointCore.passesCloseGeMaxMa(eq));
        Trade below = flat("d1", 10.0, 10.2, 10.1);
        Assert.assertFalse(MaCrossPointCore.passesCloseGeMaxMa(below));
    }

    @Test
    public void passesMacdOrMa5_acceptsMa5AboveOrMacdPositive() {
        Trade byMa = flat("d1", 10.5, 10.0, 10.2);
        byMa.setMacd(0);
        Assert.assertTrue(MaCrossPointCore.passesMacdOrMa5(byMa));
        Trade byMacd = flat("d1", 9.5, 10.0, 10.2);
        byMacd.setMacd(0.05);
        Assert.assertTrue(MaCrossPointCore.passesMacdOrMa5(byMacd));
        Trade miss = flat("d1", 9.5, 10.0, 10.2);
        miss.setMacd(0);
        Assert.assertFalse(MaCrossPointCore.passesMacdOrMa5(miss));
    }

    @Test
    public void findYangPierce_hitsWhenYangSpansMa5Ma10() {
        List<Trade> bars = Arrays.asList(
                ohlc("d1", 10.2, 10.4, 10.0, 10.1, 10.0, 10.2),
                ohlc("d2", 10.0, 10.6, 9.8, 10.5, 10.0, 10.2)
        );
        MaCrossPointTools.Hit hit = MaCrossPointTools.findYangPierceHitOnBars(bars, PeriodTypeEnum.DAY);
        Assert.assertNotNull(hit);
        Assert.assertEquals(MaCrossPointTools.BreakTarget.YANG_PIERCE, hit.getBreakTarget());
        Assert.assertEquals(10.2, hit.getBreakLine(), 1e-6);
    }

    @Test
    public void findYangPierce_missWhenNotYangOrLowAboveMinMa() {
        Assert.assertNull(MaCrossPointTools.findYangPierceHitOnBars(Arrays.asList(
                ohlc("d1", 10.5, 10.6, 9.8, 10.0, 10.0, 10.2)
        ), PeriodTypeEnum.DAY));
        Assert.assertNull(MaCrossPointTools.findYangPierceHitOnBars(Arrays.asList(
                ohlc("d1", 10.0, 10.6, 10.1, 10.5, 10.0, 10.2)
        ), PeriodTypeEnum.DAY));
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

    private static Trade dcBar(String day, double ma5, double ma10, double ma20, double ma30, double ma60,
                               double close) {
        Trade t = ohlc(day, close, close + 0.2, close - 0.2, close, ma5, ma10);
        t.setMa20(ma20);
        t.setMa30(ma30);
        t.setMa60(ma60);
        return t;
    }

    private static Trade ghBar(String day, double open, double high, double low, double close,
                               double ma5, double ma10, double ma20) {
        Trade t = ohlc(day, open, high, low, close, ma5, ma10);
        t.setMa20(ma20);
        t.setMa30(ma20 + 0.1);
        t.setMa60(ma20 + 0.2);
        return t;
    }
}
