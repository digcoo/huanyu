package com.yh.bigdata.tts.spider.strategy.tools.ultralow;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.UltraLowReboundStrategyParams;
import com.yh.bigdata.tts.common.dto.atlas.AtlasUlowMin30MarkersVo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 梯子突破 · 基准K / 突破K 标记（各周期）
 */
public final class LadderMarkersTools {

    private static final Pattern REF_DAY = Pattern.compile("refDay=([^,|]+(?:\\s[^,|]+)*)");
    private static final Pattern SIG_DAY = Pattern.compile("sigDay=([^,|]+(?:\\s[^,|]+)*)");

    private LadderMarkersTools() {
    }

    public static AtlasUlowMin30MarkersVo resolve(StockBase stock, PeriodTypeEnum period,
                                                  UltraLowReboundStrategyParams params) {
        if (stock == null) {
            return null;
        }
        UltraLowReboundStrategyParams p = params != null ? params : UltraLowReboundStrategyParams.defaults();
        PeriodTypeEnum pType = period != null ? period : PeriodTypeEnum.MIN30;

        BreakoutLadderTools.TierHit hit = findHitForPeriod(stock, pType, p);
        if (hit != null && hit.getReferenceBar() != null && hit.getSignalBar() != null) {
            return AtlasUlowMin30MarkersVo.builder()
                    .referenceDay(hit.getReferenceBar().getDay())
                    .referenceHigh(hit.getReferenceBar().getHigh())
                    .signalDay(hit.getSignalBar().getDay())
                    .signalHigh(hit.getSignalBar().getHigh())
                    .build();
        }

        String combined = safe(stock.getTrendMessage()) + "|" + safe(stock.getSignalMessage());
        return parseFromText(combined);
    }

    public static BreakoutLadderTools.TierHit findHitForPeriod(StockBase stock, PeriodTypeEnum period,
                                                               UltraLowReboundStrategyParams params) {
        if (period == null) {
            return BreakoutLadderTools.findUltraHit(stock, params);
        }
        switch (period) {
            case MIN30:
                return params.isEnableUltra() ? BreakoutLadderTools.findUltraHit(stock, params) : null;
            case DAY:
                return params.isEnableShort() ? BreakoutLadderTools.findShortHit(stock, params) : null;
            case WEEK:
                return params.isEnableMedium() ? BreakoutLadderTools.findMediumHit(stock, params) : null;
            case MONTH:
                return params.isEnableLong() ? BreakoutLadderTools.findLongHit(stock, params) : null;
            default:
                return null;
        }
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
