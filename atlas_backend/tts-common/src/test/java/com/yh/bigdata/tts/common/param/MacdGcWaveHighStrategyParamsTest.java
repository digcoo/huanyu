package com.yh.bigdata.tts.common.param;

import org.junit.Assert;
import org.junit.Test;

public class MacdGcWaveHighStrategyParamsTest {

    @Test
    public void defaults() {
        MacdGcWaveHighStrategyParams d = MacdGcWaveHighStrategyParams.defaults();
        Assert.assertEquals(MacdGcWaveHighStrategyParams.Tier.DAY, d.getTier());
        Assert.assertEquals(60, d.getLookbackDay());
        Assert.assertEquals(52, d.getLookbackWeek());
        Assert.assertEquals(36, d.getLookbackMonth());
        Assert.assertTrue(d.isEnableMinAmountFilter());
        Assert.assertTrue(d.isEnableSignalRiseGate());
        Assert.assertEquals(0.03, d.getSignalRisePct(), 1e-9);
    }

    @Test
    public void parseTier() {
        Assert.assertEquals(MacdGcWaveHighStrategyParams.Tier.DAY,
                MacdGcWaveHighStrategyParams.parseTier("short"));
        Assert.assertEquals(MacdGcWaveHighStrategyParams.Tier.WEEK,
                MacdGcWaveHighStrategyParams.parseTier("medium"));
        Assert.assertEquals(MacdGcWaveHighStrategyParams.Tier.MONTH,
                MacdGcWaveHighStrategyParams.parseTier("long"));
    }

    @Test
    public void mergePartial() {
        MacdGcWaveHighStrategyParams incoming = MacdGcWaveHighStrategyParams.builder()
                .tier(MacdGcWaveHighStrategyParams.Tier.WEEK)
                .enableMinAmountFilter(false)
                .signalRisePct(0.05)
                .build();
        MacdGcWaveHighStrategyParams merged = MacdGcWaveHighStrategyParams.merge(incoming);
        Assert.assertEquals(MacdGcWaveHighStrategyParams.Tier.WEEK, merged.getTier());
        Assert.assertFalse(merged.isEnableMinAmountFilter());
        Assert.assertEquals(0.05, merged.getSignalRisePct(), 1e-9);
    }
}
