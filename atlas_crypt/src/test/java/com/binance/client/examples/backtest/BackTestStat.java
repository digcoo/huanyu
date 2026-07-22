package com.binance.client.examples.backtest;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class BackTestStat {
    private String symbol;
    private int profitCount;
    private int lossCount;
    private BigDecimal profitRate;
    private BigDecimal totalProfit;
}
