package com.yh.bigdata.tts.spider.strategy.tools.gc2;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.dto.atlas.AtlasGc2MarkersVo;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.Gc2StrategyParams;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 金叉二次突破 · 金叉K / 突破K 标记
 */
public final class Gc2MarkersTools {

    private static final Pattern REF_DAY = Pattern.compile("refDay=([^,|]+(?:\\s[^,|]+)*)");
    private static final Pattern SIG_DAY = Pattern.compile("sigDay=([^,|]+(?:\\s[^,|]+)*)");

    private Gc2MarkersTools() {
    }

    public static AtlasGc2MarkersVo resolve(StockBase stock, PeriodTypeEnum period, Gc2StrategyParams params) {
        if (stock == null) {
            return null;
        }
        Gc2StrategyParams p = params != null ? params : Gc2StrategyParams.defaults();
        PeriodTypeEnum pType = period != null ? period : PeriodTypeEnum.DAY;

        Gc2BreakoutTools.TierHit hit = findHitForPeriod(stock, pType, p);
        if (hit != null && hit.getReferenceBar() != null && hit.getSignalBar() != null) {
            return AtlasGc2MarkersVo.builder()
                    .referenceDay(hit.getReferenceBar().getDay())
                    .referenceHigh(hit.getReferenceBar().getHigh())
                    .signalDay(hit.getSignalBar().getDay())
                    .signalHigh(hit.getSignalBar().getHigh())
                    .build();
        }

        String combined = safe(stock.getTrendMessage()) + "|" + safe(stock.getSignalMessage());
        return parseFromText(combined);
    }

    public static Gc2BreakoutTools.TierHit findHitForPeriod(StockBase stock, PeriodTypeEnum period,
                                                            Gc2StrategyParams params) {
        Gc2StrategyParams p = params != null ? params : Gc2StrategyParams.defaults();
        if (period == null) {
            return Gc2BreakoutTools.findShortHit(stock, p);
        }
        switch (period) {
            case DAY:
                return Gc2BreakoutTools.findShortHit(stock, p);
            case WEEK:
                return Gc2BreakoutTools.findMediumHit(stock, p);
            case MONTH:
                return Gc2BreakoutTools.findLongHit(stock, p);
            default:
                return null;
        }
    }

    public static AtlasGc2MarkersVo parseFromText(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        String refDay = matchGroup(REF_DAY, text);
        String sigDay = matchGroup(SIG_DAY, text);
        if (refDay.isEmpty() && sigDay.isEmpty()) {
            return null;
        }
        return AtlasGc2MarkersVo.builder()
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
