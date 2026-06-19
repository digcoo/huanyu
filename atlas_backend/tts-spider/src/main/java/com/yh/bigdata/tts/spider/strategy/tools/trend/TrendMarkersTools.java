package com.yh.bigdata.tts.spider.strategy.tools.trend;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.dto.atlas.AtlasUlowMin30MarkersVo;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.TrendV2StrategyParams;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 短线策略 · 基准K / 突破K 标记（日K）
 */
public final class TrendMarkersTools {

    private static final Pattern REF_DAY = Pattern.compile("refDay=([^,|]+(?:\\s[^,|]+)*)");
    private static final Pattern SIG_DAY = Pattern.compile("sigDay=([^,|]+(?:\\s[^,|]+)*)");

    private TrendMarkersTools() {
    }

    public static AtlasUlowMin30MarkersVo resolve(StockBase stock, PeriodTypeEnum period,
                                                  TrendV2StrategyParams params) {
        if (stock == null) {
            return null;
        }
        PeriodTypeEnum pType = period != null ? period : PeriodTypeEnum.DAY;
        if (pType != PeriodTypeEnum.DAY) {
            return parseFromText(safe(stock.getTrendMessage()) + "|" + safe(stock.getSignalMessage()));
        }

        TrendV2StrategyParams p = params != null ? params : TrendV2StrategyParams.defaults();
        TrendBreakoutTools.Hit hit = TrendBreakoutTools.findHit(stock, p);
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
