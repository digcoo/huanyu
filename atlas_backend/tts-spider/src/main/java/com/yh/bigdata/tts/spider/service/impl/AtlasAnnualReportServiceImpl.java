package com.yh.bigdata.tts.spider.service.impl;

import com.yh.bigdata.tts.common.dao.StockAnnualReportMapper;
import com.yh.bigdata.tts.common.dto.atlas.AtlasChartVo;
import com.yh.bigdata.tts.common.dto.atlas.AtlasCompassModuleVo;
import com.yh.bigdata.tts.common.dto.atlas.AtlasSeriesPointVo;
import com.yh.bigdata.tts.common.model.IndustryYearlyMetrics;
import com.yh.bigdata.tts.common.model.StockAnnualReport;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.utils.StockCodeUtil;
import com.yh.bigdata.tts.spider.crawler.report.StockAnnualReportCrawler;
import com.yh.bigdata.tts.spider.profile.AtlasCompassInsightTemplates;
import com.yh.bigdata.tts.spider.service.AtlasAnnualReportService;
import com.yh.bigdata.tts.spider.service.AtlasCompassBenchmarkService;
import com.yh.bigdata.tts.spider.service.AtlasDetailComputeService;
import com.yh.bigdata.tts.spider.service.AtlasIndustryProfileService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Service
public class AtlasAnnualReportServiceImpl implements AtlasAnnualReportService {

    @Autowired
    private StockAnnualReportMapper stockAnnualReportMapper;

    @Autowired
    private StockAnnualReportCrawler stockAnnualReportCrawler;

    @Autowired
    private AtlasIndustryProfileService atlasIndustryProfileService;

    @Autowired
    private AtlasCompassBenchmarkService atlasCompassBenchmarkService;

    @Autowired
    private AtlasDetailComputeService atlasDetailComputeService;

    @Value("${atlas.spider.report.years:15}")
    private int reportYears;

    @Override
    public List<StockAnnualReport> getReports(String code, int limit) {
        String normalized = StockCodeUtil.normalizeCnCode(code);
        return stockAnnualReportMapper.selectByCode(normalized, limit > 0 ? limit : reportYears);
    }

    @Override
    public StockAnnualReport getLatest(String code) {
        return stockAnnualReportMapper.selectLatestByCode(StockCodeUtil.normalizeCnCode(code));
    }

