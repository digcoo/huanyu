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
public class AtlasMarketIndexVo {
    /** sh000001 */
    private String code;
    /** 000001 */
    private String displayCode;
    private String name;
    private Double price;
    private Double changePct;
    private List<AtlasKlineBarVo> klines;
}
