package com.yh.bigdata.tts.spider.strategy.tools.macdgcwave;

import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class MacdGcWaveBandToolsTest {

    @Test
    public void yangGoldenCrossUsesBandContainingGcBar() {
        List<Trade> trades = Arrays.asList(
                bar("d1", 10, 11, 11.5, 10),
                bar("d2", 11, 12, 12.5, 11),
                bar("d3", 12, 11.5, 13, 11),
                bar("d4", 11.2, 11.5, 11.8, 11),
                bar("d5", 12.9, 12.9, 13, 12.5));
        YangBandTools.CompleteYangBand band = MacdGcWaveBandTools.resolveReferenceBand(
                trades, trades.get(1), 20);
        Assert.assertNotNull(band);
        Assert.assertEquals("d1", band.getFirstYang().getDay());
        Assert.assertEquals("d3", band.getLastYang().getDay());
        Assert.assertEquals("d4", band.getTerminatorBar().getDay());
        Assert.assertEquals(13.0, band.getBandHigh(), 1e-6);
    }

    @Test
    public void yinGoldenCrossAsTerminatorUsesBandEndingAtGcBar() {
        List<Trade> trades = Arrays.asList(
                bar("d1", 10, 11, 11.5, 10),
                bar("d2", 11, 12, 12.5, 11),
                bar("d3", 12, 11.5, 13, 11),
                bar("d4", 11.5, 11.2, 11.5, 11),
                bar("d5", 12.9, 12.9, 13, 12.5));
        YangBandTools.CompleteYangBand band = MacdGcWaveBandTools.resolveReferenceBand(
                trades, trades.get(3), 20);
        Assert.assertNotNull(band);
        Assert.assertEquals("d1", band.getFirstYang().getDay());
        Assert.assertEquals("d3", band.getLastYang().getDay());
        Assert.assertEquals("d4", band.getTerminatorBar().getDay());
        Assert.assertEquals(13.0, band.getBandHigh(), 1e-6);
    }

    /** sh603077 形态：07-14 阳 + 07-15 阴 K 金叉完结，应取该波段而非更早的 07-10 波段。 */
    @Test
    public void yinGoldenCrossSh603077PatternUsesNearestBand() {
        List<Trade> trades = Arrays.asList(
                bar("2026-07-08", 2.23, 2.18, 2.24, 2.16),
                bar("2026-07-09", 2.17, 2.15, 2.19, 2.08),
                bar("2026-07-10", 2.15, 2.16, 2.20, 2.10),
                bar("2026-07-13", 2.15, 2.08, 2.17, 2.07),
                bar("2026-07-14", 2.16, 2.29, 2.29, 2.15),
                bar("2026-07-15", 2.33, 2.32, 2.39, 2.29),
                bar("2026-07-17", 2.23, 2.25, 2.29, 2.22));
        YangBandTools.CompleteYangBand band = MacdGcWaveBandTools.resolveReferenceBand(
                trades, trades.get(5), 80);
        Assert.assertNotNull(band);
        Assert.assertEquals("2026-07-14", band.getFirstYang().getDay());
        Assert.assertEquals("2026-07-14", band.getLastYang().getDay());
        Assert.assertEquals("2026-07-15", band.getTerminatorBar().getDay());
        Assert.assertEquals(2.39, band.getBandHigh(), 1e-6);
        Assert.assertEquals("2026-07-15", band.getBandHighBar().getDay());
    }

    @Test
    public void yinGoldenCrossNotTerminatorFallsBackToPreviousBand() {
        List<Trade> trades = Arrays.asList(
                bar("d1", 10, 11, 11.5, 10),
                bar("d2", 11, 12, 12.5, 11),
                bar("d3", 12, 11.5, 13, 11),
                bar("d4", 11.5, 11.2, 11.5, 11),
                bar("d5", 11.1, 11.0, 11.2, 10.9),
                bar("d6", 12.9, 12.9, 13, 12.5));
        YangBandTools.CompleteYangBand band = MacdGcWaveBandTools.resolveReferenceBand(
                trades, trades.get(4), 20);
        Assert.assertNotNull(band);
        Assert.assertEquals("d1", band.getFirstYang().getDay());
        Assert.assertEquals("d3", band.getLastYang().getDay());
        Assert.assertEquals("d4", band.getTerminatorBar().getDay());
    }

    private static Trade bar(String day, double open, double close, double high, double low) {
        Trade t = new Trade();
        t.setDay(day);
        t.setOpen(open);
        t.setClose(close);
        t.setHigh(high);
        t.setLow(low);
        return t;
    }
}
