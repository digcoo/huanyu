package com.yh.bigdata.tts.common.dto.atlas;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** 回踩抬升 · L0/H1/L1/signal 标记 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtlasRetestMarkersVo {
    private String l0Day;
    private Double l0Low;
    private String h1Day;
    private Double h1High;
    private String l1Day;
    private Double l1Low;
    private String signalDay;
    private Double signalHigh;
    private List<String> modes;
}
