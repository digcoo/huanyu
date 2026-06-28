package com.yh.bigdata.tts.spider.strategy.tools.cascade;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.dto.atlas.AtlasGc2MarkersVo;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.CascadeStrategyParams;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 级联交叉突破 · 基准交叉 K / 触发日 K 标记
 */
public final class CascadeMarkersTools {

    private static final Pattern REF_DAY = Pattern.compile("refDay=([^,|]+(?:\\s[^,|]+)*)");
    private static final Pattern SIG_DAY = Pattern.compile("sigDay=([^,|]+(?:\\s[^,|]+)*)");
    private static final Pattern REF_HIGH = Pattern.compile("refHigh=([0-9.]+)");

    private CascadeMarkersTools() {
    }

    public static AtlasGc2MarkersVo resolve(StockBase stock, PeriodTypeEnum period, CascadeStrategyParams params) {
        if (stock == null) {
            return null;
        }
        CascadeStrategyParams p = params != null ? params : CascadeStrategyParams.defaults();
        PeriodTypeEnum pType = period != null ? period : PeriodTypeEnum.DAY;

        CascadeBreakoutTools.TierHit hit = findHitForPeriod(stock, pType, p);
        if (hit != null && hit.getReferenceBar() != null && hit.getTodayDayBar() != null) {
            return AtlasGc2MarkersVo.builder()
                    .referenceDay(hit.getReferenceBar().getDay())
                    .referenceHigh(hit.getReferenceBar().getHigh())
                    .signalDay(hit.getTodayDayBar().getDay())
                    .signalHigh(hit.getTodayDayBar().getHigh())
                    .build();
        }

        String combined = safe(stock.getTrendMessage()) + "|" + safe(stock.getSignalMessage());
        return parseFromText(combined);
    }

    public static CascadeBreakoutTools.TierHit findHitForPeriod(StockBase stock, PeriodTypeEnum period,
                                                                  CascadeStrategyParams params) {
        CascadeStrategyParams p = params != null ? params : CascadeStrategyParams.defaults();
        if (period == null) {
            period = PeriodTypeEnum.DAY;
        }
        switch (period) {
            case MONTH:
                if (!p.isEnableMonth()) {
                    return null;
                }
                return CascadeBreakoutTools.findMonthTierHit(
                        stock, p.getLookbackMonth(), p.getLookbackDay(), p.isEnableAltBreakout());
            case WEEK:
                if (!p.isEnableWeek()) {
                    return null;
                }
                return CascadeBreakoutTools.findWeekTierHit(
                        stock, p.getLookbackWeek(), p.getLookbackMonth(), p.getLookbackDay(), p.isEnableAltBreakout());
            case DAY:
            default:
                if (!p.isEnableDay()) {
                    return null;
                }
                return CascadeBreakoutTools.findDayTierHit(
                        stock, p.getLookbackDay(), p.getLookbackWeek(), p.isEnableAltBreakout());
        }
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

    private static String safe(String s) {
        return s != null ? s : "";
    }
}
