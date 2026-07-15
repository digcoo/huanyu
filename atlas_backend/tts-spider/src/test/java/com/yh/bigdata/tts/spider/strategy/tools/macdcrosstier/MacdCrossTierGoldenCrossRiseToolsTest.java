package com.yh.bigdata.tts.spider.strategy.tools.macdcrosstier;

import com.yh.bigdata.tts.common.model.Trade;
import org.junit.Assert;
import org.junit.Test;

public class MacdCrossTierGoldenCrossRiseToolsTest {

    @Test
    public void passesRiseWhenAboveThreshold() {
        Trade prev = bar(10.0, 10.0);
        Trade signal = bar(10.2, 10.4);
        Assert.assertTrue(MacdCrossTierGoldenCrossRiseTools.passesRiseOnBars(signal, prev, 0.03));
    }

    @Test
    public void failsRiseWhenBelowThreshold() {
        Trade prev = bar(10.0, 10.0);
        Trade signal = bar(10.1, 10.25);
        Assert.assertFalse(MacdCrossTierGoldenCrossRiseTools.passesRiseOnBars(signal, prev, 0.03));
    }

    @Test
    public void buildPathLabel() {
        Assert.assertEquals("金叉上涨率", MacdCrossTierTools.buildPathLabel(
                MacdCrossTierHitPath.fromRise(null, new Trade(), new Trade())));
    }

    private static Trade bar(double open, double close) {
        Trade t = new Trade();
        t.setOpen(open);
        t.setClose(close);
        return t;
    }
}
