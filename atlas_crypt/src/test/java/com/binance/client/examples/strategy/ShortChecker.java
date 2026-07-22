package com.binance.client.examples.strategy;

import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.enums.SideTypeEnum;
import com.binance.client.model.market.Candlestick;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;

@Slf4j
public class ShortChecker extends Checker {

    public ShortChecker(String symbol, Map<PeriodTypeEnum
            , List<Candlestick>> periodTypeCandlesticksMap
            , List<PeriodTypeEnum> trendPeriodTypes
            , PeriodTypeEnum opPeriodType
           , int offset) {
        super(symbol, periodTypeCandlesticksMap, trendPeriodTypes, opPeriodType, offset);
    }

    @Override
    SideTypeEnum getSide() {
        return SideTypeEnum.SHORT;
    }

//    @Override
//    boolean checkNoXieSanJiaoRisk(PeriodTypeEnum trendPeriodType) {
//        Candlestick trendCandlestick0 = getLastCandlestick(trendPeriodType, 0);
//        Candlestick trendCandlestick1 = getLastCandlestick(trendPeriodType, -1);
//        Candlestick trendCandlestick2 = getLastCandlestick(trendPeriodType, -2);
//        if (trendCandlestick1 == null) {
//            if (trendCandlestick0.getShiTiRate().signum() < 0) {
//                return true;
//            }
//        } else if (trendCandlestick2 == null) {
//            if (trendCandlestick0.getClose().compareTo(trendCandlestick1.getLow()) < 0) {
//                return true;
//            }
//        }else {
//            if (!(trendCandlestick1.getLow().compareTo(trendCandlestick2.getLow())  > 0 && trendCandlestick0.getLow().compareTo(trendCandlestick1.getLow()) > 0)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private boolean checkOverMACD(PeriodTypeEnum trendPeriodType) {
//        MACDIndicator.MACDPoint macdPoint = MACDIndicator.calculateCurrentMACD(Ticker.from(getLastCandlesticks(trendPeriodType, 100)));
//        return macdPoint.getMacd() < 0;
//    }
//
//    @Override
//    public CheckResponse checkMACDTrend(PeriodTypeEnum trendPeriodType) {
//        Candlestick trendCandlestick0 = getLastCandlestick(trendPeriodType, 0);
//
//        List<MACDIndicator.MACDPoint> macdPoints = MACDIndicator.calculateMACD(Ticker.from(getLastCandlesticks(trendPeriodType, 100)));
//        MACDIndicator.MACDPoint latestRedGoldMACDPoint = MACDIndicator.getLatestRedGoldMACDPoint(macdPoints);
//        MACDIndicator.MACDPoint latestGreenGoldMACDPoint = MACDIndicator.getLatestGreenGoldMACDPoint(macdPoints);
//
//        if (latestRedGoldMACDPoint == null || latestGreenGoldMACDPoint == null) {
//            return CheckResponse.builder()
//                    .success(false)
//                    .periodType(trendPeriodType)
//                    .build();
//        }
//
//        MACDIndicator.MACDPoint macdPoint0 = macdPoints.get(macdPoints.size() - 1);
//        List<Double> resistancePrices = Arrays.asList(latestRedGoldMACDPoint.getTicker().getHigh()
//                        , latestRedGoldMACDPoint.getTicker().getLow()
//                        , latestGreenGoldMACDPoint.getTicker().getHigh()
//                        , latestGreenGoldMACDPoint.getTicker().getLow()).stream()
//                .sorted(Comparator.reverseOrder())
//                .collect(Collectors.toList());
//
//        //跳过L1级别
//        for (int i = 0; i < resistancePrices.size(); i++) {
//            int level = i + 1;
//
//            if (level < 3) {
//                continue;
//            }
//
//            Double price0 = resistancePrices.get(i);
//            Double price1 = i == resistancePrices.size() - 1? null: resistancePrices.get(i + 1);
//
//            if (trendCandlestick0.getClose().doubleValue() <= price0
//                    && (price1 == null || trendCandlestick0.getClose().doubleValue() >= price1)) {
//                return CheckResponse.builder()
//                        .success(true)
//                        .periodType(trendPeriodType)
//                        .message("L" + level)
//                        .build();
//            }
//        }
//
//        return CheckResponse.builder()
//                .success(false)
//                .periodType(trendPeriodType)
//                .ifGoldCross(macdPoint0.isIfGoldCross())
//                .build();
//
//    }
//
//    @Override
//    CheckResponse checkCrossGoldMACD(PeriodTypeEnum opPeriodType) {
//
//        Candlestick opCandlestick0 = getLastCandlestick(opPeriodType, 0);
//        Candlestick opCandlestick1 = getLastCandlestick(opPeriodType, -1);
//
//        List<Ticker> tickers = Ticker.from(getLastCandlesticks(opPeriodType, 100));
//        List<MACDIndicator.MACDPoint> macdPoints = MACDIndicator.calculateMACD(tickers);
//        MACDIndicator.MACDPoint latestRedGoldMACDPoint = MACDIndicator.getLatestRedGoldMACDPoint(macdPoints);
//        MACDIndicator.MACDPoint latestGreenGoldMACDPoint = MACDIndicator.getLatestGreenGoldMACDPoint(macdPoints);
//
//        if (latestRedGoldMACDPoint == null || latestGreenGoldMACDPoint == null) {
//            return CheckResponse.builder()
//                    .success(false)
//                    .periodType(opPeriodType)
//                    .build();
//        }
//
//        List<Double> resistancePrices = Arrays.asList(latestRedGoldMACDPoint.getTicker().getHigh()
//                        , latestRedGoldMACDPoint.getTicker().getLow()
//                        , latestGreenGoldMACDPoint.getTicker().getHigh()
//                        , latestGreenGoldMACDPoint.getTicker().getLow()).stream()
//                .sorted()
//                .collect(Collectors.toList());
//
//        //跳过L1级别
//        for (int i = 1; i < resistancePrices.size(); i++) {
//            int level = i + 1;
//            if (opCandlestick0.getClose().doubleValue() <= resistancePrices.get(i)
//                    && opCandlestick1.getClose().doubleValue() >= resistancePrices.get(i)
//                    && !KDJIndicator.isRise(tickers)) {
//                return CheckResponse.builder()
//                        .success(true)
//                        .periodType(opPeriodType)
//                        .message("突破" + "L" + level)
//                        .build();
//            }
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
//
//    @Override
//    CheckResponse checkDownCrossGoldMACD(PeriodTypeEnum opPeriodType) {
//
//        Candlestick opCandlestick0 = getLastCandlestick(opPeriodType, 0);
//        Candlestick opCandlestick1 = getLastCandlestick(opPeriodType, -1);
//
//        List<Ticker> tickers = Ticker.from(getLastCandlesticks(opPeriodType, 100));
//        List<MACDIndicator.MACDPoint> macdPoints = MACDIndicator.calculateMACD(tickers);
//        MACDIndicator.MACDPoint latestRedGoldMACDPoint = MACDIndicator.getLatestRedGoldMACDPoint(macdPoints);
//        MACDIndicator.MACDPoint latestGreenGoldMACDPoint = MACDIndicator.getLatestGreenGoldMACDPoint(macdPoints);
//
//        if (latestRedGoldMACDPoint == null || latestGreenGoldMACDPoint == null) {
//            return CheckResponse.builder()
//                    .success(false)
//                    .periodType(opPeriodType)
//                    .build();
//        }
//
//        List<Double> resistancePrices = Arrays.asList(latestRedGoldMACDPoint.getTicker().getHigh()
//                        , latestRedGoldMACDPoint.getTicker().getLow()
//                        , latestGreenGoldMACDPoint.getTicker().getHigh()
//                        , latestGreenGoldMACDPoint.getTicker().getLow()).stream()
//                .sorted()
//                .collect(Collectors.toList());
//
//        //跳过L1级别
//        for (int i = 1; i < resistancePrices.size(); i++) {
//            int level = i + 1;
//            if (opCandlestick0.getClose().doubleValue() <= resistancePrices.get(i)
//
//                    //回踩
//                    && opCandlestick0.getHigh().doubleValue() >= resistancePrices.get(i)
//
//                    && opCandlestick1.getClose().doubleValue() <= resistancePrices.get(i)
//            ) {
//                return CheckResponse.builder()
//                        .success(true)
//                        .periodType(opPeriodType)
//                        .message("回踩" + "L" + level)
//                        .build();
//            }
//        }
//
//        return CheckResponse.builder()
//                .success(false)
//                .periodType(opPeriodType)
//                .build();
//
//    }
//
//    @Override
//    CheckResponse checkTurnRoundKDJ(PeriodTypeEnum opPeriodType) {
//        List<KDJIndicator.KDJPoint> kdjPoints = KDJIndicator.calculateKDJ(Ticker.from(getLastCandlesticks(opPeriodType, 100)));
//        KDJIndicator.KDJPoint kdjPoint0 = kdjPoints.get(kdjPoints.size() - 1);
//        KDJIndicator.KDJPoint kdjPoint1 = kdjPoints.get(kdjPoints.size() - 2);
//        KDJIndicator.KDJPoint kdjPoint2 = kdjPoints.get(kdjPoints.size() - 3);
//
//        if (kdjPoint0.getJ() < kdjPoint1.getJ()
//                && kdjPoint1.getJ() > kdjPoint2.getJ()) {
//            return CheckResponse.builder()
//                    .success(true)
//                    .periodType(opPeriodType)
//                    .message("KDJ反转")
//                    .build();
//        }
//
//        return CheckResponse.builder()
//                .success(false)
//                .periodType(opPeriodType)
//                .build();
//    }
//
//
//    @Override
//    public CheckResponse checkExtremeReverse(PeriodTypeEnum opPeriodType) {
//
//        Candlestick opCandlestick0 = getLastCandlestick(opPeriodType, 0);
//        Candlestick opCandlestick1 = getLastCandlestick(opPeriodType, -1);
//        Candlestick opCandlestick2 = getLastCandlestick(opPeriodType, -2);
//
//        List<Ticker> tickers = Ticker.from(getLastCandlesticks(opPeriodType, 100));
//        List<MACDIndicator.MACDPoint> macdPoints = MACDIndicator.calculateMACD(tickers);
//        MACDIndicator.MACDPoint latestRedGoldMACDPoint = MACDIndicator.getLatestRedGoldMACDPoint(macdPoints);
//        MACDIndicator.MACDPoint latestGreenGoldMACDPoint = MACDIndicator.getLatestGreenGoldMACDPoint(macdPoints);
//
//        if (latestRedGoldMACDPoint == null || latestGreenGoldMACDPoint == null) {
//            return CheckResponse.builder()
//                    .success(false)
//                    .periodType(opPeriodType)
//
//                    .build();
//        }
//
//        List<Double> resistancePrices = Arrays.asList(latestRedGoldMACDPoint.getTicker().getHigh()
//                        , latestRedGoldMACDPoint.getTicker().getLow()
//                        , latestGreenGoldMACDPoint.getTicker().getHigh()
//                        , latestGreenGoldMACDPoint.getTicker().getLow()).stream()
//                .sorted(Comparator.reverseOrder())
//                .collect(Collectors.toList());
//
//        for (int i = 1; i < resistancePrices.size() ; i++) {
//            double resistancePrice0 = resistancePrices.get(i);
//            double resistancePrice1 = resistancePrices.get(i + 1);
//
//            int level = i + 1;
//            BigDecimal upSpace = new BigDecimal(resistancePrice1).subtract(opCandlestick0.getClose()).divide(opCandlestick0.getClose(), RoundingMode.FLOOR).abs();
//
//            if (opCandlestick0.getClose().doubleValue() <= resistancePrice0
//
//                    && opCandlestick0.getClose().doubleValue() >= resistancePrice1
//
//                    && upSpace.compareTo(new BigDecimal(0.02)) > 0
//
//            ) {
//                return CheckResponse.builder()
//                        .success(true)
//                        .periodType(opPeriodType)
//                        .message("极限空间" + "L" + level)
//                        .build();
//            }
//        }
//
//        double minResistancePrice = resistancePrices.stream().min(Double::compare).get();
//        if (opCandlestick0.getClose().doubleValue()  <= minResistancePrice
//                && opCandlestick0.getClose().doubleValue() <= MathUtil.min(opCandlestick1.getLow(), opCandlestick2.getLow()).doubleValue()
//        ) {
//            return CheckResponse.builder()
//                    .success(true)
//                    .periodType(opPeriodType)
//                    .message("极限趋势" + "L4")
//                    .build();
//        }
//
//        double maxResistancePrice = resistancePrices.stream().max(Double::compare).get();
//        if (opCandlestick0.getClose().doubleValue() > maxResistancePrice
//                && opCandlestick0.getChangeRate().doubleValue() < MathUtil.min(opCandlestick1.getLow(), opCandlestick2.getLow()).doubleValue()
//        ) {
//            return CheckResponse.builder()
//                    .success(true)
//                    .periodType(opPeriodType)
//                    .message("极限反转" + "L4")
//                    .build();
//        }
//
//
//        return CheckResponse.builder()
//                .success(false)
//                .periodType(opPeriodType)
//                .build();
//    }
//
//    @Override
//    public CheckResponse checkFanBao(PeriodTypeEnum opPeriodType, boolean lastK) {
//
//        Candlestick opCandlestick0 = getLastCandlestick(opPeriodType, 0);
//        Candlestick opCandlestick1 = getLastCandlestick(opPeriodType, 1);
//        if (lastK) {
//            opCandlestick0 = getLastCandlestick(opPeriodType, 1);
//            opCandlestick1 = getLastCandlestick(opPeriodType, 2);
//        }
//
//        if (opCandlestick0.getClose().compareTo(opCandlestick1.getLow()) < 0
//
//                && opCandlestick0.getChangeRate().abs().compareTo(new BigDecimal("0.008")) > 0
//
//                && opCandlestick1.getChangeRate().compareTo(BigDecimal.ZERO) > 0
//        ) {
//            return CheckResponse.builder()
//                    .success(true)
//                    .periodType(opPeriodType)
//                    .message("反包")
//                    .build();
//        }
//
//        return CheckResponse.builder()
//                .success(false)
//                .periodType(opPeriodType)
//                .build();
//    }
//
//
//    @Override
//    public CheckResponse checkBetweenTiZiAndOverMACD(List<PeriodTypeEnum> trendPeriodTypes, PeriodTypeEnum opPeriodType) {
//        PeriodTypeEnum trendPeriodType = trendPeriodTypes.get(0);
//
//        List<Candlestick> lastCandlesticks = getLastCandlesticks(trendPeriodType, 20);
//        List<Candlestick> tiZiList = LongCheckerUtils.getTiZiList(lastCandlesticks, trendPeriodTypes.get(0));
//        if (tiZiList.isEmpty()) {
//            return CheckResponse.builder()
//                    .success(false)
//                    .periodType(opPeriodType)
//                    .build();
//        }
//
//        Candlestick opCandlestick0 = getLastCandlestick(opPeriodType, 0);
//        Candlestick opCandlestick1 = getLastCandlestick(opPeriodType, 1);
//        Candlestick opCandlestick2 = getLastCandlestick(opPeriodType, 2);
//
//        for (int i = tiZiList.size() - 1; i >= 0; i--) {
//            Candlestick tiZiCandlestick = tiZiList.get(i);
//            if (tiZiCandlestick.getShiTiRate().compareTo(new BigDecimal("0.02").negate()) < 0) {
//                if (opCandlestick0.getShiTiRate().compareTo(BigDecimal.ZERO) < 0
//                        && opCandlestick0.getClose().compareTo(tiZiCandlestick.getHigh()) < 0
//
//                        && (opCandlestick0.getClose().compareTo(MathUtil.min(opCandlestick1.getShiTiMin(), opCandlestick2.getShiTiMin())) < 0
//                                || opCandlestick0.getClose().compareTo(MathUtil.max(opCandlestick1.getLow(), opCandlestick2.getLow())) < 0)
//
//                        && checkOverMACD(opPeriodType)
//                ) {
//                    return CheckResponse.builder()
//                            .success(true)
//                            .periodType(trendPeriodType)
//                            .message("TiZi之间[MACD > 0](" + tiZiCandlestick.getOpenTimeStr() + ")")
//                            .build();
//                }
//                break;
//            }
//
//        }
//        return CheckResponse.builder()
//                .success(false)
//                .periodType(opPeriodType)
//                .build();
//    }
//
//
//    @Override
//    public CheckResponse checkCrossTiZiHighSignal(List<PeriodTypeEnum> trendPeriodTypes, PeriodTypeEnum opPeriodType) {
//        PeriodTypeEnum trendPeriodType = trendPeriodTypes.get(0);
//
//        Candlestick trendTrade0 = getLastCandlestick(trendPeriodType, 0);
//        Candlestick trendTrade1 = getLastCandlestick(trendPeriodType, 1);
//        Candlestick trendTrade2 = getLastCandlestick(trendPeriodType, 2);
//
//        Candlestick opTrade0 = getLastCandlestick(opPeriodType, 0);
//        Candlestick opTrade1 = getLastCandlestick(opPeriodType, 1);
//        Candlestick opTrade2 = getLastCandlestick(opPeriodType, 2);
//
//        if (trendTrade2 == null) {
//            return CheckResponse.builder()
//                    .success(false)
//                    .periodType(opPeriodType)
//                    .build();
//        }
//
//        //梯子
//        List<Candlestick> lastCandlesticks = getLastCandlesticks(trendPeriodType, 15);
//        List<Candlestick> tiZiPoints = ShortCheckerUtils.getTiZiList(lastCandlesticks, trendPeriodType);
//
//        if (tiZiPoints.isEmpty()) {
//            return CheckResponse.builder()
//                    .success(false)
//                    .periodType(opPeriodType)
//                    .build();
//        }
//
//        for (Candlestick tiziTrade : tiZiPoints) {
//
//            //底上移
//            if (ShortCheckerUtils.isBackCrossTiZiMin(tiziTrade, lastCandlesticks)) {
//                continue;
//            }
//
//            //区间收盘未突破梯子High
//            if (ShortCheckerUtils.isOverTiZiLow(tiziTrade, lastCandlesticks)) {
//                continue;
//            }
//
//            //突破 梯子low
//            BigDecimal tiziLow = tiziTrade.getLow();
//            if (opTrade1.getClose().compareTo(tiziLow) >= 0
//                    && opTrade0.getClose().compareTo(tiziLow) <= 0
//
//                    && opTrade0.getClose().compareTo(opTrade1.getShiTiMin()) > 0
//                    && opTrade0.getLow().compareTo(opTrade1.getLow()) > 0
//
//                    && opTrade0.getChangeRate().negate().compareTo(trendPeriodType.getCrossTiZiHighRate().negate()) < 0
//
//            ) {
//
//                return CheckResponse.builder()
//                        .success(true)
//                        .periodType(opPeriodType)
//                        .build();
//
//            }
//
//        }
//
//        return CheckResponse.builder()
//                .success(false)
//                .periodType(opPeriodType)
//                .build();
//    }


