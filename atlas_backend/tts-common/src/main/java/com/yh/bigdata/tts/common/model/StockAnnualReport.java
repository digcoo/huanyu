package com.yh.bigdata.tts.common.model;

import lombok.Data;

import java.util.Date;

@Data
public class StockAnnualReport {

    private Long id;
    private String code;
    private String name;
    private Integer reportYear;
    private Date reportDate;
    private Double totalRevenue;
    private Double netProfit;
    private Double parentNetProfit;
    private Double grossMargin;
    private Double netMargin;
    private Double roe;
    private Double operatingCashFlow;
    private Double capex;
    private Integer staffNum;
    private Double revenuePerStaff;
    private Double prepaidRatio;
    private Double interestDebtRatio;
    private Double debtRatio;
    private Double currentRatio;
    private Double inventoryDays;
    private Double receivableDays;
    private Double revenueYoy;
    private Double profitYoy;
    private String source;
    private Date createTime;
    private Date updateTime;
}
