package com.yh.bigdata.tts.spider.strategy.tools.longterm;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.dto.atlas.AtlasUlowMin30MarkersVo;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.LongStrategyParams;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LongMarkersTools {

    private static final Pattern REF_DAY = Pattern.compile("refDay=([^,|]+(?:\\s[^,|]+)*)");
    private static final Pattern SIG_DAY = Pattern.compile("sigDay=([^,|]+(?:\\s[^,|]+)*)");

    private LongMarkersTools() {
    }

    public static AtlasUlowMin30MarkersVo resolve(StockBase stock, PeriodTypeEnum period,
                                                  LongStrategyParams params) {
        if (stock == null) {
            return null;
        }
        PeriodTypeEnum pType = period != null ? period : PeriodTypeEnum.MONTH;
        if (pType != PeriodTypeEnum.MONTH) {
            return parseFromText(safe(stock.getTrendMessage()) + "|" + safe(stock.getSignalMessage()));
        }

        LongStrategyParams p = params != null ? params : LongStrategyParams.defaults();
        LongBreakoutTools.Hit hit = LongBreakoutTools.findHit(stock, p);
        if (hit != null && hit.getReferenceBar() != null && hit.getSignalBar() != null) {
            Trade ref = hit.getReferenceBar();
            Trade sig = hit.getSignalBar();
            return AtlasUlowMin30MarkersVo.builder()
                    .referenceDay(ref.getDay())
                    .referenceHigh(ref.getHigh())
                    .signalDay(sig.getDay())
                    .signalHigh(sig.getHigh())
                    .build();
        }

        return parseFromText(safe(stock.getTrendMessage()) + "|" + safe(stock.getSignalMessage()));
    }

    public static AtlasUlowMin30MarkersVo parseFromText(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        String refDay = matchGroup(REF_DAY, text);
        String sigDay = matchGroup(SIG_DAY, text);
        if (refDay.isEmpty() && sigDay.isEmpty()) {
            return null;
        }
        return AtlasUlowMin30MarkersVo.builder()
                .referenceDay(refDay)
                .signalDay(sigDay)
                .build();
    }

    private static String matchGroup(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        return m.find() ? m.group(1).trim() : "";
    }

    private static String safe(String s) {
        return s != null ? s : "";
    }
}
