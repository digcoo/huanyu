//package com.binance.client.examples.backup;
//
//import com.binance.client.enums.PeriodTypeEnum;
//import com.binance.client.enums.SideTypeEnum;
//import com.binance.client.model.market.Candlestick;
//import com.binance.client.utils.MathUtil;
//import com.google.common.collect.Lists;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.tuple.Pair;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Slf4j
//public class ShortChecker extends Checker {
//
//    public ShortChecker(String symbol, PeriodTypeEnum opPeriodType, Map<PeriodTypeEnum, List<Candlestick>> periodTypeCandlesticksMap) {
//        super(symbol, opPeriodType, periodTypeCandlesticksMap);
//    }
//
//    @Override
//    SideTypeEnum getSide() {
//        return SideTypeEnum.SHORT;
//    }
//
//    @Override
//    public boolean checkOverResistanceTrend(PeriodTypeEnum periodTypeEnum) {
//
//        Candlestick candlestick0 = getLastCandlestick(periodTypeEnum, 0);
//        Candlestick candlestick1 = getLastCandlestick(periodTypeEnum, -1);
//        Candlestick candlestick2 = getLastCandlestick(periodTypeEnum, -2);
//
//        if (candlestick0 != null
////                && candlestick0.getClose().compareTo(candlestick0.getOpen()) < 0
//                && candlestick0.getClose().compareTo(MathUtil.max(candlestick1==null?null:candlestick1.getLow(), candlestick2==null?null:candlestick2.getLow())) < 0) {
//            return true;
//        }
//        return false;
//    }
//
//    @Override
//    public boolean checkPreCrossResistanceTrend(PeriodTypeEnum periodTypeEnum) {
//        Candlestick candlestick0 = getLastCandlestick(periodTypeEnum, 0);
//        Candlestick candlestick1 = getLastCandlestick(periodTypeEnum, -1);
//        Candlestick candlestick2 = getLastCandlestick(periodTypeEnum, -2);
//
//        if (candlestick2 != null
//                && candlestick1.getLow().compareTo(candlestick2.getLow()) < 0
//                && candlestick1.getHigh().compareTo(candlestick2.getHigh()) <= 0
//
//                && candlestick0.getClose().compareTo(MathUtil.min(candlestick1.getHigh(), candlestick2.getHigh())) < 0
//
////                && checkOverResistanceTrend(GlobalConstants.OP_PERIOD)
//        ) {
//            return true;
//        }
//        return false;
//    }
//
//    @Override
//    public boolean checkSameDirectionTrend(PeriodTypeEnum periodTypeEnum) {
//        Candlestick candlestick0 = getLastCandlestick(periodTypeEnum, 0);
//        return candlestick0.getClose().compareTo(candlestick0.getOpen()) < 0;
//    }
//
//
//    @Override
//    public boolean checkCrossPeriodTrend(PeriodTypeEnum periodTypeEnum) {
//        Candlestick candlestick0 = getLastCandlestick(periodTypeEnum, 0);
//        Candlestick candlestick1 = getLastCandlestick(periodTypeEnum, -1);
//        Candlestick candlestick2 = getLastCandlestick(periodTypeEnum, -2);
//
//        //本周期反包突破1
//        if (candlestick1 != null
//                && candlestick1.getClose().compareTo(candlestick1.getOpen()) > 0
//                && candlestick0.getClose().compareTo(candlestick1.getHigh()) < 0
//                && candlestick0.getChangeRate().abs().compareTo(periodTypeEnum.getCrossRateThreshold()) > 0
//                && checkOverResistanceTrend(GlobalConstants.FIRST_OP_PERIOD)) {
//            return true;
//        }
//
//        //本周期反包突破2
//        if (candlestick2 != null
//                && candlestick2.getClose().compareTo(candlestick2.getOpen()) > 0
//                && candlestick1.getClose().compareTo(candlestick2.getHigh()) > 0
//                && candlestick0.getClose().compareTo(candlestick2.getHigh()) < 0
//                && candlestick0.getChangeRate().abs().compareTo(periodTypeEnum.getCrossRateThreshold()) > 0
//                && checkOverResistanceTrend(GlobalConstants.FIRST_OP_PERIOD)) {
//            return true;
//        }
//
//        //前1周期反包突破
//        if (candlestick2 != null
//                && candlestick2.getClose().compareTo(candlestick2.getOpen()) > 0
//                && candlestick1.getClose().compareTo(candlestick2.getHigh()) < 0
//                && candlestick1.getChangeRate().abs().compareTo(periodTypeEnum.getCrossRateThreshold()) > 0
//                && checkOverResistanceTrend(GlobalConstants.FIRST_OP_PERIOD)) {
//            return true;
//        }
//
//        return false;
//    }
//
//
//    @Override
//    public Pair<Boolean, String> checkCrossResistanceSignal(PeriodTypeEnum trendPeriodTypeEnum) {
//
//        Candlestick opCandlestick0 = getLastCandlestick(getOpPeriodType(), 0);
//        Candlestick opCandlestick1 = getLastCandlestick(getOpPeriodType(), -1);
//        Candlestick opCandlestick2 = getLastCandlestick(getOpPeriodType(), -2);
//
//        Candlestick trendCandlestick0 = getLastCandlestick(trendPeriodTypeEnum, 0);
//        Candlestick trendCandlestick1 = getLastCandlestick(trendPeriodTypeEnum, -1);
//        Candlestick trendCandlestick2 = getLastCandlestick(trendPeriodTypeEnum, -2);
//
//        Pair<String, BigDecimal> maxLowPair = Pair.of("H1", MathUtil.max(trendCandlestick1==null?null:trendCandlestick1.getLow(), trendCandlestick2==null?null:trendCandlestick2.getLow()));
//        Pair<String, BigDecimal> minLowPair = Pair.of("H2",MathUtil.min(trendCandlestick1==null?null:trendCandlestick1.getLow(), trendCandlestick2==null?null:trendCandlestick2.getLow()));
//
//        List<Pair<String, BigDecimal>> lowResistancePairList = Lists.newArrayList(maxLowPair, minLowPair).stream()
//                .filter(x -> x.getRight() != null)
//                .collect(Collectors.toList());
//
//        for (Pair<String, BigDecimal> resistancePair : lowResistancePairList) {
//            if (resistancePair.getLeft() != null
//
//                    //小周期 突破大周期阻力位
//                    && opCandlestick0.getClose().compareTo(resistancePair.getRight()) < 0
//                    && opCandlestick1.getClose().compareTo(resistancePair.getRight()) >= 0
//
//                    && trendCandlestick0.getClose().compareTo(trendCandlestick0.getOpen()) < 0
//
//                    && checkOverResistanceTrend(getOpPeriodType())) {
//
//                return Pair.of(true, resistancePair.getLeft() + "(" + resistancePair.getRight() + ")");
//            }
//        }
//        return Pair.of(false, "");
//    }
//
//    @Override
//    public Pair<Boolean, String> checkFanBaoCrossResistanceSignal(PeriodTypeEnum trendPeriodTypeEnum) {
//
//        Candlestick opCandlestick0 = getLastCandlestick(getOpPeriodType(), 0);
//        Candlestick opCandlestick1 = getLastCandlestick(getOpPeriodType(), -1);
//        Candlestick opCandlestick2 = getLastCandlestick(getOpPeriodType(), -2);
//
//        Candlestick trendCandlestick0 = getLastCandlestick(trendPeriodTypeEnum, 0);
//        Candlestick trendCandlestick1 = getLastCandlestick(trendPeriodTypeEnum, -1);
//        Candlestick trendCandlestick2 = getLastCandlestick(trendPeriodTypeEnum, -2);
//
//        Pair<String, BigDecimal> maxLowPair = Pair.of("H1", MathUtil.max(trendCandlestick1==null?null:trendCandlestick1.getLow(), trendCandlestick2==null?null:trendCandlestick2.getLow()));
//        Pair<String, BigDecimal> minLowPair = Pair.of("H2",MathUtil.min(trendCandlestick1==null?null:trendCandlestick1.getLow(), trendCandlestick2==null?null:trendCandlestick2.getLow()));
//
//        List<Pair<String, BigDecimal>> lowResistancePairList = Lists.newArrayList(maxLowPair, minLowPair).stream()
//                .filter(x -> x.getRight() != null)
//                .collect(Collectors.toList());
//
//        for (Pair<String, BigDecimal> resistancePair : lowResistancePairList) {
//            if (resistancePair.getLeft() != null
//
//                    //小周期 突破大周期阻力位
//                    && opCandlestick0.getClose().compareTo(resistancePair.getRight()) < 0
//                    && opCandlestick1.getClose().compareTo(resistancePair.getRight()) >= 0
//
//                    && trendCandlestick0.getClose().compareTo(trendCandlestick0.getOpen()) < 0
//
//                    //反包
//                    && opCandlestick1.getClose().compareTo(opCandlestick1.getOpen()) > 0
//
//                    && checkOverResistanceTrend(getOpPeriodType())) {
//
//                return Pair.of(true, resistancePair.getLeft() + "(" + resistancePair.getRight() + ")");
//            }
//        }
//        return Pair.of(false, "");
//    }
//
//
//    @Override
//    public Pair<Boolean, String> checkTurnRoundSignal(PeriodTypeEnum trendPeriodTypeEnum) {
//
//        Candlestick opCandlestick0 = getLastCandlestick(trendPeriodTypeEnum, 0);
//        Candlestick opCandlestick1 = getLastCandlestick(trendPeriodTypeEnum, -1);
//        Candlestick opCandlestick2 = getLastCandlestick(trendPeriodTypeEnum, -2);
//
//        if (opCandlestick2 != null
//                && opCandlestick1.getHigh().compareTo(opCandlestick2.getHigh()) > 0
//
//                && opCandlestick0.getClose().compareTo(MathUtil.max(opCandlestick1.getLow(), opCandlestick2.getLow())) < 0
//
//                && checkOverResistanceTrend(getOpPeriodType())) {
//            return Pair.of(true, "");
//        }
//        return Pair.of(false, "");
//
//    }
//
//
//}
