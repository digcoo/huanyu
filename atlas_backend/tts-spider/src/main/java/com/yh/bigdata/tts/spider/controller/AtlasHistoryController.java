package com.yh.bigdata.tts.spider.controller;

import com.yh.bigdata.tts.common.dto.atlas.AtlasHistorySummaryVo;
import com.yh.bigdata.tts.common.dto.atlas.AtlasWatchHistoryVo;
import com.yh.bigdata.tts.common.param.base.Response;
import com.yh.bigdata.tts.common.param.base.ResponseUtil;
import com.yh.bigdata.tts.spider.auth.AtlasAuthContext;
import com.yh.bigdata.tts.spider.service.AtlasWatchlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/history")
public class AtlasHistoryController {

    @Autowired
    private AtlasWatchlistService atlasWatchlistService;

    @GetMapping
    public Response<Map<String, Object>> list(
            @RequestParam(value = "filter", defaultValue = "all") String filter) {
        String openid = requireOpenid();
        List<AtlasWatchHistoryVo> items = atlasWatchlistService.listHistory(openid, filter);
        AtlasHistorySummaryVo summary = atlasWatchlistService.summarize(openid, filter);
        Map<String, Object> data = new HashMap<>();
        data.put("items", items);
        data.put("summary", summary);
        return ResponseUtil.success(data);
    }

    private String requireOpenid() {
        String openid = AtlasAuthContext.getOpenid();
        if (openid == null) {
            throw new IllegalStateException("unauthorized");
        }
        return openid;
    }
}
