package com.yh.bigdata.tts.common.model;

import lombok.Data;

@Data
public class StockIndustryBenchmark {

    private String industry;

    private Double roeAvg;

    private Double grossMarginAvg;

    private Double netMarginAvg;

    private Double revenueYoyAvg;

    private Double debtRatioAvg;

    private Integer sampleCount;
}
