package com.yh.bigdata.tts.spider.strategy.tools;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.dto.CheckResponse;
import com.yh.bigdata.tts.common.indicator.*;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.utils.MathUtil;
import org.springframework.util.CollectionUtils;

import java.util.List;

public final class IndicatorCommonUtils {

    /**
     * MACD金叉之上
     */
    public static CheckResponse checkOverMACD(StockBase stockBase, PeriodTypeEnum trendPeriodType) {

        List<Trade> lastCandlesticks = RealtimeStockCache.getLastTrades(stockBase, trendPeriodType, 100);

        List<MACDIndicatorUtils.MACDPoint> macdPoints = MACDIndicatorUtils.calculateMACD(Ticker.from(lastCandlesticks));
        if (CollectionUtils.isEmpty(macdPoints) || macdPoints.get(macdPoints.size() - 1).getMacd() > 0) {
            return new CheckResponse(true, "MACD金叉之上", trendPeriodType);
        }

        return new CheckResponse(false, "MACD金叉之上", trendPeriodType);

    }


    /**
     * MACD金叉cross
     */
    public static CheckResponse checkCrossGoldMACD(StockBase stockBase, PeriodTypeEnum trendPeriodType) {

        List<Trade> lastCandlesticks = RealtimeStockCache.getLastTrades(stockBase, trendPeriodType, 100);

        List<MACDIndicatorUtils.MACDPoint> macdPoints = MACDIndicatorUtils.calculateMACD(Ticker.from(lastCandlesticks));
        if (CollectionUtils.isEmpty(macdPoints) || macdPoints.get(macdPoints.size() - 1).isIfRedGoldCross()) {
            return new CheckResponse(true, "MACD金叉cross", trendPeriodType);
        }

        return new CheckResponse(false, "MACD金叉cross", trendPeriodType);

    }

    /**
     * RSI安全线内
     */
    public static CheckResponse checkBetweenSafeRSI(StockBase stockBase, PeriodTypeEnum trendPeriodType) {

        List<Trade> lastCandlesticks = RealtimeStockCache.getLastTrades(stockBase, trendPeriodType, 100);

        List<RSIIndicatorUtils.RSIPoint> rsiPoints = RSIIndicatorUtils.calculateRSI(Ticker.from(lastCandlesticks));
        if (CollectionUtils.isEmpty(rsiPoints) ||
                (rsiPoints.get(rsiPoints.size() - 1).getRsi1() > 50 && rsiPoints.get(rsiPoints.size() - 1).getRsi1() < 80)) {
            return new CheckResponse(true, "RSI安全线内", trendPeriodType);
        }

        return new CheckResponse(false, "RSI安全线内", trendPeriodType);

    }


    /**
     * MA20以上
     */
    public static CheckResponse checkOverMA20(StockBase stockBase, PeriodTypeEnum trendPeriodType) {

        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);
        List<Trade> lastCandlesticks = RealtimeStockCache.getLastTrades(stockBase, trendPeriodType, 100);

        double ma5Value = MAIndicatorUtils.calLatestMA(lastCandlesticks, 5);
        double ma20Value = MAIndicatorUtils.calLatestMA(lastCandlesticks, 20);

        if (trendTrade0.getClose() > ma20Value
//                && ma5Value > ma20Value
        ) {
            return new CheckResponse(true, "MA20以上", trendPeriodType);
        }

