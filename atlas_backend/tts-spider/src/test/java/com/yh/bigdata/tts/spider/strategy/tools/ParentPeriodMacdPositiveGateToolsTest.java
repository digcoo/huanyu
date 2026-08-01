package com.yh.bigdata.tts.spider.strategy.tools;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import org.junit.Assert;
import org.junit.Test;

public class ParentPeriodMacdPositiveGateToolsTest {

    @Test
    public void parentPeriod_mapsDayWeekMonthToWeekMonthYear() {
        Assert.assertEquals(PeriodTypeEnum.WEEK,
                ParentPeriodMacdPositiveGateTools.parentPeriod(PeriodTypeEnum.DAY));
        Assert.assertEquals(PeriodTypeEnum.MONTH,
                ParentPeriodMacdPositiveGateTools.parentPeriod(PeriodTypeEnum.WEEK));
        Assert.assertEquals(PeriodTypeEnum.YEAR,
                ParentPeriodMacdPositiveGateTools.parentPeriod(PeriodTypeEnum.MONTH));
    }
}
