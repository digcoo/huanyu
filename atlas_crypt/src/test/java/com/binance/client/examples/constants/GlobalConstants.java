package com.binance.client.examples.constants;

import com.binance.client.dto.DynamicPeriod;
import com.binance.client.enums.PeriodTypeEnum;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

public class GlobalConstants {
    public static final PeriodTypeEnum SPIDER_CHECK_PERIOD = PeriodTypeEnum.DAY1;

    public static final List<PeriodTypeEnum> TREND_PERIODS = Arrays.asList(PeriodTypeEnum.HOUR4);
    public static final PeriodTypeEnum OP_PERIOD = PeriodTypeEnum.MIN30;


//    public static final List<DynamicPeriod> DYNAMIC_PERIOD_LIST = Arrays.asList(
//            new DynamicPeriod(PeriodTypeEnum.MONTH, Lists.newArrayList(PeriodTypeEnum.WEEK), PeriodTypeEnum.DAY1)
//            , new DynamicPeriod(PeriodTypeEnum.WEEK, Lists.newArrayList(PeriodTypeEnum.DAY1), PeriodTypeEnum.HOUR4)
//    );

    public static final List<PeriodTypeEnum> SPIDER_PERIODS = Arrays.asList(
            PeriodTypeEnum.MIN30, PeriodTypeEnum.HOUR4, PeriodTypeEnum.DAY1
            , PeriodTypeEnum.WEEK
    );

}
