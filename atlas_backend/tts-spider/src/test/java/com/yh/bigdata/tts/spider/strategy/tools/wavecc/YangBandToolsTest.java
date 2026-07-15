package com.yh.bigdata.tts.spider.strategy.tools.wavecc;

import com.yh.bigdata.tts.common.model.Trade;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class YangBandToolsTest {

    @Test
    public void findCompleteBands_twoBandsInLookback() {
        List<Trade> bars = Arrays.asList(
                bar("2026-01-01", 10.0, 10.5),
                bar("2026-01-02", 10.5, 10.2),
                yang("2026-01-03", 10.2, 10.8, 10.0, 10.6),
                yin("2026-01-04", 10.6, 10.3, 10.7, 10.1),
                yang("2026-01-05", 10.3, 11.0, 10.2, 10.9),
                yang("2026-01-06", 10.9, 11.2, 10.8, 11.1),
                yin("2026-01-07", 11.1, 10.8, 11.3, 10.7),
                yang("2026-01-08", 10.8, 11.0, 10.6, 10.9)
        );

        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(bars, 20);
        Assert.assertEquals(2, bands.size());
        Assert.assertEquals("2026-01-03", bands.get(0).getFirstYang().getDay());
        Assert.assertEquals("2026-01-05", bands.get(1).getFirstYang().getDay());
        Assert.assertEquals("2026-01-06", bands.get(1).getLastYang().getDay());
    }

    @Test
    public void findLastCompleteBand_ignoresIncompleteTrailingYang() {
        List<Trade> bars = Arrays.asList(
                yang("2026-01-01", 10.0, 10.5, 9.8, 10.4),
                yang("2026-01-02", 10.4, 10.8, 10.2, 10.7),
                yin("2026-01-03", 10.7, 10.4, 10.8, 10.3),
                yang("2026-01-04", 10.4, 10.9, 10.3, 10.8)
        );

        YangBandTools.CompleteYangBand band = YangBandTools.findLastCompleteBand(bars, 20);
        Assert.assertNotNull(band);
        Assert.assertEquals("2026-01-01", band.getFirstYang().getDay());
        Assert.assertEquals("2026-01-02", band.getLastYang().getDay());
    }

    @Test
    public void bandLow_isFirstYangLow() {
        List<Trade> bars = Arrays.asList(
                yang("2026-01-01", 10.0, 10.5, 9.5, 10.4),
                yang("2026-01-02", 10.4, 10.8, 9.0, 10.7),
                yin("2026-01-03", 10.7, 10.4, 10.8, 10.3),
                yang("2026-01-04", 10.4, 10.9, 10.3, 10.8)
        );

        YangBandTools.CompleteYangBand band = YangBandTools.findLastCompleteBand(bars, 20);
        Assert.assertNotNull(band);
        Assert.assertEquals(9.5, band.getBandLow(), 1e-6);
    }

    @Test
    public void bandHigh_includesTerminatorHigh() {
        List<Trade> bars = Arrays.asList(
                yang("2026-01-01", 10.0, 10.5, 9.8, 10.4),
                yang("2026-01-02", 10.4, 10.8, 10.2, 10.7),
                yin("2026-01-03", 10.7, 10.4, 11.5, 10.3),
                yang("2026-01-04", 10.4, 10.9, 10.3, 10.8)
        );

        YangBandTools.CompleteYangBand band = YangBandTools.findLastCompleteBand(bars, 20);
        Assert.assertNotNull(band);
        Assert.assertEquals(11.5, band.getBandHigh(), 1e-6);
        Assert.assertEquals("2026-01-03", band.getTerminatorBar().getDay());
        Assert.assertEquals("2026-01-03", band.getBandHighBar().getDay());
    }

    private static Trade bar(String day, double open, double close) {
        return yang(day, open, close, Math.min(open, close), Math.max(open, close));
    }

    private static Trade yang(String day, double open, double high, double low, double close) {
        Trade t = new Trade();
        t.setDay(day);
        t.setOpen(open);
        t.setHigh(high);
        t.setLow(low);
        t.setClose(close);
        return t;
    }

    private static Trade yin(String day, double open, double high, double low, double close) {
        return yang(day, open, high, low, close);
    }
}
