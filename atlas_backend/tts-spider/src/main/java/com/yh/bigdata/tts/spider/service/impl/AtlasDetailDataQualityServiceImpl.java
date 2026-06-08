package com.yh.bigdata.tts.spider.service.impl;

import com.yh.bigdata.tts.common.dao.StockAnnualReportMapper;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.dao.StockIndustryBenchmarkMapper;
import com.yh.bigdata.tts.spider.service.AtlasDetailDataQualityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AtlasDetailDataQualityServiceImpl implements AtlasDetailDataQualityService {

    @Autowired
    private StockBaseMapper stockBaseMapper;

    @Autowired
    private StockAnnualReportMapper stockAnnualReportMapper;

    @Autowired
    private StockIndustryBenchmarkMapper stockIndustryBenchmarkMapper;

    @Value("${atlas.spider.report.years:15}")
    private int reportYears;

    @Override
    public Map<String, Object> snapshot() {
        int stockTotal = safe(stockBaseMapper.countAll());
        int withIndustry = safe(stockBaseMapper.countWithIndustry());
        int withMainBusiness = safe(stockBaseMapper.countWithMainBusiness());
        int withBusinessBrief = safe(stockBaseMapper.countWithBusinessBrief());
        int withPe = safe(stockBaseMapper.countWithPeTtm());
        int annualCodes = safe(stockAnnualReportMapper.countDistinctCodes());
        int annualRows = safe(stockAnnualReportMapper.countTotalRows());
        int annualRich = safe(stockAnnualReportMapper.countCodesWithMinYears(Math.min(5, reportYears)));
        int annualFull = safe(stockAnnualReportMapper.countCodesWithMinYears(Math.min(10, reportYears)));
        int benchmarkIndustries = safe(stockIndustryBenchmarkMapper.countAll());

        Map<String, Object> base = new LinkedHashMap<>();
        base.put("stockTotal", stockTotal);
        base.put("withIndustry", withIndustry);
        base.put("withMainBusiness", withMainBusiness);
        base.put("withBusinessBrief", withBusinessBrief);
        base.put("withPeTtm", withPe);
        base.put("industryRate", rate(withIndustry, stockTotal));
        base.put("mainBusinessRate", rate(withMainBusiness, stockTotal));
        base.put("businessBriefRate", rate(withBusinessBrief, stockTotal));
        base.put("peTtmRate", rate(withPe, stockTotal));

        Map<String, Object> annual = new LinkedHashMap<>();
        annual.put("distinctCodes", annualCodes);
        annual.put("totalRows", annualRows);
        annual.put("codesWith5PlusYears", annualRich);
        annual.put("codesWith10PlusYears", annualFull);
        annual.put("annualCodeRate", rate(annualCodes, stockTotal));
        annual.put("targetYears", reportYears);

        Map<String, Object> benchmark = new LinkedHashMap<>();
        benchmark.put("industryCount", benchmarkIndustries);

        Map<String, Object> ready = new LinkedHashMap<>();
        ready.put("detailReady", rate(withIndustry, stockTotal) >= 85.0
                && rate(withBusinessBrief, stockTotal) >= 70.0
                && rate(annualCodes, stockTotal) >= 80.0);
        ready.put("compassReady", rate(annualFull, stockTotal) >= 70.0);
        ready.put("radarReady", benchmarkIndustries >= 10 && rate(annualCodes, stockTotal) >= 80.0);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("base", base);
        out.put("annual", annual);
        out.put("benchmark", benchmark);
        out.put("readiness", ready);
        return out;
    }

    private int safe(int v) {
        return Math.max(0, v);
    }

    private double rate(int part, int total) {
        if (total <= 0) {
            return 0D;
        }
        return BigDecimal.valueOf(part * 100.0 / total)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
