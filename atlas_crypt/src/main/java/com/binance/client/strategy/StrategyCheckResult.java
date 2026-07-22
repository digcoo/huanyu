package com.binance.client.strategy;

import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.enums.SideTypeEnum;
import com.binance.client.strategy.wavecc.HourWaveCcBreakdownTools;
import com.binance.client.strategy.wavecc.HourWaveCcBreakoutTools;
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
    private HourWaveCcBreakoutTools.Hit longWaveHit;
    private HourWaveCcBreakdownTools.Hit shortWaveHit;

    public StrategyCheckResult(String symbol, SideTypeEnum sideType) {
        this.symbol = symbol;
        this.sideType = sideType;
    }
}
