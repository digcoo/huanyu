package com.yh.bigdata.tts.common.param;

import org.junit.Assert;
import org.junit.Test;

public class WavePeriodGateStrategyParamsTest {

    @Test
    public void defaultsDayTierWithBreakoutGates() {
        WavePeriodGateStrategyParams d = WavePeriodGateStrategyParams.defaults();
        Assert.assertEquals(WavePeriodGateStrategyParams.Tier.DAY, d.getTier());
        Assert.assertFalse(d.isEnableMinAmountFilter());
        Assert.assertFalse(d.isEnableMaxBandLowGate());
        Assert.assertTrue(d.isEnableConcaveBreakout());
        Assert.assertTrue(d.isEnableConvexBreakout());
        Assert.assertFalse(d.isEnableUpperPeriodMinBandLowGate());
        Assert.assertTrue(d.isEnableTierMacdPositiveGate());
        Assert.assertTrue(d.isEnableWeekMonthBandShapeGate());
        Assert.assertTrue(d.isEnableWeekMonthBandLowGate());
        Assert.assertTrue(d.isEnableYearWeekMonthYangGate());
        Assert.assertEquals(WavePeriodGateStrategyParams.DEFAULT_LOOKBACK_YEAR, d.getLookbackYear());
    }

    @Test
    public void parseTierMapsAliases() {
        Assert.assertEquals(WavePeriodGateStrategyParams.Tier.DAY,
                WavePeriodGateStrategyParams.parseTier("day"));
        Assert.assertEquals(WavePeriodGateStrategyParams.Tier.WEEK,
                WavePeriodGateStrategyParams.parseTier("medium"));
        Assert.assertEquals(WavePeriodGateStrategyParams.Tier.MONTH,
                WavePeriodGateStrategyParams.parseTier("long"));
    }

    @Test
    public void mergePreservesIncomingTier() {
        WavePeriodGateStrategyParams incoming = WavePeriodGateStrategyParams.builder()
                .tier(WavePeriodGateStrategyParams.Tier.MONTH)
                .enableConvexBreakout(true)
                .build();
        WavePeriodGateStrategyParams merged = WavePeriodGateStrategyParams.merge(incoming);
        Assert.assertEquals(WavePeriodGateStrategyParams.Tier.MONTH, merged.getTier());
        Assert.assertTrue(merged.isEnableConvexBreakout());
    }
}
