package com.yh.bigdata.tts.spider.strategy.tools.macdcrosstier;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import org.junit.Assert;
import org.junit.Test;

public class MacdCrossTierPrevHighGateToolsTest {

    @Test
    public void passesWhenCloseAbovePrevHigh() {
        Trade prev = bar("2026-01-08", 10, 12, 9, 11);
        Trade signal = bar("2026-01-09", 11, 13, 10, 12.5);
        Assert.assertTrue(MacdCrossTierPrevHighGateTools.passesCloseAbovePrevHigh(
                null, null, PeriodTypeEnum.DAY, prev, signal));
    }

    @Test
    public void failsWhenCloseAtOrBelowPrevHigh() {
        Trade prev = bar("2026-01-08", 10, 12, 9, 11);
        Trade signal = bar("2026-01-09", 11, 12, 10, 12);
        Assert.assertFalse(MacdCrossTierPrevHighGateTools.passesCloseAbovePrevHigh(
                null, null, PeriodTypeEnum.WEEK, prev, signal));
    }

    private static Trade bar(String date, double open, double high, double low, double close) {
        Trade t = new Trade();
        t.setDay(date);
        t.setOpen(open);
        t.setHigh(high);
        t.setLow(low);
        t.setClose(close);
        return t;
    }
}
