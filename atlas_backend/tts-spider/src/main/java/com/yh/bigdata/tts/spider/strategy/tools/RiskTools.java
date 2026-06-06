package com.yh.bigdata.tts.spider.strategy.tools;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.dto.CheckResponse;
import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.utils.MathUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;

import java.util.List;


/**
 * 风险工具
 */
public final class RiskTools {


    /**
     * 风险： 阴线风险
     */
    public static CheckResponse checkGreenRisk(StockBase stockBase, PeriodTypeEnum trendPeriodType) {

        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);
        if (trendTrade0 == null) {
            return new CheckResponse(true, "阴线风险", trendPeriodType);
        }

        if (trendTrade0.getShitiRate() < 0) {
            return new CheckResponse(true, "阴线风险", trendPeriodType);
        }

        return new CheckResponse(false, "阴线风险", trendPeriodType);

    }


    /**
     * 风险： 上周期阴线风险
     */
    public static CheckResponse checkLastGreenCoverRisk(StockBase stockBase, PeriodTypeEnum trendPeriodType) {

        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);
        Trade trendTrade1 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 1);
        if (trendTrade0 == null) {
            return new CheckResponse(true, "上周期阴线风险", trendPeriodType);
        }

        if (trendTrade1 == null) {
            if (trendTrade0.getShitiRate() < 0) {
                return new CheckResponse(true, "上周期阴线风险", trendPeriodType);
            }
        }else {
            if (trendTrade1.getShitiRate() < 0 && trendTrade0.getClose() < trendTrade1.getHigh()) {
                return new CheckResponse(true, "上周期阴线风险", trendPeriodType);
            }
        }

        return new CheckResponse(false, "上周期阴线风险", trendPeriodType);

    }


    /**
     * 风险： 跌破支撑（Low）
     */
    public static CheckResponse checkNotUnderLowRisk(StockBase stockBase, PeriodTypeEnum trendPeriodType) {

        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);
        Trade trendTrade1 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 1);
        Trade trendTrade2 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 2);
        if (trendTrade0 == null) {
            return new CheckResponse(true, "跌破支撑（Low）风险", trendPeriodType);
        }

        if(trendTrade1 == null) {
            if (trendTrade0.getShitiRate() >= 0) {
                return new CheckResponse(true, "跌破支撑（Low）风险", trendPeriodType);
            }
        } else if (trendTrade2 == null) {
            if (trendTrade0.getClose() >= trendTrade1.getLow()) {
                return new CheckResponse(true, "跌破支撑（Low）风险", trendPeriodType);
            }
        } else {

            if (trendTrade1.getHigh() > trendTrade2.getHigh()) {
                if (trendTrade0.getClose() >= trendTrade1.getLow()) {
                    return new CheckResponse(true, "跌破支撑（Low）风险", trendPeriodType);
                }
            } else {
                if (trendTrade0.getClose() >= MathUtil.max(trendTrade1.getLow(), trendTrade2.getLow())) {
                    return new CheckResponse(true, "跌破支撑（Low）风险", trendPeriodType);
                }
            }

        }

        return new CheckResponse(false, "跌破支撑（Low）风险", trendPeriodType);

    }

    /**
     * MACD死叉之下
     */
    public static CheckResponse checkUnderGoldMACDRisk(StockBase stockBase, PeriodTypeEnum trendPeriodType) {

        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);

        List<Trade> lastCandlesticks = RealtimeStockCache.getLastTrades(stockBase, trendPeriodType, 100);

        List<MACDIndicatorUtils.MACDPoint> macdPoints = MACDIndicatorUtils.calculateMACD(Ticker.from(lastCandlesticks));
        if (CollectionUtils.isEmpty(macdPoints)) {
            return new CheckResponse(false, "MACD死叉之下风险", trendPeriodType);
        }

        Pair<MACDIndicatorUtils.MACDPoint, MACDIndicatorUtils.MACDPoint> latestGlodMACDPoint = MACDIndicatorUtils.getLatestGlodMACDPoint(macdPoints);
        if (latestGlodMACDPoint == null) {
            return new CheckResponse(false, "MACD死叉之下风险", trendPeriodType);
        }

        if (trendTrade0.getClose() < latestGlodMACDPoint.getLeft().getTicker().getLow()) {
            return new CheckResponse(true, "MACD死叉之下风险", trendPeriodType);
        }

        return new CheckResponse(false, "MACD死叉之下风险", trendPeriodType);

    }

    /**
     * 风险： 斜三角风险
     */
    public static CheckResponse checkNoXieSanJiaoRisk(StockBase stockBase, PeriodTypeEnum trendPeriodType) {

        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);
        Trade trendTrade1 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 1);
        Trade trendTrade2 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 2);
        if (trendTrade0 == null) {
            return new CheckResponse(true, "无斜三角风险", trendPeriodType);
        }

        if (trendTrade1 == null) {
            if (trendTrade0.getShitiRate() > 0) {
                return new CheckResponse(true, "无斜三角风险", trendPeriodType);
            }
        }else if (trendTrade2 == null) {
            if (trendTrade0.getClose() > trendTrade1.getHigh()) {
                return new CheckResponse(true, "无斜三角风险", trendPeriodType);
            }
        }else {
            if (!(trendTrade1.getHigh() < trendTrade2.getHigh() && trendTrade0.getHigh() < trendTrade1.getHigh())) {
                return new CheckResponse(true, "无斜三角风险", trendPeriodType);
            }
        }

        return new CheckResponse(false, "无斜三角风险", trendPeriodType);

    }
	
}
