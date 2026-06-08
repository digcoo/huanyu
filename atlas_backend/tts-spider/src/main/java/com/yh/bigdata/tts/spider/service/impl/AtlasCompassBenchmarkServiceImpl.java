package com.yh.bigdata.tts.spider.service.impl;

import com.yh.bigdata.tts.common.dao.StockAnnualReportMapper;
import com.yh.bigdata.tts.common.dto.atlas.AtlasSeriesPointVo;
import com.yh.bigdata.tts.common.model.IndustryYearlyMetrics;
import com.yh.bigdata.tts.spider.service.AtlasCompassBenchmarkService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class AtlasCompassBenchmarkServiceImpl implements AtlasCompassBenchmarkService {

    @Autowired
    private StockAnnualReportMapper stockAnnualReportMapper;

    @Override
    public List<IndustryYearlyMetrics> loadIndustryMetrics(String industry) {
        if (StringUtils.isBlank(industry) || "综合".equals(industry.trim())) {
            return Collections.emptyList();
        }
        List<IndustryYearlyMetrics> rows = stockAnnualReportMapper.selectIndustryYearlyMetrics(industry.trim());
        return rows != null ? rows : Collections.emptyList();
    }

    @Override
    public List<AtlasSeriesPointVo> alignIndustrySeries(List<AtlasSeriesPointVo> companyData,
                                                        List<IndustryYearlyMetrics> industryRows,
                                                        Function<IndustryYearlyMetrics, Double> metricFn,
                                                        boolean scaleYi,
                                                        double fallbackRatio) {
        if (companyData == null || companyData.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Integer, Double> byYear = new HashMap<>();
        if (industryRows != null) {
            for (IndustryYearlyMetrics row : industryRows) {
                if (row == null || row.getReportYear() == null) {
                    continue;
                }
                Double raw = metricFn.apply(row);
                if (raw == null) {
                    continue;
                }
                double val = scaleYi ? raw / 100_000_000D : raw;
                byYear.put(row.getReportYear(), round2(val));
            }
        }

        List<AtlasSeriesPointVo> aligned = new ArrayList<>(companyData.size());
        for (AtlasSeriesPointVo point : companyData) {
            if (point == null || StringUtils.isBlank(point.getYear())) {
                continue;
            }
            Integer year = parseYear(point.getYear());
            Double industryVal = year != null ? byYear.get(year) : null;
            if (industryVal == null) {
                industryVal = round2(point.getValue() * fallbackRatio);
            }
            if (industryVal == null) {
                continue;
            }
            aligned.add(AtlasSeriesPointVo.builder()
                    .year(point.getYear())
                    .value(industryVal)
                    .build());
        }
        return aligned;
    }

    private Integer parseYear(String year) {
        try {
            return Integer.parseInt(year.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
