package com.yh.bigdata.tts.spider.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yh.bigdata.tts.common.utils.StockCodeUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.regex.Pattern;

/**
 * 巨潮资讯：公告检索与 PDF 下载
 */
public final class CninfoClient {

    private static final Logger log = LoggerFactory.getLogger(CninfoClient.class);

    private static final String TOP_SEARCH = "http://www.cninfo.com.cn/new/information/topSearch/query";
    private static final String ANN_QUERY = "http://www.cninfo.com.cn/new/hisAnnouncement/query";
    private static final String PDF_BASE = "http://static.cninfo.com.cn/";
    private static final int TIMEOUT_MS = 30000;

    private static final Pattern SUMMARY_TITLE = Pattern.compile("摘要|取消|更正|补充|英文");

    private CninfoClient() {
    }

    public static final class AnnualReportDoc {
        private final String title;
        private final String reportYear;
        private final String pdfUrl;

        public AnnualReportDoc(String title, String reportYear, String pdfUrl) {
            this.title = title;
            this.reportYear = reportYear;
            this.pdfUrl = pdfUrl;
        }

        public String getTitle() {
            return title;
        }

        public String getReportYear() {
            return reportYear;
        }

        public String getPdfUrl() {
            return pdfUrl;
        }
    }

    public static AnnualReportDoc fetchLatestAnnualReport(String code) {
        String normalized = StockCodeUtil.normalizeCnCode(code);
        if (StringUtils.isBlank(normalized)) {
            return null;
        }
        String num = StockCodeUtil.toSecurityCode(normalized);
        boolean sh = normalized.startsWith("sh");
        String column = sh ? "sse" : "szse";
        String plate = sh ? "sh" : "sz";

        String body = postForm(ANN_QUERY, buildAnnQuery(num, column, plate));
        if (StringUtils.isBlank(body)) {
            return null;
        }
        try {
            JSONObject root = JSON.parseObject(body);
            JSONArray anns = root.getJSONArray("announcements");
            if (anns == null || anns.isEmpty()) {
                return null;
            }
            AnnualReportDoc best = null;
            for (int i = 0; i < anns.size(); i++) {
                JSONObject ann = anns.getJSONObject(i);
                if (ann == null) {
                    continue;
                }
                String title = ann.getString("announcementTitle");
                String adjunct = ann.getString("adjunctUrl");
                if (StringUtils.isBlank(title) || StringUtils.isBlank(adjunct)) {
                    continue;
                }
                if (!title.contains("年度报告") || SUMMARY_TITLE.matcher(title).find()) {
                    continue;
                }
                String year = extractReportYear(title);
                String pdfUrl = PDF_BASE + adjunct.replaceFirst("^/", "");
                AnnualReportDoc doc = new AnnualReportDoc(title, year, pdfUrl);
                if (best == null || compareYear(doc.getReportYear(), best.getReportYear()) > 0) {
                    best = doc;
                }
            }
            return best;
        } catch (Exception e) {
            log.warn("cninfo parse announcements failed, code={}", code, e);
            return null;
        }
    }

    public static byte[] downloadPdf(String pdfUrl) {
        if (StringUtils.isBlank(pdfUrl)) {
            return null;
        }
        try {
            return Request.Get(pdfUrl)
                    .connectTimeout(TIMEOUT_MS)
                    .socketTimeout(TIMEOUT_MS)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .addHeader("Referer", "http://www.cninfo.com.cn/")
                    .execute()
                    .returnContent()
                    .asBytes();
        } catch (IOException e) {
            log.warn("cninfo pdf download failed, url={}", pdfUrl, e);
            return null;
        }
    }

    private static String buildAnnQuery(String secCode, String column, String plate) {
        int year = java.time.LocalDate.now().getYear();
        String seDate = (year - 2) + "-01-01~" + year + "-12-31";
        return "pageNum=1"
                + "&pageSize=30"
                + "&column=" + column
                + "&plate=" + plate
                + "&category=category_ndbg_szsh"
                + "&tabName=fulltext"
                + "&secCode=" + urlEncode(secCode)
                + "&searchkey=" + urlEncode(secCode)
                + "&seDate=" + urlEncode(seDate)
                + "&isHLtitle=true";
    }

    private static String postForm(String url, String formBody) {
        try {
            return Request.Post(url)
                    .connectTimeout(TIMEOUT_MS)
                    .socketTimeout(TIMEOUT_MS)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .addHeader("Referer", "http://www.cninfo.com.cn/new/commonUrl/pageOfSearch?url=disclosure/list/search")
                    .addHeader("X-Requested-With", "XMLHttpRequest")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .bodyByteArray(formBody.getBytes(StandardCharsets.UTF_8))
                    .execute()
                    .returnContent()
                    .asString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("cninfo http post failed, url={}", url, e);
            return null;
        }
    }

    private static String urlEncode(String val) {
        return URLEncoder.encode(val, StandardCharsets.UTF_8);
    }

    private static String extractReportYear(String title) {
        if (StringUtils.isBlank(title)) {
            return null;
        }
        java.util.regex.Matcher m = Pattern.compile("(20\\d{2})").matcher(title);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private static int compareYear(String a, String b) {
        return Comparator.nullsFirst(String::compareTo).compare(a, b);
    }
}