    @Override
    public CheckResponse checkCrossKeyPressureHighSignal(List<PeriodTypeEnum> trendPeriodTypes, PeriodTypeEnum opPeriodType) {
        PeriodTypeEnum trendPeriodType = trendPeriodTypes.get(0);

        Map<PeriodTypeEnum, List<Candlestick>> keyPressureMap = ShortCheckerUtils.getKeyPressureMap(getLastCandlesticks(trendPeriodType, 5), trendPeriodType, getLastCandlesticks(opPeriodType, 50), opPeriodType);

        Candlestick opTrade0 = getLastCandlestick(opPeriodType, 0);

        if (MapUtils.isEmpty(keyPressureMap)) {
            return CheckResponse.builder()
                    .success(false)
                    .opPeriodType(opPeriodType)
                    .build();
        }
        for (Map.Entry<PeriodTypeEnum, List<Candlestick>> entry: keyPressureMap.entrySet()) {
            List<Candlestick> keyPressureList = entry.getValue();
            for (Candlestick tiZiCandlestick: keyPressureList) {
                if (opTrade0.getHigh().compareTo(tiZiCandlestick.getLow()) >= 0
                        && opTrade0.getClose().compareTo(tiZiCandlestick.getLow()) < 0
//                        && opTrade0.getChangeRate().compareTo(opPeriodType.getCrossTiZiHighRate().negate()) < 0
                        )
                {
                    return CheckResponse.builder()
                            .success(false)
                            .opPeriodType(opPeriodType)
                            .message("突破关键阻力位High(" + ":" + entry.getKey() + ":" + tiZiCandlestick.getOpenTimeStr() + ":" + tiZiCandlestick.getLow() + ":" + opTrade0.getChangeRate() + ")")
                            .build();
                }
            }
        }

        return CheckResponse.builder()
                .success(false)
                .opPeriodType(opPeriodType)
                .build();
    }
//
//
//    @Override
//    public CheckResponse checkShangYiBetweenTiZiSignal(List<PeriodTypeEnum> trendPeriodTypes, PeriodTypeEnum opPeriodType) {
//        PeriodTypeEnum trendPeriodType = trendPeriodTypes.get(0);
//
//        List<Candlestick> lastCandlesticks = getLastCandlesticks(trendPeriodType, 15);
//        List<Candlestick> tiZiList = LongCheckerUtils.getTiZiList(lastCandlesticks, trendPeriodType);
//        if (tiZiList.isEmpty()) {
//            return CheckResponse.builder()
//                    .success(false)
//                    .periodType(opPeriodType)
//                    .build();
//        }
//
//        Candlestick opCandlestick0 = getLastCandlestick(opPeriodType, 0);
//        Candlestick opCandlestick1 = getLastCandlestick(opPeriodType, 1);
//
//        for (Candlestick tiZiCandlestick: tiZiList) {
//            if (opCandlestick0.getShiTiRate().compareTo(BigDecimal.ZERO) < 0
//
//                    && opCandlestick0.getClose().compareTo(tiZiCandlestick.getShiTiMax()) < 0
//                    && opCandlestick0.getClose().compareTo(tiZiCandlestick.getShiTiMin()) > 0
//
//                    && opCandlestick0.getClose().compareTo(opCandlestick1.getShiTiMin()) < 0
//                    && opCandlestick0.getLow().compareTo(opCandlestick1.getLow()) < 0
//            ) {
//                return CheckResponse.builder()
//                        .success(true)
//                        .periodType(trendPeriodType)
//                        .message("震荡下移(梯子区间)(" + "[" + tiZiCandlestick.getClose()  + "]" + tiZiCandlestick.getOpenTimeStr() + ")")
//                        .build();
//            }
//        }
//        return CheckResponse.builder()
//                .success(false)
//                .periodType(opPeriodType)
//                .build();
//    }

}
