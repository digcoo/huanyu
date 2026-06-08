package com.yh.bigdata.tts.spider.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.utils.StockCodeUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 东方财富 push2 批量行情（A 股估值）
 */
public final class EastMoneyQuoteClient {

    private static final Logger log = LoggerFactory.getLogger(EastMoneyQuoteClient.class);

    private static final String CLIST_API = "https://push2delay.eastmoney.com/api/qt/clist/get";
    /** 沪深 A 股 */
    private static final String A_SHARE_FS = "m:0+t:6,m:0+t:80,m:1+t:2,m:1+t:23";
    private static final String CLIST_FIELDS = "f12,f9,f23,f20,f45,f46,f116,f162,f167,f168";
    private static final String UT = "fa5fd1943c7eab3dcac0dd4784b235de";

    private EastMoneyQuoteClient() {
    }

    public static final class ClistPage {
        private final int total;
        private final List<JSONObject> rows;

        public ClistPage(int total, List<JSONObject> rows) {
            this.total = total;
            this.rows = rows == null ? Collections.emptyList() : rows;
        }

        public int getTotal() {
            return total;
        }

        public List<JSONObject> getRows() {
            return rows;
        }
    }

    public static ClistPage fetchClistPage(int page, int pageSize) {
        if (page < 1 || pageSize < 1) {
            return new ClistPage(0, Collections.emptyList());
        }
        String url = CLIST_API
                + "?pn=" + page
                + "&pz=" + pageSize
                + "&po=1&np=1&fltt=2&invt=2&fid=f3"
                + "&fs=" + A_SHARE_FS
                + "&fields=" + CLIST_FIELDS
                + "&ut=" + UT;
        String body = EastMoneyHttpClient.get(url);
        if (StringUtils.isBlank(body)) {
            return new ClistPage(0, Collections.emptyList());
        }
        try {
            JSONObject root = JSON.parseObject(body);
            if (root == null) {
                return new ClistPage(0, Collections.emptyList());
            }
            JSONObject data = root.getJSONObject("data");
            if (data == null) {
                return new ClistPage(0, Collections.emptyList());
            }
            int total = data.getIntValue("total");
            JSONArray diff = data.getJSONArray("diff");
            if (diff == null || diff.isEmpty()) {
                return new ClistPage(total, Collections.emptyList());
            }
            List<JSONObject> rows = new ArrayList<>(diff.size());
            for (int i = 0; i < diff.size(); i++) {
                JSONObject row = diff.getJSONObject(i);
                if (row != null) {
                    rows.add(row);
                }
            }
            return new ClistPage(total, rows);
        } catch (Exception e) {
            log.warn("eastmoney clist parse error, page={}", page, e);
            return new ClistPage(0, Collections.emptyList());
        }
    }

    public static StockBase parseValuationRow(JSONObject row) {
        if (row == null) {
            return null;
        }
        String num = row.getString("f12");
        if (StringUtils.isBlank(num)) {
            return null;
        }
        String code = toNormalizedCode(num.trim());
        if (code == null) {
            return null;
        }

        StockBase patch = new StockBase();
        patch.setCode(code);
        patch.setPeTtm(firstDouble(row.get("f162"), row.get("f9")));
        patch.setPb(firstDouble(row.get("f167"), row.get("f23")));
        patch.setDividendYield(toDouble(row.get("f168")));
        patch.setHigh52w(toDouble(row.get("f46")));
        patch.setLow52w(toDouble(row.get("f45")));
        patch.setTotalMvYi(toMvYi(row.get("f116"), row.get("f20")));
        return patch;
    }

    static String toNormalizedCode(String numCode) {
        if (StringUtils.isBlank(numCode) || !numCode.matches("\\d{6}")) {
            return null;
        }
        return StockCodeUtil.normalizeCnCode(numCode);
    }

    static Double firstDouble(Object primary, Object fallback) {
        Double v = toDouble(primary);
        if (v != null) {
            return v;
        }
        return toDouble(fallback);
    }

    static Double toDouble(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof Number) {
            double d = ((Number) val).doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                return null;
            }
            return d;
        }
        if (val instanceof String) {
            String s = ((String) val).trim();
            if (s.isEmpty() || "-".equals(s)) {
                return null;
            }
            try {
                double d = Double.parseDouble(s);
                if (Double.isNaN(d) || Double.isInfinite(d)) {
                    return null;
                }
                return d;
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    static Double toMvYi(Object primary, Object fallback) {
        Double yuan = firstDouble(primary, fallback);
        if (yuan == null || yuan <= 0) {
            return null;
        }
        return yuan / 100_000_000.0;
    }
}
