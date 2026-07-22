package com.binance.client.examples.constants;


import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.enums.SideTypeEnum;
import com.binance.client.examples.constants.GlobalConstants;
import com.binance.client.model.market.Candlestick;
import com.binance.client.utils.MathUtil;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SymbolCacheData {

    //symbol -> periodType -> candlesticks
    ConcurrentHashMap<String, ConcurrentHashMap<PeriodTypeEnum, List<Candlestick>>> symbolToPeriodToCandlestickMap = new ConcurrentHashMap<>();

    //side -> symbols
    ConcurrentHashMap<SideTypeEnum, Set<String>> sideToRecommendSymbolMap = new ConcurrentHashMap<>();

    //periodType -> side -> symbols
    ConcurrentHashMap<PeriodTypeEnum, ConcurrentHashMap<SideTypeEnum, HashSet<String>>> periodToSideToRecommendSymbolMap = new ConcurrentHashMap<>();

    public SymbolCacheData() {
        sideToRecommendSymbolMap.put(SideTypeEnum.LONG, new HashSet<>());
        sideToRecommendSymbolMap.put(SideTypeEnum.SHORT, new HashSet<>());

        GlobalConstants.SPIDER_PERIODS.stream().forEach(periodTypeEnum -> {
            ConcurrentHashMap<SideTypeEnum, HashSet<String>> sideToRecommendSymbol = new ConcurrentHashMap<>();
            sideToRecommendSymbol.put(SideTypeEnum.LONG, new HashSet<>());
            sideToRecommendSymbol.put(SideTypeEnum.SHORT, new HashSet<>());

            periodToSideToRecommendSymbolMap.put(periodTypeEnum, sideToRecommendSymbol);

        });

    }

    public List<String> getSymbols() {
        return new ArrayList<>(symbolToPeriodToCandlestickMap.keySet());
    }

    public ConcurrentHashMap<PeriodTypeEnum, List<Candlestick>> getData(String symbol) {
        return this.symbolToPeriodToCandlestickMap.get(symbol);
    }

    public Set<String> getRecommendList(SideTypeEnum sideTypeEnum) {
        return sideToRecommendSymbolMap.get(sideTypeEnum);
    }

    public Set<String> getRecommendList(PeriodTypeEnum periodTypeEnum, SideTypeEnum sideTypeEnum) {
        return periodToSideToRecommendSymbolMap.get(periodTypeEnum).get(sideTypeEnum);
    }

    public boolean containsRecommend(SideTypeEnum sideTypeEnum, String symbol) {
        return sideToRecommendSymbolMap.get(sideTypeEnum).contains(symbol);
    }
    public boolean containsRecommend(PeriodTypeEnum periodTypeEnum, SideTypeEnum sideTypeEnum, String symbol) {
        return periodToSideToRecommendSymbolMap.get(periodTypeEnum).get(sideTypeEnum).contains(symbol);
    }

    public void addRecommend(SideTypeEnum sideTypeEnum, String symbol) {
        sideToRecommendSymbolMap.get(sideTypeEnum).add(symbol);
    }
    public void addRecommend(PeriodTypeEnum periodTypeEnum, SideTypeEnum sideTypeEnum, String symbol) {
        periodToSideToRecommendSymbolMap.get(periodTypeEnum).get(sideTypeEnum).add(symbol);
    }

    public void removeRecommend(SideTypeEnum sideTypeEnum, String symbol) {
        if (sideToRecommendSymbolMap.get(sideTypeEnum).contains(symbol)) {
            sideToRecommendSymbolMap.get(sideTypeEnum).remove(symbol);
        }
    }
    public boolean removeRecommend(PeriodTypeEnum periodTypeEnum, SideTypeEnum sideTypeEnum, String symbol) {
        if (periodToSideToRecommendSymbolMap.get(periodTypeEnum).get(sideTypeEnum).contains(symbol)) {
            return periodToSideToRecommendSymbolMap.get(periodTypeEnum).get(sideTypeEnum).remove(symbol);
        }

        return false;
    }

    public void setPeriodCandlestickMap(String symbol, ConcurrentHashMap<PeriodTypeEnum, List<Candlestick>> periodTypeEnumListMap) {
        this.symbolToPeriodToCandlestickMap.put(symbol, periodTypeEnumListMap);
    }

    //    public void addCandlestick(String symbol, PeriodTypeEnum periodTypeEnum, Candlestick addObj) {
    //        if (Objects.isNull(symbolToPeriodToCandlestickMap.get(symbol))) {
    //            ConcurrentHashMap<PeriodTypeEnum, List<Candlestick>> PeriodToCandlestickMap = new ConcurrentHashMap<>();
    //            List<Candlestick> list = new ArrayList<>();
    //            list.add(addObj);
    //
    //            PeriodToCandlestickMap.put(periodTypeEnum, list);
    //            symbolToPeriodToCandlestickMap.put(symbol, PeriodToCandlestickMap);
    //        }else {
    //            if (Objects.isNull(symbolToPeriodToCandlestickMap.get(symbol).get(periodTypeEnum))) {
    //                List<Candlestick> list = new ArrayList<>();
    //                list.add(addObj);
    //                symbolToPeriodToCandlestickMap.get(symbol).put(periodTypeEnum, list);
    //            }else {
    //                List<Candlestick> candlesticks = symbolToPeriodToCandlestickMap.get(symbol).get(periodTypeEnum);
    //                if (!candlesticks.contains(addObj)) {
    //                    candlesticks.add(addObj);
    //                }
    //            }
    //        }
    //    }

    /**
     * 周期切换时，需要修正
     * @param symbol
     * @param price
     */
    public void updateCandlestick(String symbol, BigDecimal price) {
        ConcurrentHashMap<PeriodTypeEnum, List<Candlestick>> periodTypeEnumListConcurrentHashMap = this.symbolToPeriodToCandlestickMap.get(symbol);
        for (Map.Entry<PeriodTypeEnum, List<Candlestick>> entry: periodTypeEnumListConcurrentHashMap.entrySet()) {
            List<Candlestick> candlesticks = entry.getValue();
            candlesticks.get(candlesticks.size() - 1).setClose(price);
        }

    }
}