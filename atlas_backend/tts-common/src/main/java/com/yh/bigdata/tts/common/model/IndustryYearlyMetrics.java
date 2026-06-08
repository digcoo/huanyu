package com.yh.bigdata.tts.common.model;

import lombok.Data;

/** 同行业按报告年度聚合的财务指标均值 */
@Data
public class IndustryYearlyMetrics {

    private Integer reportYear;
    private Double avgTotalRevenue;
    private Double avgNetProfit;
    private Double avgOperatingCashFlow;
    private Double avgCapex;
    private Double avgStaffNum;
    private Double avgRevenuePerStaff;
    private Double avgPrepaidRatio;
    private Double avgInterestDebtRatio;
    private Double avgGrossMargin;
    private Double avgNetMargin;
    private Double avgDebtRatio;
    private Double avgCurrentRatio;
    private Double avgInventoryDays;
    private Double avgReceivableDays;
    private Double avgRevenueYoy;
    private Double avgRoe;
    private Integer sampleCount;
}
