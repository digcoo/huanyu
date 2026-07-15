package com.yh.bigdata.tts.common.param;

import org.junit.Assert;
import org.junit.Test;

public class MacdCrossTierStrategyParamsTest {

    @Test
    public void defaultsEnableUnionPaths() {
        MacdCrossTierStrategyParams d = MacdCrossTierStrategyParams.defaults();
        Assert.assertEquals(MacdCrossTierStrategyParams.Tier.DAY, d.getTier());
        Assert.assertTrue(d.isEnableGoldenCross());
        Assert.assertTrue(d.isEnableDeathCross());
        Assert.assertTrue(d.isEnableGoldenCrossRiseGate());
        Assert.assertEquals(0.03, d.getSignalRisePct(), 1e-6);
    }

    @Test
    public void mergeFallsBackToDefaultsWhenAllPathsDisabled() {
        MacdCrossTierStrategyParams incoming = MacdCrossTierStrategyParams.builder()
                .enableGoldenCross(false)
                .enableDeathCross(false)
                .enableGoldenCrossRiseGate(false)
                .build();
        MacdCrossTierStrategyParams merged = MacdCrossTierStrategyParams.merge(incoming);
        MacdCrossTierStrategyParams def = MacdCrossTierStrategyParams.defaults();
        Assert.assertEquals(def.isEnableGoldenCross(), merged.isEnableGoldenCross());
        Assert.assertEquals(def.isEnableDeathCross(), merged.isEnableDeathCross());
        Assert.assertEquals(def.isEnableGoldenCrossRiseGate(), merged.isEnableGoldenCrossRiseGate());
    }

    @Test
    public void parseTierMapsAliases() {
        Assert.assertEquals(MacdCrossTierStrategyParams.Tier.DAY,
                MacdCrossTierStrategyParams.parseTier("day"));
        Assert.assertEquals(MacdCrossTierStrategyParams.Tier.WEEK,
                MacdCrossTierStrategyParams.parseTier("medium"));
        Assert.assertEquals(MacdCrossTierStrategyParams.Tier.MONTH,
                MacdCrossTierStrategyParams.parseTier("long"));
    }

    @Test
    public void mergePreservesIncomingTier() {
        MacdCrossTierStrategyParams incoming = MacdCrossTierStrategyParams.builder()
                .tier(MacdCrossTierStrategyParams.Tier.MONTH)
                .lookbackMonth(48)
                .build();
        MacdCrossTierStrategyParams merged = MacdCrossTierStrategyParams.merge(incoming);
        Assert.assertEquals(MacdCrossTierStrategyParams.Tier.MONTH, merged.getTier());
        Assert.assertEquals(48, merged.getLookbackMonth());
    }

    @Test
    public void mergeDayTierPreservesDeathCross() {
        MacdCrossTierStrategyParams incoming = MacdCrossTierStrategyParams.builder()
                .tier(MacdCrossTierStrategyParams.Tier.DAY)
                .enableGoldenCross(false)
                .enableDeathCross(true)
                .build();
        MacdCrossTierStrategyParams merged = MacdCrossTierStrategyParams.merge(incoming);
        Assert.assertFalse(merged.isEnableGoldenCross());
        Assert.assertTrue(merged.isEnableDeathCross());
    }

    @Test
    public void mergeWeekTierPreservesDeathCross() {
        MacdCrossTierStrategyParams incoming = MacdCrossTierStrategyParams.builder()
                .tier(MacdCrossTierStrategyParams.Tier.WEEK)
                .enableGoldenCross(false)
                .enableDeathCross(true)
                .build();
        MacdCrossTierStrategyParams merged = MacdCrossTierStrategyParams.merge(incoming);
        Assert.assertFalse(merged.isEnableGoldenCross());
        Assert.assertTrue(merged.isEnableDeathCross());
    }
}
