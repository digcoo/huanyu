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

    /** 生命周期 { id, label, desc, color } */
    private Map<String, Object> stage;
    private String stageHint;

    private Integer healthScore;
    private String healthRank;
    private Map<String, Integer> healthBreakdown;

    /** 五维雷达 */
    private Map<String, Object> radar;

    /** 竞品列表 */
    private List<Map<String, Object>> competitors;

    private List<Map<String, Object>> portraitDimensions;
}
