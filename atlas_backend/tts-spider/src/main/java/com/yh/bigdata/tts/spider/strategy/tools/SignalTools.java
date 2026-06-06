package com.yh.bigdata.tts.spider.strategy.tools;

import com.yh.bigdata.tts.common.indicator.TiZiIndicatorUtils;
import com.yh.bigdata.tts.common.utils.MathUtil;
import lombok.extern.slf4j.Slf4j;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.dto.CheckResponse;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;

import java.util.List;
import java.util.Objects;

/**
 * 信号工具
 */
@Slf4j
public final class SignalTools {

    /**
     * 三连涨
     */
    public static CheckResponse checkZhangTing3Signal(StockBase stockBase, PeriodTypeEnum opPeriodType) {
        Trade opTrade0 = RealtimeStockCache.getLastTrade(stockBase, opPeriodType, 0);
        Trade opTrade1 = RealtimeStockCache.getLastTrade(stockBase, opPeriodType, 1);
        Trade opTrade2 = RealtimeStockCache.getLastTrade(stockBase, opPeriodType, 2);
        Trade opTrade3 = RealtimeStockCache.getLastTrade(stockBase, opPeriodType, 3);
        if (opTrade2 != null && opTrade1 != null && opTrade0 != null && (opTrade3 == null || !opTrade3.isZhangTing())
                && opTrade2.isZhangTing() && opTrade1.isZhangTing() && opTrade0.isApproachZhangTing()) {
            return new CheckResponse(true, "三板", opPeriodType);
        }
        return new CheckResponse(false, "三板", opPeriodType);
    }

    /**
     * 连板
     */
    public static CheckResponse checkLianBanSignal(StockBase stockBase, PeriodTypeEnum opPeriodType, int lianBanDays) {
        List<Trade> opTrades = RealtimeStockCache.getLastTrades(stockBase, opPeriodType, lianBanDays);
        if (opTrades.stream().filter(Objects::nonNull).filter(Trade::isZhangTing).count() >= lianBanDays) {
            return new CheckResponse(true, "连板", opPeriodType);
        }
        return new CheckResponse(false, "连板", opPeriodType);
    }


    /**
     * 三连涨
     */
    public static CheckResponse checkZhangTing1Signal(StockBase stockBase, PeriodTypeEnum opPeriodType) {
        Trade opTrade0 = RealtimeStockCache.getLastTrade(stockBase, opPeriodType, 0);
        Trade opTrade1 = RealtimeStockCache.getLastTrade(stockBase, opPeriodType, 1);
        if (opTrade0 != null
                && (opTrade1 == null || !opTrade1.isZhangTing())
                && opTrade0.isApproachZhangTing()) {
            return new CheckResponse(true, "首板", opPeriodType);
        }
        return new CheckResponse(false, "首板", opPeriodType);
    }

