package com.yh.bigdata.tts.spider.strategy.group.band;


import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.dto.CheckResponse;
import com.yh.bigdata.tts.common.indicator.BandIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.BandRevertIndicatorUtils;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.utils.MathUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 波段信号工具
 */
@Slf4j
public final class SignalBandTools {



    /**
     * 突破趋势波段
     */
    public static CheckResponse checkCrossBandHighSignal(StockBase stockBase, PeriodTypeEnum trendPeriodType, PeriodTypeEnum opPeriodType) {
        List<Trade> lastTrades = RealtimeStockCache.getLastTrades(stockBase, trendPeriodType, 60);

        BandIndicatorUtils.BandSegment lastBandSegmentWithoutCurrent1 = BandIndicatorUtils.getLastBandSegmentWithoutCurrent(trendPeriodType, lastTrades, 0);
        BandIndicatorUtils.BandSegment lastBandSegmentWithoutCurrent2 = BandIndicatorUtils.getLastBandSegmentWithoutCurrent(trendPeriodType, lastTrades, 1);

        if (lastBandSegmentWithoutCurrent1 == null || lastBandSegmentWithoutCurrent2 == null) {
            return new CheckResponse(false, "突破趋势波段", trendPeriodType);
        }

        //凹波段
        if (lastBandSegmentWithoutCurrent1.getHigh() < lastBandSegmentWithoutCurrent2.getHigh()) {
            return checkCrossBandPressure(stockBase, trendPeriodType, opPeriodType, lastBandSegmentWithoutCurrent1, true);
        }else {
            //凸波段
            return checkCrossBandPressure(stockBase, trendPeriodType, opPeriodType, lastBandSegmentWithoutCurrent1, false);

        }

    }

    private static CheckResponse checkCrossBandPressure(StockBase stockBase, PeriodTypeEnum trendPeriodType, PeriodTypeEnum opPeriodType, BandIndicatorUtils.BandSegment bandSegment, boolean uShapeBandFlag) {

        Trade dayTrade0 = RealtimeStockCache.getLastTrade(stockBase, opPeriodType, 0);
        Trade dayTrade1 = RealtimeStockCache.getLastTrade(stockBase, opPeriodType, 1);

        //最后一个红K: Low
        Trade lastTrade = bandSegment.getLastTrade();
        if (dayTrade0.getClose() >= lastTrade.getLow()
                && (dayTrade1.getClose() <= lastTrade.getLow() || dayTrade0.getOpen() <= lastTrade.getLow())
                && MathUtil.max(dayTrade0.getChangeRate(), dayTrade0.getShitiRate()) > opPeriodType.getCrossMaxHighRate()
        ) {
            return new CheckResponse(true, "突破波段【尾红K】Low(" + lastTrade.getDay() + ":" + lastTrade.getLow() + ")", trendPeriodType);
        }

        //最后一个红K: High
        if (dayTrade0.getClose() >= lastTrade.getHigh()
                && (dayTrade1.getClose() <= lastTrade.getHigh() || dayTrade0.getOpen() <= lastTrade.getHigh())
                && MathUtil.max(dayTrade0.getChangeRate(), dayTrade0.getShitiRate()) > opPeriodType.getCrossMaxHighRate()
        ) {
            return new CheckResponse(true, "突破波段【尾红K】High(" + lastTrade.getDay() + ":" + lastTrade.getHigh() + ")", trendPeriodType);
        }

        if (uShapeBandFlag) {

            //第一个红K: Low
            Trade firstTrade = bandSegment.getFirstTrade();
            Trade preFirstTrade = bandSegment.getPreFirstTrade();
            Double firstLow = MathUtil.max(firstTrade.getLow(), preFirstTrade.getLow());
            if (((dayTrade0.getClose() >= firstTrade.getLow() && (dayTrade1.getClose() <= firstTrade.getLow() || dayTrade0.getOpen() <= firstLow))
                    || (dayTrade0.getClose() >= preFirstTrade.getLow() && (dayTrade1.getClose() <= preFirstTrade.getLow() || dayTrade0.getOpen() <= firstLow)))
                    && MathUtil.max(dayTrade0.getChangeRate(), dayTrade0.getShitiRate()) > opPeriodType.getCrossMaxHighRate()
            ) {
                return new CheckResponse(true, "突破波段【首红K】Low(" + firstTrade.getDay() + ":" + firstLow + ")", trendPeriodType);
            }

            //第一个红K: High
            if (dayTrade0.getClose() >= firstTrade.getHigh()
                    && (dayTrade1.getClose() <= firstTrade.getHigh() || dayTrade0.getOpen() <= firstTrade.getHigh())
                    && MathUtil.max(dayTrade0.getChangeRate(), dayTrade0.getShitiRate()) > opPeriodType.getCrossMaxHighRate()
            ) {
                return new CheckResponse(true, "突破波段【首红K】High(" + firstTrade.getDay() + ":" + firstTrade.getHigh() + ")", trendPeriodType);
            }

        }

        return new CheckResponse(false, "突破波段", trendPeriodType);

    }


