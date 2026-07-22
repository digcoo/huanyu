package com.binance.client.examples.backup2;


import com.binance.client.enums.PeriodTypeEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class CheckResponse {
    private boolean success;
    private boolean ifGoldCross;
    private String message;
    private PeriodTypeEnum periodType;
}
