package com.yh.bigdata.tts.common.dto.atlas;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtlasStockDetailVo {
    private String code;
    private String name;
    private String market;
    private Double price;
    private Double changePct;
    private String industry;
    private String mainBusiness;
    private Map<String, Object> profile;
    private List<Map<String, String>> keyMetrics;
}
