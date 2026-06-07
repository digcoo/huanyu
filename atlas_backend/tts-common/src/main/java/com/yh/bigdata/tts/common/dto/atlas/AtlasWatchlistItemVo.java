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
public class AtlasWatchlistItemVo {
    private String id;
    private String code;
    private String name;
    private String market;
    private String strategy;
    private Double price;
    private Double entryPrice;
    private Double changePct;
    private List<String> tags;
    private String summary;
    private String resonance;
    private Long addedAt;
}
