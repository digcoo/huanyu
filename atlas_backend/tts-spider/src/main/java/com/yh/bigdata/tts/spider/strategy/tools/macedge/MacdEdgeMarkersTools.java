package com.yh.bigdata.tts.spider.strategy.tools.macedge;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.dto.atlas.AtlasGc2MarkersVo;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MacdEdgeStrategyParams;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MacdEdgeMarkersTools {

    private static final Pattern REF_DAY = Pattern.compile("refDay=([^,|]+(?:\\s[^,|]+)*)");
    private static final Pattern SIG_DAY = Pattern.compile("sigDay=([^,|]+(?:\\s[^,|]+)*)");
    private static final Pattern REF_HIGH = Pattern.compile("refHigh=([0-9.]+)");

    private MacdEdgeMarkersTools() {
    }

    public static AtlasGc2MarkersVo resolve(StockBase stock, PeriodTypeEnum period, MacdEdgeStrategyParams params) {
        if (stock == null) {
            return null;
        }
        MacdEdgeStrategyParams p = params != null ? params : MacdEdgeStrategyParams.defaults();
        PeriodTypeEnum pType = period != null ? period : PeriodTypeEnum.DAY;

        MacdEdgeBreakoutTools.TierHit hit = findHitForPeriod(stock, pType, p);
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

    public static MacdEdgeBreakoutTools.TierHit findHitForPeriod(StockBase stock, PeriodTypeEnum period,
                                                                   MacdEdgeStrategyParams params) {
        MacdEdgeStrategyParams p = params != null ? params : MacdEdgeStrategyParams.defaults();
        if (period == null) {
            period = PeriodTypeEnum.DAY;
        }
        switch (period) {
            case MIN30:
                if (!p.isEnableMin30()) {
                    return null;
                }
                return MacdEdgeBreakoutTools.findTierHit(stock, PeriodTypeEnum.MIN30, p.getLookbackMin30());
            case WEEK:
                if (!p.isEnableWeek()) {
                    return null;
                }
                return MacdEdgeBreakoutTools.findTierHit(stock, PeriodTypeEnum.WEEK, p.getLookbackWeek());
            case MONTH:
                if (!p.isEnableMonth()) {
                    return null;
                }
                return MacdEdgeBreakoutTools.findTierHit(stock, PeriodTypeEnum.MONTH, p.getLookbackMonth());
            case YEAR:
                if (!p.isEnableYear()) {
                    return null;
                }
                return MacdEdgeBreakoutTools.findTierHit(stock, PeriodTypeEnum.YEAR, p.getLookbackYear());
            case DAY:
            default:
                if (!p.isEnableDay()) {
                    return null;
                }
                return MacdEdgeBreakoutTools.findTierHit(stock, PeriodTypeEnum.DAY, p.getLookbackDay());
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
