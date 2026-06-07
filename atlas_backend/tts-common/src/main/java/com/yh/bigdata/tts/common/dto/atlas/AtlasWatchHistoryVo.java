package com.yh.bigdata.tts.common.dto.atlas;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtlasWatchHistoryVo {
    private String recordId;
    private String id;
    private String code;
    private String name;
    private String market;
    private String strategy;
    private String resonance;
    private List<String> tags;
    private Long addedAt;
    private Long removedAt;
    private Integer holdDays;
    private Double entryPrice;
    private Double exitPrice;
    private Double pnlPct;
    private String removeReason;
}
