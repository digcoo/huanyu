package com.yh.bigdata.tts.common.param;

import org.junit.Assert;
import org.junit.Test;

public class MacdGoldenCrossStrategyParamsTest {

    @Test
    public void defaultsEnableAllOptionalGates() {
        MacdGoldenCrossStrategyParams d = MacdGoldenCrossStrategyParams.defaults();
        Assert.assertEquals(MacdGoldenCrossStrategyParams.Tier.DAY, d.getTier());
        Assert.assertTrue(d.isEnableMinAmountFilter());
        Assert.assertEquals(3000D * 10_000D, d.getMinAvgAmount(), 1);
        Assert.assertTrue(d.isEnableSignalRiseGate());
        Assert.assertEquals(0.03, d.getSignalRisePct(), 1e-9);
        Assert.assertTrue(d.isEnableHistoryRiseGate());
        Assert.assertEquals(5, d.getHistoryLookbackBars());
        Assert.assertEquals(0.03, d.getHistoryRisePct(), 1e-9);
    }

    @Test
    public void parseTierAliases() {
        Assert.assertEquals(MacdGoldenCrossStrategyParams.Tier.DAY,
                MacdGoldenCrossStrategyParams.parseTier("short"));
        Assert.assertEquals(MacdGoldenCrossStrategyParams.Tier.WEEK,
                MacdGoldenCrossStrategyParams.parseTier("medium"));
        Assert.assertEquals(MacdGoldenCrossStrategyParams.Tier.MONTH,
                MacdGoldenCrossStrategyParams.parseTier("long"));
    }

    @Test
    public void mergeOverridesTierAndThresholds() {
        MacdGoldenCrossStrategyParams incoming = MacdGoldenCrossStrategyParams.builder()
                .tier(MacdGoldenCrossStrategyParams.Tier.WEEK)
                .enableMinAmountFilter(false)
                .signalRisePct(0.05)
                .historyLookbackBars(8)
                .historyRisePct(0.04)
                .build();
        MacdGoldenCrossStrategyParams merged = MacdGoldenCrossStrategyParams.merge(incoming);
        Assert.assertEquals(MacdGoldenCrossStrategyParams.Tier.WEEK, merged.getTier());
        Assert.assertFalse(merged.isEnableMinAmountFilter());
        Assert.assertEquals(0.05, merged.getSignalRisePct(), 1e-9);
        Assert.assertEquals(8, merged.getHistoryLookbackBars());
        Assert.assertEquals(0.04, merged.getHistoryRisePct(), 1e-9);
    }
}
