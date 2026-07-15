package com.yh.bigdata.tts.spider.strategy.tools.macdcrosstier;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.param.MacdCrossTierStrategyParams;
import org.junit.Assert;
import org.junit.Test;

public class MacdCrossTierToolsTest {

    @Test
    public void resolvePeriodAndLookbackByTier() {
        MacdCrossTierStrategyParams day = MacdCrossTierStrategyParams.builder()
                .tier(MacdCrossTierStrategyParams.Tier.DAY)
                .lookbackDay(80)
                .build();
        Assert.assertEquals(PeriodTypeEnum.DAY, MacdCrossTierTools.resolvePeriod(day.getTier()));
        Assert.assertEquals(80, MacdCrossTierTools.resolveLookback(day));

        MacdCrossTierStrategyParams week = MacdCrossTierStrategyParams.builder()
                .tier(MacdCrossTierStrategyParams.Tier.WEEK)
                .lookbackWeek(40)
                .build();
        Assert.assertEquals(PeriodTypeEnum.WEEK, MacdCrossTierTools.resolvePeriod(week.getTier()));
        Assert.assertEquals(40, MacdCrossTierTools.resolveLookback(week));

        MacdCrossTierStrategyParams month = MacdCrossTierStrategyParams.builder()
                .tier(MacdCrossTierStrategyParams.Tier.MONTH)
                .lookbackMonth(30)
                .build();
        Assert.assertEquals(PeriodTypeEnum.MONTH, MacdCrossTierTools.resolvePeriod(month.getTier()));
        Assert.assertEquals(30, MacdCrossTierTools.resolveLookback(month));
    }

    @Test
    public void buildTierLabel() {
        Assert.assertEquals("日", MacdCrossTierTools.buildTierLabel(MacdCrossTierStrategyParams.defaults()));
        Assert.assertEquals("周", MacdCrossTierTools.buildTierLabel(
                MacdCrossTierStrategyParams.builder().tier(MacdCrossTierStrategyParams.Tier.WEEK).build()));
        Assert.assertEquals("月", MacdCrossTierTools.buildTierLabel(
                MacdCrossTierStrategyParams.builder().tier(MacdCrossTierStrategyParams.Tier.MONTH).build()));
    }
}
