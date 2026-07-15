package com.yh.bigdata.tts.common.param;

import org.junit.Assert;
import org.junit.Test;

public class BodyBarTierStrategyParamsTest {

    @Test
    public void defaultsDayTierThresholds() {
        BodyBarTierStrategyParams d = BodyBarTierStrategyParams.defaults();
        Assert.assertEquals(BodyBarTierStrategyParams.Tier.DAY, d.getTier());
        Assert.assertEquals(10, d.getLookbackBars());
        Assert.assertEquals(0.04, d.resolveHistoryBodyPct(), 1e-6);
        Assert.assertEquals(0.03, d.resolveSignalRisePct(), 1e-6);
    }

    @Test
    public void weekTierThresholds() {
        BodyBarTierStrategyParams p = BodyBarTierStrategyParams.builder()
                .tier(BodyBarTierStrategyParams.Tier.WEEK)
                .build();
        Assert.assertEquals(0.06, p.resolveHistoryBodyPct(), 1e-6);
        Assert.assertEquals(0.05, p.resolveSignalRisePct(), 1e-6);
    }

    @Test
    public void parseTierMapsAliases() {
        Assert.assertEquals(BodyBarTierStrategyParams.Tier.WEEK,
                BodyBarTierStrategyParams.parseTier("medium"));
        Assert.assertEquals(BodyBarTierStrategyParams.Tier.MONTH,
                BodyBarTierStrategyParams.parseTier("long"));
    }
}