    /**
     * 突破梯子High(区间在实体Min之上)
     */
    public static CheckResponse checkCrossTiZiHighSignal(StockBase stockBase, PeriodTypeEnum trendPeriodType, PeriodTypeEnum opPeriodType) {
        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);
        Trade trendTrade1 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 1);
        Trade trendTrade2 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 2);

        Trade opTrade0 = RealtimeStockCache.getLastTrade(stockBase, opPeriodType, 0);
        Trade opTrade1 = RealtimeStockCache.getLastTrade(stockBase, opPeriodType, 1);
        Trade opTrade2 = RealtimeStockCache.getLastTrade(stockBase, opPeriodType, 2);

        Trade zaoPanTrade0 = RealtimeStockCache.getLastZaoPanTrade(stockBase, opPeriodType, 0);

        if (trendTrade2 == null) {
            return new CheckResponse(false, "突破梯子High(区间Low上移)", trendPeriodType);
        }

        //梯子
        List<Trade> trendTrades = RealtimeStockCache.getLastTrades(stockBase, trendPeriodType, 15);
        List<TiZiIndicatorUtils.TiZiPoint> tiZiPoints = TiZiIndicatorUtils.getTiZiPoints(trendTrades, trendPeriodType);

        if (tiZiPoints.isEmpty()) {
            return new CheckResponse(false, "突破梯子High(区间Low上移)", trendPeriodType);
        }

        for (TiZiIndicatorUtils.TiZiPoint tiziPoint : tiZiPoints) {
            Trade tiziTrade = tiziPoint.getTrade();

//            if (KlineCommonTools.isBackCrossTiZiMin(tiziTrade, trendTrades)) {
//                continue;
//            }

            if (KlineCommonTools.isOverTiZiHigh(tiziTrade, trendTrades)) {
                continue;
            }

            //突破 梯子high
            Double tiziHigh = tiziTrade.getHigh();
            if (opTrade1.getClose() <= tiziHigh
                    && opTrade0.getClose() >= tiziHigh

                    && opTrade0.getClose() > opTrade1.getShitiMax()
                    && opTrade0.getHigh() > opTrade1.getHigh()

                    && MathUtil.max(opTrade0.getShitiRate(), opTrade0.getChangeRate()) > trendPeriodType.getCrossMaxHighRate()

                    && zaoPanTrade0.getClose() >= tiziHigh

            ) {
                return new CheckResponse(true, "突破梯子High(区间Low上移)(" + tiziTrade.getDay() + ")", trendPeriodType);
            }

        }

        return new CheckResponse(false, "突破梯子High(区间Low上移)", trendPeriodType);
    }

    /**
     * 梯子之间 : macd > 0
     */
    public static CheckResponse checkBetweenTiZiMACDSignal(StockBase stockBase, PeriodTypeEnum trendPeriodType, PeriodTypeEnum opPeriodType) {
        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);
        Trade trendTrade1 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 1);
        Trade trendTrade2 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 2);

        Trade opTrade0 = RealtimeStockCache.getLastTrade(stockBase, opPeriodType, 0);
        Trade opTrade1 = RealtimeStockCache.getLastTrade(stockBase, opPeriodType, 1);
        Trade opTrade2 = RealtimeStockCache.getLastTrade(stockBase, opPeriodType, 2);

        if (trendTrade2 == null) {
            return new CheckResponse(false, "梯子之间", trendPeriodType);
        }

        //梯子
        List<Trade> trendTrades = RealtimeStockCache.getLastTrades(stockBase, trendPeriodType, 30);
        List<TiZiIndicatorUtils.TiZiPoint> tiZiPoints = TiZiIndicatorUtils.getTiZiPoints(trendTrades, trendPeriodType);


        if (tiZiPoints.isEmpty()) {
            return new CheckResponse(false, "梯子之间", trendPeriodType);
        }

        for (TiZiIndicatorUtils.TiZiPoint tiziPoint : tiZiPoints) {
            Trade tiziTrade = tiziPoint.getTrade();

            //最近一个大梯子
            if (MathUtil.min(tiziTrade.getShitiRate(), tiziTrade.getChangeRate()) > opPeriodType.getBandShiTiRate()) {
                if (    //梯子之间
                        opTrade0.getClose() > tiziTrade.getLow()
//                        && opTrade0.getClose() < tiziTrade.getHigh()

                        //日级别macd > 0
                        && IndicatorCommonUtils.checkOverMACD(stockBase, opPeriodType).isSuccess()

                        && opTrade0.getShitiRate() >= 0
                        && KlineCommonTools.checkHighShangYi(stockBase, opPeriodType).isSuccess()

                ) {
                    return new CheckResponse(true, "梯子之间(" + tiziTrade.getDay() + ")", trendPeriodType);
                }

                break;
            }



        }

        return new CheckResponse(false, "梯子之间", trendPeriodType);
    }

    /**
     * MACD金叉
     */
    public static CheckResponse checkCrossGoldMACDSignal(StockBase stockBase, PeriodTypeEnum opPeriodType) {
        return IndicatorCommonUtils.checkCrossGoldMACD(stockBase, opPeriodType);
    }

    
}
