package com.yh.bigdata.tts.spider.strategy.tools.trendrelayprevhigh;



import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;

import com.yh.bigdata.tts.common.model.Trade;

import org.junit.Assert;

import org.junit.Test;



import java.util.Arrays;

import java.util.List;



public class TrendRelayPrevHighToolsTest {



    @Test

    public void findHitOnBars_noRetestBreaksPrevHigh() {

        List<Trade> bars = Arrays.asList(

                yang("2026-01-01", 10.0, 11.0, 9.5, 10.8),

                yin("2026-01-02", 10.8, 10.5, 11.0, 10.4),

                yang("2026-01-03", 10.3, 10.6, 10.0, 10.5),

                yang("2026-01-04", 10.5, 10.9, 10.2, 10.85),

                yang("2026-01-05", 10.85, 11.0, 10.5, 10.95)

        );

        TrendRelayPrevHighTools.Hit hit = TrendRelayPrevHighTools.findHitOnBars(bars, PeriodTypeEnum.DAY, 20);

        Assert.assertNotNull(hit);

        Assert.assertEquals("2026-01-05", hit.getSignalBar().getDay());

        Assert.assertEquals(10.9, hit.getPrevHigh(), 1e-6);

    }



    @Test

    public void findHitOnBars_missWhenNoBreakPrevHigh() {

        List<Trade> bars = Arrays.asList(

                yang("2026-01-01", 10.0, 11.0, 9.5, 10.8),

                yin("2026-01-02", 10.8, 10.5, 11.0, 10.4),

                yang("2026-01-03", 10.3, 10.6, 10.0, 10.5),

                yang("2026-01-04", 10.5, 10.9, 10.2, 10.85),

                yang("2026-01-05", 10.85, 10.88, 10.5, 10.87)

        );

        Assert.assertNull(TrendRelayPrevHighTools.findHitOnBars(bars, PeriodTypeEnum.DAY, 20));

    }



    @Test

    public void findHitOnBars_missWhenLastBarNotYang() {

        List<Trade> bars = Arrays.asList(

                yang("2026-01-01", 10.0, 11.0, 9.5, 10.8),

                yin("2026-01-02", 10.8, 10.5, 11.0, 10.4),

                yang("2026-01-03", 10.3, 10.6, 10.0, 10.5),

                yang("2026-01-04", 10.5, 10.9, 10.2, 10.85),

                yin("2026-01-05", 10.85, 11.0, 10.5, 10.84)

        );

        Assert.assertNull(TrendRelayPrevHighTools.findHitOnBars(bars, PeriodTypeEnum.DAY, 20));

    }



    @Test

    public void findHitOnBars_missWhenCloseAboveBandHigh() {

        List<Trade> bars = Arrays.asList(

                yang("2026-01-01", 10.0, 11.0, 9.5, 10.8),

                yin("2026-01-02", 10.8, 10.5, 11.0, 10.4),

                yang("2026-01-03", 10.3, 10.6, 10.0, 10.5),

                yang("2026-01-04", 10.5, 10.9, 10.2, 10.85),

                yang("2026-01-05", 10.85, 11.5, 10.5, 11.2)

        );

        Assert.assertNull(TrendRelayPrevHighTools.findHitOnBars(bars, PeriodTypeEnum.DAY, 20));

    }



    @Test

    public void findHitOnBars_missWhenRetestLow() {

        List<Trade> bars = Arrays.asList(

                yang("2026-01-01", 10.0, 11.0, 9.5, 10.8),

                yin("2026-01-02", 10.8, 10.5, 11.0, 10.4),

                yang("2026-01-03", 10.3, 10.6, 9.4, 10.5),

                yang("2026-01-04", 10.5, 10.9, 10.2, 10.85),

                yang("2026-01-05", 10.85, 11.0, 10.5, 10.95)

        );

        Assert.assertNull(TrendRelayPrevHighTools.findHitOnBars(bars, PeriodTypeEnum.DAY, 20));

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

