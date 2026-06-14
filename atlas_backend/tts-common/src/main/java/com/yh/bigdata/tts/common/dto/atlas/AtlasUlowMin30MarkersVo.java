package com.yh.bigdata.tts.common.dto.atlas;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 梯子突破 · 30m 基准 K + 突破 K 标记 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtlasUlowMin30MarkersVo {
    private String referenceDay;
    private Double referenceHigh;
    private String signalDay;
    private Double signalHigh;
}
