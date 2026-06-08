package com.yh.bigdata.tts.spider.service.impl;

import com.yh.bigdata.tts.common.model.StockAnnualReport;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.spider.profile.AtlasIndustryProfileTemplates;
import com.yh.bigdata.tts.spider.service.AtlasBusinessBriefService;
import com.yh.bigdata.tts.spider.service.AtlasIndustryProfileService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AtlasIndustryProfileServiceImpl implements AtlasIndustryProfileService {

    private static final int MAX_STRENGTHS = 5;
    private static final int MAX_RISKS = 5;

    @Autowired
    private AtlasBusinessBriefService atlasBusinessBriefService;

    @Override
    public Map<String, Object> buildProfile(StockBase stock, StockAnnualReport latest) {
        String name = stock != null ? StringUtils.defaultIfBlank(stock.getName(), "该公司") : "该公司";
        String industry = stock != null ? StringUtils.defaultIfBlank(stock.getIndustry(), "综合") : "综合";

        Map<String, Object> profile = AtlasIndustryProfileTemplates.buildTemplateProfile(name, industry);
        String templateLine = (String) profile.get("businessOneLiner");

        String oneLiner = atlasBusinessBriefService.buildBrief(stock, templateLine);
        profile.put("businessOneLiner", oneLiner);
        profile.put("briefSource", atlasBusinessBriefService.resolveBriefSource(stock, templateLine));

        if (stock != null && StringUtils.isNotBlank(stock.getBusinessScope())) {
            profile.put("businessScope", stock.getBusinessScope());
        }

        String position = (String) profile.get("industryPosition");
        profile.put("industryPosition", enrichIndustryPosition(position, latest));

        @SuppressWarnings("unchecked")
        List<String> templateStrengths = (List<String>) profile.get("strengths");
        @SuppressWarnings("unchecked")
        List<String> templateRisks = (List<String>) profile.get("risks");
        profile.put("strengths", mergeStrengths(buildAnnualStrengths(latest), templateStrengths));
        profile.put("risks", mergeRisks(buildAnnualRisks(latest), templateRisks));

        profile.put("dataSource", buildDataSource(stock, latest));
        if (StringUtils.isNotBlank(industry)) {
            profile.put("industryTag", industry);
        }
        return profile;
    }

    private String enrichIndustryPosition(String templatePosition, StockAnnualReport latest) {
        if (latest == null) {
            return templatePosition;
        }
        String metrics = buildAnnualMetricsSnippet(latest);
        if (StringUtils.isBlank(metrics)) {
            return templatePosition;
        }
        return templatePosition + "（" + metrics + "）";
    }

    private String buildAnnualMetricsSnippet(StockAnnualReport latest) {
        StringBuilder sb = new StringBuilder();
        sb.append(latest.getReportYear()).append(" 年报");
        boolean has = false;
        if (latest.getRevenueYoy() != null) {
            sb.append("：营收同比 ").append(round2(latest.getRevenueYoy())).append("%");
            has = true;
        }
        if (latest.getRoe() != null) {
            sb.append(has ? "，ROE " : "：ROE ").append(round2(latest.getRoe())).append("%");
            has = true;
        }
        if (latest.getGrossMargin() != null) {
            sb.append(has ? "，毛利率 " : "：毛利率 ").append(round2(latest.getGrossMargin())).append("%");
        }
        return has || latest.getGrossMargin() != null ? sb.toString() : "";
    }

    private List<String> buildAnnualStrengths(StockAnnualReport latest) {
        List<String> list = new ArrayList<>();
        if (latest == null) {
            return list;
        }
        if (latest.getRoe() != null && latest.getRoe() >= 15) {
            list.add("ROE " + round2(latest.getRoe()) + "%，盈利质量较好");
        }
        if (latest.getRevenueYoy() != null && latest.getRevenueYoy() >= 10) {
            list.add("营收同比增长 " + round2(latest.getRevenueYoy()) + "%");
        }
        if (latest.getGrossMargin() != null && latest.getGrossMargin() >= 30) {
            list.add("毛利率 " + round2(latest.getGrossMargin()) + "%，产品议价能力较强");
        }
        return list;
    }

    private List<String> buildAnnualRisks(StockAnnualReport latest) {
        List<String> list = new ArrayList<>();
        if (latest == null) {
            return list;
        }
        if (latest.getDebtRatio() != null && latest.getDebtRatio() >= 65) {
            list.add("资产负债率 " + round2(latest.getDebtRatio()) + "%，杠杆偏高");
        }
        if (latest.getProfitYoy() != null && latest.getProfitYoy() < 0) {
            list.add("净利润同比 " + round2(latest.getProfitYoy()) + "%");
        }
        if (latest.getReceivableDays() != null && latest.getReceivableDays() > 90) {
            list.add("应收周转天数 " + round2(latest.getReceivableDays()) + " 天，回款压力需关注");
        }
        return list;
    }

    private List<String> mergeStrengths(List<String> annual, List<String> template) {
        return mergeLists(annual, template, MAX_STRENGTHS);
    }

    private List<String> mergeRisks(List<String> annual, List<String> template) {
        List<String> merged = mergeLists(annual, template, MAX_RISKS - 1);
        merged.add("数据来源于公开财报，仅供参考");
        return merged;
    }

    private List<String> mergeLists(List<String> primary, List<String> secondary, int max) {
        Set<String> seen = new LinkedHashSet<>();
        List<String> out = new ArrayList<>();
        appendUnique(out, seen, primary, max);
        appendUnique(out, seen, secondary, max);
        return out;
    }

    private void appendUnique(List<String> out, Set<String> seen, List<String> items, int max) {
        if (items == null) {
            return;
        }
        for (String item : items) {
            if (out.size() >= max || StringUtils.isBlank(item) || seen.contains(item)) {
                continue;
            }
            seen.add(item);
            out.add(item);
        }
    }

    private String buildDataSource(StockBase stock, StockAnnualReport latest) {
        boolean hasBrief = stock != null && StringUtils.isNotBlank(
                firstNonBlank(stock.getBusinessBrief(), stock.getMainBusiness()));
        boolean hasScope = stock != null && StringUtils.isNotBlank(stock.getBusinessScope());
        boolean hasAnnual = latest != null;
        if (hasBrief && hasAnnual) {
            return "brief+annual" + (hasScope ? "+scope" : "");
        }
        if (hasBrief) {
            return "brief" + (hasScope ? "+scope" : "");
        }
        if (hasAnnual) {
            return "industry_template+annual";
        }
        return "industry_template";
    }

    private String firstNonBlank(String... values) {
        for (String v : values) {
            if (StringUtils.isNotBlank(v)) {
                return v;
            }
        }
        return null;
    }

    private double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
