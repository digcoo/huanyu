package com.yh.bigdata.tts.spider.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从巨潮年报 PDF 解析前五客户 / 前五供应商
 */
public final class CninfoTradingPartiesParser {

    private static final Logger log = LoggerFactory.getLogger(CninfoTradingPartiesParser.class);

    private static final Pattern ROW_PATTERN = Pattern.compile(
            "^\\s*(\\d+)\\s+(.+?)\\s+([\\d,]+(?:\\.\\d+)?)\\s+([\\d\\.]+)\\s*%\\s*$");

    private CninfoTradingPartiesParser() {
    }

    public static final class TradingPartyRow {
        private final int rank;
        private final String name;
        private final String amountText;
        private final String ratioPct;

        public TradingPartyRow(int rank, String name, String amountText, String ratioPct) {
            this.rank = rank;
            this.name = name;
            this.amountText = amountText;
            this.ratioPct = ratioPct;
        }

        public int getRank() {
            return rank;
        }

        public String getName() {
            return name;
        }

        public String getAmountText() {
            return amountText;
        }

        public String getRatioPct() {
            return ratioPct;
        }

        public String displayLabel() {
            StringBuilder sb = new StringBuilder(name);
            if (StringUtils.isNotBlank(amountText)) {
                sb.append(" · ").append(formatAmountYi(amountText));
            }
            if (StringUtils.isNotBlank(ratioPct)) {
                sb.append(" · ").append(ratioPct).append("%");
            }
            return sb.toString();
        }

        private static String formatAmountYi(String amountText) {
            try {
                String num = amountText.replace(",", "");
                BigDecimal val = new BigDecimal(num);
                // 年报金额单位通常为千元
                BigDecimal yi = val.divide(new BigDecimal("100000"), 2, RoundingMode.HALF_UP);
                return yi.toPlainString() + "亿";
            } catch (Exception e) {
                return amountText + "千元";
            }
        }
    }

    public static final class ParseResult {
        private final List<TradingPartyRow> customers;
        private final List<TradingPartyRow> suppliers;

        public ParseResult(List<TradingPartyRow> customers, List<TradingPartyRow> suppliers) {
            this.customers = customers == null ? Collections.emptyList() : customers;
            this.suppliers = suppliers == null ? Collections.emptyList() : suppliers;
        }

        public List<TradingPartyRow> getCustomers() {
            return customers;
        }

        public List<TradingPartyRow> getSuppliers() {
            return suppliers;
        }
    }

    public static ParseResult parse(byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            return new ParseResult(Collections.emptyList(), Collections.emptyList());
        }
        try (PDDocument doc = PDDocument.load(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            int maxPage = Math.min(doc.getNumberOfPages(), 80);
            stripper.setStartPage(1);
            stripper.setEndPage(maxPage);
            String text = stripper.getText(doc);
            return parseText(text);
        } catch (IOException e) {
            log.warn("cninfo pdf parse io error", e);
            return new ParseResult(Collections.emptyList(), Collections.emptyList());
        }
    }

    static ParseResult parseText(String text) {
        if (StringUtils.isBlank(text)) {
            return new ParseResult(Collections.emptyList(), Collections.emptyList());
        }
        String normalized = text.replace('\u00A0', ' ')
                .replace("公司前5大客户资料", "公司前 5 大客户资料")
                .replace("公司前5名供应商资料", "公司前 5 名供应商资料")
                .replace("公司前 5 名客户资料", "公司前 5 大客户资料");

        int sectionStart = indexOfAny(normalized,
                "主要销售客户和主要供应商情况",
                "主要销售客户情况",
                "公司主要销售客户情况");
        if (sectionStart < 0) {
            return new ParseResult(Collections.emptyList(), Collections.emptyList());
        }
        int sectionEnd = indexOfAny(normalized, sectionStart + 10,
                "3、费用", "3．费用", "四、", "五、非主营业务");
        if (sectionEnd < 0) {
            sectionEnd = Math.min(normalized.length(), sectionStart + 12000);
        }
        String section = normalized.substring(sectionStart, sectionEnd);

        String customerBlock = sliceBlock(section,
                new String[]{"公司前 5 大客户资料", "公司前5大客户资料", "公司前 5 名客户资料"},
                new String[]{"2）公司主要供应商情况", "2)公司主要供应商情况", "公司前 5 名供应商资料", "公司前5名供应商资料"});
        String supplierBlock = sliceBlock(section,
                new String[]{"公司前 5 名供应商资料", "公司前5名供应商资料", "公司前 5 大供应商资料"},
                new String[]{"3、费用", "3．费用", "合计 --"});

        List<TradingPartyRow> customers = parseRows(customerBlock);
        List<TradingPartyRow> suppliers = parseRows(supplierBlock);
        return new ParseResult(customers, suppliers);
    }

    private static String sliceBlock(String section, String[] startMarkers, String[] endMarkers) {
        int start = indexOfAny(section, startMarkers);
        if (start < 0) {
            return "";
        }
        int end = indexOfAny(section, start + 5, endMarkers);
        if (end < 0) {
            end = section.length();
        }
        return section.substring(start, end);
    }

    private static List<TradingPartyRow> parseRows(String block) {
        if (StringUtils.isBlank(block)) {
            return Collections.emptyList();
        }
        List<TradingPartyRow> rows = new ArrayList<>();
        for (String line : block.split("\\R")) {
            if (StringUtils.isBlank(line) || line.contains("序号") || line.contains("合计")) {
                continue;
            }
            if (line.contains("年度报告全文") || line.contains("年度报告")) {
                continue;
            }
            Matcher m = ROW_PATTERN.matcher(line.trim());
            if (!m.matches()) {
                continue;
            }
            int rank = Integer.parseInt(m.group(1));
            if (rank < 1 || rank > 5) {
                continue;
            }
            String name = cleanupName(m.group(2));
            if (StringUtils.isBlank(name) || "--".equals(name)) {
                continue;
            }
            rows.add(new TradingPartyRow(rank, name, m.group(3), m.group(4)));
        }
        return rows;
    }

    private static String cleanupName(String name) {
        if (name == null) {
            return null;
        }
        return name.replaceAll("\\s+", " ").trim();
    }

    private static int indexOfAny(String text, String... markers) {
        return indexOfAny(text, 0, markers);
    }

    private static int indexOfAny(String text, int from, String... markers) {
        int best = -1;
        for (String marker : markers) {
            int idx = text.indexOf(marker, from);
            if (idx >= 0 && (best < 0 || idx < best)) {
                best = idx;
            }
        }
        return best;
    }
}
