package com.yh.bigdata.tts.spider.service.impl;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.utils.StockCodeUtil;
import com.yh.bigdata.tts.spider.service.AtlasBusinessBriefService;
import com.yh.bigdata.tts.spider.service.AtlasIndustryChainService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AtlasBusinessBriefServiceImpl implements AtlasBusinessBriefService {

    private static final int BRIEF_MAX = 160;

    @Autowired
    private AtlasIndustryChainService atlasIndustryChainService;

    @Override
    public String buildBrief(StockBase stock, String industryTemplateLine) {
        return pickBrief(stock, industryTemplateLine).text;
    }

    @Override
    public String resolveBriefSource(StockBase stock, String industryTemplateLine) {
        return pickBrief(stock, industryTemplateLine).source;
    }

    @Override
    public void applyCrawlFields(StockBase patch, String businessScope, String orgProfile, String code,
                                 String industryTemplateLine) {
        if (StringUtils.isNotBlank(businessScope)) {
            patch.setBusinessScope(trimTo(businessScope, 800));
        }
        if (StringUtils.isNotBlank(orgProfile)) {
            patch.setOrgProfile(trimTo(orgProfile, 4000));
        }
        StockBase ctx = new StockBase();
        ctx.setCode(StockCodeUtil.normalizeCnCode(code));
        ctx.setBusinessScope(patch.getBusinessScope());
        ctx.setOrgProfile(patch.getOrgProfile());
        ctx.setBusinessBrief(patch.getBusinessBrief());
        ctx.setMainBusiness(patch.getMainBusiness());

        BriefPick pick = pickBrief(ctx, industryTemplateLine);
        patch.setBusinessBrief(pick.text);
        patch.setMainBusiness(pick.text);
    }

    @Override
    public boolean isRegistrationScope(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        String t = text.trim();
        if (t.contains("许可项目") || t.contains("一般项目")) {
            return true;
        }
        if (t.contains("除依法须经批准") || t.contains("依法须经批准的项目")) {
            return true;
        }
        if (t.contains("经营范围") && t.length() > 80) {
            return true;
        }
        int semi = countChar(t, ';') + countChar(t, '；');
        if (semi >= 2 && t.length() > 60) {
            return true;
        }
        return t.length() > 150 && !t.contains("。") && semi >= 1;
    }

    private BriefPick pickBrief(StockBase stock, String industryTemplateLine) {
        String code = stock != null ? stock.getCode() : null;
        if (StringUtils.isNotBlank(code)) {
            String segment = atlasIndustryChainService.buildSegmentBrief(code);
            if (isUsableBrief(segment)) {
                return new BriefPick(trimTo(segment, BRIEF_MAX), "segment");
            }
        }

        String org = extractOrgExcerpt(stock != null ? stock.getOrgProfile() : null);
        if (isUsableBrief(org)) {
            return new BriefPick(org, "org_profile");
        }

        if (stock != null) {
            String stored = firstNonBlank(stock.getBusinessBrief(), stock.getMainBusiness());
            if (isUsableBrief(stored) && !isRegistrationScope(stored)) {
                return new BriefPick(trimTo(stored, BRIEF_MAX), "stored");
            }
        }

        String template = StringUtils.defaultIfBlank(industryTemplateLine, "");
        if (isUsableBrief(template)) {
            return new BriefPick(trimTo(template, BRIEF_MAX), "template");
        }
        return new BriefPick(template, "template");
    }

    private String extractOrgExcerpt(String orgProfile) {
        if (StringUtils.isBlank(orgProfile) || isRegistrationScope(orgProfile)) {
            return null;
        }
        String normalized = orgProfile.replaceAll("\\s+", " ").trim();
        String[] parts = normalized.split("[。；;\\n]");
        for (String part : parts) {
            String s = part.trim();
            if (s.length() < 12) {
                continue;
            }
            if (s.startsWith("公司成立于") || s.startsWith("公司于") || s.matches("^\\d{4}年.*成立.*")) {
                continue;
            }
            if (s.contains("注册资本") && s.length() < 40) {
                continue;
            }
            return trimTo(s, BRIEF_MAX);
        }
        return trimTo(normalized, BRIEF_MAX);
    }

    private boolean isUsableBrief(String text) {
        return StringUtils.isNotBlank(text) && text.trim().length() >= 12;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (StringUtils.isNotBlank(v)) {
                return v;
            }
        }
        return null;
    }

    private int countChar(String s, char c) {
        int n = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                n++;
            }
        }
        return n;
    }

    private String trimTo(String text, int maxLen) {
        if (StringUtils.isBlank(text)) {
            return "";
        }
        String t = text.replaceAll("\\s+", " ").trim();
        if (t.length() <= maxLen) {
            return t;
        }
        return t.substring(0, maxLen - 1) + "…";
    }

    private static final class BriefPick {
        final String text;
        final String source;

        BriefPick(String text, String source) {
            this.text = text;
            this.source = source;
        }
    }
}
