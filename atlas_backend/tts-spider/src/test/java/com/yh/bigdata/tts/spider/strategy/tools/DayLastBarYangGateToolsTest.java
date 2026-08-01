package com.yh.bigdata.tts.spider.strategy.tools;

import com.yh.bigdata.tts.common.model.Trade;
import org.junit.Assert;
import org.junit.Test;

public class DayLastBarYangGateToolsTest {

    @Test
    public void isDayLastBarYang_acceptsStrictYangAndDoji() {
        Assert.assertTrue(DayLastBarYangGateTools.isDayLastBarYang(bar(10.0, 10.5)));
        Assert.assertTrue(DayLastBarYangGateTools.isDayLastBarYang(bar(10.0, 10.0)));
    }

    @Test
    public void isDayLastBarYang_rejectsYin() {
        Assert.assertFalse(DayLastBarYangGateTools.isDayLastBarYang(bar(10.5, 10.4)));
    }

    private static Trade bar(double open, double close) {
        Trade t = new Trade();
        t.setOpen(open);
        t.setClose(close);
        return t;
    }
}
