package com.yh.bigdata.tts.common.indicator;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.utils.MathUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;


@Slf4j
public final class TiZiIndicatorUtils {

    /**
     * 梯子特征：
     * 1、较大比率实体，且后一个周期未连续突破：能量蓄积且未释放
     */
    public static List<TiZiPoint> getTiZiPoints(List<Trade> trades, PeriodTypeEnum periodType) {

        List<TiZiPoint> realTiZiPoints = new ArrayList<>();
        List<TiZiPoint> tmpTiZiPoints = new ArrayList<>();
        if (CollectionUtils.isEmpty(trades) || trades.size() < 3) {
            return realTiZiPoints;
        }

        Trade trade0 = trades.get(trades.size() - 1);

        //当前位置与梯子之间的最低值
        double betweenLow = trade0.getLow();
        double betweenHigh = trade0.getHigh();

        //上一个周期，不作为梯子
        TiZiPoint lastTiziPoint = null;
        for (int i = trades.size() - 3; i > 1; i--) {
            Trade tmpTrade0 = trades.get(i);

            Trade nexTmpTrade1 = trades.get(i + 1);
            Trade nexTmpTrade2 = trades.get(i + 2);     //本周期不算
            Double nexTmpTrade2Close = nexTmpTrade2.getClose();
            if (i + 3 == trades.size()) {
                nexTmpTrade2Close = nexTmpTrade1.getClose();
            }

            Trade preTmpTrade1 = trades.get(i - 1);
            Trade preTmpTrade2 = trades.get(i -2);
            tmpTrade0.setPreTrade(preTmpTrade1);

            betweenLow = Math.min(betweenLow, preTmpTrade1.getLow());
            betweenHigh = Math.max(betweenHigh, preTmpTrade1.getHigh());


            if (MathUtil.max(tmpTrade0.getShitiRate(), tmpTrade0.getChangeRate()) > periodType.getBandShiTiRate()
                    && tmpTrade0.getShockRate() > periodType.getBandShockRate()) {
                TiZiPoint tiZiPoint = new TiZiPoint(tmpTrade0, periodType, betweenLow, betweenHigh);

                //梯子合并： 当前梯子，与前一个有效的梯子有交叉，则合并
                if (lastTiziPoint != null
                        && tiZiPoint.getTrade().getClose() <= lastTiziPoint.getTrade().getShitiMax()
                        && tiZiPoint.getTrade().getClose() >= lastTiziPoint.getTrade().getShitiMin()) {
                    tmpTiZiPoints.add(tiZiPoint);
                    continue;
                }
                //梯子合并：后一个已是梯子，当前的价格低于后续梯子的close
                if (tmpTiZiPoints.contains(new TiZiPoint(nexTmpTrade1, periodType, betweenLow, betweenHigh)) && tmpTrade0.getClose() < nexTmpTrade1.getClose()) {
                    tmpTiZiPoints.add(tiZiPoint);
                    continue;
                }
                if (tmpTiZiPoints.contains(new TiZiPoint(nexTmpTrade2, periodType, betweenLow, betweenHigh)) && tmpTrade0.getClose() < nexTmpTrade2.getClose()) {
                    tmpTiZiPoints.add(tiZiPoint);
                    continue;
                }

                realTiZiPoints.add(tiZiPoint);
                tmpTiZiPoints.add(tiZiPoint);
                lastTiziPoint = tiZiPoint;
            }

        }

        return realTiZiPoints.size() > 1?List.of(realTiZiPoints.get(0)): realTiZiPoints;
    }

    /**
     * 梯子特征：
     * 1、较大比率实体，且后一个周期未连续突破：能量蓄积且未释放
     */
    public static List<TiZiPoint> getTiZiPointsNoMerge(List<Trade> trades, PeriodTypeEnum periodType) {

        List<TiZiPoint> realTiZiPoints = new ArrayList<>();
        if (CollectionUtils.isEmpty(trades) || trades.size() < 3) {
            return realTiZiPoints;
        }

        Trade trade0 = trades.get(trades.size() - 1);

        //当前位置与梯子之间的最低值
        double betweenLow = trade0.getLow();
        double betweenHigh = trade0.getHigh();

        //上一个周期，不作为梯子
        for (int i = trades.size() - 3; i > 1; i--) {
            Trade tmpTrade0 = trades.get(i);

            Trade preTmpTrade1 = trades.get(i - 1);
            Trade preTmpTrade2 = trades.get(i -2);
            tmpTrade0.setPreTrade(preTmpTrade1);

            betweenLow = Math.min(betweenLow, preTmpTrade1.getLow());
            betweenHigh = Math.max(betweenHigh, preTmpTrade1.getHigh());

            if (tmpTrade0.getShitiRate() > periodType.getBandShiTiRate()
                    && tmpTrade0.getShockRate() > periodType.getBandShockRate()) {
                TiZiPoint tiZiPoint = new TiZiPoint(tmpTrade0, periodType, betweenLow, betweenHigh);

                realTiZiPoints.add(tiZiPoint);
            }

        }

        return realTiZiPoints.size() > 1?List.of(realTiZiPoints.get(0)): realTiZiPoints;
    }


