package com.yh.bigdata.tts.common.param;

import org.junit.Assert;
import org.junit.Test;

public class StockPageQueryMacdGcWaveHighLiftTest {

    @Test
    public void toMacdGcWaveHighLiftParams_canDisableDayAndEnableMin60() {
        StockPageQuery q = new StockPageQuery();
        q.setMgRequireDayMacd(false);
        q.setMgRequireWeekMacd(true);
        q.setMgRequireMonthMacd(true);
        q.setMgRequireMin60Macd(true);

        MacdGcWaveHighLiftStrategyParams p = q.toMacdGcWaveHighLiftParams();

        Assert.assertFalse(p.isRequireDayMacd());
        Assert.assertTrue(p.isRequireWeekMacd());
        Assert.assertTrue(p.isRequireMonthMacd());
        Assert.assertTrue(p.isRequireMin60Macd());
    }

    @Test
    public void toMacdGcWaveHighLiftParams_defaultsWhenFlagsMissing() {
        MacdGcWaveHighLiftStrategyParams p = new StockPageQuery().toMacdGcWaveHighLiftParams();

        Assert.assertTrue(p.isRequireDayMacd());
        Assert.assertTrue(p.isRequireWeekMacd());
        Assert.assertTrue(p.isRequireMonthMacd());
        Assert.assertFalse(p.isRequireMin60Macd());
    }
}
