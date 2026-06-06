package com.yh.bigdata.tts.common.indicator;

import com.alibaba.fastjson.JSON;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

@Slf4j
public final class MAIndicatorUtils {

    public static Map<String, Double> calAllMAs(List<Trade> trades, int MA) {
        Map<String, Double> maMap = new HashMap<String, Double>();
        for (int i = trades.size(); i > 0; i--) {
            Trade tmpTrade0 = trades.get(i-1);
            List<Trade> tmpTrades = trades.subList(Math.max(0, i - MA), i);

            double ma = tmpTrades.stream().mapToDouble(Trade::getClose).average().getAsDouble();
            maMap.put(tmpTrade0.getDay(), ma);
        }
        return maMap;
    }

    public static double calLatestMA(List<Trade> trades, int MA) {
        List<Trade> tmpTrades = trades.subList(Math.max(0, trades.size() - MA), trades.size());
        return tmpTrades.stream().mapToDouble(Trade::getClose).average().getAsDouble();
    }

    public static Trade getLatestGoldMA(List<Trade> trades) {
        for (int i = trades.size() - 1; i > 0; i--) {
            Trade tmpTrade0 = trades.get(i);
            Trade tmpTrade1 = trades.get(i - 1);

            if ((tmpTrade0.getMa5() > tmpTrade0.getMa10() && tmpTrade1.getMa5() < tmpTrade1.getMa10())

                    || (tmpTrade0.getMa5() < tmpTrade0.getMa10() && tmpTrade1.getMa5() > tmpTrade1.getMa10())

            ) {
                return tmpTrade0;
            }
        }
        return null;
    }

    public static Trade getLatestRedGoldMA(List<Trade> trades) {
        for (int i = trades.size() - 1; i > 0; i--) {
            Trade tmpTrade0 = trades.get(i);
            Trade tmpTrade1 = trades.get(i - 1);

            if (tmpTrade0.getMa5() > tmpTrade0.getMa10()
                    && tmpTrade1.getMa5() < tmpTrade1.getMa10()) {
                tmpTrade0.setPreTrade(tmpTrade1);
                return tmpTrade0;
            }
        }
        return null;
    }


    public static void calLatestMAAndFill(StockBase stockBase, List trades) {
        try {
            double ma5 = MAIndicatorUtils.calLatestMA(trades, 5);
            double ma10 = MAIndicatorUtils.calLatestMA(trades, 10);
            double ma20 = MAIndicatorUtils.calLatestMA(trades, 20);
            double ma30 = MAIndicatorUtils.calLatestMA(trades, 30);
            Trade trade = (Trade)trades.get(trades.size() - 1);
            trade.setMa5(ma5);
            trade.setMa10(ma10);
            trade.setMa20(ma20);
            trade.setMa30(ma30);

        }catch (Exception e) {
            log.error("MAIndicator calLatestMAAndFill exception, stock: {}", JSON.toJSONString(stockBase), e);
        }
    }

    public static Map<String, Trade> calAllMAsAndFill(StockBase stockBase, List trades) {
        Map<String, Trade> map = new HashMap<>();
        try {
            Map<String, Double> ma5Map = MAIndicatorUtils.calAllMAs(trades, 5);
            Map<String, Double> ma10Map = MAIndicatorUtils.calAllMAs(trades, 10);
            Map<String, Double> ma20Map = MAIndicatorUtils.calAllMAs(trades, 20);
            Map<String, Double> ma30Map = MAIndicatorUtils.calAllMAs(trades, 30);
            Map<String, Double> ma60Map = MAIndicatorUtils.calAllMAs(trades, 60);
            for (int i = 0; i < trades.size(); i++) {
                Trade trade = (Trade)trades.get(i);
                trade.setMa5(ma5Map.get(trade.getDay()));
                trade.setMa10(ma10Map.get(trade.getDay()));
                trade.setMa20(ma20Map.get(trade.getDay()));
                trade.setMa30(ma30Map.get(trade.getDay()));
                trade.setMa60(ma60Map.get(trade.getDay()));
                map.put(trade.getDay(), trade);
            }

        }catch (Exception e) {
            log.error("MAIndicator calAllMas exception, stock: {}", JSON.toJSONString(stockBase), e);
        }

        return map;
    }

    public static List<Double> calMAs(List<Trade> trades, int... MAs) {
        List<Double> maValues = new ArrayList<>(MAs.length);
        for (int ma: MAs) {
            maValues.add(calLatestMA(trades, ma));
        }
        return maValues;
    }

    public static Pair<Double, Double> calTopMAPair(List<Trade> trades, int topMA) {
        List<Double> maValues = calMAs(trades, 5, 10, 20, 30);
        maValues.sort(Comparator.reverseOrder());

        return Pair.of(maValues.get(0), maValues.get(topMA - 1));
    }


}
