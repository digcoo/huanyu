package com.yh.bigdata.tts.common.param;

import org.junit.Assert;
import org.junit.Test;

public class MacdGcWaveHighRetestStrategyParamsTest {

    @Test
    public void defaults() {
        MacdGcWaveHighRetestStrategyParams d = MacdGcWaveHighRetestStrategyParams.defaults();
        Assert.assertEquals(MacdGcWaveHighRetestStrategyParams.Tier.DAY, d.getTier());
        Assert.assertTrue(d.isEnableMinAmountFilter());
        Assert.assertEquals(0.01, d.getMaxBarRangePct(), 1e-9);
    }
}
