package com.yh.bigdata.tts.spider.service;

import com.yh.bigdata.tts.common.dto.atlas.BacktestResultVo;
import com.yh.bigdata.tts.common.dto.atlas.BacktestStrategyOptionVo;
import com.yh.bigdata.tts.common.param.BacktestParam;

import java.util.List;

public interface BacktestService {

    BacktestResultVo run(BacktestParam param);

    List<BacktestStrategyOptionVo> listStrategies();

    boolean isCacheReady();
}
