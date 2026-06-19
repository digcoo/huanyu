package com.yh.bigdata.tts.common.dto.atlas;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 死叉突破 · 死叉K / 突破K 标记 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtlasDc2MarkersVo {
    private String referenceDay;
    private Double referenceHigh;
    private String signalDay;
    private Double signalHigh;
}
