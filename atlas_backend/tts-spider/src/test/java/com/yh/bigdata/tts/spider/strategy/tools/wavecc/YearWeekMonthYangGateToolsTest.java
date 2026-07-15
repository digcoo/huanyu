package com.yh.bigdata.tts.spider.strategy.tools.wavecc;

import com.yh.bigdata.tts.common.model.Trade;
import org.junit.Assert;
import org.junit.Test;

public class YearWeekMonthYangGateToolsTest {

    @Test
    public void passesPeriodOnLastBarWhenStrictYang() {
        Trade bar = new Trade();
        bar.setOpen(10.0);
        bar.setClose(11.0);
        Assert.assertTrue(YearWeekMonthYangGateTools.passesPeriodOnLastBar(bar));
    }

    @Test
    public void failsPeriodOnLastBarWhenYin() {
        Trade bar = new Trade();
        bar.setOpen(11.0);
        bar.setClose(10.0);
        Assert.assertFalse(YearWeekMonthYangGateTools.passesPeriodOnLastBar(bar));
    }

    @Test
    public void gateFailsWhenStockMissing() {
        Assert.assertFalse(YearWeekMonthYangGateTools.passesGate(null, null, "[WPG]"));
    }
}
