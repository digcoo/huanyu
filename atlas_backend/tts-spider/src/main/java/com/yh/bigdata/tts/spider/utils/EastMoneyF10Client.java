package com.yh.bigdata.tts.spider.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yh.bigdata.tts.common.utils.StockCodeUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 东方财富 F10 公司概况 / 行业分析 / 行情估值
 */
public final class EastMoneyF10Client {

    private static final String F10_BASE = "https://emweb.securities.eastmoney.com/PC_HSF10";
    private static final String QUOTE_API = "https://push2.eastmoney.com/api/qt/stock/get";

    private EastMoneyF10Client() {
    }

    public static String toF10Code(String code) {
        String normalized = StockCodeUtil.normalizeCnCode(code);
        if (normalized == null) {
            return null;
        }
        if (normalized.startsWith("sh")) {
            return "SH" + normalized.substring(2);
        }
        if (normalized.startsWith("sz")) {
            return "SZ" + normalized.substring(2);
        }
        return normalized.toUpperCase();
    }

    public static String toSecId(String code) {
        String normalized = StockCodeUtil.normalizeCnCode(code);
        if (normalized == null) {
            return null;
        }
        String num = StockCodeUtil.toSecurityCode(normalized);
        if (num == null) {
            return null;
        }
        if (normalized.startsWith("sh")) {
            return "1." + num;
        }
        return "0." + num;
    }

    /** 公司概况 jbzl[0] */
    public static JSONObject fetchCompanySurvey(String code) {
        String f10 = toF10Code(code);
        if (f10 == null) {
            return null;
        }
        String body = EastMoneyHttpClient.get(F10_BASE + "/CompanySurvey/PageAjax?code=" + f10);
        if (StringUtils.isBlank(body)) {
            return null;
        }
        JSONObject root = JSON.parseObject(body);
        if (root == null) {
            return null;
        }
        JSONArray jbzl = root.getJSONArray("jbzl");
        if (jbzl == null || jbzl.isEmpty()) {
            return null;
        }
        return jbzl.getJSONObject(0);
    }

    /** 行业分析：竞品 czxbj */
    public static List<JSONObject> fetchCompetitors(String code) {
        String f10 = toF10Code(code);
        if (f10 == null) {
            return Collections.emptyList();
        }
        String body = EastMoneyHttpClient.get(F10_BASE + "/IndustryAnalysis/PageAjax?code=" + f10);
        if (StringUtils.isBlank(body)) {
            return Collections.emptyList();
        }
        JSONObject root = JSON.parseObject(body);
        if (root == null) {
            return Collections.emptyList();
        }
        JSONArray czxbj = root.getJSONArray("czxbj");
        if (czxbj == null || czxbj.isEmpty()) {
            return Collections.emptyList();
        }
        List<JSONObject> list = new ArrayList<>(czxbj.size());
        for (int i = 0; i < czxbj.size(); i++) {
            JSONObject row = czxbj.getJSONObject(i);
            if (row != null) {
                list.add(row);
            }
        }
        return list;
    }

    /** 经营分析：主营构成 zygcfx、经营范围 zyfw */
    public static JSONObject fetchBusinessAnalysis(String code) {
        String f10 = toF10Code(code);
        if (f10 == null) {
            return null;
        }
        String body = EastMoneyHttpClient.get(F10_BASE + "/BusinessAnalysis/PageAjax?code=" + f10);
        if (StringUtils.isBlank(body)) {
            return null;
        }
        return JSON.parseObject(body);
    }

    /** 行情估值：PE/PB/市值/52周高低 */
    public static JSONObject fetchQuote(String code) {
        String secId = toSecId(code);
        if (secId == null) {
            return null;
        }
        String fields = "f57,f58,f43,f162,f167,f168,f116,f46,f45";
        String url = QUOTE_API + "?secid=" + secId + "&fields=" + fields;
        String body = EastMoneyHttpClient.get(url);
        if (StringUtils.isBlank(body)) {
            return null;
        }
        JSONObject root = JSON.parseObject(body);
        if (root == null) {
            return null;
        }
        return root.getJSONObject("data");
    }

    public static Double scale100(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof Number) {
            double d = ((Number) val).doubleValue();
            if (d == 0 || d == -0) {
                return null;
            }
            return d / 100.0;
        }
        return null;
    }

    public static Double toYi(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof Number) {
            double d = ((Number) val).doubleValue();
            if (d <= 0) {
                return null;
            }
            return d / 100_000_000.0;
        }
        return null;
    }

    /** EM2016 申万行业：取末级 */
    public static String parseIndustry(String em2016) {
        if (StringUtils.isBlank(em2016)) {
            return null;
        }
        String[] parts = em2016.split("-");
        if (parts.length == 0) {
            return em2016.trim();
        }
        return parts[parts.length - 1].trim();
    }

    public static String trimText(String text, int maxLen) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        String t = text.replaceAll("\\s+", " ").trim();
        if (t.length() <= maxLen) {
            return t;
        }
        return t.substring(0, maxLen);
    }
}
