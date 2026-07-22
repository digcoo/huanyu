package com.binance.client.enums;

import com.binance.client.model.enums.CandlestickInterval;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum PeriodTypeEnum {
    HOUR1(new BigDecimal("0.01"), new BigDecimal("0.015"), new BigDecimal("0.008"), "1小时", CandlestickInterval.HOURLY),
    MIN30(new BigDecimal("0.008"), new BigDecimal("0.0065"), new BigDecimal("0.0035"),"30分钟", CandlestickInterval.HALF_HOURLY),
    HOUR4(new BigDecimal("0.02"), new BigDecimal("0.025"), new BigDecimal("0.01"),"4小时", CandlestickInterval.FOUR_HOURLY),
    DAY1(new BigDecimal("0.02"), new BigDecimal("0.025"), new BigDecimal("0.01"),  "天", CandlestickInterval.DAILY),

    WEEK(new BigDecimal("0.03"), new BigDecimal("0.035"), new BigDecimal("0.02"), "周", CandlestickInterval.WEEKLY),
    MONTH(new BigDecimal("0.1"), new BigDecimal("0.1"), new BigDecimal("0.02"), "月", CandlestickInterval.MONTHLY),
    ;

    private final BigDecimal tiZiShiTiRate;  //梯子实体阈值
    private final BigDecimal tiZiShockRate;  //梯子振幅阈值
    private final BigDecimal crossTiZiHighRate;  //突破梯子High阈值

    private final CandlestickInterval interval;

    private final String desc;

    PeriodTypeEnum(BigDecimal tiZiShiTiRate
            , BigDecimal tiZiShockRate
            , BigDecimal crossTiZiHighRate
            , String desc, CandlestickInterval interval) {
        this.tiZiShiTiRate = tiZiShiTiRate;
        this.tiZiShockRate = tiZiShockRate;
        this.crossTiZiHighRate = crossTiZiHighRate;
        this.desc = desc;
        this.interval = interval;
    }

}
