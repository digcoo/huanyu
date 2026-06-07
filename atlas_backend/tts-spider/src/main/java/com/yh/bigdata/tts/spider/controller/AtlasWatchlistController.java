package com.yh.bigdata.tts.spider.controller;

import com.yh.bigdata.tts.common.dto.atlas.AtlasHistorySummaryVo;
import com.yh.bigdata.tts.common.dto.atlas.AtlasWatchHistoryVo;
import com.yh.bigdata.tts.common.dto.atlas.AtlasWatchlistItemVo;
import com.yh.bigdata.tts.common.param.base.Response;
import com.yh.bigdata.tts.common.param.base.ResponseUtil;
import com.yh.bigdata.tts.spider.auth.AtlasAuthContext;
import com.yh.bigdata.tts.spider.service.AtlasWatchlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/watchlist")
public class AtlasWatchlistController {

    @Autowired
    private AtlasWatchlistService atlasWatchlistService;

    @GetMapping
    public Response<List<AtlasWatchlistItemVo>> list() {
        return ResponseUtil.success(atlasWatchlistService.list(requireOpenid()));
    }

    @PostMapping
    public Response<Map<String, Object>> add(@RequestBody Map<String, Object> body) {
        boolean added = atlasWatchlistService.add(requireOpenid(), body);
        Map<String, Object> data = new HashMap<>();
        data.put("added", added);
        data.put("duplicate", !added);
        return ResponseUtil.success(data);
    }

    @DeleteMapping("/{stockId}")
    public Response<Map<String, Object>> remove(
            @PathVariable("stockId") String stockId,
            @RequestParam(value = "reason", defaultValue = "manual") String reason) {
        boolean removed = atlasWatchlistService.remove(requireOpenid(), stockId, reason);
        Map<String, Object> data = new HashMap<>();
        data.put("removed", removed);
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
