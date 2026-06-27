package com.yh.bigdata.tts.spider.strategy.tools.markers;

import com.yh.bigdata.tts.common.dto.atlas.AtlasGc2MarkersVo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从 trend/signal 文本解析 refDay / sigDay 标记（多策略共用）。
 */
public final class MarkerRefSignalParseTools {

    private static final Pattern REF_DAY = Pattern.compile("refDay=([^,|]+(?:\\s[^,|]+)*)");
    private static final Pattern SIG_DAY = Pattern.compile("sigDay=([^,|]+(?:\\s[^,|]+)*)");
    private static final Pattern REF_HIGH = Pattern.compile("refHigh=([0-9.]+)");

    private MarkerRefSignalParseTools() {
    }

    public static AtlasGc2MarkersVo parseFromText(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        String refDay = matchGroup(REF_DAY, text);
        String sigDay = matchGroup(SIG_DAY, text);
        Double refHigh = parseDouble(matchGroup(REF_HIGH, text));
        if (refDay.isEmpty() && sigDay.isEmpty()) {
            return null;
        }
        return AtlasGc2MarkersVo.builder()
                .referenceDay(refDay)
                .referenceHigh(refHigh)
                .signalDay(sigDay)
                .build();
    }

    private static Double parseDouble(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String matchGroup(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        return m.find() ? m.group(1).trim() : "";
    }
}
