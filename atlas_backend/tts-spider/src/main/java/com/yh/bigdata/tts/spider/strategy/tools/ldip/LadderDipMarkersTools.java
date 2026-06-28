package com.yh.bigdata.tts.spider.strategy.tools.ldip;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.dto.atlas.AtlasGc2MarkersVo;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.LadderDipStrategyParams;

public final class LadderDipMarkersTools {

    private LadderDipMarkersTools() {
    }

    public static AtlasGc2MarkersVo resolve(StockBase stock, PeriodTypeEnum period, LadderDipStrategyParams params) {
        if (stock == null) {
            return null;
        }
        LadderDipStrategyParams p = params != null ? params : LadderDipStrategyParams.defaults();
        PeriodTypeEnum pType = period != null ? period : PeriodTypeEnum.DAY;

        LadderDipBreakoutTools.PeriodHit hit = findHitForPeriod(stock, pType, p);
        if (hit != null && hit.getReferenceBar() != null && hit.getLastBar() != null) {
            return AtlasGc2MarkersVo.builder()
                    .referenceDay(hit.getReferenceBar().getDay())
                    .referenceHigh(hit.getReferenceBar().getHigh())
                    .signalDay(hit.getLastBar().getDay())
                    .signalHigh(hit.getLastBar().getHigh())
                    .build();
        }
        return null;
    }

    public static LadderDipBreakoutTools.PeriodHit findHitForPeriod(StockBase stock, PeriodTypeEnum period,
                                                                    LadderDipStrategyParams params) {
        LadderDipStrategyParams p = params != null ? params : LadderDipStrategyParams.defaults();
        if (period == null) {
            period = PeriodTypeEnum.DAY;
        }
        switch (period) {
            case MONTH:
                return p.isEnableMonth()
                        ? LadderDipBreakoutTools.findMonthHit(stock, p.getLookbackMonth()) : null;
            case WEEK:
                return p.isEnableWeek()
                        ? LadderDipBreakoutTools.findWeekHit(stock, p.getLookbackWeek()) : null;
            case DAY:
            default:
                return p.isEnableDay()
                        ? LadderDipBreakoutTools.findDayHit(stock, p.getLookbackDay()) : null;
        }
    }
}
