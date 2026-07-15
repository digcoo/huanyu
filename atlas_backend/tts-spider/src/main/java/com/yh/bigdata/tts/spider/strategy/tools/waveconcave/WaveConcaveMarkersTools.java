package com.yh.bigdata.tts.spider.strategy.tools.waveconcave;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.dto.atlas.AtlasGc2MarkersVo;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.WaveConcaveStrategyParams;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeTools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WaveConcaveMarkersTools {

    private static final Pattern LAST_YANG_DAY = Pattern.compile("lastYangDay=([^,|]+(?:\\s[^,|]+)*)");
    private static final Pattern SIG_DAY = Pattern.compile("sigDay=([^,|]+(?:\\s[^,|]+)*)");
    private static final Pattern BREAK_LINE = Pattern.compile("breakLine=([0-9.]+)");

    private WaveConcaveMarkersTools() {
    }

    public static AtlasGc2MarkersVo resolve(StockBase stock, PeriodTypeEnum period,
                                            WaveConcaveStrategyParams params) {
        if (stock == null) {
            return null;
        }
        WaveConcaveStrategyParams p = params != null ? params : WaveConcaveStrategyParams.defaults();
        PeriodTypeEnum pType = period != null ? period : PeriodTypeEnum.DAY;

        WaveShapeTools.TierHit hit = findHitForPeriod(stock, pType, p);
        if (hit != null && hit.getLastYangBar() != null && hit.getSignalBar() != null) {
            return AtlasGc2MarkersVo.builder()
                    .referenceDay(hit.getLastYangBar().getDay())
                    .referenceHigh(hit.getBreakLine())
                    .signalDay(hit.getSignalBar().getDay())
                    .signalHigh(hit.getSignalBar().getHigh())
                    .build();
        }

        String combined = safe(stock.getTrendMessage()) + "|" + safe(stock.getSignalMessage());
        return parseFromText(combined);
    }

    public static WaveShapeTools.TierHit findHitForPeriod(StockBase stock, PeriodTypeEnum period,
                                                          WaveConcaveStrategyParams params) {
        WaveConcaveStrategyParams p = params != null ? params : WaveConcaveStrategyParams.defaults();
        if (period == null) {
            period = PeriodTypeEnum.DAY;
        }
        WaveShapeTools.BreakLineConfig breakLine = WaveShapeTools.BreakLineConfig.of(
                p.isEnableLastHighBreak(), p.isEnableLastMedianBreak(), p.isEnableLastLowBreak());
        switch (period) {
            case WEEK:
                if (!p.isEnableWeek()) {
                    return null;
                }
                return WaveShapeTools.findTierHit(stock, PeriodTypeEnum.WEEK, p.getLookbackWeek(),
                        WaveShapeTools.BandShape.CONCAVE, breakLine);
            case MONTH:
                if (!p.isEnableMonth()) {
                    return null;
                }
                return WaveShapeTools.findTierHit(stock, PeriodTypeEnum.MONTH, p.getLookbackMonth(),
                        WaveShapeTools.BandShape.CONCAVE, breakLine);
            case DAY:
            default:
                if (!p.isEnableDay()) {
                    return null;
                }
                return WaveShapeTools.findTierHit(stock, PeriodTypeEnum.DAY, p.getLookbackDay(),
                        WaveShapeTools.BandShape.CONCAVE, breakLine);
        }
    }

    public static AtlasGc2MarkersVo parseFromText(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        String lastDay = matchGroup(LAST_YANG_DAY, text);
        String sigDay = matchGroup(SIG_DAY, text);
        Double breakLine = parseDouble(matchGroup(BREAK_LINE, text));
        if (lastDay.isEmpty() && sigDay.isEmpty()) {
            return null;
        }
        return AtlasGc2MarkersVo.builder()
                .referenceDay(lastDay)
                .referenceHigh(breakLine)
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
