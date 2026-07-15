package com.yh.bigdata.tts.spider.strategy.tools.waveperiodgate;

import com.yh.bigdata.tts.common.param.WavePeriodGateStrategyParams;
import org.junit.Assert;
import org.junit.Test;

public class WavePeriodGateYearWeekMonthYangGateToolsTest {

    @Test
    public void skipsWhenDisabled() {
        WavePeriodGateStrategyParams params = WavePeriodGateStrategyParams.builder()
                .enableYearWeekMonthYangGate(false)
                .build();
        Assert.assertTrue(WavePeriodGateYearWeekMonthYangGateTools.passesYearWeekMonthYangGate(
                null, null, params));
    }

    @Test
    public void failsWhenStockMissing() {
        WavePeriodGateStrategyParams params = WavePeriodGateStrategyParams.builder()
                .enableYearWeekMonthYangGate(true)
                .build();
        Assert.assertFalse(WavePeriodGateYearWeekMonthYangGateTools.passesYearWeekMonthYangGate(
                null, null, params));
    }
}
