package com.yh.bigdata.tts.common.dto.atlas;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BacktestSummaryVo {

    private String strategy;
    private String strategyName;
    private Integer scanDays;
    private Integer holdDays;
    private Integer stockCount;
    private Integer signalCount;
    private Integer winCount;
    private Integer lossCount;
    private Double winRate;
    private String winRateText;
    private Double avgPnlPct;
    private String avgPnlText;
    private Double maxWinPct;
    private Double maxLossPct;
    private Long elapsedMs;
}
