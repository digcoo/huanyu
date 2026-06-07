package com.yh.bigdata.tts.spider.service.impl;

import com.alibaba.fastjson.JSON;
import com.yh.bigdata.tts.common.dao.UserWatchHistoryMapper;
import com.yh.bigdata.tts.common.dao.UserWatchlistMapper;
import com.yh.bigdata.tts.common.dto.atlas.AtlasHistorySummaryVo;
import com.yh.bigdata.tts.common.dto.atlas.AtlasWatchHistoryVo;
import com.yh.bigdata.tts.common.dto.atlas.AtlasWatchlistItemVo;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.UserWatchHistory;
import com.yh.bigdata.tts.common.model.UserWatchlist;
import com.yh.bigdata.tts.common.utils.StockCodeUtil;
import com.yh.bigdata.tts.spider.service.AtlasWatchlistService;
import com.yh.bigdata.tts.spider.service.StockService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AtlasWatchlistServiceImpl implements AtlasWatchlistService {

    private static final int MAX_HISTORY = 200;

    @Autowired
    private UserWatchlistMapper userWatchlistMapper;

    @Autowired
    private UserWatchHistoryMapper userWatchHistoryMapper;

    @Autowired
    private StockService stockService;

    @Override
    public List<AtlasWatchlistItemVo> list(String openid) {
        return userWatchlistMapper.selectByOpenid(openid).stream()
                .map(this::toWatchlistVo)
                .collect(Collectors.toList());
    }

    @Override
    public boolean add(String openid, Map<String, Object> body) {
        if (body == null) {
            return false;
        }
        String stockId = asString(body.get("id"));
        String code = asString(body.get("code"));
        if (StringUtils.isBlank(stockId) || StringUtils.isBlank(code)) {
            return false;
        }
        if (userWatchlistMapper.selectByOpenidAndStockId(openid, stockId) != null) {
            return false;
        }

        Double entryPrice = asDouble(body.get("entryPrice"));
        if (entryPrice == null) {
            entryPrice = asDouble(body.get("price"));
        }
        Long addedAt = asLong(body.get("addedAt"));
        if (addedAt == null) {
            addedAt = System.currentTimeMillis();
        }

        UserWatchlist row = new UserWatchlist();
        row.setOpenid(openid);
        row.setStockId(stockId);
        row.setCode(StockCodeUtil.normalizeCnCode(code));
        row.setName(asString(body.get("name")));
        row.setMarket(StringUtils.defaultIfBlank(asString(body.get("market")), "cn"));
        row.setStrategy(StringUtils.defaultIfBlank(asString(body.get("strategy")), "trend"));
        row.setEntryPrice(entryPrice);
        row.setResonance(asString(body.get("resonance")));
        row.setSummary(asString(body.get("summary")));
        row.setTagsJson(JSON.toJSONString(body.get("tags")));
        row.setAddedAt(addedAt);
        userWatchlistMapper.insert(row);
        return true;
    }

    @Override
    @Transactional
    public boolean remove(String openid, String stockId, String removeReason) {
        UserWatchlist existing = userWatchlistMapper.selectByOpenidAndStockId(openid, stockId);
        if (existing == null) {
            return false;
        }
        archiveInternal(openid, existing, removeReason);
        userWatchlistMapper.deleteByOpenidAndStockId(openid, stockId);
        return true;
    }

    @Override
    public List<AtlasWatchHistoryVo> listHistory(String openid, String filter) {
        String f = normalizeFilter(filter);
        return userWatchHistoryMapper.selectByOpenid(openid, f).stream()
                .map(this::toHistoryVo)
                .collect(Collectors.toList());
    }

    @Override
    public AtlasHistorySummaryVo summarize(String openid, String filter) {
        List<UserWatchHistory> rows = userWatchHistoryMapper.selectByOpenid(openid, normalizeFilter(filter));
        if (rows.isEmpty()) {
            return AtlasHistorySummaryVo.builder()
                    .total(0).winRate(0).winRateText("0%")
                    .avgHoldDays(0).avgHoldText("0天")
                    .avgPnlPct(0).avgPnlText("0%")
                    .build();
        }
        int wins = 0;
        int holdSum = 0;
        double pnlSum = 0;
        for (UserWatchHistory r : rows) {
            if (r.getPnlPct() != null && r.getPnlPct() > 0) {
                wins++;
            }
            holdSum += r.getHoldDays() != null ? r.getHoldDays() : 0;
            pnlSum += r.getPnlPct() != null ? r.getPnlPct() : 0;
        }
        double avgPnl = pnlSum / rows.size();
        int avgHold = Math.round((float) holdSum / rows.size());
        int winRate = Math.round(wins * 100f / rows.size());
        return AtlasHistorySummaryVo.builder()
                .total(rows.size())
                .winRate(winRate)
                .winRateText(winRate + "%")
                .avgHoldDays(avgHold)
                .avgHoldText(avgHold + "天")
                .avgPnlPct(round2(avgPnl))
                .avgPnlText((avgPnl >= 0 ? "+" : "") + round2(avgPnl) + "%")
                .build();
    }

    private void archiveInternal(String openid, UserWatchlist item, String removeReason) {
        double exitPrice = resolveExitPrice(item.getCode(), item.getEntryPrice());
        double entryPrice = item.getEntryPrice() != null ? item.getEntryPrice() : exitPrice;
        long removedAt = System.currentTimeMillis();
        long addedAt = item.getAddedAt() != null ? item.getAddedAt() : removedAt;
        int holdDays = (int) Math.max((removedAt - addedAt) / (24 * 60 * 60 * 1000L), 0);
        double pnlPct = entryPrice != 0 ? ((exitPrice - entryPrice) / Math.abs(entryPrice)) * 100 : 0;

        UserWatchHistory history = new UserWatchHistory();
        history.setOpenid(openid);
        history.setRecordId("wh_" + removedAt + "_" + item.getStockId());
        history.setStockId(item.getStockId());
        history.setCode(item.getCode());
        history.setName(item.getName());
        history.setMarket(item.getMarket());
        history.setStrategy(item.getStrategy());
        history.setResonance(item.getResonance());
        history.setTagsJson(item.getTagsJson());
        history.setEntryPrice(round2(entryPrice));
        history.setExitPrice(round2(exitPrice));
        history.setAddedAt(addedAt);
        history.setRemovedAt(removedAt);
        history.setHoldDays(holdDays);
        history.setPnlPct(round2(pnlPct));
        history.setRemoveReason(StringUtils.defaultIfBlank(removeReason, "manual"));
        userWatchHistoryMapper.insert(history);

        if (userWatchHistoryMapper.countByOpenid(openid) > MAX_HISTORY) {
            userWatchHistoryMapper.deleteOldestBeyond(openid, MAX_HISTORY);
        }
    }

    private double resolveExitPrice(String code, Double fallback) {
        try {
            StockBase stock = stockService.findStockBase(StockCodeUtil.normalizeCnCode(code));
            if (stock != null && stock.getTrade() > 0) {
                return stock.getTrade();
            }
        } catch (Exception ignored) {
        }
        return fallback != null ? fallback : 0;
    }

    private AtlasWatchlistItemVo toWatchlistVo(UserWatchlist row) {
        Double livePrice = resolveExitPrice(row.getCode(), row.getEntryPrice());
        Double changePct = null;
        try {
            StockBase stock = stockService.findStockBase(row.getCode());
            if (stock != null && stock.getLastTrade() != null && stock.getLastTrade() > 0) {
                changePct = round2((stock.getTrade() - stock.getLastTrade()) / stock.getLastTrade() * 100);
            }
        } catch (Exception ignored) {
        }
        return AtlasWatchlistItemVo.builder()
                .id(row.getStockId())
                .code(row.getCode())
                .name(row.getName())
                .market(row.getMarket())
                .strategy(row.getStrategy())
                .price(livePrice)
                .entryPrice(row.getEntryPrice())
                .changePct(changePct)
                .tags(parseTags(row.getTagsJson()))
                .summary(row.getSummary())
                .resonance(row.getResonance())
                .addedAt(row.getAddedAt())
                .build();
    }

    private AtlasWatchHistoryVo toHistoryVo(UserWatchHistory row) {
        return AtlasWatchHistoryVo.builder()
                .recordId(row.getRecordId())
                .id(row.getStockId())
                .code(row.getCode())
                .name(row.getName())
                .market(row.getMarket())
                .strategy(row.getStrategy())
                .resonance(row.getResonance())
                .tags(parseTags(row.getTagsJson()))
                .addedAt(row.getAddedAt())
                .removedAt(row.getRemovedAt())
                .holdDays(row.getHoldDays())
                .entryPrice(row.getEntryPrice())
                .exitPrice(row.getExitPrice())
                .pnlPct(row.getPnlPct())
                .removeReason(row.getRemoveReason())
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<String> parseTags(String tagsJson) {
        if (StringUtils.isBlank(tagsJson)) {
            return Collections.emptyList();
        }
        try {
            return JSON.parseArray(tagsJson, String.class);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String normalizeFilter(String filter) {
        if ("win".equals(filter) || "loss".equals(filter)) {
            return filter;
        }
        return "all";
    }

    private String asString(Object val) {
        return val == null ? null : String.valueOf(val).trim();
    }

    private Double asDouble(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(val));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long asLong(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(val));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private double round2(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
