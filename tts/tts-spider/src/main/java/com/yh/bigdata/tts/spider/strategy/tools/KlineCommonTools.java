package com.yh.bigdata.tts.spider.strategy.tools;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.dto.CheckResponse;
import com.yh.bigdata.tts.common.indicator.BandIndicatorUtils;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.utils.DateUtil;
import com.yh.bigdata.tts.common.utils.MathUtil;

import java.util.List;

public final class KlineCommonTools {

    /**
     * 收红
     */
    public static CheckResponse checkRed(StockBase stockBase, PeriodTypeEnum trendPeriodType) {

        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);

        if (trendTrade0 == null) {
            return new CheckResponse(false, "收红", trendPeriodType);
        }

        if (trendTrade0.getShitiRate() >= 0) {
            return new CheckResponse(true, "收红", trendPeriodType);
        }

        return new CheckResponse(false, "收红", trendPeriodType);

    }

    /**
     * 本周期High上移
     */
    public static CheckResponse checkHighShangYi(StockBase stockBase, PeriodTypeEnum trendPeriodType) {

        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);
        Trade trendTrade1 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 1);
        Trade trendTrade2 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 2);

        if (trendTrade2 == null) {
            return checkTrend2IsNull(stockBase, trendPeriodType);
        } else {
            if ((trendTrade0.getClose() >= MathUtil.min(trendTrade1.getHigh(), trendTrade2.getHigh()))
                    || (trendTrade0.getClose() >= MathUtil.max(trendTrade1.getShitiMax(), trendTrade2.getShitiMax()))) {
                return new CheckResponse(true, "本周期High上移", trendPeriodType);
            }
        }
        return new CheckResponse(false, "本周期High上移", trendPeriodType);

    }


    /**
     * 上周期High上移，且本周期底上移
     */
    public static CheckResponse checkLastHighShangYi(StockBase stockBase, PeriodTypeEnum trendPeriodType) {

        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);
        Trade trendTrade1 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 1);
        Trade trendTrade2 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 2);

        if (trendTrade2 == null) {
            return checkTrend2IsNull(stockBase, trendPeriodType);
        } else {
            if (trendTrade1.getHigh() >= trendTrade2.getHigh()) {
                return new CheckResponse(true, "上周期High上移", trendPeriodType);
            }
        }

        return new CheckResponse(false, "上周期High上移", trendPeriodType);

    }


    /**
     * 上周期High上移，且本周期底上移
     */
    public static CheckResponse checkLastHighXiaYi(StockBase stockBase, PeriodTypeEnum trendPeriodType) {

        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);
        Trade trendTrade1 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 1);
        Trade trendTrade2 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 2);

        if (trendTrade2 == null) {
            return checkTrend2IsNull(stockBase, trendPeriodType);
        } else {
            if (trendTrade1.getHigh() <= trendTrade2.getHigh()) {
                return new CheckResponse(true, "上周期High下移", trendPeriodType);
            }
        }

        return new CheckResponse(false, "上周期High下移", trendPeriodType);

    }
//
//    /**
//     * 本周期High上移
//     */
//    public static CheckResponse checkHighShangYi2(StockBase stockBase, PeriodTypeEnum trendPeriodType) {
//
//        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);
//        Trade trendTrade1 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 1);
//        Trade trendTrade2 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 2);
//
//        if (trendTrade2 == null) {
//            return checkTrend2IsNull(stockBase, trendPeriodType);
//        } else {
//            if (trendTrade0.getShitiRate() > 0
//                    && trendTrade0.getHigh() > MathUtil.min(trendTrade1.getHigh(), trendTrade2.getHigh())
//                    && trendTrade0.getClose() >= MathUtil.min(trendTrade1.getShitiMax(), trendTrade2.getShitiMax())) {
//                return new CheckResponse(true, "本周期High上移", trendPeriodType);
//            }
//        }
//
//        return new CheckResponse(false, "本周期High上移", trendPeriodType);
//
//    }

    /**
     * 上周期底上移
     * 上周期low上移 + 上周期high上移 + 本周期low上移 ： 无
     * 本周期low下移：
     * 上周期low上移 + 上周期high下移 + 本周期low上移 ：
     * 上周期low上移 + 上周期high下移 + 本周期low上移 ：
     * 上周期low下移 + 上周期high上移 + 本周期low上移 ：
     * 上周期low下移 + 上周期high下移 + 本周期low上移 ： trendTrade0.getClose() > MathUtil.min(trendTrade1.getHigh(), trendTrade2.getHigh())
     */
    public static CheckResponse checkLastLowShangYi(StockBase stockBase, PeriodTypeEnum trendPeriodType) {

        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);
        Trade trendTrade1 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 1);
        Trade trendTrade2 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 2);

        if (trendTrade2 == null) {
            return KlineCommonTools.checkTrend2IsNull(stockBase, trendPeriodType);
        } else {
            if (trendTrade1.getLow() > trendTrade2.getLow()
                    && trendTrade1.getHigh() > trendTrade2.getHigh()

                    && trendTrade0.getLow() > trendTrade1.getLow()
            ) {

                return new CheckResponse(true, "有支撑(low上移)", trendPeriodType);

            } else if (trendTrade0.getClose() > MathUtil.min(trendTrade1.getHigh(), trendTrade2.getHigh())) {

                return new CheckResponse(true, "有支撑(low上移)", trendPeriodType);

            }
        }

        return new CheckResponse(false, "有支撑(low上移)", trendPeriodType);

    }

    public static CheckResponse checkTrend2IsNull(StockBase stockBase, PeriodTypeEnum trendPeriodType) {
        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);
        Trade trendTrade1 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 1);
        Trade trendTrade2 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 2);
        if (trendTrade0 == null) {
            return new CheckResponse(false, "default", trendPeriodType);
        } else if (trendTrade1 == null) {
            if (trendTrade0.getShitiRate() > 0) {
                return new CheckResponse(true, "default", trendPeriodType);
            }
        } else if (trendTrade2 == null) {
            if ((trendTrade1.getShitiRate() > 0 && trendTrade0.getClose() > trendTrade1.getShitiMax())
                    || (trendTrade1.getShitiRate() <= 0 && trendTrade0.getHigh() > trendTrade1.getHigh() && trendTrade0.getClose() > trendTrade1.getShitiMax())) {
                return new CheckResponse(true, "default", trendPeriodType);
            }
        } else {
            return new CheckResponse(true, "default", trendPeriodType);
        }
        return new CheckResponse(false, "default", trendPeriodType);
    }


    /**
     * 是否突破梯子High：收盘
     */
    public static boolean isOverTiZiHigh(Trade tiziTrade, List<Trade> opTrades) {
        for (int i = 0; i <opTrades.size() - 1; i++) {
            Trade trade = opTrades.get(i);

            if (DateUtil.isAfter(trade.getDay(), tiziTrade.getDay())) {

                if (trade.getClose() > tiziTrade.getHigh()) {
                    return true;
                }

            }

        }

        return false;
    }


    /**
     * 是否突破梯子High：收盘
     */
    public static boolean isOverTiZiHigh(Trade tiziTrade, List<Trade> opTrades, String startTime, String endTime) {
        for (int i = 0; i <opTrades.size() - 1; i++) {
            Trade trade = opTrades.get(i);
            if (DateUtil.isAfter(trade.getDay(), startTime) && DateUtil.isBefore(trade.getDay(), endTime)) {

                if (trade.getClose() > tiziTrade.getHigh()) {
                    return true;
                }

            }
        }

        return false;
    }


}
