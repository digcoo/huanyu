package com.yh.bigdata.tts.spider.strategy.tools.macdcrosstier;

import com.yh.bigdata.tts.common.param.MacdCrossTierStrategyParams;
import org.junit.Assert;
import org.junit.Test;

public class MacdCrossTierBandShapeGateToolsTest {

    @Test
    public void weekMonthShapeGateAppliesToAllTiers() {
        MacdCrossTierStrategyParams week = MacdCrossTierStrategyParams.builder()
                .tier(MacdCrossTierStrategyParams.Tier.WEEK)
                .build();
        MacdCrossTierStrategyParams month = MacdCrossTierStrategyParams.builder()
                .tier(MacdCrossTierStrategyParams.Tier.MONTH)
                .build();
        Assert.assertFalse(MacdCrossTierBandShapeGateTools.passesWeekMonthShapeGate(null, null, week));
        Assert.assertFalse(MacdCrossTierBandShapeGateTools.passesWeekMonthShapeGate(null, null, month));
    }
}
