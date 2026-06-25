package com.yh.bigdata.tts.spider.strategy.tools.trendm;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.dto.atlas.AtlasGc2MarkersVo;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.TrendmStrategyParams;
import com.yh.bigdata.tts.spider.strategy.tools.bogo.BogoBreakoutTools;
import com.yh.bigdata.tts.spider.strategy.tools.bogo.BogoMarkersTools;

/**
 * 趋势策略 · 日/周/月 基准 K / 突破 K 标记
 */
public final class TrendmMarkersTools {

    private TrendmMarkersTools() {
    }

    public static AtlasGc2MarkersVo resolve(StockBase stock, PeriodTypeEnum period, TrendmStrategyParams params) {
        if (stock == null) {
            return null;
        }
        TrendmStrategyParams p = params != null ? params : TrendmStrategyParams.defaults();
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
        return BogoMarkersTools.parseFromText(combined);
    }

    public static BogoBreakoutTools.PeriodHit findHitForPeriod(StockBase stock, PeriodTypeEnum period,
                                                               TrendmStrategyParams params) {
        TrendmStrategyParams p = params != null ? params : TrendmStrategyParams.defaults();
        if (period == null) {
            period = PeriodTypeEnum.DAY;
        }
        switch (period) {
            case MONTH:
                return BogoBreakoutTools.findHit(stock, PeriodTypeEnum.MONTH, p.getLookbackMonth());
            case WEEK:
                return BogoBreakoutTools.findHit(stock, PeriodTypeEnum.WEEK, p.getLookbackWeek());
            case DAY:
            default:
                return BogoBreakoutTools.findHit(stock, PeriodTypeEnum.DAY, p.getLookbackDay());
        }
    }

    private static String safe(String s) {
        return s != null ? s : "";
    }
}
