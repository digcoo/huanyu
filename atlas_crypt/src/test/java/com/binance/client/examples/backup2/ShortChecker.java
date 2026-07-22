//package com.binance.client.examples.backup2;
//
//import com.binance.client.enums.PeriodTypeEnum;
//import com.binance.client.enums.SideTypeEnum;
//import com.binance.client.model.market.Candlestick;
//import com.binance.client.utils.MathUtil;
//import com.binance.client.utils.indicator.MACDIndicator;
//import com.binance.client.utils.indicator.Ticker;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections.CollectionUtils;
//import org.apache.commons.lang3.tuple.Pair;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.Map;
//
//@Slf4j
//public class ShortChecker extends Checker {
//
//    public ShortChecker(String symbol, Map<PeriodTypeEnum, List<Candlestick>> periodTypeCandlesticksMap) {
//        super(symbol, periodTypeCandlesticksMap);
//    }
//
//    @Override
//    SideTypeEnum getSide() {
//        return SideTypeEnum.SHORT;
//    }
//
//    public Pair<Boolean, String> checkFanBaoSignal(PeriodTypeEnum opPeriodType) {
//        Candlestick opCandlestick0 = getLastCandlestick(opPeriodType, 0);
//        Candlestick opCandlestick1 = getLastCandlestick(opPeriodType, -1);
//        Candlestick opCandlestick2 = getLastCandlestick(opPeriodType, -2);
//
//        BigDecimal minLow = MathUtil.min(opCandlestick1 == null ? null : opCandlestick1.getLow(), opCandlestick2 == null ? null : opCandlestick2.getLow());
//
//        if (opCandlestick2 != null
//                && opCandlestick1.getClose().compareTo(opCandlestick1.getOpen()) > 0
//                && opCandlestick0.getClose().compareTo(minLow) < 0) {
//            return Pair.of(true, "(" + minLow + ")");
//        }
//
//        return Pair.of(false, "");
//    }
//
//
//    public Pair<Boolean, String> checkTurnRound(PeriodTypeEnum opPeriodType) {
//
//        Candlestick opCandlestick0 = getLastCandlestick(opPeriodType, 0);
//        Candlestick opCandlestick1 = getLastCandlestick(opPeriodType, -1);
//        Candlestick opCandlestick2 = getLastCandlestick(opPeriodType, -2);
//
//        BigDecimal minLow = MathUtil.min(opCandlestick1 == null ? null : opCandlestick1.getLow(), opCandlestick2 == null ? null : opCandlestick2.getLow());
//
//        if (opCandlestick2 != null
//                && opCandlestick1.getLow().compareTo(opCandlestick2.getLow()) > 0
//                && opCandlestick0.getClose().compareTo(minLow) < 0) {
//            return Pair.of(true, "(" + minLow + ")");
//        }
//
//        return Pair.of(false, "");
//    }
//
//    @Override
//    public CheckResponse checkCrossKeyResistanceSignal(PeriodTypeEnum keyPeriodType, PeriodTypeEnum opPeriodType) {
//
//        Candlestick opCandlestick0 = getLastCandlestick(opPeriodType, 0);
//        Candlestick opCandlestick1 = getLastCandlestick(opPeriodType, -1);
//        Candlestick opCandlestick2 = getLastCandlestick(opPeriodType, -2);
//
//        Candlestick keyCandlestick0 = getLastCandlestick(keyPeriodType, 0);
//        Candlestick keyCandlestick1 = getLastCandlestick(keyPeriodType, -1);
//        Candlestick keyCandlestick2 = getLastCandlestick(keyPeriodType, -2);
//
//        BigDecimal maxLow = MathUtil.max(keyCandlestick1 == null ? null : keyCandlestick1.getLow(), keyCandlestick2 == null ? null : keyCandlestick2.getLow());
//        BigDecimal minLow = MathUtil.min(keyCandlestick1 == null ? null : keyCandlestick1.getLow(), keyCandlestick2 == null ? null : keyCandlestick2.getLow());
//
//        BigDecimal maxLowWindowValue = maxLow == null? null : BigDecimal.ONE.subtract(keyPeriodType.getCrossRateThreshold()).multiply(maxLow);
//        BigDecimal minLowWindowValue = minLow == null? null : BigDecimal.ONE.subtract(keyPeriodType.getCrossRateThreshold()).multiply(minLow);
//
//
//        if (maxLow != null
//                && opCandlestick0.getClose().compareTo(maxLow) < 0
//                && opCandlestick1.getClose().compareTo(maxLow) >= 0) {
//
//            return CheckResponse.builder()
//                    .success(true)
//                    .message("(" + maxLow + ")")
//                    .periodType(keyPeriodType)
//                    .build();
//        }
//
//        if (minLow != null
//                && opCandlestick0.getClose().compareTo(minLow) < 0
//                && opCandlestick1.getClose().compareTo(minLow) >= 0) {
//
//            return CheckResponse.builder()
//                    .success(true)
//                    .message("(" + minLow + ")")
//                    .periodType(keyPeriodType)
//                    .build();
//        }
//
//        return CheckResponse.builder()
//                .success(false)
//                .periodType(keyPeriodType)
//                .build();
//    }
//
//    @Override
//    public boolean checkOverLowTrend(PeriodTypeEnum opPeriodType) {
//
//        Candlestick opCandlestick0 = getLastCandlestick(opPeriodType, 0);
//        Candlestick opCandlestick1 = getLastCandlestick(opPeriodType, -1);
//        if (opCandlestick1 != null) {
//            if (opCandlestick0.getClose().compareTo(opCandlestick1.getHigh()) > 0) {
//                return false;
//            }
//        }
//
//        return true;
//    }
//
//
//    @Override
//    public CheckResponse checkOverMACDTrend(PeriodTypeEnum trendPeriodType) {
//        Candlestick trendCandlestick0 = getLastCandlestick(trendPeriodType, 0);
//        Candlestick trendCandlestick1 = getLastCandlestick(trendPeriodType, -1);
//
//        if (trendCandlestick1 != null) {
//            if (trendCandlestick0.getClose().compareTo(trendCandlestick1.getHigh()) > 0) {
//                return CheckResponse.builder()
//                        .success(false)
//                        .periodType(trendPeriodType)
//                        .build();
//            }
//        }
//
//        //MACD趋势
//        List<Candlestick> lastCandlesticks = getLastCandlesticks(trendPeriodType, 100);
//        List<MACDIndicator.MACDPoint> macdPoints = MACDIndicator.calculateMACD(Ticker.from(lastCandlesticks));
//        if (CollectionUtils.isEmpty(macdPoints)) {
//            return CheckResponse.builder()
//                    .success(false)
//                    .periodType(trendPeriodType)
//                    .build();
//        }
//
//        MACDIndicator.MACDPoint latestGoldMACDPoint = MACDIndicator.getLatestGoldMACDPoint(macdPoints);
//        if (latestGoldMACDPoint == null) {
//            log.error("latestGoldMACDPoint is null");
//            return CheckResponse.builder()
//                    .success(false)
//                    .periodType(trendPeriodType)
//                    .build();
//        }
//
//        MACDIndicator.MACDPoint MACDPoint0 = macdPoints.get(macdPoints.size() - 1);
//        if (MACDPoint0.getMacd() < 0) {
//            if (trendCandlestick0.getClose().doubleValue() < latestGoldMACDPoint.getTicker().getLow()) {
//                String tickerMessage = "(" + latestGoldMACDPoint.getTicker().getTimestampStr() + ":" + latestGoldMACDPoint.getTicker().getLow() + ")";
//                return CheckResponse.builder()
//                        .success(true)
//                        .periodType(trendPeriodType)
////                        .message("MACD水【上】,金叉-箱顶【上】" + "-" + tickerMessage)
//                        .message("M【上】,G【顶上】")
//                        .ifGoldCross(MACDPoint0.isIfGoldCross())
//                        .build();
//            }
//
//            if (trendCandlestick0.getClose().doubleValue() < latestGoldMACDPoint.getTicker().getHigh()) {
//                String tickerMessage = "(" + latestGoldMACDPoint.getTicker().getTimestampStr() + ":" + latestGoldMACDPoint.getTicker().getHigh() + ")";
//                return CheckResponse.builder()
//                        .success(true)
//                        .periodType(trendPeriodType)
////                        .message("MACD水【上】,金叉-箱底【上】" + "-" + tickerMessage)
//                        .message("M【上】,G【底上】")
//                        .ifGoldCross(MACDPoint0.isIfGoldCross())
//                        .build();
//            }
//
//        }else{
//            if (trendCandlestick0.getClose().doubleValue() < latestGoldMACDPoint.getTicker().getLow()) {
//                String tickerMessage = "(" + latestGoldMACDPoint.getTicker().getTimestampStr() + ":" + latestGoldMACDPoint.getTicker().getLow() + ")";
//                return CheckResponse.builder()
//                        .success(true)
//                        .periodType(trendPeriodType)
////                        .message("MACD水【下】,金叉-箱顶【上】" + "-" + tickerMessage)
//                        .message("M【下】,G【顶上】")
//                        .ifGoldCross(MACDPoint0.isIfGoldCross())
//                        .build();
//            }
//        }
//
//        return CheckResponse.builder()
//                .success(false)
//                .periodType(trendPeriodType)
//                .ifGoldCross(MACDPoint0.isIfGoldCross())
//                .build();
//    }
//
//    @Override
//    public CheckResponse checkConcaveCrossSignal(PeriodTypeEnum opPeriodType) {
//
//        Candlestick opCandlestick0 = getLastCandlestick(opPeriodType, 0);
//        Candlestick opCandlestick1 = getLastCandlestick(opPeriodType, -1);
//        Candlestick opCandlestick2 = getLastCandlestick(opPeriodType, -2);
//        Candlestick opCandlestick3 = getLastCandlestick(opPeriodType, -3);
//
//        //凹1：今日凹突破
//        if (opCandlestick2 != null
//                && opCandlestick1.getLow().compareTo(opCandlestick2.getLow()) >= 0
//                && opCandlestick0.getLow().compareTo(opCandlestick1.getLow()) < 0
//                && opCandlestick0.getClose().compareTo(opCandlestick0.getOpen()) < 0
//        ) {
//            return CheckResponse.builder()
//                    .success(true)
//                    .periodType(opPeriodType)
//                    .message("今凹"+opCandlestick1.getLow())
//                    .build();
//        }
//
//        //凹3
//        if (opCandlestick3 != null
//
//                //今日
//                && opCandlestick0.getClose().compareTo(MathUtil.max(opCandlestick1.getLow(), opCandlestick2.getLow())) < 0
//
//                && opCandlestick1.getClose().compareTo(MathUtil.max(opCandlestick2.getLow(), opCandlestick3.getLow())) >= 0
//
//        ) {
//            return CheckResponse.builder()
//                    .success(true)
//                    .periodType(opPeriodType)
//                    .message("凹2"+MathUtil.max(opCandlestick1.getLow(), opCandlestick2.getLow()))
//                    .build();
//        }
//
//        return CheckResponse.builder()
//                .success(false)
//                .periodType(opPeriodType)
//                .build();
//
//    }
//
//
//    @Override
//    public CheckResponse checkCrossGoldSignal(PeriodTypeEnum trendPeriodType, PeriodTypeEnum opPeriodType) {
//
//        Candlestick opCandlestick0 = getLastCandlestick(opPeriodType, 0);
//        Candlestick opCandlestick1 = getLastCandlestick(opPeriodType, -1);
//        Candlestick opCandlestick2 = getLastCandlestick(opPeriodType, -2);
//
//        //MACD黄金交叉位
//        List<Candlestick> lastCandlesticks = getLastCandlesticks(trendPeriodType, 100);
//        lastCandlesticks = lastCandlesticks.subList(0, lastCandlesticks.size() - 1);
//        List<MACDIndicator.MACDPoint> macdPoints = MACDIndicator.calculateMACD(Ticker.from(lastCandlesticks));
//
//
//        MACDIndicator.MACDPoint latestGoldMACDPoint = MACDIndicator.getLatestGoldMACDPoint(macdPoints);
//        if (latestGoldMACDPoint != null) {
//            //箱顶
//            if (opCandlestick0.getClose().doubleValue() < latestGoldMACDPoint.getTicker().getLow()
////                    && opCandlestick0.getClose().compareTo(opCandlestick0.getOpen()) < 0
//                    && MathUtil.max(opCandlestick1.getClose(), opCandlestick0.getHigh()).doubleValue() > latestGoldMACDPoint.getTicker().getLow()
//            ) {
//                String tickerMessage = "(" + latestGoldMACDPoint.getTicker().getTimestampStr() + ":" + latestGoldMACDPoint.getTicker().getLow() + ")";
//                return CheckResponse.builder()
//                        .success(true)
//                        .periodType(trendPeriodType)
//                        .message("M黄金-顶" +  tickerMessage)
//                        .build();
//            }
//
//            if (opCandlestick0.getClose().doubleValue() < latestGoldMACDPoint.getTicker().getHigh()
////                    && opCandlestick0.getClose().compareTo(opCandlestick0.getOpen()) < 0
//                    && MathUtil.max(opCandlestick1.getClose(), opCandlestick0.getHigh()).doubleValue() > latestGoldMACDPoint.getTicker().getHigh()
//            ) {
//                String tickerMessage = "(" + latestGoldMACDPoint.getTicker().getTimestampStr() + ":" + latestGoldMACDPoint.getTicker().getHigh() + ")";
//                return CheckResponse.builder()
//                        .success(true)
//                        .periodType(trendPeriodType)
//                        .message("M黄金-底" +  tickerMessage)
//                        .build();
//            }
//        }
//
//
////        MACDIndicator.MACDPoint latestRedGoldMACDPoint = MACDIndicator.getLatestRedGoldMACDPoint(macdPoints);
////        if (latestRedGoldMACDPoint != null) {
////            //箱顶
////            if (opCandlestick0.getClose().doubleValue() < latestRedGoldMACDPoint.getTicker().getLow()
////                    && opCandlestick0.getHigh().doubleValue() > latestRedGoldMACDPoint.getTicker().getLow()
////            ) {
////                String tickerMessage = "(" + latestRedGoldMACDPoint.getTicker().getTimestampStr() + ":" + latestRedGoldMACDPoint.getTicker().getLow() + ")";
////                return CheckResponse.builder()
////                        .success(true)
////                        .periodType(trendPeriodType)
////                        .message("MACD红黄金--箱顶" +  tickerMessage)
////                        .build();
////            }
////
////            if (opCandlestick0.getClose().doubleValue() < latestRedGoldMACDPoint.getTicker().getHigh()
////                    && opCandlestick0.getHigh().doubleValue() > latestRedGoldMACDPoint.getTicker().getHigh()
////            ) {
////                String tickerMessage = "(" + latestRedGoldMACDPoint.getTicker().getTimestampStr() + ":" + latestRedGoldMACDPoint.getTicker().getHigh() + ")";
////                return CheckResponse.builder()
////                        .success(true)
////                        .periodType(trendPeriodType)
////                        .message("MACD红黄金--箱底" +  tickerMessage)
////                        .build();
////            }
////        }
////
////        MACDIndicator.MACDPoint latestGreenGoldMACDPoint = MACDIndicator.getLatestGreenGoldMACDPoint(macdPoints);
////        if (latestGreenGoldMACDPoint != null) {
////            //箱顶
////            if (opCandlestick0.getClose().doubleValue() < latestGreenGoldMACDPoint.getTicker().getLow()
////                    && opCandlestick0.getHigh().doubleValue() > latestGreenGoldMACDPoint.getTicker().getLow()
////            ) {
////                String tickerMessage = "(" + latestGreenGoldMACDPoint.getTicker().getTimestampStr() + ":" + latestGreenGoldMACDPoint.getTicker().getLow() + ")";
////                return CheckResponse.builder()
////                        .success(true)
////                        .periodType(trendPeriodType)
////                        .message("MACD绿黄金--箱顶" +  tickerMessage)
////                        .build();
////            }
////
////            if (opCandlestick0.getClose().doubleValue() < latestGreenGoldMACDPoint.getTicker().getHigh()
////                    && opCandlestick0.getHigh().doubleValue() > latestGreenGoldMACDPoint.getTicker().getHigh()
////            ) {
////                String tickerMessage = "(" + latestGreenGoldMACDPoint.getTicker().getTimestampStr() + ":" + latestGreenGoldMACDPoint.getTicker().getHigh() + ")";
////                return CheckResponse.builder()
////                        .success(true)
////                        .periodType(trendPeriodType)
////                        .message("MACD绿黄金--箱底" +  tickerMessage)
////                        .build();
////            }
////        }
//
//        return CheckResponse.builder()
//                .success(false)
//                .periodType(trendPeriodType)
//                .build();
//    }
//
//}
