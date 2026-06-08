package com.yh.bigdata.tts.spider.service.impl;

import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.dao.StockCompanyRelationMapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockCompanyRelation;
import com.yh.bigdata.tts.common.utils.StockCodeUtil;
import com.yh.bigdata.tts.spider.service.AtlasSupplierCustomerService;
import com.yh.bigdata.tts.spider.utils.CninfoClient;
import com.yh.bigdata.tts.spider.utils.CninfoPdfStore;
import com.yh.bigdata.tts.spider.utils.CninfoTradingPartiesParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.nio.file.Path;

@Service
public class AtlasSupplierCustomerServiceImpl implements AtlasSupplierCustomerService {

    private static final Logger log = LoggerFactory.getLogger(AtlasSupplierCustomerServiceImpl.class);

    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile Map<String, Object> lastResult = new LinkedHashMap<>();

    @Autowired
    private StockCompanyRelationMapper stockCompanyRelationMapper;

    @Autowired
    private StockBaseMapper stockBaseMapper;

    @Value("${atlas.spider.trading-parties.interval-ms:1500}")
    private long intervalMs;

    @Value("${atlas.spider.cninfo.pdf-dir:./data/cninfo-pdf}")
    private String cninfoPdfDir;

    @Value("${atlas.spider.cninfo.pdf-cache-read:true}")
    private boolean cninfoPdfCacheRead;

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public Map<String, Object> lastResult() {
        return lastResult;
    }

    @Override
    public boolean crawlAllAsync() {
        if (!running.compareAndSet(false, true)) {
            return false;
        }
        CompletableFuture.runAsync(() -> {
            try {
                doCrawlAll();
            } finally {
                running.set(false);
            }
        });
        return true;
    }

    @Override
    public Map<String, Object> crawlOne(String code) {
        Map<String, Object> result = crawlOneInternal(code, true);
        if (Boolean.TRUE.equals(result.get("ok"))) {
            @SuppressWarnings("unchecked")
            List<CninfoTradingPartiesParser.TradingPartyRow> customers =
                    (List<CninfoTradingPartiesParser.TradingPartyRow>) result.remove("_customers");
            @SuppressWarnings("unchecked")
            List<CninfoTradingPartiesParser.TradingPartyRow> suppliers =
                    (List<CninfoTradingPartiesParser.TradingPartyRow>) result.remove("_suppliers");
            result.put("customers", toPreview(customers));
            result.put("suppliers", toPreview(suppliers));
        }
        return result;
    }

    private Map<String, Object> doCrawlAll() {
        long start = System.currentTimeMillis();
        int total = 0;
        int ok = 0;
        int noReport = 0;
        int pdfFail = 0;
        int empty = 0;
        int error = 0;
        log.info("AtlasSupplierCustomer ALL start");
        try {
            List<StockBase> all = stockBaseMapper.selectAll();
            for (StockBase stock : all) {
                if (stock == null || StringUtils.isBlank(stock.getCode())) {
                    continue;
                }
                total++;
                try {
                    Map<String, Object> one = crawlOneInternal(stock.getCode(), false);
                    String status = (String) one.get("status");
                    if ("ok".equals(status)) {
                        ok++;
                    } else if ("no_report".equals(status)) {
                        noReport++;
                    } else if ("pdf_fail".equals(status)) {
                        pdfFail++;
                    } else if ("empty".equals(status)) {
                        empty++;
                    } else {
                        error++;
                    }
                    if (total % 50 == 0) {
                        log.info("AtlasSupplierCustomer progress {}/{}, ok={}", total, all.size(), ok);
                    }
                    sleepInterval();
                } catch (Exception e) {
                    error++;
                    log.warn("AtlasSupplierCustomer failed, code={}", stock.getCode(), e);
                }
            }
            int withCustomer = stockCompanyRelationMapper.countDistinctCodesByType("customer");
            int withSupplier = stockCompanyRelationMapper.countDistinctCodesByType("supplier");
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("started", true);
            result.put("total", total);
            result.put("ok", ok);
            result.put("noReport", noReport);
            result.put("pdfFail", pdfFail);
            result.put("empty", empty);
            result.put("error", error);
            result.put("withCustomer", withCustomer);
            result.put("withSupplier", withSupplier);
            result.put("costSec", (System.currentTimeMillis() - start) / 1000);
            lastResult = result;
            log.info("AtlasSupplierCustomer ALL done, {}", result);
            return result;
        } catch (Exception e) {
            log.error("AtlasSupplierCustomer ALL failed", e);
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("started", true);
            err.put("error", e.getMessage());
            err.put("ok", ok);
            err.put("total", total);
            err.put("costSec", (System.currentTimeMillis() - start) / 1000);
            lastResult = err;
            return err;
        }
    }

