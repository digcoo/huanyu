package com.yh.bigdata.tts.spider.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yh.bigdata.tts.common.dto.atlas.AtlasKlineBarVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 新浪免费接口：A 股大盘指数行情与 K 线
 */
public final class SinaIndexClient {

    private static final Logger log = LoggerFactory.getLogger(SinaIndexClient.class);

    private static final String REFERER = "https://finance.sina.com.cn/";
    private static final String KLINE_REFERER = "https://finance.sina.com.cn/realstock/company/sh000001/nc.shtml";
    private static final Charset GBK = Charset.forName("GBK");

    private SinaIndexClient() {
    }

    public static final class IndexDef {
        public final String symbol;
        public final String displayCode;
        public final String name;

        public IndexDef(String symbol, String displayCode, String name) {
            this.symbol = symbol;
            this.displayCode = displayCode;
            this.name = name;
        }
    }

    public static final List<IndexDef> CN_INDICES;
    private static final Map<String, IndexDef> CN_BY_SYMBOL;

    static {
        List<IndexDef> list = new ArrayList<>();
        list.add(new IndexDef("sh000001", "000001", "上证指数"));
        list.add(new IndexDef("sz399001", "399001", "深证成指"));
        list.add(new IndexDef("sz399006", "399006", "创业板指"));
        CN_INDICES = Collections.unmodifiableList(list);
        Map<String, IndexDef> map = new LinkedHashMap<>();
        for (IndexDef def : list) {
            map.put(def.symbol, def);
        }
        CN_BY_SYMBOL = Collections.unmodifiableMap(map);
    }

    public static IndexDef findCnIndex(String codeOrSymbol) {
        if (StringUtils.isBlank(codeOrSymbol)) {
            return null;
        }
        String normalized = codeOrSymbol.trim().toLowerCase();
        IndexDef bySymbol = CN_BY_SYMBOL.get(normalized);
        if (bySymbol != null) {
            return bySymbol;
        }
        for (IndexDef def : CN_INDICES) {
            if (def.displayCode.equals(normalized)) {
                return def;
            }
        }
        return null;
    }

    public static int resolveSinaScale(String period) {
        if (period == null) {
            return 1200;
        }
        switch (period.toLowerCase()) {
            case "day":
                return 240;
            case "week":
                return 1200;
            case "month":
                return 7200;
            case "year":
                return 86400;
            default:
                return 1200;
        }
    }

    public static Quote fetchQuote(IndexDef def) {
        if (def == null) {
            return null;
        }
        try {
            String url = "http://hq.sinajs.cn/list=" + def.symbol;
            String body = Request.Get(url)
                    .addHeader("Referer", REFERER)
                    .execute()
                    .returnContent()
                    .asString(GBK);
            return parseQuoteLine(body, def);
        } catch (IOException e) {
            log.warn("sina index quote failed, symbol={}", def.symbol, e);
            return null;
        }
    }

    public static List<AtlasKlineBarVo> fetchKlines(String symbol, String period, int limit) {
        if (StringUtils.isBlank(symbol) || limit <= 0) {
            return Collections.emptyList();
        }
        int datalen = Math.min(Math.max(limit, 1), 200);
        List<AtlasKlineBarVo> bars = fetchKlinesFromSina(symbol, period, datalen);
        if (!bars.isEmpty()) {
            return bars;
        }
        bars = fetchKlinesFromEastMoney(symbol, period, datalen);
        if (bars.isEmpty()) {
            log.warn("index kline empty after sina+eastmoney, symbol={}, period={}", symbol, period);
        }
        return bars;
    }

    private static List<AtlasKlineBarVo> fetchKlinesFromSina(String symbol, String period, int datalen) {
        int scale = resolveSinaScale(period);
        String url = "https://quotes.sina.cn/cn/api/jsonp_v2.php=/CN_MarketDataService.getKLineData"
                + "?symbol=" + symbol
                + "&scale=" + scale
                + "&ma=no"
                + "&datalen=" + datalen;
        try {
            String body = Request.Get(url)
                    .connectTimeout(15000)
                    .socketTimeout(15000)
                    .addHeader("Referer", KLINE_REFERER)
                    .addHeader("User-Agent", "Mozilla/5.0 AtlasBot/1.0")
                    .execute()
                    .returnContent()
                    .asString(GBK);
            return parseKlineBody(body);
        } catch (IOException e) {
            log.warn("sina index kline failed, symbol={}, period={}", symbol, period, e);
            return Collections.emptyList();
        }
    }

    static String toEastMoneySecId(String symbol) {
        if (StringUtils.isBlank(symbol)) {
            return symbol;
        }
        String s = symbol.trim().toLowerCase();
        if (s.startsWith("sh")) {
            return "1." + s.substring(2);
        }
        if (s.startsWith("sz")) {
            return "0." + s.substring(2);
        }
        return s;
    }

