package com.binance.client.examples.backup;//package com.binance.client.examples;
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
//public class ShortChecker3 extends Checker {
//
//    public ShortChecker3(String symbol, Map<PeriodTypeEnum, List<Candlestick>> periodTypeCandlesticksMap) {
//        super(symbol, periodTypeCandlesticksMap);
//    }
//
//    @Override
//    SideTypeEnum getSide() {
//        return SideTypeEnum.SHORT;
//    }
//
//    @Override
//    public boolean checkQuShi(PeriodTypeEnum periodTypeEnum) {
//
//        Candlestick candlestick0 = getLastCandlestick(periodTypeEnum, 0);
//        Candlestick candlestick1 = getLastCandlestick(periodTypeEnum, -1);
//        Candlestick candlestick2 = getLastCandlestick(periodTypeEnum, -2);
//
//        if (candlestick0 != null
//                && candlestick0.getClose().compareTo(candlestick0.getOpen()) < 0
//                && candlestick0.getClose().compareTo(MathUtil.max(candlestick1==null?null:candlestick1.getLow(), candlestick2==null?null:candlestick2.getLow())) < 0) {
//            return true;
//        }
//
//        return false;
//
//    }
//
//    @Override
//    public boolean checkFanBao(PeriodTypeEnum periodTypeEnum) {
//
//        Candlestick candlestick0 = getLastCandlestick(periodTypeEnum, 0);
//        Candlestick candlestick1 = getLastCandlestick(periodTypeEnum, -1);
//
//        if (candlestick0 != null
//                && candlestick0.getClose().compareTo(candlestick1.getHigh()) < 0
//                && candlestick1.getClose().compareTo(candlestick1.getOpen()) > 0) {
//            return true;
//        }
//
//        return false;
//
//    }
//
//    @Override
//    boolean checkMidShunShi(PeriodTypeEnum smallPeriodTypeEnum, PeriodTypeEnum midPeriodTypeEnum, List<PeriodTypeEnum> bigPeriodTypeEnums) {
//
//        Candlestick smallCandlestick0 = getLastCandlestick(smallPeriodTypeEnum, 0);
//
//        if (smallCandlestick0.getChangeRate().abs().compareTo(smallPeriodTypeEnum.getChangeRateThreshold()) > 0
//                && checkQuShi(smallPeriodTypeEnum)
//                && checkQuShi(midPeriodTypeEnum)
//                && bigPeriodTypeEnums.stream().anyMatch(bigPeriodTypeEnum -> checkQuShi(bigPeriodTypeEnum))) {
//
//            return true;
//        }
//
//        return false;
//
//    }
//
//    /**
//     * 突破大周期阻力位or支撑位
//     */
//    @Override
//    public CheckResult checkResistanceTuPo(PeriodTypeEnum smallPeriodTypeEnum, PeriodTypeEnum midPeriodTypeEnum, List<PeriodTypeEnum> bigPeriodTypeEnums) {
//
//        Candlestick smallCandlestick0 = getLastCandlestick(smallPeriodTypeEnum, 0);
//        Candlestick smallCandlestick1 = getLastCandlestick(smallPeriodTypeEnum, -1);
//        Candlestick smallCandlestick2 = getLastCandlestick(smallPeriodTypeEnum, -2);
//
//        Candlestick midCandlestick0 = getLastCandlestick(midPeriodTypeEnum, 0);
//        Candlestick midCandlestick1 = getLastCandlestick(midPeriodTypeEnum, -1);
//        Candlestick midCandlestick2 = getLastCandlestick(midPeriodTypeEnum, -2);
//
//        CheckResult.CheckResultBuilder builder = CheckResult.builder()
//                .symbol(getSymbol())
//                .smallSideTypeEnum(getSide())
//                .smallChangeRate(smallCandlestick0.getChangeRate())
//                .close(smallCandlestick0.getClose())
//                .smallPeriodTypeEnum(smallPeriodTypeEnum)
//                .periodMessage("")
//                .success(false)
//                ;
//
//        Pair<String, BigDecimal> maxLowPair = Pair.of("突破顶部阻力位1", MathUtil.max(midCandlestick1==null?null:midCandlestick1.getLow(), midCandlestick2==null?null:midCandlestick2.getLow()));
//        Pair<String, BigDecimal> minLowPair = Pair.of("突破顶部阻力位2",MathUtil.min(midCandlestick1==null?null:midCandlestick1.getLow(), midCandlestick2==null?null:midCandlestick2.getLow()));
//        Pair<String, BigDecimal> maxHighPair = Pair.of("突破底部支撑位1", MathUtil.max(midCandlestick1==null?null:midCandlestick1.getHigh(), midCandlestick2==null?null:midCandlestick2.getHigh()));
//        Pair<String, BigDecimal> minHighPair = Pair.of("突破底部支撑位2", MathUtil.min(midCandlestick1==null?null:midCandlestick1.getHigh(), midCandlestick2==null?null:midCandlestick2.getHigh()));
//
//        List<Pair<String, BigDecimal>> lowResistancePairList = Lists.newArrayList(maxLowPair, minLowPair).stream()
//                .filter(x -> x.getRight() != null)
//                .collect(Collectors.toList());
//        for (Pair<String, BigDecimal> resistancePair : lowResistancePairList) {
//            if (smallCandlestick0.getChangeRate().compareTo(smallPeriodTypeEnum.getChangeRateThreshold().negate()) < 0
//                    && checkQuShi(smallPeriodTypeEnum)
//                    && checkQuShi(midPeriodTypeEnum)
//                    && bigPeriodTypeEnums.stream().anyMatch(bigPeriodTypeEnum -> checkQuShi(bigPeriodTypeEnum))
//
//                    //小周期 突破大周期阻力位
//                    && smallCandlestick0.getClose().compareTo(resistancePair.getRight()) < 0
//                    && midCandlestick0.getClose().compareTo(midCandlestick0.getOpen()) < 0
//
//                    //首次突破
//                    && smallCandlestick1.getClose().compareTo(resistancePair.getRight()) >= 0
//            ){
//                return builder
//                        .success(true)
//                        .strategyMessage(smallPeriodTypeEnum + resistancePair.getLeft() + "(" + midPeriodTypeEnum + "):" + resistancePair.getRight())
//                        .build()
//                        ;
//            }
//        }
//
//
//        List<Pair<String, BigDecimal>> highResistancePairList = Lists.newArrayList(maxHighPair, minHighPair).stream()
//                .filter(x -> x.getRight() != null)
//                .collect(Collectors.toList());
//        for (Pair<String, BigDecimal> resistancePair : highResistancePairList) {
//            if (smallCandlestick0.getChangeRate().compareTo(smallPeriodTypeEnum.getChangeRateThreshold().negate()) < 0
//                    && checkQuShi(smallPeriodTypeEnum)
//                    && checkQuShi(midPeriodTypeEnum)
//                    && bigPeriodTypeEnums.stream().anyMatch(bigPeriodTypeEnum -> checkQuShi(bigPeriodTypeEnum))
//
//                    //小周期 突破大周期阻力位
//                    && smallCandlestick0.getClose().compareTo(resistancePair.getRight()) < 0
//                    && midCandlestick0.getClose().compareTo(midCandlestick0.getOpen()) < 0
//
//                    //下探或首次突破
//                    && MathUtil.max(smallCandlestick1.getClose(), smallCandlestick0.getHigh()).compareTo(resistancePair.getRight()) >= 0
//
//            ){
//                return builder
//                        .success(true)
//                        .strategyMessage(smallPeriodTypeEnum + resistancePair.getLeft() + "(" + midPeriodTypeEnum + "):" + resistancePair.getRight())
//                        .build()
//                        ;
//            }
//        }
//
//        return builder.build();
//    }
//
//
//    /**
//     * 突破大周期阻力位or支撑位
//     */
//
//    @Override
//    public Pair<Boolean, BigDecimal> checkResistanceTuPo(PeriodTypeEnum smallPeriodTypeEnum, PeriodTypeEnum bigPeriodTypeEnum) {
//
//        Candlestick smallCandlestick0 = getLastCandlestick(smallPeriodTypeEnum, 0);
//        Candlestick smallCandlestick1 = getLastCandlestick(smallPeriodTypeEnum, -1);
//        Candlestick smallCandlestick2 = getLastCandlestick(smallPeriodTypeEnum, -2);
//
//        Candlestick bigCandlestick0 = getLastCandlestick(bigPeriodTypeEnum, 0);
//        Candlestick bigCandlestick1 = getLastCandlestick(bigPeriodTypeEnum, -1);
//        Candlestick bigCandlestick2 = getLastCandlestick(bigPeriodTypeEnum, -2);
//
//        Pair<String, BigDecimal> minHighPair = Pair.of("突破顶1", MathUtil.min(bigCandlestick1==null?null:bigCandlestick1.getHigh(), bigCandlestick2==null?null:bigCandlestick2.getHigh()));
//        Pair<String, BigDecimal> maxHighPair = Pair.of("突破顶2", MathUtil.max(bigCandlestick1==null?null:bigCandlestick1.getHigh(), bigCandlestick2==null?null:bigCandlestick2.getHigh()));
//
//        List<Pair<String, BigDecimal>> highResistancePairList = Lists.newArrayList(minHighPair, maxHighPair).stream()
//                .filter(x -> x.getRight() != null)
//                .collect(Collectors.toList());
//        for (Pair<String, BigDecimal> resistancePair : highResistancePairList) {
//            if (
//                //小周期 突破大周期阻力位
//                smallCandlestick0.getClose().compareTo(resistancePair.getRight()) < 0
//                && smallCandlestick1.getClose().compareTo(resistancePair.getRight()) >= 0
//
//                && bigCandlestick0.getClose().compareTo(bigCandlestick0.getOpen()) < 0
//
//            ){
//
//                return Pair.of(true, resistancePair.getRight());
//            }
//        }
//
//        return Pair.of(false, null);
//    }
//
//
//    /**
//     * 反包突破阻力位or支撑位
//     * @return
//     */
//    @Override
//    CheckResult checkFanBaoTuPo(PeriodTypeEnum smallPeriodTypeEnum, PeriodTypeEnum midPeriodTypeEnum, List<PeriodTypeEnum> bigPeriodTypeEnums) {
//
//        Candlestick smallCandlestick0 = getLastCandlestick(smallPeriodTypeEnum, 0);
//        Candlestick smallCandlestick1 = getLastCandlestick(smallPeriodTypeEnum, -1);
//        Candlestick smallCandlestick2 = getLastCandlestick(smallPeriodTypeEnum, -2);
//
//        Candlestick midCandlestick0 = getLastCandlestick(midPeriodTypeEnum, 0);
//        Candlestick midCandlestick1 = getLastCandlestick(midPeriodTypeEnum, -1);
//        Candlestick midCandlestick2 = getLastCandlestick(midPeriodTypeEnum, -2);
//
//        CheckResult.CheckResultBuilder builder = CheckResult.builder()
//                .symbol(getSymbol())
//                .smallSideTypeEnum(getSide())
//                .smallChangeRate(smallCandlestick0.getChangeRate())
//                .close(smallCandlestick0.getClose())
//                .smallPeriodTypeEnum(smallPeriodTypeEnum)
//                .periodMessage("")
//                .success(false)
//                ;
//
//        Pair<String, BigDecimal> maxLowPair = Pair.of("突破顶部阻力位1", MathUtil.max(midCandlestick1==null?null:midCandlestick1.getLow(), midCandlestick2==null?null:midCandlestick2.getLow()));
//        Pair<String, BigDecimal> minLowPair = Pair.of("突破顶部阻力位2",MathUtil.min(midCandlestick1==null?null:midCandlestick1.getLow(), midCandlestick2==null?null:midCandlestick2.getLow()));
//        Pair<String, BigDecimal> maxHighPair = Pair.of("突破底部支撑位1", MathUtil.max(midCandlestick1==null?null:midCandlestick1.getHigh(), midCandlestick2==null?null:midCandlestick2.getHigh()));
//        Pair<String, BigDecimal> minHighPair = Pair.of("突破底部支撑位2", MathUtil.min(midCandlestick1==null?null:midCandlestick1.getHigh(), midCandlestick2==null?null:midCandlestick2.getHigh()));
//
//        List<Pair<String, BigDecimal>> resistancePairList = Lists.newArrayList(maxLowPair, minLowPair, maxHighPair, minHighPair).stream()
//                .filter(x -> x.getRight() != null)
//                .collect(Collectors.toList());
//
//        for (Pair<String, BigDecimal> resistancePair : resistancePairList) {
//            if (smallCandlestick0.getChangeRate().compareTo(smallPeriodTypeEnum.getChangeRateThreshold().negate()) < 0
//                    && checkQuShi(smallPeriodTypeEnum)
//                    && checkQuShi(midPeriodTypeEnum)
//                    && bigPeriodTypeEnums.stream().anyMatch(bigPeriodTypeEnum -> checkQuShi(bigPeriodTypeEnum))
//
//                    //小周期 突破大周期阻力位
//                    && smallCandlestick0.getClose().compareTo(resistancePair.getRight()) < 0
//                    && midCandlestick0.getClose().compareTo(midCandlestick0.getOpen()) < 0
//
//                    //首次突破
//                    && smallCandlestick1.getClose().compareTo(resistancePair.getRight()) >= 0
//
//                    && smallCandlestick1.getClose().compareTo(smallCandlestick1.getOpen()) > 0
//            ){
//                return builder
//                        .success(true)
//                        .strategyMessage(smallPeriodTypeEnum + resistancePair.getLeft() + "(" + midPeriodTypeEnum + "):" + resistancePair.getRight())
//                        .build()
//                        ;
//            }
//        }
//
//        return builder.build();
//
//    }
//
//
//    /**
//     * 大周期内部：宽幅震荡
//     */
//    @Override
//    public CheckResult checkBottomShock(PeriodTypeEnum smallPeriodTypeEnum, PeriodTypeEnum midPeriodTypeEnum, List<PeriodTypeEnum> bigPeriodTypeEnums) {
//
//        Candlestick smallCandlestick0 = getLastCandlestick(smallPeriodTypeEnum, 0);
//        Candlestick smallCandlestick1 = getLastCandlestick(smallPeriodTypeEnum, -1);
//        Candlestick smallCandlestick2 = getLastCandlestick(smallPeriodTypeEnum, -2);
//
//        Candlestick midCandlestick0 = getLastCandlestick(midPeriodTypeEnum, 0);
//        Candlestick midCandlestick1 = getLastCandlestick(midPeriodTypeEnum, -1);
//        Candlestick midCandlestick2 = getLastCandlestick(midPeriodTypeEnum, -2);
//
//        CheckResult.CheckResultBuilder builder = CheckResult.builder()
//                .symbol(getSymbol())
//                .smallSideTypeEnum(getSide())
//                .smallChangeRate(smallCandlestick0.getChangeRate())
//                .close(smallCandlestick0.getClose())
//                .smallPeriodTypeEnum(smallPeriodTypeEnum)
//                .periodMessage("")
//                .success(false)
//                ;
//
//        BigDecimal minHigh = MathUtil.max(midCandlestick1==null?null:midCandlestick1.getHigh(), midCandlestick2==null?null:midCandlestick2.getHigh());
//        BigDecimal maxLow = MathUtil.max(midCandlestick1==null?null:midCandlestick1.getLow(), midCandlestick2==null?null:midCandlestick2.getLow());
//
//        if (minHigh != null && maxLow != null
//
//                && smallCandlestick0.getChangeRate().compareTo(smallPeriodTypeEnum.getChangeRateThreshold().negate()) < 0
//                && checkQuShi(smallPeriodTypeEnum)
//                && checkQuShi(midPeriodTypeEnum)
//                && bigPeriodTypeEnums.stream().anyMatch(bigPeriodTypeEnum -> checkQuShi(bigPeriodTypeEnum))
//
//                //小周期 在底部震荡
//                && smallCandlestick0.getClose().compareTo(minHigh) < 0
//                && midCandlestick0.getClose().compareTo(midCandlestick0.getOpen()) < 0
//
//                && smallCandlestick0.getLow().compareTo(maxLow) > 0
//                && midCandlestick0.getLow().compareTo(maxLow) > 0
//
//        ){
//            builder
//                    .success(true)
//                    .strategyMessage(smallPeriodTypeEnum + "底部震荡(" + midPeriodTypeEnum + ")-(" + maxLow + ":" + minHigh + ")")
//            ;
//        }
//
//        return builder.build();
//
//    }
//}
