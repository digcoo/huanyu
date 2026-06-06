package com.yh.bigdata.tts.common.dto.atlas;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtlasStockSummaryVo {
    private String code;
    private String name;
    private String market;
    private Double price;
    private Double changePct;
    private String mainBusiness;
    private String strategy;
    private String summary;
    private String trendMessage;
    private String signalMessage;
}
