package com.binance.client.strategy;

import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.enums.SideTypeEnum;
import com.binance.client.strategy.ma4m.HourMaBear4mTools;
import com.binance.client.strategy.ma4m.HourMaBull4mTools;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StrategyCheckResult {

    private final String symbol;
    private final SideTypeEnum sideType;
    private boolean hit;
    private BigDecimal close;
    private BigDecimal changeRate;
    private PeriodTypeEnum opPeriodType = PeriodTypeEnum.HOUR1;
    private String trendMessage;
    private String signalMessage;
    private HourMaBull4mTools.Hit longMaHit;
    private HourMaBear4mTools.Hit shortMaHit;

    public StrategyCheckResult(String symbol, SideTypeEnum sideType) {
        this.symbol = symbol;
        this.sideType = sideType;
    }
}