    @Override
    public Map<String, AtlasCompassModuleVo> buildCompassFromAnnual(StockBase stock) {
        if (stock == null || StringUtils.isBlank(stock.getCode())) {
            return null;
        }
        List<StockAnnualReport> reports = getReports(stock.getCode(), reportYears);
        if (reports.isEmpty()) {
            return null;
        }

        String industry = StringUtils.defaultIfBlank(stock.getIndustry(), "综合");
        List<IndustryYearlyMetrics> industryRows = atlasCompassBenchmarkService.loadIndustryMetrics(industry);
        StockAnnualReport latest = reports.get(reports.size() - 1);
        String stageId = stageId(latest);

        List<AtlasSeriesPointVo> revenue = series(reports, r -> yi(r.getTotalRevenue()));
        List<AtlasSeriesPointVo> netProfit = series(reports, r -> yi(r.getNetProfit()));
        List<AtlasSeriesPointVo> ocf = series(reports, r -> yi(r.getOperatingCashFlow()));
        List<AtlasSeriesPointVo> capex = series(reports, r -> yi(r.getCapex()));
        List<AtlasSeriesPointVo> revenueYoy = series(reports, StockAnnualReport::getRevenueYoy);
        List<AtlasSeriesPointVo> staffNum = series(reports, r -> r.getStaffNum() == null ? null : r.getStaffNum().doubleValue());
        List<AtlasSeriesPointVo> revenuePerStaff = series(reports, StockAnnualReport::getRevenuePerStaff);
        List<AtlasSeriesPointVo> grossMargin = series(reports, StockAnnualReport::getGrossMargin);
        List<AtlasSeriesPointVo> netMargin = series(reports, StockAnnualReport::getNetMargin);
        List<AtlasSeriesPointVo> prepaidRatio = series(reports, StockAnnualReport::getPrepaidRatio);
        List<AtlasSeriesPointVo> inventoryDays = series(reports, StockAnnualReport::getInventoryDays);
        List<AtlasSeriesPointVo> receivableDays = series(reports, StockAnnualReport::getReceivableDays);
        List<AtlasSeriesPointVo> debtRatio = series(reports, StockAnnualReport::getDebtRatio);
        List<AtlasSeriesPointVo> interestDebtRatio = series(reports, StockAnnualReport::getInterestDebtRatio);
        List<AtlasSeriesPointVo> currentRatio = series(reports, r -> r.getCurrentRatio());

        Map<String, AtlasCompassModuleVo> compass = new LinkedHashMap<>();
        compass.put("financial", module("财务动能", "#0ecb81",
                AtlasCompassInsightTemplates.financial(stageId),
                Arrays.asList(
                        compareChart("营收", "亿", "#0ecb81", revenue, industryRows,
                                IndustryYearlyMetrics::getAvgTotalRevenue, true, 0.82),
                        compareChart("净利润", "亿", "#f0b90b", netProfit, industryRows,
                                IndustryYearlyMetrics::getAvgNetProfit, true, 0.78),
                        compareChart("经营现金流", "亿", "#848e9c", ocf, industryRows,
                                IndustryYearlyMetrics::getAvgOperatingCashFlow, true, 0.85),
                        compareChart("资本开支", "亿", "#f6465d", capex, industryRows,
                                IndustryYearlyMetrics::getAvgCapex, true, 0.88),
                        compareChart("营收同比", "%", "#a78bfa", revenueYoy, industryRows,
                                IndustryYearlyMetrics::getAvgRevenueYoy, false, 0.90)
                )));
        compass.put("operation", module("运营人效", "#f0b90b",
                AtlasCompassInsightTemplates.operation(stageId),
                Arrays.asList(
                        compareChart("员工数", "人", "#848e9c", staffNum, industryRows,
                                IndustryYearlyMetrics::getAvgStaffNum, false, 0.95),
                        compareChart("人均创收", "万", "#0ecb81", revenuePerStaff, industryRows,
                                IndustryYearlyMetrics::getAvgRevenuePerStaff, false, 0.72),
                        compareChart("存货周转天数", "天", "#ff9500", inventoryDays, industryRows,
                                IndustryYearlyMetrics::getAvgInventoryDays, false, 1.08),
                        compareChart("应收周转天数", "天", "#a78bfa", receivableDays, industryRows,
                                IndustryYearlyMetrics::getAvgReceivableDays, false, 1.05)
                )));
        compass.put("chain", module("产业链地位", "#a78bfa",
                AtlasCompassInsightTemplates.chain(stageId),
                Arrays.asList(
                        compareChart("毛利率", "%", "#0ecb81", grossMargin, industryRows,
                                IndustryYearlyMetrics::getAvgGrossMargin, false, 0.82),
                        compareChart("净利率", "%", "#f0b90b", netMargin, industryRows,
                                IndustryYearlyMetrics::getAvgNetMargin, false, 0.78),
                        compareChart("预收占比", "%", "#848e9c", prepaidRatio, industryRows,
                                IndustryYearlyMetrics::getAvgPrepaidRatio, false, 0.65)
                )));
        compass.put("capital", module("资本结构", "#f6465d",
                AtlasCompassInsightTemplates.capital(stageId),
                Arrays.asList(
                        compareChart("资产负债率", "%", "#f6465d", debtRatio, industryRows,
                                IndustryYearlyMetrics::getAvgDebtRatio, false, 1.02),
                        compareChart("有息负债率", "%", "#ff9500", interestDebtRatio, industryRows,
                                IndustryYearlyMetrics::getAvgInterestDebtRatio, false, 0.92),
                        compareChart("流动比率", "x", "#848e9c", currentRatio, industryRows,
                                IndustryYearlyMetrics::getAvgCurrentRatio, false, 0.92)
                )));
        return compass;
    }

