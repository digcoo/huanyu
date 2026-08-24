package com.binance.client.examples.constants;

import com.binance.client.enums.PeriodTypeEnum;

import java.util.Arrays;
import java.util.List;

public class GlobalConstants {
    public static final PeriodTypeEnum SPIDER_CHECK_PERIOD = PeriodTypeEnum.DAY1;
    public static final PeriodTypeEnum OP_PERIOD = PeriodTypeEnum.HOUR1;

    public static final List<PeriodTypeEnum> SPIDER_PERIODS = Arrays.asList(
            PeriodTypeEnum.HOUR1, PeriodTypeEnum.HOUR4
    );
}