    /**
     * 突破反转波段
     */
    public static CheckResponse checkRevertBandHighSignal(StockBase stockBase, PeriodTypeEnum trendPeriodType, PeriodTypeEnum opPeriodType) {
        List<Trade> trendTrades = RealtimeStockCache.getLastTrades(stockBase, trendPeriodType, 60);


        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);
        Trade trendTrade1 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 1);

        BandRevertIndicatorUtils.BandSegment lastBandSegmentWithCurrent1 = BandRevertIndicatorUtils.getLastBandSegmentWithCurrent(trendPeriodType, trendTrades, 0);
        BandRevertIndicatorUtils.BandSegment lastBandSegmentWithCurrent2 = BandRevertIndicatorUtils.getLastBandSegmentWithCurrent(trendPeriodType, trendTrades, 1);

        if (lastBandSegmentWithCurrent1 == null || lastBandSegmentWithCurrent2 == null) {
            return new CheckResponse(false, "突破反转波段", trendPeriodType);
        }

        if (trendTrade0.isDecline()) {
            return new CheckResponse(false, "突破反转波段", trendPeriodType);
        }


        //凹波段
        if (lastBandSegmentWithCurrent1.getLow() < lastBandSegmentWithCurrent2.getLow()) {
            return checkCrossRevertBandPressure(stockBase, trendPeriodType, opPeriodType, lastBandSegmentWithCurrent1, true);

        }else {

            //凸波段
            return checkCrossRevertBandPressure(stockBase, trendPeriodType, opPeriodType, lastBandSegmentWithCurrent1, false);

        }
    }


    private static CheckResponse checkCrossRevertBandPressure(StockBase stockBase, PeriodTypeEnum trendPeriodType, PeriodTypeEnum opPeriodType, BandRevertIndicatorUtils.BandSegment bandSegment, boolean uShapeBandFlag) {

        Trade dayTrade0 = RealtimeStockCache.getLastTrade(stockBase, opPeriodType, 0);
        Trade dayTrade1 = RealtimeStockCache.getLastTrade(stockBase, opPeriodType, 1);

        // 凹
        // 第一个：High
        // 最后一个： High

        // 凸
        // 第一个：Low、High
        // 最后一个： High

        //第一个绿K: High
        Trade firstTrade = bandSegment.getFirstTrade();
        if (dayTrade0.getClose() >= firstTrade.getHigh()
                && (dayTrade1.getClose() <= firstTrade.getHigh() || dayTrade0.getOpen() <= firstTrade.getHigh())
                && MathUtil.max(dayTrade0.getChangeRate(), dayTrade0.getShitiRate()) > opPeriodType.getCrossMaxHighRate()
        ) {
            return new CheckResponse(true, "突破反转波段【首绿K】High(" + firstTrade.getDay() + ":" + firstTrade.getHigh() + ")", trendPeriodType);
        }

        //最后一个绿K: High
        Trade lastTrade = bandSegment.getLastTrade();
        if (dayTrade0.getClose() >= lastTrade.getHigh()
                && (dayTrade1.getClose() <= firstTrade.getHigh() || dayTrade0.getOpen() <= lastTrade.getHigh())
                && MathUtil.max(dayTrade0.getChangeRate(), dayTrade0.getShitiRate()) > opPeriodType.getCrossMaxHighRate()
        ) {
            return new CheckResponse(true, "突破反转波段【尾绿K】High(" + lastTrade.getDay() + ":" + lastTrade.getHigh() + ")", trendPeriodType);
        }


        //凸波段：第一个绿K: Low
        if (!uShapeBandFlag) {
            if (dayTrade0.getClose() >= firstTrade.getLow()
                    && (dayTrade1.getClose() <= firstTrade.getHigh() || dayTrade0.getOpen() <= firstTrade.getLow())
                    && MathUtil.max(dayTrade0.getChangeRate(), dayTrade0.getShitiRate()) > opPeriodType.getCrossMaxHighRate()
            ) {
                return new CheckResponse(true, "突破反转波段【首绿K】Low(" + firstTrade.getDay() + ":" + firstTrade.getLow() + ")", trendPeriodType);
            }
        }

        return new CheckResponse(false, "突破反转波段", trendPeriodType);

    }

}
