package com.yh.bigdata.tts.spider.strategy.tools;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.dto.CheckResponse;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.utils.MathUtil;


/**
 * 趋势工具
 */
public final class TrendTools {

    /**
     * 无阻力
     */
    public static CheckResponse checkNoPressureTrend(StockBase stockBase, PeriodTypeEnum trendPeriodType) {

        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);
        Trade trendTrade1 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 1);
        Trade trendTrade2 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 2);

        if (trendTrade2 == null) {
            return KlineCommonTools.checkTrend2IsNull(stockBase, trendPeriodType);
        }else {
            if (trendTrade0.getClose() > MathUtil.max(trendTrade1.getShitiMax(), trendTrade2.getShitiMax())
                    || trendTrade0.getClose() > MathUtil.min(trendTrade1.getHigh(), trendTrade2.getHigh())) {
                return new CheckResponse(true, "无阻力", trendPeriodType);
            }
        }

        return new CheckResponse(false, "无阻力", trendPeriodType);

    }

    /**
     * 有支撑
     */
    public static CheckResponse checkHasSupportTrend(StockBase stockBase, PeriodTypeEnum trendPeriodType) {

        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);
        Trade trendTrade1 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 1);
        Trade trendTrade2 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 2);

        if (trendTrade2 == null) {
            return KlineCommonTools.checkTrend2IsNull(stockBase, trendPeriodType);
        }else {
            return KlineCommonTools.checkLastLowShangYi(stockBase, trendPeriodType);
        }
    }



    /**
     * red
     */
    public static CheckResponse checkRedTrend(StockBase stockBase, PeriodTypeEnum trendPeriodType) {
        return KlineCommonTools.checkRed(stockBase, trendPeriodType);
    }

    /**
     * > MACD
     */
    public static CheckResponse checkOverMACDTrend(StockBase stockBase, PeriodTypeEnum trendPeriodType) {
        return IndicatorCommonUtils.checkOverMACD(stockBase, trendPeriodType);
    }

    /**
     * RSI安全线内
     */
    public static CheckResponse checkBetweenSafeRSITrend(StockBase stockBase, PeriodTypeEnum trendPeriodType) {
        return IndicatorCommonUtils.checkBetweenSafeRSI(stockBase, trendPeriodType);
    }


    /**
     * > MA20
     */
    public static CheckResponse checkOverMA20Trend(StockBase stockBase, PeriodTypeEnum trendPeriodType) {
        return IndicatorCommonUtils.checkOverMA20(stockBase, trendPeriodType);
    }


    /**
     * MA5>MA20
     */
    public static CheckResponse checkMA5OverMA20Trend(StockBase stockBase, PeriodTypeEnum trendPeriodType) {
        return IndicatorCommonUtils.checkMA5OverMA20(stockBase, trendPeriodType);
    }


    /**
     * 波段底上(High)
     */
    public static CheckResponse checkOverBandBottomHighTrend(StockBase stockBase, PeriodTypeEnum trendPeriodType) {
        return IndicatorCommonUtils.checkOverBandBottomHigh(stockBase, trendPeriodType);
    }


    /**
     * 波段底上(High)
     */
    public static CheckResponse checkOverBandBottomLowTrend(StockBase stockBase, PeriodTypeEnum trendPeriodType) {
        return IndicatorCommonUtils.checkOverBandBottomLow(stockBase, trendPeriodType);
    }


    /**
     * 前一个红色实体之上
     */
    public static CheckResponse checkOverLastRedLowTrend(StockBase stockBase, PeriodTypeEnum trendPeriodType) {
        return IndicatorCommonUtils.checkOverLastRedLow(stockBase, trendPeriodType);
    }



    /**
     * 上周期Low之上
     */
    public static CheckResponse checkOverLastLowTrend(StockBase stockBase, PeriodTypeEnum trendPeriodType) {
        return IndicatorCommonUtils.checkOverLastLow(stockBase, trendPeriodType);
    }

    /**
     * 波段底上
     */
    public static CheckResponse checkOverBandLowTrend(StockBase stockBase, PeriodTypeEnum trendPeriodType) {
        return IndicatorCommonUtils.checkOverBandLow(stockBase, trendPeriodType);
    }


    /**
     * 波段底上
     */
    public static CheckResponse checkOverRevertBandLowTrend(StockBase stockBase, PeriodTypeEnum trendPeriodType) {
        return IndicatorCommonUtils.checkOverRevertBandLow(stockBase, trendPeriodType);
    }
}
