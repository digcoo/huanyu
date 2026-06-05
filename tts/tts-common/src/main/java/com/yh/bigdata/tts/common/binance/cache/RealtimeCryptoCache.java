package com.yh.bigdata.tts.common.binance.cache;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yh.bigdata.tts.common.binance.model.Candlestick;
import com.yh.bigdata.tts.common.binance.utils.BinanceUtils;
import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class RealtimeCryptoCache {

    //symbol_period : List
    private static final Map<String, List<Candlestick>> cryptoMap = Maps.newConcurrentMap();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static List<Candlestick> getAllTrades(String symbol, PeriodTypeEnum periodTypeEnum) {
        return cryptoMap.getOrDefault(BinanceUtils.getCandleKey(symbol, periodTypeEnum), new ArrayList());
    }

    public static Candlestick getLastTrade(String symbol, PeriodTypeEnum periodTypeEnum, int leftOffset) {
        leftOffset = -Math.abs(leftOffset);
        List<Candlestick> trades = getAllTrades(symbol, periodTypeEnum);
        if (trades != null && trades.size() > Math.abs(leftOffset)) {
            return trades.get(trades.size() - 1 + leftOffset);
        }
        return null;
    }

    public static List<Candlestick> getLastTrades(StockBase stockBase, PeriodTypeEnum periodTypeEnum, int num) {
        List<Candlestick> trades = getAllTrades(stockBase.getCode(), periodTypeEnum);
        if (trades.size() > num) {
            return trades.subList(trades.size() - num, trades.size());
        }
        return trades;
    }

    public static void updateCandlestick(Candlestick candlestick) {
        cryptoMap.putIfAbsent(BinanceUtils.getCandleKey(candlestick.getSymbol(), candlestick.getPeriodType()), Lists.newArrayList(candlestick));
    }

}
