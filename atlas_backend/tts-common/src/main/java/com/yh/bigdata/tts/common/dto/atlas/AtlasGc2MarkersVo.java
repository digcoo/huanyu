package com.yh.bigdata.tts.common.dto.atlas;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 金叉二次突破 · 金叉K / 突破K 标记 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtlasGc2MarkersVo {
    private String referenceDay;
    private Double referenceHigh;
    private String signalDay;
    private Double signalHigh;
}
