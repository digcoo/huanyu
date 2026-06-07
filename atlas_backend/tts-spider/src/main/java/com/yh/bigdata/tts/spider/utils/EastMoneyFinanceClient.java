package com.yh.bigdata.tts.spider.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import com.yh.bigdata.tts.spider.utils.EastMoneyHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 东方财富 datacenter 年报接口（免费）
 */
public final class EastMoneyFinanceClient {

    private static final Logger log = LoggerFactory.getLogger(EastMoneyFinanceClient.class);

    private static final String API = "https://datacenter-web.eastmoney.com/api/data/v1/get";

    private EastMoneyFinanceClient() {
    }

    public static List<JSONObject> fetchAnnualReports(String securityCode, int pageSize) {
        if (StringUtils.isBlank(securityCode)) {
            return Collections.emptyList();
        }
        String filter = "(SECURITY_CODE=\"" + securityCode + "\")(REPORT_TYPE=\"年报\")";
        String url = API
                + "?reportName=RPT_F10_FINANCE_MAINFINADATA"
                + "&columns=ALL"
                + "&pageNumber=1&pageSize=" + pageSize
                + "&sortTypes=-1&sortColumns=REPORT_DATE"
                + "&filter=" + java.net.URLEncoder.encode(filter, java.nio.charset.StandardCharsets.UTF_8);
        try {
            String body = EastMoneyHttpClient.get(url);
            if (StringUtils.isBlank(body)) {
                return Collections.emptyList();
            }
            JSONObject root = JSON.parseObject(body);
            if (root == null || !root.getBooleanValue("success")) {
                log.warn("eastmoney annual api failed, code={}, msg={}", securityCode,
                        root != null ? root.getString("message") : "empty");
                return Collections.emptyList();
            }
            JSONObject result = root.getJSONObject("result");
            if (result == null) {
                return Collections.emptyList();
            }
            JSONArray data = result.getJSONArray("data");
            if (data == null || data.isEmpty()) {
                return Collections.emptyList();
            }
            List<JSONObject> list = new ArrayList<>(data.size());
            for (int i = 0; i < data.size(); i++) {
                JSONObject row = data.getJSONObject(i);
                if (row != null && "年报".equals(row.getString("REPORT_TYPE"))) {
                    list.add(row);
                }
            }
            return list;
        } catch (Exception e) {
            log.warn("eastmoney annual api io error, code={}", securityCode, e);
            return Collections.emptyList();
        }
    }

    public static Double getDouble(JSONObject row, String... keys) {
        if (row == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            if (!row.containsKey(key)) {
                continue;
            }
            Object val = row.get(key);
            if (val == null) {
                continue;
            }
            if (val instanceof Number) {
                return ((Number) val).doubleValue();
            }
            String s = String.valueOf(val).trim();
            if (s.isEmpty() || "-".equals(s) || "--".equals(s)) {
                continue;
            }
            try {
                return Double.parseDouble(s.replace(",", ""));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    public static String getString(JSONObject row, String key) {
        if (row == null) {
            return null;
        }
        return row.getString(key);
    }

    public static Integer getInteger(JSONObject row, String... keys) {
        if (row == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            if (!row.containsKey(key)) {
                continue;
            }
            Object val = row.get(key);
            if (val == null) {
                continue;
            }
            if (val instanceof Number) {
                return ((Number) val).intValue();
            }
            String s = String.valueOf(val).trim();
            if (s.isEmpty() || "-".equals(s)) {
                continue;
            }
            try {
                return Integer.parseInt(s.replace(".0", ""));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }
}
