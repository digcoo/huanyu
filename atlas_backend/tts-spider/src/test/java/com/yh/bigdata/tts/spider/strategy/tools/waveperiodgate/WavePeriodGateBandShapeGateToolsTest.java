package com.yh.bigdata.tts.spider.strategy.tools.waveperiodgate;

import com.yh.bigdata.tts.common.param.WavePeriodGateStrategyParams;
import org.junit.Assert;
import org.junit.Test;

public class WavePeriodGateBandShapeGateToolsTest {

    @Test
    public void skipsWhenDisabled() {
        WavePeriodGateStrategyParams params = WavePeriodGateStrategyParams.builder()
                .enableWeekMonthBandShapeGate(false)
                .build();
        Assert.assertTrue(WavePeriodGateBandShapeGateTools.passesWeekMonthShapeGate(null, null, params));
    }

    @Test
    public void failsWhenStockMissing() {
        WavePeriodGateStrategyParams params = WavePeriodGateStrategyParams.builder()
                .enableWeekMonthBandShapeGate(true)
                .build();
        Assert.assertFalse(WavePeriodGateBandShapeGateTools.passesWeekMonthShapeGate(null, null, params));
    }
}
