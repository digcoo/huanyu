package com.yh.bigdata.tts.spider.strategy.tools.dc2;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.dto.atlas.AtlasDc2MarkersVo;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.Dc2StrategyParams;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 死叉突破 · 死叉K / 突破K 标记
 */
public final class Dc2MarkersTools {

    private static final Pattern REF_DAY = Pattern.compile("refDay=([^,|]+(?:\\s[^,|]+)*)");
    private static final Pattern SIG_DAY = Pattern.compile("sigDay=([^,|]+(?:\\s[^,|]+)*)");

    private Dc2MarkersTools() {
    }

    public static AtlasDc2MarkersVo resolve(StockBase stock, PeriodTypeEnum period, Dc2StrategyParams params) {
        if (stock == null) {
            return null;
        }
        Dc2StrategyParams p = params != null ? params : Dc2StrategyParams.defaults();
        PeriodTypeEnum pType = period != null ? period : PeriodTypeEnum.DAY;

        Dc2BreakoutTools.TierHit hit = findHitForPeriod(stock, pType, p);
        if (hit != null && hit.getReferenceBar() != null && hit.getSignalBar() != null) {
            return AtlasDc2MarkersVo.builder()
                    .referenceDay(hit.getReferenceBar().getDay())
                    .referenceHigh(hit.getReferenceBar().getHigh())
                    .signalDay(hit.getSignalBar().getDay())
                    .signalHigh(hit.getSignalBar().getHigh())
                    .build();
        }

        String combined = safe(stock.getTrendMessage()) + "|" + safe(stock.getSignalMessage());
        return parseFromText(combined);
    }

    public static Dc2BreakoutTools.TierHit findHitForPeriod(StockBase stock, PeriodTypeEnum period,
                                                            Dc2StrategyParams params) {
        Dc2StrategyParams p = params != null ? params : Dc2StrategyParams.defaults();
        if (period == null) {
            return Dc2BreakoutTools.findShortHit(stock, p);
        }
        switch (period) {
            case DAY:
                return Dc2BreakoutTools.findShortHit(stock, p);
            case WEEK:
                return Dc2BreakoutTools.findLongHit(stock, p);
            default:
                return null;
        }
    }

    public static AtlasDc2MarkersVo parseFromText(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        String refDay = matchGroup(REF_DAY, text);
        String sigDay = matchGroup(SIG_DAY, text);
        if (refDay.isEmpty() && sigDay.isEmpty()) {
            return null;
        }
        return AtlasDc2MarkersVo.builder()
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
