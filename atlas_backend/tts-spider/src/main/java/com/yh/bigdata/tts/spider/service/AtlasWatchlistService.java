package com.yh.bigdata.tts.spider.service;

import com.yh.bigdata.tts.common.dto.atlas.AtlasHistorySummaryVo;
import com.yh.bigdata.tts.common.dto.atlas.AtlasWatchHistoryVo;
import com.yh.bigdata.tts.common.dto.atlas.AtlasWatchlistItemVo;

import java.util.List;
import java.util.Map;

public interface AtlasWatchlistService {

    List<AtlasWatchlistItemVo> list(String openid);

    boolean add(String openid, Map<String, Object> body);

    boolean remove(String openid, String stockId, String removeReason);

    List<AtlasWatchHistoryVo> listHistory(String openid, String filter);

    AtlasHistorySummaryVo summarize(String openid, String filter);
}
