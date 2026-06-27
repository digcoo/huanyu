package com.yh.bigdata.tts.spider.strategy.tools.cladder;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.dto.atlas.AtlasGc2MarkersVo;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.CascadeLadderStrategyParams;

public final class CascadeLadderMarkersTools {

    private CascadeLadderMarkersTools() {
    }

    public static AtlasGc2MarkersVo resolve(StockBase stock, PeriodTypeEnum period, CascadeLadderStrategyParams params) {
        if (stock == null) {
            return null;
        }
        CascadeLadderStrategyParams p = params != null ? params : CascadeLadderStrategyParams.defaults();
        PeriodTypeEnum pType = period != null ? period : PeriodTypeEnum.DAY;

        CascadeLadderBreakoutTools.PeriodHit hit = findHitForPeriod(stock, pType, p);
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

    public static CascadeLadderBreakoutTools.PeriodHit findHitForPeriod(StockBase stock, PeriodTypeEnum period,
                                                                        CascadeLadderStrategyParams params) {
        CascadeLadderStrategyParams p = params != null ? params : CascadeLadderStrategyParams.defaults();
        if (period == null) {
            period = PeriodTypeEnum.DAY;
        }
        switch (period) {
            case MONTH:
                return CascadeLadderBreakoutTools.findMonthHit(stock, p.getLookbackMonth());
            case WEEK:
                return CascadeLadderBreakoutTools.findWeekHit(stock, p.getLookbackWeek());
            case DAY:
            default:
                return CascadeLadderBreakoutTools.findDayHit(stock, p.getLookbackDay());
        }
    }
}
