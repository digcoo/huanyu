package com.yh.bigdata.tts.spider.strategy.group.min30;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.dto.CheckResponse;
import com.yh.bigdata.tts.common.indicator.BandIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.BandRevertIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.TiZiIndicatorUtils;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * min30信号工具
 */
@Slf4j
public final class SignalMin30Tools {

    /**
     * min30突破关键阻力位（实时）
     * 1、昨日早盘30min
     * 2、昨日最大成交量30min
     * 3、昨日High
     */
    public static CheckResponse checkCrossZaoPanMin30Signal(StockBase stockBase) {

        List<Trade> dayTrades = RealtimeStockCache.getLastTrades(stockBase, PeriodTypeEnum.DAY, 5);
        List<Trade> min30Trades = RealtimeStockCache.getLastTrades(stockBase, PeriodTypeEnum.MIN30, 30);

        Pair<Trade, Trade> zaoPanMin30TradePair = TiZiIndicatorUtils.getZaoPanMin30PressureTrade(dayTrades, min30Trades);
        if (Min30CheckTools.checkShockAndCrossHighZaoPanSignal(stockBase, zaoPanMin30TradePair)) {
            return new CheckResponse(true, "早盘Min30震荡上移", PeriodTypeEnum.MIN30);
        }
        return new CheckResponse(false, "早盘Min30震荡上移", PeriodTypeEnum.MIN30);
    }


    /**
     * 突破趋势波段Step-High
     */
    public static CheckResponse checkCrossBandStepHighSignal(StockBase stockBase, PeriodTypeEnum trendPeriodType, boolean realtimeFlag) {

        List<Trade> lastTrades = RealtimeStockCache.getLastTrades(stockBase, trendPeriodType, 60);

        BandIndicatorUtils.BandSegment lastBandSegmentWithoutCurrent1 = BandIndicatorUtils.getLastBandSegmentWithoutCurrent(trendPeriodType, lastTrades, 0);

        if (lastBandSegmentWithoutCurrent1 == null) {
            return new CheckResponse(false, "突破趋势波段Step-High", trendPeriodType);
        }
        List<Trade> rangeTrades = lastBandSegmentWithoutCurrent1.getRangeTrades();
        List<Trade> keyPressureTrades = new ArrayList<>();
        keyPressureTrades.addAll(rangeTrades);
        keyPressureTrades.add(lastBandSegmentWithoutCurrent1.getPreFirstTrade());

        for (Trade keyPressureTrade: keyPressureTrades) {
            boolean result = false;
            if (realtimeFlag) {
                result = Min30CheckTools.checkShockAndCrossHighRealtimeSignal(stockBase, keyPressureTrade, PeriodTypeEnum.MIN30.getCrossMinHighRate(), true);
            }else  {
                result = Min30CheckTools.checkShockAndCrossHighZaoPanSignal(stockBase, keyPressureTrade, PeriodTypeEnum.MIN30.getCrossMinHighRate(), true);
            }
            if (result) {
                return new CheckResponse(true, "突破趋势波段Step-High(" + keyPressureTrade.getDay() + ":" + keyPressureTrade.getHigh() + ")", trendPeriodType);
            }
        }
        return new CheckResponse(false, "突破趋势波段Step-High", trendPeriodType);
    }


    /**
     * 突破反转波段Step-High
     */
    public static CheckResponse checkRevertBandStepHighSignal(StockBase stockBase, PeriodTypeEnum trendPeriodType, boolean realtimeFlag) {
        Trade min30Trade0 = RealtimeStockCache.getLastTrade(stockBase, PeriodTypeEnum.MIN30, 0);
        Trade min30Trade1 = RealtimeStockCache.getLastTrade(stockBase, PeriodTypeEnum.MIN30, 1);

        List<Trade> trendTrades = RealtimeStockCache.getLastTrades(stockBase, trendPeriodType, 60);

        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);
        Trade trendTrade1 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 1);

        BandRevertIndicatorUtils.BandSegment lastBandSegmentWithCurrent1 = BandRevertIndicatorUtils.getLastBandSegmentWithCurrent(trendPeriodType, trendTrades, 0);

        if (lastBandSegmentWithCurrent1 == null) {
            return new CheckResponse(false, "突破反转波段Step-High", trendPeriodType);
        }

        if (trendTrade0.isDecline()) {
            return new CheckResponse(false, "突破反转波段Step-High", trendPeriodType);
        }

        List<Trade> rangeTrades = lastBandSegmentWithCurrent1.getRangeTrades();
        List<Trade> keyPressureTrades = new ArrayList<>();
        keyPressureTrades.addAll(rangeTrades);

        for (Trade keyPressureTrade: keyPressureTrades) {
            boolean result = false;
            if (realtimeFlag) {
                result = Min30CheckTools.checkShockAndCrossHighRealtimeSignal(stockBase, keyPressureTrade, PeriodTypeEnum.MIN30.getCrossMinHighRate(), true);
            }else  {
                result = Min30CheckTools.checkShockAndCrossHighZaoPanSignal(stockBase, keyPressureTrade, PeriodTypeEnum.MIN30.getCrossMinHighRate(), true);
            }
            if (result) {
                return new CheckResponse(true, "突破趋势波段Step-High(" + keyPressureTrade.getDay() + ":" + keyPressureTrade.getClose() + ")", trendPeriodType);
            }
        }
        return new CheckResponse(false, "突破反转波段Step-High", trendPeriodType);
    }

    public static CheckResponse checkNewBottomRedSignal(StockBase stockBase, PeriodTypeEnum trendPeriodType) {
        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);
        Trade trendTrade1 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 1);
        Trade trendTrade2 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 2);
        Trade trendTrade3 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 3);

        // 本周期新低翻红
        if (trendTrade2 != null
                && trendTrade1.getShitiRate() < 0
                && trendTrade1.getLow() < trendTrade2.getLow()

                //本周期新低翻红
                && trendTrade0.getLow() < trendTrade1.getLow()
                && trendTrade0.getShitiRate() > 0) {
            return new CheckResponse(true, "本周期新低翻红", trendPeriodType);
        }

        // 上上周期新低，上周期震荡未新低，本周期回踩翻红
        if (trendTrade3 != null
                && trendTrade2.getShitiRate() < 0
                && trendTrade2.getLow() < trendTrade3.getLow()

                && trendTrade1.getLow() >= trendTrade2.getLow()
                && trendTrade0.getLow() < trendTrade1.getLow()

                && trendTrade0.getShitiRate() > 0) {
            return new CheckResponse(true, "上上周期新低翻红", trendPeriodType);
        }


        // 上周期新低，本周期未新低，且直接反包
        if (trendTrade2 != null
                && trendTrade1.getShitiRate() < 0
                && trendTrade1.getLow() < trendTrade2.getLow()


                && trendTrade0.getLow() > trendTrade1.getLow()
                && trendTrade0.getClose() > trendTrade1.getHigh()) {
            return new CheckResponse(true, "新低反包", trendPeriodType);
        }

        return new CheckResponse(false, "新低翻红", trendPeriodType);
    }

}
