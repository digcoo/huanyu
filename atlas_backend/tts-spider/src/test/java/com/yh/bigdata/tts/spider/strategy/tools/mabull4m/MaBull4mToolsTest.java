package com.yh.bigdata.tts.spider.strategy.tools.mabull4m;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class MaBull4mToolsTest {

    @Test
    public void findHitOnBars_hitsWhen4mYangAndCloseAbovePrevLow() {
        List<Trade> bars = Arrays.asList(
                bar("d1", 10.0, 10.2, 9.8, 10.0, 10.0, 9.9, 9.8, 9.7),
                bar("d2", 10.0, 10.5, 9.9, 10.4, 10.3, 10.2, 10.1, 10.0)
        );
        MaBull4mTools.Hit hit = MaBull4mTools.findHitOnBars(bars, PeriodTypeEnum.DAY);
        Assert.assertNotNull(hit);
        Assert.assertEquals(9.8, hit.getPrevLow(), 1e-6);
    }

    @Test
    public void findHitOnBars_missWhenNotYang() {
        List<Trade> bars = Arrays.asList(
                bar("d1", 10.0, 10.2, 9.8, 10.0, 10.0, 9.9, 9.8, 9.7),
                bar("d2", 10.5, 10.6, 9.9, 10.0, 10.3, 10.2, 10.1, 10.0)
        );
        Assert.assertNull(MaBull4mTools.findHitOnBars(bars, PeriodTypeEnum.DAY));
    }

    @Test
    public void findHitOnBars_missWhenCloseNotAbovePrevLow() {
        List<Trade> bars = Arrays.asList(
                bar("d1", 10.0, 10.2, 10.1, 10.15, 10.0, 9.9, 9.8, 9.7),
                bar("d2", 10.0, 10.5, 9.9, 10.05, 10.3, 10.2, 10.1, 10.0)
        );
        Assert.assertNull(MaBull4mTools.findHitOnBars(bars, PeriodTypeEnum.DAY));
    }

    @Test
    public void findHitOnBars_missWhenNot4mAlign() {
        List<Trade> bars = Arrays.asList(
                bar("d1", 10.0, 10.2, 9.8, 10.0, 10.0, 9.9, 9.8, 9.7),
                bar("d2", 10.0, 10.5, 9.9, 10.4, 10.0, 10.2, 10.1, 10.3)
        );
        Assert.assertNull(MaBull4mTools.findHitOnBars(bars, PeriodTypeEnum.DAY));
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
