package com.yh.bigdata.tts.spider.strategy.tools.bottomprev2high;



import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;

import com.yh.bigdata.tts.common.model.Trade;

import org.junit.Assert;

import org.junit.Test;



import java.util.Arrays;

import java.util.List;



public class BottomPrev2HighToolsTest {



    @Test

    public void findHitOnBars_breaksMaxOfPrevTwoHighs() {

        List<Trade> bars = Arrays.asList(

                bar("2026-01-01", 10.0, 10.5, 9.8, 10.2),

                bar("2026-01-02", 10.2, 10.8, 10.0, 10.75),

                bar("2026-01-03", 10.75, 10.9, 10.5, 10.85),

                bar("2026-01-04", 10.85, 11.0, 10.6, 10.95)

        );

        BottomPrev2HighTools.Hit hit = BottomPrev2HighTools.findHitOnBars(bars, PeriodTypeEnum.DAY);

        Assert.assertNotNull(hit);

        Assert.assertEquals("2026-01-04", hit.getSignalBar().getDay());

        Assert.assertEquals(11.0, hit.getBreakLine(), 1e-6);

    }



    @Test

    public void findHitOnBars_missWhenCloseBelowBreakLine() {

        List<Trade> bars = Arrays.asList(

                bar("2026-01-01", 10.0, 10.5, 9.8, 10.2),

                bar("2026-01-02", 10.2, 10.8, 10.0, 10.75),

                bar("2026-01-03", 10.75, 10.9, 10.5, 10.85),

                bar("2026-01-04", 10.85, 10.98, 10.6, 10.92)

        );

        Assert.assertNull(BottomPrev2HighTools.findHitOnBars(bars, PeriodTypeEnum.DAY));

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

}

