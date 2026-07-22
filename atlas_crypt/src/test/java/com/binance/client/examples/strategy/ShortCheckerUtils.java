package com.binance.client.examples.strategy;

import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.model.market.Candlestick;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

public final class ShortCheckerUtils {

    /**
     * 梯子特征：
     * 1、较大比率实体，且后一个周期未连续突破：能量蓄积且未释放
     */
    public static List<Candlestick> getKeyPressureList(List<Candlestick> trades, PeriodTypeEnum periodType) {

        List<Candlestick> realTiZiPoints = new ArrayList<>();
        List<Candlestick> tmpTiZiPoints = new ArrayList<>();
        if (CollectionUtils.isEmpty(trades) || trades.size() < 3) {
            return realTiZiPoints;
        }

        Candlestick trade0 = trades.get(trades.size() - 1);

        //上一个周期，不作为梯子
        Candlestick lastTiziPoint = null;
        for (int i = trades.size() - 3; i > 1; i--) {
            Candlestick tmpTrade0 = trades.get(i);

            Candlestick nexTmpTrade1 = trades.get(i + 1);
            Candlestick nexTmpTrade2 = trades.get(i + 2);     //本周期不算

            if (tmpTrade0.getChangeRate().compareTo(periodType.getTiZiShiTiRate().negate()) < 0
                    && tmpTrade0.getCrestShockRate().compareTo(periodType.getTiZiShockRate()) > 0) {
                Candlestick tiZiPoint = tmpTrade0;

                //梯子合并： 当前梯子，与前一个有效的梯子有交叉，则合并
                if (lastTiziPoint != null
                        && tiZiPoint.getClose().compareTo(lastTiziPoint.getShiTiMax()) >= 0
                        && tiZiPoint.getClose().compareTo(lastTiziPoint.getShiTiMin()) >= 0) {
                    tmpTiZiPoints.add(tiZiPoint);
                    continue;
                }

                //梯子合并：后一个已是梯子，当前的价格低于后续梯子的close
                if (tmpTiZiPoints.contains(nexTmpTrade1) && tmpTrade0.getClose().compareTo(nexTmpTrade1.getClose()) > 0) {
                    tmpTiZiPoints.add(tiZiPoint);
                    continue;
                }
                if (tmpTiZiPoints.contains(nexTmpTrade2) && tmpTrade0.getClose().compareTo(nexTmpTrade2.getClose()) > 0) {
                    tmpTiZiPoints.add(tiZiPoint);
                    continue;
                }

                realTiZiPoints.add(tiZiPoint);
                tmpTiZiPoints.add(tiZiPoint);
                lastTiziPoint = tiZiPoint;
            }

        }

        return realTiZiPoints;
    }


    public static Map<PeriodTypeEnum, List<Candlestick>> getKeyPressureMap(List<Candlestick> trendTrades, PeriodTypeEnum trendPeriodType, List<Candlestick> opTrades, PeriodTypeEnum opPeriodType) {
        Candlestick trendCandlestick0 = trendTrades.get(trendTrades.size() - 1);
        Candlestick trendCandlestick1 = trendTrades.size() >= 2? trendTrades.get(trendTrades.size() - 2): null;
        Candlestick trendCandlestick2 = trendTrades.size() >= 3? trendTrades.get(trendTrades.size() - 3): null;

        Map<PeriodTypeEnum, List<Candlestick>> keyPressureMap = new HashMap<>();
        if (trendCandlestick1 != null) {
            keyPressureMap.put(trendPeriodType, List.of(trendCandlestick1));
        }

        Set<Candlestick> tiZiCandlesticks = new HashSet<>();
        Arrays.asList(trendCandlestick1, trendCandlestick2).stream().filter(Objects::nonNull).forEach(trendCandlestick -> {
            //最大成交量
            Candlestick maxAmountCandlestick = null;
            //最大振幅
            Candlestick maxShockCandlestick = null;

            for (int i = 0; i < opTrades.size(); i++) {
                Candlestick opCandlestick = opTrades.get(i);
                if (opCandlestick.getOpenTime() < trendCandlestick.getOpenTime() && opCandlestick.getOpenTime() > trendCandlestick.getCloseTime()) {
                    continue;
                }

                maxAmountCandlestick = maxAmountCandlestick == null || opCandlestick.getQuoteAssetVolume().compareTo(maxAmountCandlestick.getQuoteAssetVolume()) > 0? opCandlestick: maxAmountCandlestick;
                maxShockCandlestick = maxShockCandlestick == null || opCandlestick.getCrestShockRate().compareTo(maxShockCandlestick.getCrestShockRate()) > 0? opCandlestick: maxShockCandlestick;

            }

            if (maxShockCandlestick.getCrestShockRate().compareTo(opPeriodType.getTiZiShockRate()) > 0) {
                tiZiCandlesticks.add(maxShockCandlestick);
            }

            if (maxAmountCandlestick.getCrestShockRate().compareTo(opPeriodType.getTiZiShockRate()) > 0) {
                tiZiCandlesticks.add(maxAmountCandlestick);
            }

            if (!tiZiCandlesticks.isEmpty()) {
                keyPressureMap.put(opPeriodType, new ArrayList<>(tiZiCandlesticks));
            }
        });

        return keyPressureMap;
    }


    public static boolean isBackCrossTiZiMin(Candlestick tiZiTrade, List<Candlestick> opTrades) {
        for (int i = 0; i < opTrades.size(); i++) {
            Candlestick candlestick = opTrades.get(i);
            if (candlestick.getOpenTime() <= tiZiTrade.getOpenTime()) {
                continue;
            }
            if (candlestick.getHigh().compareTo(tiZiTrade.getOpen()) > 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOverTiZiLow(Candlestick tiziTrade, List<Candlestick> opTrades) {
        for (int i = 0; i < opTrades.size(); i++) {
            Candlestick candlestick = opTrades.get(i);
            if (candlestick.getOpenTime() <= tiziTrade.getOpenTime()) {
                continue;
            }
            if (candlestick.getClose().compareTo(tiziTrade.getLow()) < 0) {
                return true;
            }
        }
        return false;
    }
}
