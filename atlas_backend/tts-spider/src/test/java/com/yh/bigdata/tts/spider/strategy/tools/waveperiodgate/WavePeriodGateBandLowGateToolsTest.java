package com.yh.bigdata.tts.spider.strategy.tools.waveperiodgate;

import com.yh.bigdata.tts.common.param.WavePeriodGateStrategyParams;
import org.junit.Assert;
import org.junit.Test;

public class WavePeriodGateBandLowGateToolsTest {

    @Test
    public void skipsWhenDisabled() {
        WavePeriodGateStrategyParams params = WavePeriodGateStrategyParams.builder()
                .enableWeekMonthBandLowGate(false)
                .build();
        Assert.assertTrue(WavePeriodGateBandLowGateTools.passesWeekMonthBandLowGate(null, null, params));
    }

    @Test
    public void failsWhenStockMissing() {
        WavePeriodGateStrategyParams params = WavePeriodGateStrategyParams.builder()
                .enableWeekMonthBandLowGate(true)
                .build();
        Assert.assertFalse(WavePeriodGateBandLowGateTools.passesWeekMonthBandLowGate(null, null, params));
    }
}
