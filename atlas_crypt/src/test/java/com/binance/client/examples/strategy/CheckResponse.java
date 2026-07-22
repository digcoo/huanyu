package com.binance.client.examples.strategy;


import com.binance.client.enums.PeriodTypeEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class CheckResponse {
    private boolean success;
    private boolean ifGoldCross;
    private String message;
    private PeriodTypeEnum opPeriodType;
    private PeriodTypeEnum trendPeriodType;
}
