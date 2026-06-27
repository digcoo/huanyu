package com.yh.bigdata.tts.spider.strategy.tools.pillar;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.dto.atlas.AtlasGc2MarkersVo;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.PillarStrategyParams;
import com.yh.bigdata.tts.spider.strategy.tools.markers.MarkerRefSignalParseTools;

/**
 * 柱子内上移 · 基准 K / 突破 K 标记
 */
public final class PillarMarkersTools {

    private PillarMarkersTools() {
    }

    public static AtlasGc2MarkersVo resolve(StockBase stock, PeriodTypeEnum period, PillarStrategyParams params) {
        if (stock == null) {
            return null;
        }
        PillarStrategyParams p = params != null ? params : PillarStrategyParams.defaults();
        PeriodTypeEnum pType = period != null ? period : PeriodTypeEnum.DAY;

        PillarBreakoutTools.PeriodHit hit = findHitForPeriod(stock, pType, p);
        if (hit != null && hit.getReferenceBar() != null && hit.getSignalBar() != null) {
            return AtlasGc2MarkersVo.builder()
                    .referenceDay(hit.getReferenceBar().getDay())
                    .referenceHigh(hit.getReferenceBar().getHigh())
                    .signalDay(hit.getSignalBar().getDay())
                    .signalHigh(hit.getSignalBar().getHigh())
                    .build();
        }

        String combined = safe(stock.getTrendMessage()) + "|" + safe(stock.getSignalMessage());
        return MarkerRefSignalParseTools.parseFromText(combined);
    }

    public static PillarBreakoutTools.PeriodHit findHitForPeriod(StockBase stock, PeriodTypeEnum period,
                                                                 PillarStrategyParams params) {
        PillarStrategyParams p = params != null ? params : PillarStrategyParams.defaults();
        if (period == null) {
            period = PeriodTypeEnum.DAY;
        }
        switch (period) {
            case MONTH:
                if (!p.isEnableMonth()) {
                    return null;
                }
                return PillarBreakoutTools.findHit(stock, PeriodTypeEnum.MONTH, p.getLookbackMonth());
            case WEEK:
                if (!p.isEnableWeek()) {
                    return null;
                }
                return PillarBreakoutTools.findHit(stock, PeriodTypeEnum.WEEK, p.getLookbackWeek());
            case DAY:
            default:
                if (!p.isEnableDay()) {
                    return null;
                }
                return PillarBreakoutTools.findHit(stock, PeriodTypeEnum.DAY, p.getLookbackDay());
        }
    }

    private static String safe(String s) {
        return s != null ? s : "";
    }
}
