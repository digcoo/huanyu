package com.yh.bigdata.tts.spider.service.impl;

import com.yh.bigdata.tts.common.dao.StockAnnualReportMapper;
import com.yh.bigdata.tts.common.dto.atlas.AtlasChartVo;
import com.yh.bigdata.tts.common.dto.atlas.AtlasCompassModuleVo;
import com.yh.bigdata.tts.common.dto.atlas.AtlasSeriesPointVo;
import com.yh.bigdata.tts.common.model.StockAnnualReport;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.utils.StockCodeUtil;
import com.yh.bigdata.tts.spider.crawler.report.StockAnnualReportCrawler;
import com.yh.bigdata.tts.spider.service.AtlasAnnualReportService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class AtlasAnnualReportServiceImpl implements AtlasAnnualReportService {

    @Autowired
    private StockAnnualReportMapper stockAnnualReportMapper;

    @Autowired
    private StockAnnualReportCrawler stockAnnualReportCrawler;

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
    public Map<String, AtlasCompassModuleVo> buildCompassFromAnnual(String code) {
        List<StockAnnualReport> reports = getReports(code, reportYears);
        if (reports.isEmpty()) {
            return null;
        }
        Map<String, AtlasCompassModuleVo> compass = new LinkedHashMap<>();
        compass.put("financial", module("财务动能", "#0ecb81",
                "基于东方财富年报：营收、净利润、经营现金流（近" + reports.size() + "年）。",
                Arrays.asList(
                        chart("营收", "亿", "#0ecb81", series(reports, r -> yi(r.getTotalRevenue()))),
                        chart("净利润", "亿", "#f0b90b", series(reports, r -> yi(r.getNetProfit()))),
                        chart("经营现金流", "亿", "#848e9c", series(reports, r -> yi(r.getOperatingCashFlow())))
                )));
        compass.put("operation", module("运营人效", "#f0b90b",
                "基于年报周转天数指标。",
                Arrays.asList(
                        chart("存货周转天数", "天", "#ff9500", series(reports, StockAnnualReport::getInventoryDays)),
                        chart("应收周转天数", "天", "#a78bfa", series(reports, StockAnnualReport::getReceivableDays))
                )));
        compass.put("chain", module("产业链地位", "#a78bfa",
                "基于年报毛利率、净利率。",
                Arrays.asList(
                        chart("毛利率", "%", "#0ecb81", series(reports, StockAnnualReport::getGrossMargin)),
                        chart("净利率", "%", "#f0b90b", series(reports, StockAnnualReport::getNetMargin))
                )));
        compass.put("capital", module("资本结构", "#f6465d",
                "基于年报资产负债率、流动比率。",
                Arrays.asList(
                        chart("资产负债率", "%", "#f6465d", series(reports, StockAnnualReport::getDebtRatio)),
                        chart("流动比率", "x", "#ff9500", series(reports, r -> r.getCurrentRatio()))
                )));
        return compass;
    }

    @Override
    public Map<String, Object> buildProfileFromAnnual(StockBase stock, StockAnnualReport latest) {
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("businessOneLiner", StringUtils.defaultIfBlank(stock.getMainBusiness(),
                stock.getName() + " · A股上市公司"));
        profile.put("industryPosition", buildIndustryPosition(latest));
        profile.put("strengths", buildStrengths(latest));
        profile.put("risks", buildRisks(latest));
        profile.put("dimensions", Collections.emptyList());
        profile.put("dataSource", latest != null ? "eastmoney_annual" : "market_only");
        return profile;
    }

    @Override
    public List<Map<String, String>> buildKeyMetricsFromAnnual(StockBase stock, StockAnnualReport latest) {
        List<Map<String, String>> metrics = new ArrayList<>();
        metrics.add(metric("最新价", formatPrice(stock.getTrade())));
        metrics.add(metric("涨跌幅", formatPct(stock.getChangeRate())));
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

    private String buildIndustryPosition(StockAnnualReport latest) {
        if (latest == null) {
            return "年报数据待爬取，当前展示主营业务与行情摘要。";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(latest.getReportYear()).append(" 年报：");
        if (latest.getRevenueYoy() != null) {
            sb.append("营收同比 ").append(round2(latest.getRevenueYoy())).append("%");
        }
        if (latest.getRoe() != null) {
            sb.append("，ROE ").append(round2(latest.getRoe())).append("%");
        }
        sb.append("（数据来源：东方财富 F10）");
        return sb.toString();
    }

    private List<String> buildStrengths(StockAnnualReport latest) {
        if (latest == null) {
            return Arrays.asList("纳入 Atlas 行情缓存", "支持多周期 K 线查询");
        }
        List<String> list = new ArrayList<>();
        if (latest.getRoe() != null && latest.getRoe() >= 15) {
            list.add("ROE " + round2(latest.getRoe()) + "%，盈利质量较好");
        }
        if (latest.getRevenueYoy() != null && latest.getRevenueYoy() >= 10) {
            list.add("营收同比增长 " + round2(latest.getRevenueYoy()) + "%");
        }
        if (latest.getGrossMargin() != null && latest.getGrossMargin() >= 30) {
            list.add("毛利率 " + round2(latest.getGrossMargin()) + "%，产品议价能力较强");
        }
        if (list.isEmpty()) {
            list.add("已接入 " + latest.getReportYear() + " 年报数据");
        }
        return list;
    }

    private List<String> buildRisks(StockAnnualReport latest) {
        if (latest == null) {
            return Arrays.asList("财务深度数据待接入", "请以官方披露为准");
        }
        List<String> list = new ArrayList<>();
        if (latest.getDebtRatio() != null && latest.getDebtRatio() >= 65) {
            list.add("资产负债率 " + round2(latest.getDebtRatio()) + "%，杠杆偏高");
        }
        if (latest.getProfitYoy() != null && latest.getProfitYoy() < 0) {
            list.add("净利润同比 " + round2(latest.getProfitYoy()) + "%");
        }
        if (latest.getReceivableDays() != null && latest.getReceivableDays() > 90) {
            list.add("应收周转天数 " + round2(latest.getReceivableDays()) + " 天，回款压力需关注");
        }
        list.add("数据来源于公开财报，仅供参考");
        return list;
    }

    private List<AtlasSeriesPointVo> series(List<StockAnnualReport> reports,
                                            java.util.function.Function<StockAnnualReport, Double> fn) {
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

    private AtlasChartVo chart(String name, String unit, String color, List<AtlasSeriesPointVo> data) {
        return AtlasChartVo.builder()
                .name(name)
                .unit(unit)
                .color(color)
                .data(data)
                .build();
    }

    private Map<String, String> metric(String label, String value) {
        Map<String, String> item = new LinkedHashMap<>();
        item.put("label", label);
        item.put("value", value);
        return item;
    }

    private String formatPrice(double price) {
        return BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP).toPlainString();
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
