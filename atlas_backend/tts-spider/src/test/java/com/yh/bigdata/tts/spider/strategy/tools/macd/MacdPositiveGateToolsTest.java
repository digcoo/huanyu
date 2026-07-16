package com.yh.bigdata.tts.spider.strategy.tools.macd;

import com.yh.bigdata.tts.common.param.MacdPositiveGateParams;
import org.junit.Assert;
import org.junit.Test;

public class MacdPositiveGateToolsTest {

    @Test
    public void passesWhenAllGatesDisabled() {
        Assert.assertTrue(MacdPositiveGateTools.passGate(null, null, MacdPositiveGateParams.defaults()));
    }

    @Test
    public void rejectsWhenDayGateOnAndStockMissing() {
        MacdPositiveGateParams on = MacdPositiveGateParams.builder().requireDayMacd(true).build();
        Assert.assertFalse(MacdPositiveGateTools.passGate(null, null, on));
    }
}
