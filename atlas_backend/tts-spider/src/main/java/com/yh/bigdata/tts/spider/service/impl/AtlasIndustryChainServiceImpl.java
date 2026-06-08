package com.yh.bigdata.tts.spider.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.dao.StockCompanyRelationMapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockCompanyRelation;
import com.yh.bigdata.tts.common.utils.StockCodeUtil;
import com.yh.bigdata.tts.spider.service.AtlasIndustryChainService;
import com.yh.bigdata.tts.spider.utils.EastMoneyF10Client;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class AtlasIndustryChainServiceImpl implements AtlasIndustryChainService {

    @Autowired
    private StockCompanyRelationMapper stockCompanyRelationMapper;

    @Autowired
    private StockBaseMapper stockBaseMapper;

    @Override
    public Map<String, Object> buildIndustryChain(String code) {
        String normalized = StockCodeUtil.normalizeCnCode(code);
        Map<String, Object> chain = new LinkedHashMap<>();
        chain.put("upstream", toRelationList(stockCompanyRelationMapper.selectByCodeAndType(normalized, "supplier")));
        chain.put("downstream", toRelationList(stockCompanyRelationMapper.selectByCodeAndType(normalized, "customer")));
        chain.put("segments", loadSegments(normalized));
        return chain;
    }

    @Override
    public void refreshSegmentsIfMissingAsync(String code) {
        String normalized = StockCodeUtil.normalizeCnCode(code);
        if (StringUtils.isBlank(normalized)) {
            return;
        }
        List<StockCompanyRelation> existing = stockCompanyRelationMapper.selectByCodeAndType(normalized, "segment");
        if (existing != null && !existing.isEmpty()) {
            return;
        }
        CompletableFuture.runAsync(() -> crawlAndSaveSegments(normalized));
    }

    @Override
    public void ensureSegments(String code) {
        String normalized = StockCodeUtil.normalizeCnCode(code);
        if (StringUtils.isBlank(normalized)) {
            return;
        }
        List<StockCompanyRelation> existing = stockCompanyRelationMapper.selectByCodeAndType(normalized, "segment");
        if (existing != null && !existing.isEmpty()) {
            return;
        }
        crawlAndSaveSegments(normalized);
    }

    @Override
    public void crawlAndSaveSegments(String code) {
        JSONObject root = EastMoneyF10Client.fetchBusinessAnalysis(code);
        if (root == null) {
            return;
        }
        List<StockCompanyRelation> rows = parseSegments(code, root);
        if (rows.isEmpty()) {
            return;
        }
        stockCompanyRelationMapper.deleteByCodeAndType(code, "segment");
        stockCompanyRelationMapper.insertBatch(rows);
    }

    @Override
    public String buildSegmentBrief(String code) {
        String normalized = StockCodeUtil.normalizeCnCode(code);
        if (StringUtils.isBlank(normalized)) {
            return null;
        }
        List<StockCompanyRelation> rows = stockCompanyRelationMapper.selectByCodeAndType(normalized, "segment");
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        List<SegmentPart> products = new ArrayList<>();
        List<SegmentPart> regions = new ArrayList<>();
        for (StockCompanyRelation row : rows) {
            if (row == null || StringUtils.isBlank(row.getRelatedName())) {
                continue;
            }
            SegmentPart part = parseSegmentName(row.getRelatedName());
            if (part == null) {
                continue;
            }
            String source = row.getSource() != null ? row.getSource() : "";
            if (source.contains("product")) {
                products.add(part);
            } else if (source.contains("region")) {
                regions.add(part);
            }
        }
        if (products.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        SegmentPart top = products.get(0);
        sb.append("以").append(top.name).append("为主");
        if (StringUtils.isNotBlank(top.pct)) {
            sb.append("（").append(top.pct).append("）");
        }
        if (products.size() > 1) {
            SegmentPart second = products.get(1);
            sb.append("，").append(second.name);
            if (StringUtils.isNotBlank(second.pct)) {
                sb.append(" ").append(second.pct);
            }
        }
        if (!regions.isEmpty()) {
            SegmentPart region = regions.get(0);
            sb.append("；").append(region.name);
            if (StringUtils.isNotBlank(region.pct)) {
                sb.append(" ").append(region.pct);
            }
        }
        return sb.toString();
    }

    private SegmentPart parseSegmentName(String relatedName) {
        if (StringUtils.isBlank(relatedName)) {
            return null;
        }
        int idx = relatedName.indexOf('·');
        if (idx < 0) {
            idx = relatedName.indexOf(' ');
        }
        if (idx > 0) {
            return new SegmentPart(
                    relatedName.substring(0, idx).trim(),
                    relatedName.substring(idx + 1).trim());
        }
        return new SegmentPart(relatedName.trim(), null);
    }

    private static final class SegmentPart {
        final String name;
        final String pct;

        SegmentPart(String name, String pct) {
            this.name = name;
            this.pct = pct;
        }
    }

    List<StockCompanyRelation> parseSegments(String code, JSONObject root) {
        JSONArray zygcfx = root.getJSONArray("zygcfx");
        if (zygcfx == null || zygcfx.isEmpty()) {
            return Collections.emptyList();
        }
        String latestAnnualDate = findLatestAnnualReportDate(zygcfx);
        if (latestAnnualDate == null) {
            return Collections.emptyList();
        }

        List<JSONObject> productRows = new ArrayList<>();
        List<JSONObject> regionRows = new ArrayList<>();
        for (int i = 0; i < zygcfx.size(); i++) {
            JSONObject row = zygcfx.getJSONObject(i);
            if (row == null || !latestAnnualDate.equals(row.getString("REPORT_DATE"))) {
                continue;
            }
            String itemName = row.getString("ITEM_NAME");
            if (StringUtils.isBlank(itemName) || itemName.contains("补充")) {
                continue;
            }
            String mainOpType = row.getString("MAINOP_TYPE");
            if ("2".equals(mainOpType)) {
                productRows.add(row);
            } else if ("3".equals(mainOpType)) {
                regionRows.add(row);
            }
        }

        productRows.sort(Comparator.comparingDouble(r -> -nz(r.getDouble("MBI_RATIO"))));
        regionRows.sort(Comparator.comparingDouble(r -> -nz(r.getDouble("MBI_RATIO"))));

        List<StockCompanyRelation> list = new ArrayList<>();
        appendSegmentRows(list, code, productRows, "product", 5);
        appendSegmentRows(list, code, regionRows, "region", 3);
        return list;
    }

    private void appendSegmentRows(List<StockCompanyRelation> list, String code,
                                   List<JSONObject> rows, String segmentKind, int limit) {
        int order = list.size();
        for (int i = 0; i < rows.size() && i < limit; i++) {
            JSONObject row = rows.get(i);
            Double ratio = row.getDouble("MBI_RATIO");
            if (ratio == null || ratio <= 0) {
                continue;
            }
            StockCompanyRelation rel = new StockCompanyRelation();
            rel.setCode(code);
            rel.setRelatedCode(segmentKind + "_" + (i + 1));
            rel.setRelatedName(row.getString("ITEM_NAME") + " · " + formatPct(ratio * 100));
            rel.setRelationType("segment");
            rel.setSortOrder(order++);
            rel.setSource("eastmoney_zygcfx:" + segmentKind);
            list.add(rel);
        }
    }

    private String findLatestAnnualReportDate(JSONArray zygcfx) {
        String latest = null;
        for (int i = 0; i < zygcfx.size(); i++) {
            JSONObject row = zygcfx.getJSONObject(i);
            if (row == null) {
                continue;
            }
            String reportDate = row.getString("REPORT_DATE");
            if (StringUtils.isBlank(reportDate) || !reportDate.contains("-12-31")) {
                continue;
            }
            if (latest == null || reportDate.compareTo(latest) > 0) {
                latest = reportDate;
            }
        }
        return latest;
    }

    private List<Map<String, Object>> loadSegments(String code) {
        List<StockCompanyRelation> rows = stockCompanyRelationMapper.selectByCodeAndType(code, "segment");
        return toRelationList(rows);
    }

    private List<Map<String, Object>> toRelationList(List<StockCompanyRelation> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (StockCompanyRelation row : rows) {
            if (row == null) {
                continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", row.getRelatedCode());
            m.put("name", row.getRelatedName());
            m.put("type", row.getRelationType());
            m.put("source", row.getSource());
            enrichQuote(m, row.getRelatedCode());
            list.add(m);
        }
        return list;
    }

    private void enrichQuote(Map<String, Object> target, String relatedCode) {
        if (StringUtils.isBlank(relatedCode) || relatedCode.startsWith("product_")
                || relatedCode.startsWith("region_") || relatedCode.contains("行业")) {
            return;
        }
        String normalized = StockCodeUtil.normalizeCnCode(relatedCode);
        if (StringUtils.isBlank(normalized)) {
            return;
        }
        StockBase cached = RealtimeStockCache.filterStockMap != null
                ? RealtimeStockCache.filterStockMap.get(normalized) : null;
        if (cached == null) {
            cached = stockBaseMapper.selectByPrimaryKey(normalized);
        }
        if (cached == null) {
            return;
        }
        if (cached.getClose() > 0) {
            target.put("price", round2(cached.getClose()));
        }
        if (cached.getChangeRate() != null) {
            target.put("changePct", round2(cached.getChangeRate() * 100));
        }
        if (cached.getPeTtm() != null && cached.getPeTtm() > 0) {
            target.put("peTtm", round2(cached.getPeTtm()));
        }
    }

    private double nz(Double v) {
        return v == null ? 0D : v;
    }

    private String formatPct(double pct) {
        return BigDecimal.valueOf(pct).setScale(1, RoundingMode.HALF_UP).toPlainString() + "%";
    }

    private double round2(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
