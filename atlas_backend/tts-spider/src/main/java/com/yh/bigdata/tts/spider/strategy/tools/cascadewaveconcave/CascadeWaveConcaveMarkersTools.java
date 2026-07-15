package com.yh.bigdata.tts.spider.strategy.tools.cascadewaveconcave;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.dto.atlas.AtlasGc2MarkersVo;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.CascadeWaveConcaveStrategyParams;
import com.yh.bigdata.tts.spider.strategy.tools.cascadewave.CascadeWaveBreakoutTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeTools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CascadeWaveConcaveMarkersTools {

    private static final Pattern LAST_YANG_DAY = Pattern.compile("lastYangDay=([^,|]+(?:\\s[^,|]+)*)");
    private static final Pattern SIG_DAY = Pattern.compile("sigDay=([^,|]+(?:\\s[^,|]+)*)");
    private static final Pattern BREAK_LINE = Pattern.compile("breakLine=([0-9.]+)");

    private CascadeWaveConcaveMarkersTools() {
    }

    public static AtlasGc2MarkersVo resolve(StockBase stock, PeriodTypeEnum period,
                                            CascadeWaveConcaveStrategyParams params) {
        if (stock == null) {
            return null;
        }
        CascadeWaveConcaveStrategyParams p = params != null ? params : CascadeWaveConcaveStrategyParams.defaults();
        PeriodTypeEnum pType = period != null ? period : PeriodTypeEnum.DAY;

        CascadeWaveBreakoutTools.TierHit hit = findHitForPeriod(stock, pType, p);
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

    public static CascadeWaveBreakoutTools.TierHit findHitForPeriod(StockBase stock, PeriodTypeEnum period,
                                                                    CascadeWaveConcaveStrategyParams params) {
        CascadeWaveConcaveStrategyParams p = params != null ? params : CascadeWaveConcaveStrategyParams.defaults();
        if (period == null) {
            period = PeriodTypeEnum.DAY;
        }
        switch (period) {
            case WEEK:
                if (!p.isEnableWeek()) {
                    return null;
                }
                return CascadeWaveBreakoutTools.findTierHit(stock, PeriodTypeEnum.WEEK, p.getLookbackWeek(),
                        WaveShapeTools.BandShape.CONCAVE, p.isEnablePrevBandBreak());
            case MONTH:
                if (!p.isEnableMonth()) {
                    return null;
                }
                return CascadeWaveBreakoutTools.findTierHit(stock, PeriodTypeEnum.MONTH, p.getLookbackMonth(),
                        WaveShapeTools.BandShape.CONCAVE, p.isEnablePrevBandBreak());
            case DAY:
            default:
                if (!p.isEnableDay()) {
                    return null;
                }
                return CascadeWaveBreakoutTools.findTierHit(stock, PeriodTypeEnum.DAY, p.getLookbackDay(),
                        WaveShapeTools.BandShape.CONCAVE, p.isEnablePrevBandBreak());
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
