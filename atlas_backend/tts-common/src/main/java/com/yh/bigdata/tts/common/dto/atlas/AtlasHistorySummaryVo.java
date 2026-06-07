package com.yh.bigdata.tts.common.dto.atlas;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtlasHistorySummaryVo {
    private int total;
    private int winRate;
    private String winRateText;
    private int avgHoldDays;
    private String avgHoldText;
    private double avgPnlPct;
    private String avgPnlText;
}
