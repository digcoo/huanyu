package com.yh.bigdata.tts.common.utils;

public final class StockCodeUtil {

    private StockCodeUtil() {
    }

    public static String normalizeCnCode(String code) {
        if (code == null) {
            return null;
        }
        String normalized = code.trim().toLowerCase();
        if (normalized.startsWith("sh") || normalized.startsWith("sz")) {
            return normalized;
        }
        if (normalized.matches("\\d{6}")) {
            if (normalized.startsWith("6")) {
                return "sh" + normalized;
            }
            return "sz" + normalized;
        }
        return normalized;
    }
}
