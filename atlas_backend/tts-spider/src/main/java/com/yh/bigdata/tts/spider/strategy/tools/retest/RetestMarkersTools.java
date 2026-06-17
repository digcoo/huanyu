package com.yh.bigdata.tts.spider.strategy.tools.retest;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.dto.atlas.AtlasRetestMarkersVo;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.RetestStrategyParams;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 回踩抬升 K 线四点标记
 */
public final class RetestMarkersTools {

    private static final Pattern L0_DAY = Pattern.compile("l0Day=([^,|]+)");
    private static final Pattern H1_DAY = Pattern.compile("h1Day=([^,|]+)");
    private static final Pattern L1_DAY = Pattern.compile("l1Day=([^,|]+)");
    private static final Pattern SIG_DAY = Pattern.compile("sigDay=([^,|]+)");
    private static final Pattern MODES = Pattern.compile("modes=([^,|]+)");

    private RetestMarkersTools() {
    }

    public static AtlasRetestMarkersVo resolve(StockBase stock, PeriodTypeEnum periodType,
                                               RetestStrategyParams params) {
        if (stock == null || periodType == null) {
            return null;
        }
        RetestStrategyParams p = params != null ? params : RetestStrategyParams.defaults();
        RetestTierTools.TierHit hit = findHitForPeriod(stock, periodType, p);
        if (hit != null && hit.getStructure() != null) {
            return toVo(hit);
        }
        return parseFromMessages(stock);
    }

    private static RetestTierTools.TierHit findHitForPeriod(StockBase stock, PeriodTypeEnum periodType,
                                                            RetestStrategyParams p) {
        switch (periodType) {
            case MIN30:
                return RetestTierTools.findUltraHit(stock, p);
            case DAY:
                return RetestTierTools.findShortHit(stock, p);
            case WEEK:
                return RetestTierTools.findMediumHit(stock, p);
            case MONTH:
                return RetestTierTools.findLongHit(stock, p);
            default:
                return RetestTierTools.findShortHit(stock, p);
        }
    }

    private static AtlasRetestMarkersVo toVo(RetestTierTools.TierHit hit) {
        RetestStructureTools.StructureHit s = hit.getStructure();
        List<String> modes = new ArrayList<>();
        if (hit.isBear()) {
            modes.add("bear");
        }
        if (hit.isBull()) {
            modes.add("bull");
        }
        return AtlasRetestMarkersVo.builder()
                .l0Day(dayOf(s.getL0()))
                .l0Low(lowOf(s.getL0()))
                .h1Day(dayOf(s.getH1()))
                .h1High(highOf(s.getH1()))
                .l1Day(dayOf(s.getL1()))
                .l1Low(lowOf(s.getL1()))
                .signalDay(dayOf(s.getSignal()))
                .signalHigh(highOf(s.getSignal()))
                .modes(modes)
                .build();
    }

    private static AtlasRetestMarkersVo parseFromMessages(StockBase stock) {
        String combined = safe(stock.getTrendMessage()) + "|" + safe(stock.getSignalMessage());
        if (!L0_DAY.matcher(combined).find() && !SIG_DAY.matcher(combined).find()) {
            return null;
        }
        String l0Day = null;
        String h1Day = null;
        String l1Day = null;
        String sigDay = null;
        Matcher l0m = L0_DAY.matcher(combined);
        if (l0m.find()) {
            l0Day = l0m.group(1).trim();
        }
        Matcher h1m = H1_DAY.matcher(combined);
        if (h1m.find()) {
            h1Day = h1m.group(1).trim();
        }
        Matcher l1m = L1_DAY.matcher(combined);
        if (l1m.find()) {
            l1Day = l1m.group(1).trim();
        }
        Matcher sigm = SIG_DAY.matcher(combined);
        if (sigm.find()) {
            sigDay = sigm.group(1).trim();
        }
        List<String> modes = new ArrayList<>();
        Matcher modesM = MODES.matcher(combined);
        if (modesM.find()) {
            for (String part : modesM.group(1).split(",")) {
                if (!part.isEmpty()) {
                    modes.add(part.trim());
                }
            }
        }
        return AtlasRetestMarkersVo.builder()
                .l0Day(l0Day)
                .h1Day(h1Day)
                .l1Day(l1Day)
                .signalDay(sigDay)
                .modes(modes)
                .build();
    }

    private static String safe(String s) {
        return s != null ? s : "";
    }

    private static String dayOf(Trade bar) {
        return bar != null ? bar.getDay() : null;
    }

    private static Double lowOf(Trade bar) {
        return bar != null ? bar.getLow() : null;
    }

    private static Double highOf(Trade bar) {
        return bar != null ? bar.getHigh() : null;
    }
}
