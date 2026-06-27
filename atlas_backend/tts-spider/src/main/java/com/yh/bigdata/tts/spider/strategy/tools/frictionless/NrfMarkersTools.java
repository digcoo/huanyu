package com.yh.bigdata.tts.spider.strategy.tools.frictionless;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.dto.atlas.AtlasGc2MarkersVo;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.FrictionlessLadderStrategyParams;
import com.yh.bigdata.tts.common.param.LongStrategyParams;
import com.yh.bigdata.tts.common.param.MediumStrategyParams;
import com.yh.bigdata.tts.common.param.TrendV2StrategyParams;
import com.yh.bigdata.tts.spider.strategy.tools.markers.MarkerRefSignalParseTools;

/**
 * 跨周期内梯子上移 · 基准 K / 突破 K 标记
 */
public final class NrfMarkersTools {

    private NrfMarkersTools() {
    }

    public static AtlasGc2MarkersVo resolve(StockBase stock, PeriodTypeEnum period,
                                            FrictionlessLadderStrategyParams nrfParams,
                                            TrendV2StrategyParams trendParams,
                                            MediumStrategyParams mediumParams,
                                            LongStrategyParams longParams) {
        if (stock == null) {
            return null;
        }
        FrictionlessLadderStrategyParams p = nrfParams != null
                ? nrfParams : FrictionlessLadderStrategyParams.defaults();
        PeriodTypeEnum tierPeriod = periodForTier(p.getActiveTier());
        PeriodTypeEnum pType = period != null ? period : tierPeriod;

        if (pType != tierPeriod) {
            return parseFromStockText(stock);
        }

        FrictionlessLadderEvaluator.Evaluation eval = FrictionlessLadderEvaluator.evaluate(
                stock, null, p, null, trendParams, mediumParams, longParams);
        CrossPeriodInBarBreakoutTools.Hit hit = eval.getPeriodHit();
        if (hit == null) {
            hit = FrictionlessLadderEvaluator.findBreakoutHit(stock, p, trendParams, mediumParams, longParams);
        }
        if (hit != null && hit.getReferenceBar() != null && hit.getSignalBar() != null) {
            return AtlasGc2MarkersVo.builder()
                    .referenceDay(hit.getReferenceBar().getDay())
                    .referenceHigh(hit.getReferenceBar().getHigh())
                    .signalDay(hit.getSignalBar().getDay())
                    .signalHigh(hit.getSignalBar().getHigh())
                    .build();
        }
        return parseFromStockText(stock);
    }

    private static AtlasGc2MarkersVo parseFromStockText(StockBase stock) {
        String combined = safe(stock.getTrendMessage()) + "|" + safe(stock.getSignalMessage());
        return MarkerRefSignalParseTools.parseFromText(combined);
    }

    private static PeriodTypeEnum periodForTier(FrictionlessLadderStrategyParams.ActiveTier tier) {
        if (tier == FrictionlessLadderStrategyParams.ActiveTier.MEDIUM) {
            return PeriodTypeEnum.WEEK;
        }
        if (tier == FrictionlessLadderStrategyParams.ActiveTier.LONG) {
            return PeriodTypeEnum.MONTH;
        }
        return PeriodTypeEnum.DAY;
    }

    private static String safe(String s) {
        return s != null ? s : "";
    }
}