    private String stageId(StockAnnualReport latest) {
        Map<String, Object> stage = atlasDetailComputeService.computeStage(latest);
        Object id = stage.get("id");
        return id != null ? String.valueOf(id) : "stable";
    }

    @Override
    public Map<String, Object> buildProfileFromAnnual(StockBase stock, StockAnnualReport latest) {
        return atlasIndustryProfileService.buildProfile(stock, latest);
    }

    @Override
    public List<Map<String, String>> buildKeyMetricsFromAnnual(StockBase stock, StockAnnualReport latest) {
        List<Map<String, String>> metrics = new ArrayList<>();
        if (latest != null) {
            if (latest.getRoe() != null) {
                metrics.add(metric("ROE", round2(latest.getRoe()) + "%"));
            }
            if (latest.getGrossMargin() != null) {
                metrics.add(metric("毛利率", round2(latest.getGrossMargin()) + "%"));
            }
            if (latest.getNetMargin() != null) {
                metrics.add(metric("净利率", round2(latest.getNetMargin()) + "%"));
            }
            if (latest.getDebtRatio() != null) {
                metrics.add(metric("资产负债率", round2(latest.getDebtRatio()) + "%"));
            }
            if (latest.getRevenueYoy() != null) {
                metrics.add(metric("营收同比", round2(latest.getRevenueYoy()) + "%"));
            }
            metrics.add(metric("财报年度", String.valueOf(latest.getReportYear())));
        }
        if (stock.getTurnoverRate() != null) {
            metrics.add(metric("换手率", formatPct(stock.getTurnoverRate())));
        }
        return metrics;
    }

    @Override
    public void refreshIfMissingAsync(String code, String name) {
        String normalized = StockCodeUtil.normalizeCnCode(code);
        if (stockAnnualReportMapper.countByCode(normalized) > 0) {
            return;
        }
        CompletableFuture.runAsync(() -> stockAnnualReportCrawler.runOne(normalized, name));
    }

    private AtlasChartVo compareChart(String name, String unit, String color,
                                      List<AtlasSeriesPointVo> data,
                                      List<IndustryYearlyMetrics> industryRows,
                                      Function<IndustryYearlyMetrics, Double> metricFn,
                                      boolean scaleYi, double fallbackRatio) {
        List<AtlasSeriesPointVo> industryData = atlasCompassBenchmarkService.alignIndustrySeries(
                data, industryRows, metricFn, scaleYi, fallbackRatio);
        return AtlasChartVo.builder()
                .name(name)
                .unit(unit)
                .color(color)
                .data(data)
                .industryData(industryData.isEmpty() ? null : industryData)
                .build();
    }

    private List<AtlasSeriesPointVo> series(List<StockAnnualReport> reports,
                                            Function<StockAnnualReport, Double> fn) {
        List<AtlasSeriesPointVo> points = new ArrayList<>();
        for (StockAnnualReport r : reports) {
            Double val = fn.apply(r);
            if (val == null) {
                continue;
            }
            points.add(AtlasSeriesPointVo.builder()
                    .year(String.valueOf(r.getReportYear()))
                    .value(round2(val))
                    .build());
        }
        return points;
    }

    private double yi(Double amount) {
        if (amount == null) {
            return 0D;
        }
        return amount / 100_000_000D;
    }

    private AtlasCompassModuleVo module(String title, String color, String insight, List<AtlasChartVo> charts) {
        return AtlasCompassModuleVo.builder()
                .title(title)
                .color(color)
                .insight(insight)
                .charts(charts)
                .build();
    }

    private Map<String, String> metric(String label, String value) {
        Map<String, String> item = new LinkedHashMap<>();
        item.put("label", label);
        item.put("value", value);
        return item;
    }

    private String formatPct(Double rate) {
        if (rate == null) {
            return "0%";
        }
        return round2(rate * 100) + "%";
    }

    private double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
