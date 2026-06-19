package com.yh.bigdata.tts.common.dto.atlas;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BacktestResultVo {

    private BacktestSummaryVo summary;
    private List<BacktestTradeVo> trades;
}
