package com.yh.bigdata.tts.spider.strategy.tools.waveperiodgate;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.WavePeriodGateStrategyParams;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class WavePeriodGateToolsTest {

    @Test
    public void sz301099WeekConvexWithConcaveOnlyShouldFail() {
        List<Trade> weekBars = loadSz301099WeekBars();
        Trade current = weekBars.get(weekBars.size() - 1);
        Trade prev = weekBars.get(weekBars.size() - 2);

        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(weekBars, 52);
        Assert.assertTrue(bands.size() >= 2);

        YangBandTools.CompleteYangBand lastBand = bands.get(bands.size() - 1);
        YangBandTools.CompleteYangBand prevBand = bands.get(bands.size() - 2);
        Assert.assertTrue(WaveShapeTools.isConvex(lastBand, prevBand));

        double maxLow = Math.max(lastBand.getBandLow(), prevBand.getBandLow());
        Assert.assertTrue(current.getClose() > maxLow);
        Assert.assertFalse(current.getClose() > prev.getHigh());

        WavePeriodGateStrategyParams params = WavePeriodGateStrategyParams.builder()
                .tier(WavePeriodGateStrategyParams.Tier.WEEK)
                .enableConcaveBreakout(true)
                .enableConvexBreakout(false)
                .build();

        Assert.assertFalse(WavePeriodGateTools.passesTierGateOnBars(
                weekBars, null, null, PeriodTypeEnum.WEEK, 52, params));
    }

    @Test
    public void convexFirstBreakFailsWhenPrevAlreadyClosedAbovePrevPrevHigh() {
        List<Trade> bars = new ArrayList<>();
        bars.add(bar("2026-07-06", 59, 61, 58, 60));
        bars.add(bar("2026-07-07", 60, 62, 59, 61));
        bars.add(bar("2026-07-08", 61, 69.49, 60, 68.22));
        bars.add(bar("2026-07-09", 68.22, 69.88, 65.66, 69.5));
        bars.add(bar("2026-07-10", 69.81, 71.39, 68.14, 70.21));
        WavePeriodGateStrategyParams params = WavePeriodGateStrategyParams.builder()
                .tier(WavePeriodGateStrategyParams.Tier.DAY)
                .enableConcaveBreakout(false)
                .enableConvexBreakout(true)
                .build();
        Assert.assertFalse(WavePeriodGateTools.passesTierGateOnBars(
                bars, null, null, PeriodTypeEnum.DAY, 120, params));
    }

    @Test
    public void convexFirstBreakPassesWhenPrevStillBelowPrevPrevHigh() {
        List<Trade> bars = new ArrayList<>();
        bars.add(bar("2026-07-06", 10, 12, 9, 11));
        bars.add(bar("2026-07-07", 11, 13, 10, 10.5));
        bars.add(bar("2026-07-08", 10.5, 14, 10, 13.5));
        bars.add(bar("2026-07-09", 13.5, 15, 13, 14.5));
        bars.add(bar("2026-07-10", 14.5, 16.5, 14, 16.0));
        WavePeriodGateStrategyParams params = WavePeriodGateStrategyParams.builder()
                .tier(WavePeriodGateStrategyParams.Tier.DAY)
                .enableConcaveBreakout(false)
                .enableConvexBreakout(true)
                .build();
        Assert.assertTrue(WavePeriodGateTools.passesTierGateOnBars(
                bars, null, null, PeriodTypeEnum.DAY, 120, params));
    }

    @Test
    public void sz301099WeekConvexWithConvexEnabledShouldFailBreakout() {
        List<Trade> weekBars = loadSz301099WeekBars();
        WavePeriodGateStrategyParams params = WavePeriodGateStrategyParams.builder()
                .tier(WavePeriodGateStrategyParams.Tier.WEEK)
                .enableConcaveBreakout(false)
                .enableConvexBreakout(true)
                .build();
        Assert.assertFalse(WavePeriodGateTools.passesTierGateOnBars(
                weekBars, null, null, PeriodTypeEnum.WEEK, 52, params));
    }

    @Test
    public void dayConcavePassesOnBreakoutGateOnly() {
        List<Trade> bars = buildConcaveDayEdgeBars();
        WavePeriodGateStrategyParams params = WavePeriodGateStrategyParams.builder()
                .tier(WavePeriodGateStrategyParams.Tier.DAY)
                .build();
        Assert.assertTrue(WavePeriodGateTools.passesBreakoutGateOnBars(
                bars, null, null, PeriodTypeEnum.DAY, 120, params));
    }

    @Test
    public void dayConcavePassesOnEdgeBreakout() {
        List<Trade> bars = buildConcaveDayEdgeBars();
        WavePeriodGateStrategyParams params = WavePeriodGateStrategyParams.builder()
                .tier(WavePeriodGateStrategyParams.Tier.DAY)
                .build();
        Assert.assertTrue(WavePeriodGateTools.passesTierGateOnBars(
                bars, null, null, PeriodTypeEnum.DAY, 120, params));
    }

    @Test
    public void failsWhenSignalBarIsYin() {
        List<Trade> bars = buildConcaveDayEdgeBars();
        Trade signal = bars.get(bars.size() - 1);
        signal.setOpen(45.0);
        signal.setClose(44.0);
        WavePeriodGateStrategyParams params = WavePeriodGateStrategyParams.builder()
                .tier(WavePeriodGateStrategyParams.Tier.DAY)
                .build();
        Assert.assertFalse(WavePeriodGateTools.passesTierGateOnBars(
                bars, null, null, PeriodTypeEnum.DAY, 120, params));
    }

    @Test
    public void passesWhenSignalBarIsFlat() {
        List<Trade> bars = buildConcaveDayEdgeBars();
        Trade signal = bars.get(bars.size() - 1);
        signal.setOpen(43.0);
        signal.setClose(43.0);
        WavePeriodGateStrategyParams params = WavePeriodGateStrategyParams.builder()
                .tier(WavePeriodGateStrategyParams.Tier.DAY)
                .build();
        Assert.assertTrue(WavePeriodGateTools.passesTierGateOnBars(
                bars, null, null, PeriodTypeEnum.DAY, 120, params));
    }

    @Test
    public void dayConcaveFailsWhenPrevCloseAlreadyAboveBandHigh() {
        List<Trade> bars = buildConcaveDayEdgeBars();
        Trade prev = bars.get(bars.size() - 2);
        Trade signal = bars.get(bars.size() - 1);
        prev.setClose(42.0);
        signal.setClose(43.0);
        WavePeriodGateStrategyParams params = WavePeriodGateStrategyParams.builder()
                .tier(WavePeriodGateStrategyParams.Tier.DAY)
                .build();
        Assert.assertFalse(WavePeriodGateTools.passesTierGateOnBars(
                bars, null, null, PeriodTypeEnum.DAY, 120, params));
    }

    @Test
    public void weekConcavePassesOnEdgeBreakout() {
        List<Trade> bars = buildConcaveWeekEdgeBars();
        WavePeriodGateStrategyParams params = WavePeriodGateStrategyParams.builder()
                .tier(WavePeriodGateStrategyParams.Tier.WEEK)
                .build();
        Assert.assertTrue(WavePeriodGateTools.passesTierGateOnBars(
                bars, null, null, PeriodTypeEnum.WEEK, 52, params));
    }

    @Test
    public void upperPeriodTwoBandsPassWhenCloseAboveMinLow() {
        List<Trade> weekBars = buildTwoBandBars(8.0, 12.0, 15.0);
        WavePeriodGateTools.UpperPeriodContext ctx =
                new WavePeriodGateTools.UpperPeriodContext(PeriodTypeEnum.WEEK, 20);
        Assert.assertTrue(WavePeriodGateTools.passesUpperPeriodGateOnBars(weekBars, null, ctx));
    }

    @Test
    public void upperPeriodTwoBandsFailWhenCloseBelowMinLow() {
        List<Trade> weekBars = buildTwoBandBars(8.0, 12.0, 7.0);
        WavePeriodGateTools.UpperPeriodContext ctx =
                new WavePeriodGateTools.UpperPeriodContext(PeriodTypeEnum.WEEK, 20);
        Assert.assertFalse(WavePeriodGateTools.passesUpperPeriodGateOnBars(weekBars, null, ctx));
    }

    @Test
    public void upperPeriodOneBandUsesSingleLow() {
        List<Trade> weekBars = buildOneBandBars(10.0, 11.0);
        WavePeriodGateTools.UpperPeriodContext ctx =
                new WavePeriodGateTools.UpperPeriodContext(PeriodTypeEnum.WEEK, 20);
        Assert.assertTrue(WavePeriodGateTools.passesUpperPeriodGateOnBars(weekBars, null, ctx));
        weekBars.get(weekBars.size() - 1).setClose(9.0);
        Assert.assertFalse(WavePeriodGateTools.passesUpperPeriodGateOnBars(weekBars, null, ctx));
    }

    @Test
    public void upperPeriodNoBandRequiresLastYang() {
        List<Trade> weekBars = new ArrayList<>();
        weekBars.add(bar("2026-01-01", 10, 11, 9, 9.5));
        weekBars.add(bar("2026-01-08", 9.5, 10, 9, 9.2));
        weekBars.add(bar("2026-01-15", 9.2, 10.5, 9, 10.0));
        WavePeriodGateTools.UpperPeriodContext ctx =
                new WavePeriodGateTools.UpperPeriodContext(PeriodTypeEnum.WEEK, 20);
        Assert.assertTrue(WavePeriodGateTools.passesUpperPeriodGateOnBars(weekBars, null, ctx));
        weekBars.get(weekBars.size() - 1).setClose(9.0);
        Assert.assertFalse(WavePeriodGateTools.passesUpperPeriodGateOnBars(weekBars, null, ctx));
    }

    @Test
    public void tierMacdGateSkipsWhenDisabled() {
        WavePeriodGateStrategyParams params = WavePeriodGateStrategyParams.builder()
                .enableTierMacdPositiveGate(false)
                .build();
        Assert.assertTrue(WavePeriodGateTools.passesTierMacdPositiveGate(null, null, params));
    }

    @Test
    public void resolveUpperPeriodMapsTierToParent() {
        WavePeriodGateStrategyParams day = WavePeriodGateStrategyParams.builder()
                .tier(WavePeriodGateStrategyParams.Tier.DAY).build();
        Assert.assertEquals(PeriodTypeEnum.WEEK, WavePeriodGateTools.resolveUpperPeriod(day).getPeriod());
        WavePeriodGateStrategyParams week = WavePeriodGateStrategyParams.builder()
                .tier(WavePeriodGateStrategyParams.Tier.WEEK).build();
        Assert.assertEquals(PeriodTypeEnum.MONTH, WavePeriodGateTools.resolveUpperPeriod(week).getPeriod());
        WavePeriodGateStrategyParams month = WavePeriodGateStrategyParams.builder()
                .tier(WavePeriodGateStrategyParams.Tier.MONTH).build();
        Assert.assertEquals(PeriodTypeEnum.YEAR, WavePeriodGateTools.resolveUpperPeriod(month).getPeriod());
    }

    private static List<Trade> buildTwoBandBars(double band1Low, double band2Low, double lastClose) {
        List<Trade> bars = new ArrayList<>();
        bars.add(bar("2026-01-01", band1Low + 1, band1Low + 3, band1Low, band1Low + 2));
        bars.add(bar("2026-01-08", band1Low + 2, band1Low + 2.5, band1Low, band1Low));
        bars.add(bar("2026-01-15", band2Low + 1, band2Low + 3, band2Low, band2Low + 2));
        bars.add(bar("2026-01-22", band2Low + 2, band2Low + 2.5, band2Low, band2Low));
        bars.add(bar("2026-01-29", lastClose, lastClose + 1, lastClose - 1, lastClose));
        return bars;
    }

    private static List<Trade> buildOneBandBars(double bandLow, double lastClose) {
        List<Trade> bars = new ArrayList<>();
        bars.add(bar("2026-01-01", bandLow + 1, bandLow + 3, bandLow, bandLow + 2));
        bars.add(bar("2026-01-08", bandLow + 2, bandLow + 2.5, bandLow, bandLow));
        bars.add(bar("2026-01-15", lastClose, lastClose + 1, lastClose - 1, lastClose));
        return bars;
    }

    private static List<Trade> buildConcaveDayEdgeBars() {
        List<Trade> bars = buildConcaveDayBars();
        Trade prev = bars.get(bars.size() - 2);
        Trade signal = bars.get(bars.size() - 1);
        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(bars, 120);
        YangBandTools.CompleteYangBand lastBand = bands.get(bands.size() - 1);
        double line = lastBand.getBandHigh();
        prev.setClose(line - 0.5);
        signal.setClose(line + 1.0);
        return bars;
    }

    private static List<Trade> buildConcaveWeekEdgeBars() {
        List<Trade> bars = buildConcaveWeekBarsForSecondBreakout();
        Trade prev = bars.get(bars.size() - 2);
        Trade signal = bars.get(bars.size() - 1);
        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(bars, 52);
        YangBandTools.CompleteYangBand lastBand = bands.get(bands.size() - 1);
        double line = lastBand.getBandHigh();
        prev.setClose(line - 0.5);
        signal.setClose(line + 1.0);
        return bars;
    }

    private static List<Trade> buildConcaveDayBars() {
        List<Trade> bars = new ArrayList<>();
        bars.add(bar("2026-01-01", 10, 12, 9, 11));
        bars.add(bar("2026-01-02", 11, 13, 10, 10.5));
        bars.add(bar("2026-01-03", 10.5, 14, 10, 13.5));
        bars.add(bar("2026-01-06", 13.5, 15, 13, 14.5));
        bars.add(bar("2026-01-07", 14.5, 16, 14, 15.5));
        bars.add(bar("2026-01-08", 15.5, 17, 15, 16.5));
        bars.add(bar("2026-01-09", 16.5, 18, 16, 17.5));
        bars.add(bar("2026-01-10", 17.5, 20, 17, 19.5));
        bars.add(bar("2026-01-13", 19.5, 21, 19, 20.5));
        bars.add(bar("2026-01-14", 20.5, 22, 20, 21.5));
        bars.add(bar("2026-01-15", 21.5, 23, 21, 22.5));
        bars.add(bar("2026-01-16", 22.5, 24, 22, 23.5));
        bars.add(bar("2026-01-17", 23.5, 25, 23, 24.5));
        bars.add(bar("2026-01-20", 24.5, 26, 24, 25.5));
        bars.add(bar("2026-01-21", 25.5, 27, 25, 26.5));
        bars.add(bar("2026-01-22", 26.5, 28, 26, 27.5));
        bars.add(bar("2026-01-23", 27.5, 29, 27, 28.5));
        bars.add(bar("2026-01-24", 28.5, 30, 28, 29.5));
        bars.add(bar("2026-01-27", 29.5, 31, 29, 30.5));
        bars.add(bar("2026-01-28", 30.5, 32, 30, 31.5));
        bars.add(bar("2026-01-29", 31.5, 33, 31, 32.5));
        bars.add(bar("2026-01-30", 32.5, 34, 32, 33.5));
        bars.add(bar("2026-01-31", 33.5, 35, 33, 34.5));
        bars.add(bar("2026-02-03", 34.5, 36, 34, 35.5));
        bars.add(bar("2026-02-04", 35.5, 38, 35, 37.5));
        bars.add(bar("2026-02-05", 37.5, 39, 37, 38.5));
        bars.add(bar("2026-02-06", 38.5, 40, 38, 39.5));
        bars.add(bar("2026-02-07", 39.5, 42, 39, 41.5));
        return bars;
    }

    private static List<Trade> buildConcaveWeekBarsForSecondBreakout() {
        List<Trade> bars = new ArrayList<>();
        bars.add(bar("2025-01-03", 10, 12, 9, 11));
        bars.add(bar("2025-01-10", 11, 13, 10, 10.5));
        bars.add(bar("2025-01-17", 10.5, 14, 10, 13.5));
        bars.add(bar("2025-01-24", 13.5, 15, 13, 14.5));
        bars.add(bar("2025-01-31", 14.5, 16, 14, 15.5));
        bars.add(bar("2025-02-07", 15.5, 17, 15, 16.5));
        bars.add(bar("2025-02-14", 16.5, 18, 16, 17.5));
        bars.add(bar("2025-02-21", 17.5, 20, 17, 19.5));
        bars.add(bar("2025-02-28", 19.5, 21, 19, 20.5));
        bars.add(bar("2025-03-07", 20.5, 22, 20, 21.5));
        bars.add(bar("2025-03-14", 21.5, 23, 21, 22.5));
        bars.add(bar("2025-03-21", 22.5, 24, 22, 23.5));
        bars.add(bar("2025-03-28", 23.5, 25, 23, 24.5));
        bars.add(bar("2025-04-04", 24.5, 26, 24, 25.5));
        bars.add(bar("2025-04-11", 25.5, 27, 25, 26.5));
        bars.add(bar("2025-04-18", 26.5, 28, 26, 27.5));
        bars.add(bar("2025-04-25", 27.5, 29, 27, 28.5));
        bars.add(bar("2025-05-02", 28.5, 30, 28, 29.5));
        bars.add(bar("2025-05-09", 29.5, 31, 29, 30.5));
        bars.add(bar("2025-05-16", 30.5, 32, 30, 31.5));
        bars.add(bar("2025-05-23", 31.5, 33, 31, 32.5));
        bars.add(bar("2025-05-30", 32.5, 34, 32, 33.5));
        bars.add(bar("2025-06-06", 33.5, 35, 33, 34.5));
        bars.add(bar("2025-06-13", 34.5, 36, 34, 35.5));
        bars.add(bar("2025-06-20", 35.5, 38, 35, 37.5));
        bars.add(bar("2025-06-27", 37.5, 39, 37, 38.5));
        bars.add(bar("2025-07-04", 38.5, 40, 38, 39.5));
        bars.add(bar("2025-07-11", 39.5, 42, 39, 41.5));
        bars.add(bar("2025-07-18", 41.0, 41.5, 39.0, 39.5));
        bars.add(bar("2025-07-25", 39.5, 43, 39, 42.5));
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

    private static List<Trade> loadSz301099WeekBars() {
        double[][] rows = {
                {36.3687, 37.1813, 34.2867, 35.1922},
                {35.1768, 35.8965, 33.7681, 33.8223},
                {33.8301, 34.1938, 32.6691, 33.0251},
                {32.8316, 34.5808, 32.7233, 33.9694},
                {34.1164, 34.6505, 32.3827, 32.4679},
                {32.3905, 34.2944, 32.2434, 33.0096},
                {32.5917, 36.3687, 32.3518, 35.4012},
                {35.5173, 37.0498, 34.9833, 36.1984},
                {36.0281, 36.5389, 34.813, 35.5483},
                {35.3702, 37.7618, 35.3702, 36.7634},
                {36.5699, 36.686, 35.4399, 36.3996},
                {36.4925, 36.6628, 35.169, 36.1055},
                {35.7959, 40.1688, 35.6024, 37.7773},
                {37.7076, 40.0, 37.6844, 39.44},
                {39.4, 40.45, 39.08, 40.1},
                {40.68, 41.98, 38.62, 39.6},
                {39.68, 40.36, 35.28, 36.73},
                {36.88, 41.3, 36.33, 40.52},
                {44.44, 45.36, 39.51, 40.22},
                {40.2, 41.66, 39.28, 39.65},
                {42.71, 45.3, 41.8, 43.28},
                {44.26, 45.48, 41.62, 42.01},
                {39.3, 55.6, 38.3, 52.29},
                {52.23, 59.02, 48.26, 53.5},
                {53.39, 53.75, 46.46, 47.21},
                {46.75, 49.47, 45.6, 47.77},
                {48.3, 50.96, 44.89, 44.94},
                {45.09, 46.63, 40.38, 40.6},
                {41.2, 43.25, 40.61, 43.11},
                {43.3, 43.55, 41.65, 42.88},
                {43.03, 44.17, 41.61, 42.4},
                {42.0, 42.48, 40.21, 41.58},
                {41.85, 44.59, 41.85, 43.6},
                {43.85, 44.11, 42.38, 42.56},
                {43.0, 51.69, 43.0, 46.92},
                {47.14, 51.16, 46.34, 50.77},
                {50.28, 53.88, 46.93, 52.15},
                {52.1, 54.18, 49.2, 50.57},
                {49.74, 50.19, 44.1, 44.91},
                {45.8, 47.08, 45.8, 46.11},
                {46.66, 49.76, 44.66, 46.89},
                {46.01, 46.64, 41.78, 43.68},
                {42.66, 44.5, 41.9, 42.45},
                {42.43, 55.1, 42.05, 48.95},
                {47.78, 50.33, 45.47, 47.51},
                {46.5, 48.53, 43.17, 43.5},
                {44.32, 54.35, 43.68, 53.01},
                {52.5, 55.32, 51.7, 53.88},
                {54.12, 59.93, 52.9, 53.59},
                {53.14, 62.12, 53.14, 58.47},
                {60.19, 66.28, 59.56, 64.05},
                {65.0, 66.94, 61.37, 62.23},
                {62.26, 67.12, 60.86, 64.25},
                {64.9, 74.0, 63.8, 66.44},
                {65.17, 82.88, 64.35, 79.84},
                {74.7, 81.6, 67.56, 67.69},
                {69.04, 95.34, 69.04, 93.32},
                {92.8, 98.0, 86.28, 91.74},
                {91.74, 100.65, 86.89, 87.11},
                {88.35, 103.63, 76.3, 95.6}
        };
        String[] days = {
                "2025-05-16", "2025-05-23", "2025-05-30", "2025-06-06", "2025-06-13", "2025-06-20",
                "2025-06-27", "2025-07-04", "2025-07-11", "2025-07-18", "2025-07-25", "2025-08-01",
                "2025-08-08", "2025-08-15", "2025-08-22", "2025-08-29", "2025-09-05", "2025-09-12",
                "2025-09-19", "2025-09-26", "2025-10-03", "2025-10-10", "2025-10-17", "2025-10-24",
                "2025-10-31", "2025-11-07", "2025-11-14", "2025-11-21", "2025-11-28", "2025-12-05",
                "2025-12-12", "2025-12-19", "2025-12-26", "2026-01-02", "2026-01-09", "2026-01-16",
                "2026-01-23", "2026-01-30", "2026-02-06", "2026-02-13", "2026-02-27", "2026-03-06",
                "2026-03-13", "2026-03-20", "2026-03-27", "2026-04-03", "2026-04-10", "2026-04-17",
                "2026-04-24", "2026-05-01", "2026-05-08", "2026-05-15", "2026-05-22", "2026-05-29",
                "2026-06-05", "2026-06-12", "2026-06-19", "2026-06-26", "2026-07-03", "2026-07-10"
        };
        List<Trade> bars = new ArrayList<>();
        for (int i = 0; i < rows.length; i++) {
            Trade t = new Trade();
            t.setDay(days[i]);
            t.setOpen(rows[i][0]);
            t.setHigh(rows[i][1]);
            t.setLow(rows[i][2]);
            t.setClose(rows[i][3]);
            bars.add(t);
        }
        return bars;
    }
}
