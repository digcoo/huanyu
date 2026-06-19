package com.yh.bigdata.tts.common.dto.atlas;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BacktestTradeVo {

    private String code;
    private String name;
    private String signalDay;
    private String exitDay;
    private Double entryPrice;
    private Double exitPrice;
    private Double pnlPct;
    private Boolean win;
    private String tier;
    private String trendMessage;
    private String signalMessage;
}
