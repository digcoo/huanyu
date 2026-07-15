package com.yh.bigdata.tts.common.param;

import org.junit.Assert;
import org.junit.Test;

public class StockPageQueryMacdCrossTierTest {

    @Test
    public void toMacdCrossTierParams_deathOnlyDisablesGoldenPath() {
        StockPageQuery q = new StockPageQuery();
        q.setMctEnableGoldenCross(false);
        q.setMctEnableDeathCross(true);
        q.setMctEnableGoldenCrossRiseGate(false);

        MacdCrossTierStrategyParams p = q.toMacdCrossTierParams();

        Assert.assertFalse(p.isEnableGoldenCross());
        Assert.assertTrue(p.isEnableDeathCross());
        Assert.assertFalse(p.isEnableGoldenCrossRiseGate());
    }

    @Test
    public void toMacdCrossTierParams_partialPathFlagsTreatMissingAsFalse() {
        StockPageQuery q = new StockPageQuery();
        q.setMctEnableDeathCross(true);

        MacdCrossTierStrategyParams p = q.toMacdCrossTierParams();

        Assert.assertFalse(p.isEnableGoldenCross());
        Assert.assertTrue(p.isEnableDeathCross());
        Assert.assertFalse(p.isEnableGoldenCrossRiseGate());
    }
}
