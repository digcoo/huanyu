package com.yh.bigdata.tts.spider.strategy.tools.bogo;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.dto.atlas.AtlasGc2MarkersVo;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.BogoStrategyParams;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 底部机会 · 基准 K / 突破 K 标记
 */
public final class BogoMarkersTools {

    private static final Pattern REF_DAY = Pattern.compile("refDay=([^,|]+(?:\\s[^,|]+)*)");
    private static final Pattern SIG_DAY = Pattern.compile("sigDay=([^,|]+(?:\\s[^,|]+)*)");

    private BogoMarkersTools() {
    }

    public static AtlasGc2MarkersVo resolve(StockBase stock, PeriodTypeEnum period, BogoStrategyParams params) {
        if (stock == null) {
            return null;
        }
        BogoStrategyParams p = params != null ? params : BogoStrategyParams.defaults();
        PeriodTypeEnum pType = period != null ? period : PeriodTypeEnum.DAY;

        BogoBreakoutTools.PeriodHit hit = findHitForPeriod(stock, pType, p);
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

    public static BogoBreakoutTools.PeriodHit findHitForPeriod(StockBase stock, PeriodTypeEnum period,
                                                              BogoStrategyParams params) {
        BogoStrategyParams p = params != null ? params : BogoStrategyParams.defaults();
        if (period == null) {
            period = PeriodTypeEnum.DAY;
        }
        switch (period) {
            case MONTH:
                if (!p.isEnableMonth()) {
                    return null;
                }
                return BogoBreakoutTools.findHit(stock, PeriodTypeEnum.MONTH, p.getLookbackMonth());
            case WEEK:
                if (!p.isEnableWeek()) {
                    return null;
                }
                return BogoBreakoutTools.findHit(stock, PeriodTypeEnum.WEEK, p.getLookbackWeek());
            case DAY:
            default:
                if (!p.isEnableDay()) {
                    return null;
                }
                return BogoBreakoutTools.findHit(stock, PeriodTypeEnum.DAY, p.getLookbackDay());
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
