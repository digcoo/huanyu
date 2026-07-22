package com.binance.client.dto;

import com.binance.client.enums.PeriodTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;


@Data
@AllArgsConstructor
public class DynamicPeriod {
    private PeriodTypeEnum trendPeriod;
    private List<PeriodTypeEnum> assistTrendPeriods;
    private PeriodTypeEnum opPeriod;
}