        return new CheckResponse(false, "MA20以上", trendPeriodType);

    }

    /**
     * MA5>MA20
     */
    public static CheckResponse checkMA5OverMA20(StockBase stockBase, PeriodTypeEnum trendPeriodType) {

        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);
        List<Trade> lastCandlesticks = RealtimeStockCache.getLastTrades(stockBase, trendPeriodType, 100);

        double ma5Value = MAIndicatorUtils.calLatestMA(lastCandlesticks, 5);
        double ma20Value = MAIndicatorUtils.calLatestMA(lastCandlesticks, 20);

        if (trendTrade0.getClose() > ma20Value  && ma5Value > ma20Value ) {
            return new CheckResponse(true, "MA5>MA20", trendPeriodType);
        }

        return new CheckResponse(false, "MA5>MA20", trendPeriodType);

    }


    /**
     * 趋势底上(Band)
     */
    public static CheckResponse checkOverBandBottomHigh(StockBase stockBase, PeriodTypeEnum trendPeriodType) {

        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);
        List<Trade> lastCandlesticks = RealtimeStockCache.getLastTrades(stockBase, trendPeriodType, 40);

        BandIndicatorUtils.BandSegment lastBandWithoutCurrent = BandIndicatorUtils.getLastBandSegmentWithoutCurrent(trendPeriodType, lastCandlesticks, 0);

        if (trendTrade0.getClose() > Math.min(lastBandWithoutCurrent.getPreFirstTrade().getHigh(), lastBandWithoutCurrent.getFirstTrade().getHigh())) {
            return new CheckResponse(true, "趋势底High上", trendPeriodType);
        }

        return new CheckResponse(false, "趋势底High上", trendPeriodType);

    }


    /**
     * 趋势底上(Band)
     */
    public static CheckResponse checkOverBandBottomLow(StockBase stockBase, PeriodTypeEnum trendPeriodType) {

        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);
        List<Trade> lastCandlesticks = RealtimeStockCache.getLastTrades(stockBase, trendPeriodType, 40);

        BandIndicatorUtils.BandSegment lastBandWithoutCurrent = BandIndicatorUtils.getLastBandSegmentWithoutCurrent(trendPeriodType, lastCandlesticks, 0);

        if (lastBandWithoutCurrent !=
                null && trendTrade0.getClose() > MathUtil.min(lastBandWithoutCurrent.getFirstTrade().getLow(), lastBandWithoutCurrent.getPreFirstTrade().getLow())) {
            return new CheckResponse(true, "趋势底Low上", trendPeriodType);
        }

        return new CheckResponse(false, "趋势底Low上", trendPeriodType);

    }


    /**
     * 支撑红K之上
     */
    public static CheckResponse checkOverLastRedLow(StockBase stockBase, PeriodTypeEnum trendPeriodType) {

        Trade lastRedTrendTrade = KlineIndicatorUtils.getLastRedTrade(RealtimeStockCache.getLastTrades(stockBase, trendPeriodType, 20));


        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);

        if (lastRedTrendTrade != null
                && trendTrade0.getClose() >= lastRedTrendTrade.getLow()) {

            return new CheckResponse(true, "支撑红K之上", trendPeriodType);

        }

        return new CheckResponse(false, "支撑红K之上", trendPeriodType);

    }



    /**
     * 上周期Low之上
     */
    public static CheckResponse checkOverLastLow(StockBase stockBase, PeriodTypeEnum trendPeriodType) {

        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);
        Trade trendTrade1 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 1);

        if (trendTrade1 != null
                && trendTrade0.getClose() >= trendTrade1.getLow()) {

            return new CheckResponse(true, "上周期Low之上", trendPeriodType);

        }

        return new CheckResponse(false, "上周期Low之上", trendPeriodType);

    }


    /**
     * 波段底上
     */
    public static CheckResponse checkOverBandLow(StockBase stockBase, PeriodTypeEnum trendPeriodType) {
        List<Trade> lastTrades = RealtimeStockCache.getLastTrades(stockBase, trendPeriodType, 60);
        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);

        BandIndicatorUtils.BandSegment lastBandSegmentWithoutCurrent1 = BandIndicatorUtils.getLastBandSegmentWithoutCurrent(trendPeriodType, lastTrades, 0);
        BandIndicatorUtils.BandSegment lastBandSegmentWithoutCurrent2 = BandIndicatorUtils.getLastBandSegmentWithoutCurrent(trendPeriodType, lastTrades, 1);

        if (lastBandSegmentWithoutCurrent1 == null || lastBandSegmentWithoutCurrent2 == null) {
            return new CheckResponse(false, "波段底上", trendPeriodType);
        }

        //凹
        if (lastBandSegmentWithoutCurrent1.getHigh() < lastBandSegmentWithoutCurrent2.getHigh()) {
            Trade firstTrade = lastBandSegmentWithoutCurrent1.getFirstTrade();
            Trade preFirstTrade = lastBandSegmentWithoutCurrent1.getPreFirstTrade();
            if (trendTrade0.getClose() >= MathUtil.min(firstTrade.getLow(), preFirstTrade.getLow())) {
                return new CheckResponse(true, "凹波段：底上", trendPeriodType);
            } else {
                return new CheckResponse(false, "凹波段：底上", trendPeriodType);
            }
        } else {
            Trade lastTrade = lastBandSegmentWithoutCurrent1.getLastTrade();
            if (trendTrade0.getClose() >= lastTrade.getLow()) {
                return new CheckResponse(true, "凸波段：顶上", trendPeriodType);
            } else {
                return new CheckResponse(false, "凸波段：顶上", trendPeriodType);
            }
        }
    }


    /**
     * 波段底上
     */
    public static CheckResponse checkOverRevertBandLow(StockBase stockBase, PeriodTypeEnum trendPeriodType) {
        List<Trade> lastTrades = RealtimeStockCache.getLastTrades(stockBase, trendPeriodType, 60);
        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);

        BandRevertIndicatorUtils.BandSegment lastBandSegmentWithoutCurrent1 = BandRevertIndicatorUtils.getLastBandSegmentWithCurrent(trendPeriodType, lastTrades, 0);
        BandRevertIndicatorUtils.BandSegment lastBandSegmentWithoutCurrent2 = BandRevertIndicatorUtils.getLastBandSegmentWithCurrent(trendPeriodType, lastTrades, 1);

        if (lastBandSegmentWithoutCurrent1 == null || lastBandSegmentWithoutCurrent2 == null) {
            return new CheckResponse(false, "反转波段底上", trendPeriodType);
        }

        //凹
        if (lastBandSegmentWithoutCurrent1.getLow() < lastBandSegmentWithoutCurrent2.getLow()) {
            Trade lastTrade = lastBandSegmentWithoutCurrent1.getLastTrade();
            if (trendTrade0.getClose() >= lastTrade.getLow()) {
                return new CheckResponse(true, "凹波段：底上", trendPeriodType);
            } else {
                return new CheckResponse(false, "凹波段：底上", trendPeriodType);
            }
        } else {
            Trade firstTrade = lastBandSegmentWithoutCurrent1.getFirstTrade();
            if (trendTrade0.getClose() >= firstTrade.getLow()) {
                return new CheckResponse(true, "凸波段：顶上", trendPeriodType);
            } else {
                return new CheckResponse(false, "凸波段：顶上", trendPeriodType);
            }
        }
    }

}
