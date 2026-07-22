package com.binance.client.enums;

import com.binance.client.model.enums.CandlestickInterval;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum SideTypeEnum {
    LONG("LONG", "多"),
    SHORT("SHORT", "空");


    private String code;
    private String desc;

    SideTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public SideTypeEnum getOppositeSide() {
        return this == LONG ? SHORT : LONG;
    }
}
