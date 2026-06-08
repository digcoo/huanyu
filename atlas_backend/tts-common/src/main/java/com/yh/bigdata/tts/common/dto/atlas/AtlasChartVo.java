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
public class AtlasChartVo {
    private String name;
    private String unit;
    private String color;
    private List<AtlasSeriesPointVo> data;
    /** 行业均值对比线（与 data 同年份序列） */
    private List<AtlasSeriesPointVo> industryData;
}
