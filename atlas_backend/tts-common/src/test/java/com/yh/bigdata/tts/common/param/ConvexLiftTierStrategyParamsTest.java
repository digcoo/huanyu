package com.yh.bigdata.tts.common.param;

import org.junit.Assert;
import org.junit.Test;

public class ConvexLiftTierStrategyParamsTest {

    @Test
    public void defaultsAndMerge() {
        ConvexLiftTierStrategyParams d = ConvexLiftTierStrategyParams.defaults();
        Assert.assertEquals(ConvexLiftTierStrategyParams.Tier.WEEK, d.getTier());
        Assert.assertEquals(120, d.getLookbackDay());

        ConvexLiftTierStrategyParams incoming = ConvexLiftTierStrategyParams.builder()
                .lookbackDay(80)
                .build();
        ConvexLiftTierStrategyParams merged = ConvexLiftTierStrategyParams.merge(incoming);
        Assert.assertEquals(80, merged.getLookbackDay());
    }

    @Test
    public void parseTier() {
        Assert.assertEquals(ConvexLiftTierStrategyParams.Tier.WEEK,
                ConvexLiftTierStrategyParams.parseTier("week"));
        Assert.assertEquals(ConvexLiftTierStrategyParams.Tier.MONTH,
                ConvexLiftTierStrategyParams.parseTier("medium"));
        Assert.assertEquals(ConvexLiftTierStrategyParams.Tier.YEAR,
                ConvexLiftTierStrategyParams.parseTier("long"));
    }
}
