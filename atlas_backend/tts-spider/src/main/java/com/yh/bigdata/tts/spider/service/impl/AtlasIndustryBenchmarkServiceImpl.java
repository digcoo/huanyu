package com.yh.bigdata.tts.spider.service.impl;

import com.yh.bigdata.tts.common.dao.StockAnnualReportMapper;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.dao.StockIndustryBenchmarkMapper;
import com.yh.bigdata.tts.common.model.StockAnnualReport;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockIndustryBenchmark;
import com.yh.bigdata.tts.spider.service.AtlasIndustryBenchmarkService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AtlasIndustryBenchmarkServiceImpl implements AtlasIndustryBenchmarkService {

    @Autowired
    private StockBaseMapper stockBaseMapper;

    @Autowired
    private StockAnnualReportMapper stockAnnualReportMapper;

    @Autowired
    private StockIndustryBenchmarkMapper stockIndustryBenchmarkMapper;

    @Override
    public int rebuildAll() {
        List<StockBase> stocks = stockBaseMapper.selectAll();
        Map<String, Agg> aggMap = new HashMap<>();

        for (StockBase stock : stocks) {
            if (stock == null || StringUtils.isBlank(stock.getIndustry())) {
                continue;
            }
            StockAnnualReport latest = stockAnnualReportMapper.selectLatestByCode(stock.getCode());
            if (latest == null) {
                continue;
            }
            Agg agg = aggMap.computeIfAbsent(stock.getIndustry(), k -> new Agg());
            agg.count++;
            agg.add(latest.getRoe());
            agg.addGross(latest.getGrossMargin());
            agg.addNet(latest.getNetMargin());
            agg.addRev(latest.getRevenueYoy());
            agg.addDebt(latest.getDebtRatio());
        }

        stockIndustryBenchmarkMapper.deleteAll();
        for (Map.Entry<String, Agg> e : aggMap.entrySet()) {
            Agg agg = e.getValue();
            if (agg.count < 2) {
                continue;
            }
            StockIndustryBenchmark row = new StockIndustryBenchmark();
            row.setIndustry(e.getKey());
            row.setRoeAvg(agg.avgRoe());
            row.setGrossMarginAvg(agg.avgGross());
            row.setNetMarginAvg(agg.avgNet());
            row.setRevenueYoyAvg(agg.avgRev());
            row.setDebtRatioAvg(agg.avgDebt());
            row.setSampleCount(agg.count);
            stockIndustryBenchmarkMapper.upsert(row);
        }
        return aggMap.size();
    }

    private static class Agg {
        int count;
        double roeSum;
        int roeN;
        double grossSum;
        int grossN;
        double netSum;
        int netN;
        double revSum;
        int revN;
        double debtSum;
        int debtN;

        void add(Double v) {
            if (v != null) {
                roeSum += v;
                roeN++;
            }
        }

        void addGross(Double v) {
            if (v != null) {
                grossSum += v;
                grossN++;
            }
        }

        void addNet(Double v) {
            if (v != null) {
                netSum += v;
                netN++;
            }
        }

        void addRev(Double v) {
            if (v != null) {
                revSum += v;
                revN++;
            }
        }

        void addDebt(Double v) {
            if (v != null) {
                debtSum += v;
                debtN++;
            }
        }

        Double avgRoe() {
            return roeN > 0 ? roeSum / roeN : null;
        }

        Double avgGross() {
            return grossN > 0 ? grossSum / grossN : null;
        }

        Double avgNet() {
            return netN > 0 ? netSum / netN : null;
        }

        Double avgRev() {
            return revN > 0 ? revSum / revN : null;
        }

        Double avgDebt() {
            return debtN > 0 ? debtSum / debtN : null;
        }
    }
}