    private Map<String, Object> crawlOneInternal(String code, boolean includePreview) {
        Map<String, Object> result = new LinkedHashMap<>();
        String normalized = StockCodeUtil.normalizeCnCode(code);
        if (StringUtils.isBlank(normalized)) {
            result.put("ok", false);
            result.put("status", "invalid");
            result.put("message", "invalid code");
            return result;
        }
        result.put("code", normalized);

        CninfoClient.AnnualReportDoc doc = CninfoClient.fetchLatestAnnualReport(normalized);
        if (doc == null) {
            result.put("ok", false);
            result.put("status", "no_report");
            result.put("message", "未找到巨潮年报");
            return result;
        }
        result.put("reportTitle", doc.getTitle());
        result.put("reportYear", doc.getReportYear());
        result.put("pdfUrl", doc.getPdfUrl());

        byte[] pdf = null;
        String pdfSource = "download";
        if (cninfoPdfCacheRead) {
            pdf = CninfoPdfStore.loadIfExists(cninfoPdfDir, normalized, doc.getReportYear());
            if (pdf != null) {
                pdfSource = "cache";
            }
        }
        if (pdf == null) {
            pdf = CninfoClient.downloadPdf(doc.getPdfUrl());
            if (pdf != null && pdf.length > 0) {
                String savedPath = CninfoPdfStore.save(cninfoPdfDir, normalized, doc.getReportYear(), pdf);
                result.put("pdfPath", savedPath);
            }
        } else {
            Path cached = CninfoPdfStore.resolveFile(cninfoPdfDir, normalized, doc.getReportYear());
            if (cached != null) {
                result.put("pdfPath", cached.toAbsolutePath().toString());
            }
        }
        result.put("pdfSource", pdfSource);

        if (pdf == null || pdf.length == 0) {
            result.put("ok", false);
            result.put("status", "pdf_fail");
            result.put("message", "年报 PDF 下载失败");
            return result;
        }
        result.put("pdfBytes", pdf.length);

        CninfoTradingPartiesParser.ParseResult parsed = CninfoTradingPartiesParser.parse(pdf);
        List<CninfoTradingPartiesParser.TradingPartyRow> customers = parsed.getCustomers();
        List<CninfoTradingPartiesParser.TradingPartyRow> suppliers = parsed.getSuppliers();
        if (customers.isEmpty() && suppliers.isEmpty()) {
            result.put("ok", false);
            result.put("status", "empty");
            result.put("message", "年报中未解析到前五客户/供应商");
            return result;
        }

        saveRows(normalized, customers, "customer", doc.getReportYear());
        saveRows(normalized, suppliers, "supplier", doc.getReportYear());

        result.put("ok", true);
        result.put("status", "ok");
        result.put("customerCount", customers.size());
        result.put("supplierCount", suppliers.size());
        if (includePreview) {
            result.put("_customers", customers);
            result.put("_suppliers", suppliers);
        }
        log.debug("AtlasSupplierCustomer crawl done, code={}, customers={}, suppliers={}, year={}",
                normalized, customers.size(), suppliers.size(), doc.getReportYear());
        return result;
    }

    private void saveRows(String code, List<CninfoTradingPartiesParser.TradingPartyRow> rows,
                          String relationType, String reportYear) {
        stockCompanyRelationMapper.deleteByCodeAndType(code, relationType);
        if (rows == null || rows.isEmpty()) {
            return;
        }
        List<StockCompanyRelation> list = new ArrayList<>();
        for (CninfoTradingPartiesParser.TradingPartyRow row : rows) {
            StockCompanyRelation rel = new StockCompanyRelation();
            rel.setCode(code);
            rel.setRelatedCode(relationType + "_" + row.getRank());
            rel.setRelatedName(row.displayLabel());
            rel.setRelationType(relationType);
            rel.setSortOrder(row.getRank());
            rel.setSource("cninfo_annual:" + StringUtils.defaultString(reportYear, "unknown"));
            list.add(rel);
        }
        stockCompanyRelationMapper.insertBatch(list);
    }

    private static List<Map<String, String>> toPreview(List<CninfoTradingPartiesParser.TradingPartyRow> rows) {
        List<Map<String, String>> list = new ArrayList<>();
        if (rows == null) {
            return list;
        }
        for (CninfoTradingPartiesParser.TradingPartyRow row : rows) {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("rank", String.valueOf(row.getRank()));
            m.put("name", row.getName());
            m.put("label", row.displayLabel());
            list.add(m);
        }
        return list;
    }

    private void sleepInterval() {
        if (intervalMs <= 0) {
            return;
        }
        try {
            Thread.sleep(intervalMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