    public static Map<PeriodTypeEnum, List<Trade>> getKeyPressureTradeMapBetween(List<Trade> trendTrades, PeriodTypeEnum trendPeriodType, List<Trade> opTrades, PeriodTypeEnum opPeriodType) {
        Trade trendTrade0 = trendTrades.get(trendTrades.size() - 1);
        Trade trendTrade1 = trendTrades.size() >= 2?trendTrades.get(trendTrades.size() - 2): null;
        Trade trendTrade2 = trendTrades.size() >= 3?trendTrades.get(trendTrades.size() - 3): null;

        Map<PeriodTypeEnum, List<Trade>> keyTiZiMap = new HashMap<>();
        Arrays.asList(trendTrade1, trendTrade2).stream().filter(Objects::nonNull).forEach(trendTrade -> {
            Map<PeriodTypeEnum, List<Trade>> keyTiZiTradeMap = getKeyPressureTradeMapBetween(trendTrade, trendPeriodType, opTrades, opPeriodType);
            keyTiZiTradeMap.entrySet().forEach(x -> {
                keyTiZiMap.computeIfAbsent(x.getKey(), k -> new ArrayList<>()).addAll(x.getValue());
            });
        });

        return keyTiZiMap;
    }

    public static Map<PeriodTypeEnum, List<Trade>> getKeyPressureTradeMapBetween(Trade trendTrade, PeriodTypeEnum trendPeriodType, List<Trade> opTrades, PeriodTypeEnum opPeriodType) {
        Map<PeriodTypeEnum, List<Trade>> keyTiZiMap = new HashMap<>();

        Set<Trade> keyTiZiTrades = new HashSet<>();

        Trade amountTiZiTrade = null;
        Trade shockTiZiTrade = null;
        Trade zaoPanTiZiTrade = null;
        String dayStr = trendTrade.getDay().substring(0, 10);
        for (Trade opTrade: opTrades) {
            if (!opTrade.getDay().startsWith(dayStr)) {
                continue;
            }
            if (opTrade.getDay().contains("10:00")) {
                zaoPanTiZiTrade = opTrade;
            }else {
                amountTiZiTrade = amountTiZiTrade == null || opTrade.getAmount() > amountTiZiTrade.getAmount()? opTrade: amountTiZiTrade;
                shockTiZiTrade = shockTiZiTrade == null || opTrade.getShockRate() > shockTiZiTrade.getAmount()? opTrade: shockTiZiTrade;
            }
        }
        if (MathUtil.max(amountTiZiTrade.getShockRate(), amountTiZiTrade.getChangeRate()) > opPeriodType.getBandShockRate()) {
            keyTiZiTrades.add(amountTiZiTrade);
        }
        if (MathUtil.max(shockTiZiTrade.getShockRate(), shockTiZiTrade.getChangeRate()) > opPeriodType.getBandShockRate()) {
            keyTiZiTrades.add(shockTiZiTrade);
        }
        keyTiZiTrades.add(zaoPanTiZiTrade);

        keyTiZiMap.put(trendPeriodType, List.of(trendTrade));
        keyTiZiMap.put(opPeriodType, new ArrayList<>(keyTiZiTrades));

        return keyTiZiMap;
    }

    public static Pair<Trade, Trade> getZaoPanMin30PressureTrade(List<Trade> dayTrades, List<Trade> min30Trades) {
        Trade trendTrade0 = dayTrades.get(dayTrades.size() - 1);
        Trade trendTrade1 = dayTrades.get(dayTrades.size() - 2);
        Trade trendTrade2 = dayTrades.get(dayTrades.size() - 3);

        Trade zaoPanMin30_1 = null;
        Trade zaoPanMin30_2 = null;
        String day1Str = trendTrade1.getDay().substring(0, 10);
        String day2Str = trendTrade2.getDay().substring(0, 10);
        for (Trade opTrade: min30Trades) {
            if (opTrade.getDay().startsWith(day1Str) && opTrade.getDay().contains("10:00")) {
                zaoPanMin30_1 = opTrade;
            } else if (opTrade.getDay().startsWith(day2Str) && opTrade.getDay().contains("10:00")) {
                zaoPanMin30_2 = opTrade;
            }
        }
        return Pair.of(zaoPanMin30_1, zaoPanMin30_2);
    }


    @Data
    @AllArgsConstructor
    public static class TiZiPoint{

        private Trade trade;

        private PeriodTypeEnum periodType;

        //当前位置与梯子之间最低值
        private double betweenLow;

        //当前位置与梯子之间最高值
        private double betweenHigh;

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            TiZiPoint tiZiPoint = (TiZiPoint) o;
            return Objects.equals(trade.getDay(), tiZiPoint.trade.getDay()) && Objects.equals(trade.getCode(), tiZiPoint.trade.getCode()) && periodType == tiZiPoint.periodType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(trade, periodType);
        }
    }


    public static void main(String[] args) throws IOException {
//        PeriodTypeEnum periodType = PeriodTypeEnum.WEEK;
//        List<Trade> trades = XueQiuUtils.getXueQiuJson("sz301306", periodType.getCode());
//        List<TiZiPoint> tiZiPoints = TiZiIndicatorUtils.getTiZiPoints(trades, periodType);
//
//        for (TiZiPoint tiZiPoint: tiZiPoints) {
//            System.out.println("梯子：" + tiZiPoint.getTrade().getDay());
//        }

        PeriodTypeEnum trendPeriodType = PeriodTypeEnum.DAY;
        PeriodTypeEnum opPeriodType = PeriodTypeEnum.MIN30;

        List<Trade> trendTrades = XueQiuUtils.getXueQiuJson("sh603311", trendPeriodType.getCode());
        List<Trade> opTrades = XueQiuUtils.getXueQiuJson("sh603311", opPeriodType.getCode());
        Map<PeriodTypeEnum, List<Trade>> keyPressureTradeMapBetween = TiZiIndicatorUtils.getKeyPressureTradeMapBetween(trendTrades, trendPeriodType, opTrades, opPeriodType);
        keyPressureTradeMapBetween.entrySet().forEach(x-> {
            for (Trade trade: x.getValue()) {
                log.info("关键阻力位:{}-{}-{}", x.getKey(), trade.getDay(), trade.getHigh());
            }
        });
    }
}
