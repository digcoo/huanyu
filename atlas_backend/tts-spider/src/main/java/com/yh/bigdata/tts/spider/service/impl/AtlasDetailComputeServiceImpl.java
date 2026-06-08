package com.yh.bigdata.tts.spider.service.impl;

import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.dao.StockCompanyRelationMapper;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.model.StockAnnualReport;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockCompanyRelation;
import com.yh.bigdata.tts.common.model.StockIndustryBenchmark;
import com.yh.bigdata.tts.spider.service.AtlasDetailComputeService;
import com.yh.bigdata.tts.common.utils.StockCodeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class AtlasDetailComputeServiceImpl implements AtlasDetailComputeService {

    private static final Map<String, Map<String, Object>> STAGE_MAP = new LinkedHashMap<>();

    static {
        STAGE_MAP.put("expansion", stage("expansion", "快速扩张", "营收高增长，资本开支加大，现金流紧但结构健康", "#0ecb81"));
        STAGE_MAP.put("stable", stage("stable", "稳定守业", "营收增速回落，现金流充沛，分红回报提升", "#f0b90b"));
        STAGE_MAP.put("shrink", stage("shrink", "收缩聚焦", "营收下滑但利润短期回升，降本增效特征明显", "#ff9500"));
        STAGE_MAP.put("decline", stage("decline", "萎缩淘汰", "量价齐跌，连续承压，现金流失血风险", "#f6465d"));
    }

    private static final Map<String, String> STAGE_HINTS = new HashMap<>();

    static {
        STAGE_HINTS.put("expansion", "处于快速扩张期 → 关注营收增速与资本开支效率，而非短期估值。");
        STAGE_HINTS.put("stable", "处于稳定守业期 → 适合看现金流、分红与护城河，而非爆发式增长。");
        STAGE_HINTS.put("shrink", "处于收缩聚焦期 → 重点看降本增效能否持续、现金流能否修复。");
        STAGE_HINTS.put("decline", "处于萎缩承压期 → 优先评估偿债能力与核心业务是否还有拐点。");
    }

    @Autowired
    private StockCompanyRelationMapper stockCompanyRelationMapper;

    @Autowired
    private StockBaseMapper stockBaseMapper;

    @Override
    public Map<String, Object> computeStage(StockAnnualReport latest) {
        String id = pickStageId(latest);
        Map<String, Object> result = new LinkedHashMap<>(STAGE_MAP.get(id));
        result.put("stageHint", STAGE_HINTS.getOrDefault(id, ""));
        return result;
    }

    @Override
    public Map<String, Object> computeHealth(StockBase stock, StockAnnualReport latest, StockIndustryBenchmark benchmark) {
        int score = 62;
        Map<String, Integer> breakdown = new LinkedHashMap<>();

        if (latest != null) {
            score = 50;
            int profit = scoreMetric(latest.getRoe(), benchmark != null ? benchmark.getRoeAvg() : 10, 30);
            int growth = scoreMetric(latest.getRevenueYoy(), benchmark != null ? benchmark.getRevenueYoyAvg() : 5, 25);
            int debt = scoreMetricInverse(latest.getDebtRatio(), benchmark != null ? benchmark.getDebtRatioAvg() : 50, 25);
            int operation = scoreMetric(latest.getNetMargin(), benchmark != null ? benchmark.getNetMarginAvg() : 8, 20);
            breakdown.put("profit", profit);
            breakdown.put("growth", growth);
            breakdown.put("debt", debt);
            breakdown.put("operation", operation);
            score = (profit + growth + debt + operation) / 4;
        } else {
            breakdown.put("profit", 60);
            breakdown.put("growth", 58);
            breakdown.put("debt", 62);
            breakdown.put("operation", 60);
        }

        score = Math.min(95, Math.max(35, score));
        String industry = stock != null && StringUtils.isNotBlank(stock.getIndustry()) ? stock.getIndustry() : "综合";
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("healthScore", score);
        health.put("healthRank", "盈利力超过" + industry + "行业 " + score + "% 的公司");
        health.put("healthBreakdown", breakdown);
        return health;
    }

    @Override
    public Map<String, Object> computeRadar(StockAnnualReport latest, StockIndustryBenchmark benchmark) {
        List<String> dimensions = Arrays.asList("ROE", "毛利率", "营收增速", "资产负债率", "净利率");
        List<String> units = Arrays.asList("%", "%", "%", "%", "%");

        double[] company = new double[5];
        double[] industry = new double[5];

        if (latest != null) {
            company[0] = nz(latest.getRoe());
            company[1] = nz(latest.getGrossMargin());
            company[2] = nz(latest.getRevenueYoy());
            company[3] = nz(latest.getDebtRatio());
            company[4] = nz(latest.getNetMargin());
        }
        if (benchmark != null) {
            industry[0] = nz(benchmark.getRoeAvg());
            industry[1] = nz(benchmark.getGrossMarginAvg());
            industry[2] = nz(benchmark.getRevenueYoyAvg());
            industry[3] = nz(benchmark.getDebtRatioAvg());
            industry[4] = nz(benchmark.getNetMarginAvg());
        } else {
            System.arraycopy(company, 0, industry, 0, 5);
            for (int i = 0; i < 5; i++) {
                industry[i] = industry[i] * 0.85;
            }
        }

        List<Double> maxRadar = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            maxRadar.add(Math.max(company[i], industry[i]) * 1.2 + 0.01);
        }

        List<Map<String, Object>> insights = new ArrayList<>();
        boolean[] higherBetter = {true, true, true, false, true};
        String[] winTexts = {"股东回报优于同业", "定价权与盈利空间较强", "成长动能领先行业", "杠杆水平相对可控", "盈利质量优于同业"};
        String[] loseTexts = {"资本回报落后于行业", "盈利空间弱于同业", "营收增长落后于同业", "负债压力高于行业", "净利率低于同业"};
        for (int i = 0; i < 5; i++) {
            boolean win = higherBetter[i] ? company[i] >= industry[i] : company[i] <= industry[i];
            String unit = units.get(i);
            String cmp = win ? "高于" : "低于";
            String dim = dimensions.get(i);
            double cVal = round2(company[i]);
            double iVal = round2(industry[i]);
            Map<String, Object> ins = new LinkedHashMap<>();
            ins.put("dim", dim);
            ins.put("company", cVal);
            ins.put("industry", iVal);
            ins.put("unit", unit);
            ins.put("win", win);
            ins.put("tag", win ? "优势" : "关注");
            ins.put("text", dim + " " + cVal + unit + "，" + cmp + "行业 " + iVal + unit + "，"
                    + (win ? winTexts[i] : loseTexts[i]));
            insights.add(ins);
        }

        Map<String, Object> radar = new LinkedHashMap<>();
        radar.put("dimensions", dimensions);
        radar.put("unit", units);
        radar.put("company", toList(company));
        radar.put("industry", toList(industry));
        radar.put("companyPoints", toRadarPoints(company, maxRadar));
        radar.put("industryPoints", toRadarPoints(industry, maxRadar));
        radar.put("gridLevels", Arrays.asList(20, 35, 50));
        radar.put("insights", insights);
        return radar;
    }

    @Override
    public List<Map<String, Object>> buildCompetitorList(String code) {
        List<StockCompanyRelation> rows = stockCompanyRelationMapper.selectByCodeAndType(code, "competitor");
        List<Map<String, Object>> list = new ArrayList<>();
        for (StockCompanyRelation row : rows) {
            if (row == null) {
                continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", row.getRelatedCode());
            m.put("name", row.getRelatedName());
            enrichCompetitorQuote(m, row.getRelatedCode());
            list.add(m);
        }
        return list;
    }

    private void enrichCompetitorQuote(Map<String, Object> target, String relatedCode) {
        if (StringUtils.isBlank(relatedCode)) {
            return;
        }
        String normalized = StockCodeUtil.normalizeCnCode(relatedCode);
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

    private String pickStageId(StockAnnualReport latest) {
        if (latest == null) {
            return "stable";
        }
        Double revYoy = latest.getRevenueYoy();
        Double profitYoy = latest.getProfitYoy();
        if (revYoy != null && revYoy >= 15) {
            return "expansion";
        }
        if (revYoy != null && revYoy >= 0) {
            return "stable";
        }
        if (revYoy != null && revYoy < 0 && profitYoy != null && profitYoy >= 0) {
            return "shrink";
        }
        if (revYoy != null && revYoy < -5) {
            return "decline";
        }
        return "stable";
    }

    private static Map<String, Object> stage(String id, String label, String desc, String color) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("label", label);
        m.put("desc", desc);
        m.put("color", color);
        return m;
    }

    private int scoreMetric(Double val, double bench, int weight) {
        if (val == null) {
            return 55;
        }
        double ratio = val / (bench == 0 ? 1 : bench);
        return (int) Math.min(99, Math.max(40, 50 + ratio * weight));
    }

    private int scoreMetricInverse(Double val, double bench, int weight) {
        if (val == null) {
            return 55;
        }
        double ratio = bench / (val == 0 ? 1 : val);
        return (int) Math.min(99, Math.max(40, 50 + ratio * weight * 0.5));
    }

    private double nz(Double v) {
        return v == null ? 0 : round2(v);
    }

    private double round2(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private List<Double> toList(double[] arr) {
        List<Double> list = new ArrayList<>(arr.length);
        for (double v : arr) {
            list.add(round2(v));
        }
        return list;
    }

    /** 与小程序 detail-mock radarToPoints 一致的极坐标布局 */
    private List<Map<String, Object>> toRadarPoints(double[] values, List<Double> maxValues) {
        int count = values.length;
        List<Map<String, Object>> points = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2 * i / count) - Math.PI / 2;
            double max = maxValues.get(i);
            double ratio = max <= 0 ? 0 : values[i] / max;
            ratio = Math.min(ratio, 1.15);
            double r = ratio * 42;
            Map<String, Object> pt = new LinkedHashMap<>();
            pt.put("x", String.format(Locale.US, "%.2f", 50 + r * Math.cos(angle)));
            pt.put("y", String.format(Locale.US, "%.2f", 50 + r * Math.sin(angle)));
            points.add(pt);
        }
        return points;
    }
}
