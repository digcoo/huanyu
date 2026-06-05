package com.yh.bigdata.tts.spider.strategy.group.min30;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.utils.MathUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * min30信号工具
 */
@Slf4j
public final class Min30CheckTools {

    /**
     * 当前min30突破前两日早盘
     */
    public static boolean checkShockAndCrossHighZaoPanSignal(StockBase stockBase, Pair<Trade, Trade> zaoPanMin30Pair) {

        Trade lastZaoPanMin30Trade1 = zaoPanMin30Pair.getLeft();
        Trade lastZaoPanMin30Trade2 = zaoPanMin30Pair.getRight();

        //凹
        if (lastZaoPanMin30Trade1.getHigh() < lastZaoPanMin30Trade2.getHigh()) {
            //突破斜侧低位
            if (checkShockAndCrossHighZaoPanSignal(stockBase, lastZaoPanMin30Trade1, PeriodTypeEnum.MIN30.getCrossMaxHighRate(), false)) {
                return true;
            }

            //突破斜侧高
            if (checkShockAndCrossHighZaoPanSignal(stockBase, lastZaoPanMin30Trade2, PeriodTypeEnum.MIN30.getCrossMinHighRate(), true)) {
                return true;
            }

        } else {

            //突破斜侧高
            if (checkShockAndCrossHighZaoPanSignal(stockBase, lastZaoPanMin30Trade1, PeriodTypeEnum.MIN30.getCrossMinHighRate(), true)) {
                return true;
            }

            //突破斜侧低
            if (checkShockAndCrossHighZaoPanSignal(stockBase, lastZaoPanMin30Trade2, PeriodTypeEnum.MIN30.getCrossMinHighRate(), true)) {
                return true;
            }
        }

        return false;

    }


    /**
     * 早盘突破High
     */
    public static boolean checkShockAndCrossHighZaoPanSignal(StockBase stockBase, Trade keyPressureTrade, Double crossRate, boolean downLowFlag) {

        //昨日收盘min30
        Trade dayTrade0 = RealtimeStockCache.getLastTrade(stockBase, PeriodTypeEnum.DAY, 0);
        Trade dayTrade1 = RealtimeStockCache.getLastTrade(stockBase, PeriodTypeEnum.DAY, 1);
        List<Trade> lastTrades = RealtimeStockCache.getLastTrades(stockBase, PeriodTypeEnum.MIN30, dayTrade1.getStartTime(), dayTrade0.getStartTime());
        Trade preZaoPanMin30Trade = lastTrades.get(lastTrades.size() - 1);

        //今日早盘min30
        Trade zaoPanMin30Trade0 = RealtimeStockCache.getLastZaoPanTrade(stockBase, PeriodTypeEnum.MIN30, 0);
        //今日早盘第2个Min30
        Trade min30Trade0 = RealtimeStockCache.getLastTrade(stockBase, PeriodTypeEnum.MIN30, 0);
        List<Trade> min30Trades = RealtimeStockCache.getLastTrades(stockBase, PeriodTypeEnum.MIN30, zaoPanMin30Trade0.getStartTime(), min30Trade0.getStartTime());
        Trade secondMin30Trade = min30Trades.size() >= 2? min30Trades.get(1): null;

        //实体、震荡
        if (!(MathUtil.max(keyPressureTrade.getShockRate(), keyPressureTrade.getChangeRate()) > PeriodTypeEnum.MIN30.getBandShockRate()
                && Math.abs(keyPressureTrade.getShitiRate()) > PeriodTypeEnum.MIN30.getBandShiTiRate())) {
            return false;
        }

        if (!(MathUtil.max(zaoPanMin30Trade0.getChangeRate(), zaoPanMin30Trade0.getShitiRate()) > crossRate
                && zaoPanMin30Trade0.getChangeRate() > PeriodTypeEnum.MIN30.getBandChangeRate()

                && zaoPanMin30Trade0.getClose() > preZaoPanMin30Trade.getHigh()

                //下探支撑位
                && (!downLowFlag || MathUtil.min(preZaoPanMin30Trade.getLow(), zaoPanMin30Trade0.getLow()) <= keyPressureTrade.getHigh()))

        ) {
            return false;
        }

        //早盘Close突破
        if (
                 zaoPanMin30Trade0.getClose() > keyPressureTrade.getHigh()
        ) {
            return true;
        }

        //早盘High突破
        if (
            (secondMin30Trade == null && zaoPanMin30Trade0.getClose() > keyPressureTrade.getHigh())
                || (secondMin30Trade != null && zaoPanMin30Trade0.getClose() <= keyPressureTrade.getHigh() && secondMin30Trade.getClose() >= keyPressureTrade.getHigh())
        ) {
            return true;
        }
        return false;
    }



    /**
     * 实时突破High
     */
    public static boolean checkShockAndCrossHighRealtimeSignal(StockBase stockBase, Trade keyPressureTrade, Double crossRate, boolean downLowFlag) {

        Trade min30Trade0 = RealtimeStockCache.getLastTrade(stockBase, PeriodTypeEnum.MIN30, 0);
        Trade min30Trade1 = RealtimeStockCache.getLastTrade(stockBase, PeriodTypeEnum.MIN30, 1);
        Trade min30Trade2 = RealtimeStockCache.getLastTrade(stockBase, PeriodTypeEnum.MIN30, 2);

        //close突破
        if (MathUtil.max(min30Trade0.getChangeRate(), min30Trade0.getShitiRate())  > crossRate

                //下探支撑位
                && (!downLowFlag || MathUtil.min(min30Trade1.getLow(), min30Trade0.getLow()) <= keyPressureTrade.getHigh())

                && min30Trade0.getClose() >= keyPressureTrade.getHigh()

        ) {
            return true;
        }

        //上一个high突破，当前close突破
        if (MathUtil.max(min30Trade1.getChangeRate(), min30Trade1.getShitiRate()) > crossRate
                && min30Trade1.getHigh() <= keyPressureTrade.getHigh()

                //下探支撑位
                && (!downLowFlag || MathUtil.min(min30Trade2.getLow(), min30Trade1.getLow()) <= keyPressureTrade.getHigh())

                && min30Trade1.getHigh() >= keyPressureTrade.getHigh()
                && min30Trade0.getClose() >= keyPressureTrade.getHigh()

        ) {
            return true;
        }
        return false;
    }



}