    static int resolveEastMoneyKlt(String period) {
        if (period == null) {
            return 102;
        }
        switch (period.toLowerCase()) {
            case "day":
                return 101;
            case "week":
                return 102;
            case "month":
                return 103;
            case "year":
                return 106;
            default:
                return 102;
        }
    }

    private static List<AtlasKlineBarVo> fetchKlinesFromEastMoney(String symbol, String period, int limit) {
        String secid = toEastMoneySecId(symbol);
        int klt = resolveEastMoneyKlt(period);
        String url = "https://push2his.eastmoney.com/api/qt/stock/kline/get"
                + "?secid=" + secid
                + "&klt=" + klt
                + "&fqt=1"
                + "&lmt=" + limit
                + "&fields1=f1,f2,f3,f4,f5,f6"
                + "&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61";
        try {
            String body = EastMoneyHttpClient.get(url);
            return parseEastMoneyKlineBody(body);
        } catch (Exception e) {
            log.warn("eastmoney index kline failed, symbol={}, period={}", symbol, period, e);
            return Collections.emptyList();
        }
    }

    private static List<AtlasKlineBarVo> parseEastMoneyKlineBody(String body) {
        if (StringUtils.isBlank(body)) {
            return Collections.emptyList();
        }
        JSONObject root = JSON.parseObject(body);
        if (root == null) {
            return Collections.emptyList();
        }
        JSONObject data = root.getJSONObject("data");
        if (data == null) {
            return Collections.emptyList();
        }
        JSONArray lines = data.getJSONArray("klines");
        if (lines == null || lines.isEmpty()) {
            return Collections.emptyList();
        }
        List<AtlasKlineBarVo> bars = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.getString(i);
            if (StringUtils.isBlank(line)) {
                continue;
            }
            String[] parts = line.split(",");
            if (parts.length < 5) {
                continue;
            }
            bars.add(AtlasKlineBarVo.builder()
                    .open(parseDouble(parts[1]))
                    .close(parseDouble(parts[2]))
                    .high(parseDouble(parts[3]))
                    .low(parseDouble(parts[4]))
                    .build());
        }
        return bars;
    }

    private static Quote parseQuoteLine(String line, IndexDef def) {
        if (StringUtils.isBlank(line) || !line.contains("=")) {
            return null;
        }
        try {
            int eq = line.indexOf('=');
            String data = line.substring(eq + 1).trim();
            if (data.startsWith("\"")) {
                data = data.substring(1);
            }
            if (data.endsWith("\";")) {
                data = data.substring(0, data.length() - 2);
            } else if (data.endsWith("\"")) {
                data = data.substring(0, data.length() - 1);
            }
            String[] parts = data.split(",");
            if (parts.length < 4) {
                return null;
            }
            double open = parseDouble(parts[1]);
            double prevClose = parseDouble(parts[2]);
            double current = parseDouble(parts[3]);
            double changePct = 0D;
            if (prevClose > 0) {
                changePct = (current - prevClose) / prevClose * 100D;
            }
            Quote quote = new Quote();
            quote.symbol = def.symbol;
            quote.name = def.name;
            quote.price = round2(current);
            quote.changePct = round2(changePct);
            quote.open = round2(open);
            quote.prevClose = round2(prevClose);
            return quote;
        } catch (Exception e) {
            log.warn("parse sina index quote failed, symbol={}", def.symbol, e);
            return null;
        }
    }

    private static List<AtlasKlineBarVo> parseKlineBody(String body) {
        if (StringUtils.isBlank(body)) {
            return Collections.emptyList();
        }
        int start = body.indexOf('(');
        int end = body.lastIndexOf(')');
        if (start < 0 || end <= start) {
            return Collections.emptyList();
        }
        String json = body.substring(start + 1, end);
        JSONArray arr = JSON.parseArray(json);
        if (arr == null || arr.isEmpty()) {
            return Collections.emptyList();
        }
        List<AtlasKlineBarVo> bars = new ArrayList<>(arr.size());
        for (int i = 0; i < arr.size(); i++) {
            JSONObject row = arr.getJSONObject(i);
            if (row == null) {
                continue;
            }
            bars.add(AtlasKlineBarVo.builder()
                    .open(parseDouble(row.getString("open")))
                    .high(parseDouble(row.getString("high")))
                    .low(parseDouble(row.getString("low")))
                    .close(parseDouble(row.getString("close")))
                    .build());
        }
        return bars;
    }

    private static double parseDouble(String val) {
        if (StringUtils.isBlank(val)) {
            return 0D;
        }
        return Double.parseDouble(val.trim());
    }

    private static double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public static final class Quote {
        public String symbol;
        public String name;
        public double price;
        public double changePct;
        public double open;
        public double prevClose;
    }
}
