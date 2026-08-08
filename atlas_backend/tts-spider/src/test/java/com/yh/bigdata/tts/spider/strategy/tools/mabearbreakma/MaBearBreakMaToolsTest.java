package com.yh.bigdata.tts.spider.strategy.tools.mabearbreakma;

import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.strategy.tools.ma3m.Ma3mCore;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class MaBearBreakMaToolsTest {

    @Test
    public void findDayBear_hitsMa20BearWithCloseAboveShortMa() {
        List<Trade> bars = Collections.singletonList(
                bar("d1", 10.0, 10.5, 9.8, 10.4, 10.0, 10.1, 10.5, 10.6)
        );
        Assert.assertNotNull(MaBearBreakMaTools.findDayBear(bars));
    }

    @Test
    public void findDayBear_missWhenCloseNotAboveShortMa() {
        List<Trade> bars = Collections.singletonList(
                bar("d1", 10.0, 10.5, 9.8, 9.9, 10.0, 10.1, 10.5, 10.6)
        );
        Assert.assertNull(MaBearBreakMaTools.findDayBear(bars));
    }

    @Test
    public void findDayBear_hitsMa30WhenMa20NotBear() {
        // 相对 MA20 非空头（MA5>MA20），相对 MA30 空头
        List<Trade> bars = Collections.singletonList(
                bar("d1", 10.0, 10.5, 9.8, 10.4, 10.2, 10.3, 10.0, 10.5)
        );
        Assert.assertNotNull(MaBearBreakMaTools.findDayBear(bars));
    }

    @Test
    public void ma3mCore_bearAlignHelpers() {
        Trade bear20 = bar("d1", 10, 10, 10, 10.4, 10.0, 10.1, 10.5, 10.6);
        Assert.assertTrue(Ma3mCore.passesBearAlign(bear20, Ma3mCore.AlignKind.MA20));
        Assert.assertTrue(Ma3mCore.passesBearCloseAboveMa5Ma10(bear20));
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
