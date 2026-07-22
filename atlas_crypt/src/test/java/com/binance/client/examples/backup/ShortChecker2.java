package com.binance.client.examples.backup;//package com.binance.client.examples;
//
//import com.binance.client.enums.PeriodTypeEnum;
//import com.binance.client.model.market.Candlestick;
//import com.binance.client.utils.MathUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.tuple.Pair;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//
//@Slf4j
//public class ShortChecker2 extends Checker {
//
//    String checkMessage = "";
//    String changeRateMessage = "";
//
//    public ShortChecker2(String symbol, Map<PeriodTypeEnum, List<Candlestick>> periodTypeCandlesticksMap) {
//        super(symbol, periodTypeCandlesticksMap);
//    }
//
//    @Override
//    String getCheckMessage() {
//        return checkMessage;
//    }
//
//    @Override
//    String getChangeRateMessage() {
//        return changeRateMessage;
//    }
//
//    public boolean check() {
//        try {
//
////            if ("TSTUSDT".equals(getSymbol())){
////                System.out.println("TSTUSDT");
////            }
//            BigDecimal threshold = new BigDecimal(-0.007);
//            Candlestick min30Candlestick0 = getLastCandlestick(PeriodTypeEnum.MIN30, 0);
//            Candlestick min30Candlestick1 = getLastCandlestick(PeriodTypeEnum.MIN30, -1);
//            Candlestick min30Candlestick2 = getLastCandlestick(PeriodTypeEnum.MIN30, -1);
//            if (!checkChangeRate(threshold, min30Candlestick0, min30Candlestick1, min30Candlestick2)) {
//                return false;
//            }
//
//            Candlestick dayCandlestick0 = getLastCandlestick(PeriodTypeEnum.DAY1, 0);
//            Candlestick dayCandlestick1 = getLastCandlestick(PeriodTypeEnum.DAY1, -1);
//            Candlestick weekCandlestick0 = getLastCandlestick(PeriodTypeEnum.WEEK, 0);
//            Candlestick weekCandlestick1 = getLastCandlestick(PeriodTypeEnum.WEEK, -1);
//
//            String messageTuPo = "";
//            Pair<Boolean, BigDecimal> weekTuPo = checkTuPo(PeriodTypeEnum.WEEK);
//            Pair<Boolean, BigDecimal> day3TuPo = checkTuPo(PeriodTypeEnum.DAY3);
//            Pair<Boolean, BigDecimal> day1TuPo = checkTuPo(PeriodTypeEnum.DAY1);
//            Pair<Boolean, BigDecimal> hour12TuPo = checkTuPo(PeriodTypeEnum.HOUR12);
//            Pair<Boolean, BigDecimal> hour8TuPo = checkTuPo(PeriodTypeEnum.HOUR8);
//            Pair<Boolean, BigDecimal> hour6TuPo = checkTuPo(PeriodTypeEnum.HOUR6);
//            Pair<Boolean, BigDecimal> hour4TuPo = checkTuPo(PeriodTypeEnum.HOUR4);
//            Pair<Boolean, BigDecimal> hour2TuPo = checkTuPo(PeriodTypeEnum.HOUR2);
//            Pair<Boolean, BigDecimal> hour1TuPo = checkTuPo(PeriodTypeEnum.HOUR1);
//            Pair<Boolean, BigDecimal> min30TuPo = checkTuPo(PeriodTypeEnum.MIN30);
//            if (weekTuPo.getLeft()) {
//                messageTuPo = "周突破(" + weekTuPo.getRight()+")";
//            }else if (day3TuPo.getLeft()) {
//                messageTuPo = "3日突破(" + day3TuPo.getRight()+")";
//            }else if (day1TuPo.getLeft()) {
//                messageTuPo = "1日突破(" + day1TuPo.getRight()+")";
//            }else if (hour12TuPo.getLeft()) {
//                messageTuPo = "12小时突破(" + hour12TuPo.getRight()+")";
//            }else if (hour8TuPo.getLeft()) {
//                messageTuPo = "8小时突破(" + hour8TuPo.getRight()+")";
//            }else if (hour6TuPo.getLeft()) {
//                messageTuPo = "6小时突破(" + hour6TuPo.getRight()+")";
//            }else if (hour4TuPo.getLeft()) {
//                messageTuPo = "4小时突破(" + hour4TuPo.getRight()+")";
//            }else if (hour2TuPo.getLeft()) {
//                messageTuPo = "2小时突破(" + hour2TuPo.getRight()+")";
//            }else if (hour1TuPo.getLeft()) {
//                messageTuPo = "1小时突破(" + hour1TuPo.getRight()+")";
//            }else if (min30TuPo.getLeft()) {
//                messageTuPo = "30分钟突破(" + min30TuPo.getRight()+")";
//            }
//
//            boolean WeekBid = (checkQuShi(PeriodTypeEnum.WEEK) || checkQuShi(PeriodTypeEnum.DAY3))
//                    && (checkQuShi(PeriodTypeEnum.DAY1) || checkQuShi(PeriodTypeEnum.HOUR12))
//                    && (checkQuShi(PeriodTypeEnum.HOUR6) || checkQuShi(PeriodTypeEnum.HOUR4))
//                    && checkQuShi(PeriodTypeEnum.HOUR1)
//                    ;
//            if (WeekBid && !("".equals(messageTuPo))) {
//                BigDecimal min30ChangeRate = getLastCandlestick(PeriodTypeEnum.MIN30, 0).getChangeRate().multiply(new BigDecimal(100)).setScale(3, RoundingMode.HALF_DOWN);
//                BigDecimal targetChangeRate = getLastCandlestick(PeriodTypeEnum.WEEK, 0).getChangeRate().multiply(new BigDecimal(100)).setScale(3, RoundingMode.HALF_DOWN);
//                checkMessage = "WEEK(长线)" + ":" + messageTuPo;
//                changeRateMessage = min30ChangeRate + "%:" + targetChangeRate + "%";
//                return true;
//            }
//
//            boolean DayBid = (checkQuShi(PeriodTypeEnum.DAY1) || checkQuShi(PeriodTypeEnum.HOUR12))
//                    && (checkQuShi(PeriodTypeEnum.HOUR6) || checkQuShi(PeriodTypeEnum.HOUR4))
//                    && checkQuShi(PeriodTypeEnum.HOUR1)
//
//                    && weekCandlestick0.getClose().compareTo(weekCandlestick0.getOpen()) < 0
//                    && weekCandlestick0.getClose().compareTo(weekCandlestick1.getLow()) < 0
//                    ;
//
//            if (DayBid && !("".equals(messageTuPo))) {
//                BigDecimal min30ChangeRate = getLastCandlestick(PeriodTypeEnum.MIN30, 0).getChangeRate().multiply(new BigDecimal(100)).setScale(3, RoundingMode.HALF_DOWN);
//                BigDecimal targetChangeRate = getLastCandlestick(PeriodTypeEnum.DAY1, 0).getChangeRate().multiply(new BigDecimal(100)).setScale(3, RoundingMode.HALF_DOWN);
//                checkMessage = "DAY(中线)" + ":" + messageTuPo;
//                changeRateMessage = min30ChangeRate + "%:" + targetChangeRate + "%";
//                return true;
//            }
//
//            boolean Hour8Bid = (checkQuShi(PeriodTypeEnum.HOUR6) || checkQuShi(PeriodTypeEnum.HOUR8))
//                    && checkQuShi(PeriodTypeEnum.HOUR2)
//                    && checkQuShi(PeriodTypeEnum.MIN30)
//
//                    && dayCandlestick0.getClose().compareTo(dayCandlestick0.getOpen()) < 0
//                    && dayCandlestick0.getClose().compareTo(dayCandlestick0.getLow()) < 0
//                    ;
//
//            if (Hour8Bid && !("".equals(messageTuPo))) {
//                BigDecimal min30ChangeRate = getLastCandlestick(PeriodTypeEnum.MIN30, 0).getChangeRate().multiply(new BigDecimal(100)).setScale(3, RoundingMode.HALF_DOWN);
//                BigDecimal targetChangeRate = getLastCandlestick(PeriodTypeEnum.HOUR8, 0).getChangeRate().multiply(new BigDecimal(100)).setScale(3, RoundingMode.HALF_DOWN);
//                checkMessage = "Hour8(短线)" + ":" + messageTuPo;
//                changeRateMessage = min30ChangeRate + "%:" + targetChangeRate + "%";
//
//                return true;
//            }
//
//            return false;
//
//        } catch (Exception ex) {
//            log.error("{} exception...symbol={}",this.getClass().getName(), getSymbol(), ex);
//            ex.printStackTrace();
//        }
//
//        return false;
//    }
//
//    private boolean checkQuShi(List<PeriodTypeEnum> periodTypeEnums) {
//        return periodTypeEnums.stream().allMatch(x -> checkQuShi(x));
//    }
//
//
//    private boolean checkQuShi(PeriodTypeEnum periodTypeEnum) {
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
//    }
//
//    private Pair<Boolean, BigDecimal> checkTuPo(PeriodTypeEnum periodTypeEnum) {
//
//        Candlestick min30Candlestick0 = getLastCandlestick(PeriodTypeEnum.MIN30, 0);
//        Candlestick min30Candlestick1 = getLastCandlestick(PeriodTypeEnum.MIN30, -1);
//
//        Candlestick candlestick0 = getLastCandlestick(periodTypeEnum, 0);
//        Candlestick candlestick1 = getLastCandlestick(periodTypeEnum, -1);
//        Candlestick candlestick2 = getLastCandlestick(periodTypeEnum, -2);
//
//        BigDecimal maxLow = MathUtil.max(candlestick1==null?null:candlestick1.getLow(), candlestick2==null?null:candlestick2.getLow());
//        BigDecimal minLow = MathUtil.min(candlestick1==null?null:candlestick1.getLow(), candlestick2==null?null:candlestick2.getLow());
//
//        if (maxLow != null
//                && candlestick0.getClose().compareTo(candlestick0.getOpen()) < 0
//                && candlestick0.getClose().compareTo(maxLow) < 0
//
//                && min30Candlestick1.getClose().compareTo(maxLow) >= 0
//                && min30Candlestick0.getClose().compareTo(min30Candlestick0.getOpen()) < 0) {
//
//            return Pair.of(true, maxLow);
//        }
//
//        if (minLow != null
//                && candlestick0.getClose().compareTo(candlestick0.getOpen()) < 0
//                && candlestick0.getClose().compareTo(minLow) < 0
//
//                && min30Candlestick1.getClose().compareTo(minLow) >= 0
//                && min30Candlestick0.getClose().compareTo(min30Candlestick0.getOpen()) < 0) {
//
//            return Pair.of(true, minLow);
//        }
//        return Pair.of(false, null);
//    }
//
////
////    private boolean checkTuPo(PeriodTypeEnum smallPeriodTypeEnum, PeriodTypeEnum bigPeriodTypeEnum) {
////
////        Candlestick min30Candlestick0 = getLastCandlestick(PeriodTypeEnum.MIN30, 0);
////        Candlestick min30Candlestick1 = getLastCandlestick(PeriodTypeEnum.MIN30, -1);
////        Candlestick min30Candlestick2 = getLastCandlestick(PeriodTypeEnum.MIN30, -2);
////
////        Candlestick smallCandlestick0 = getLastCandlestick(smallPeriodTypeEnum, 0);
////        Candlestick smallCandlestick1 = getLastCandlestick(smallPeriodTypeEnum, -1);
////        Candlestick smallCandlestick2 = getLastCandlestick(smallPeriodTypeEnum, -2);
////
////        Candlestick bigCandlestick0 = getLastCandlestick(bigPeriodTypeEnum, 0);
////        Candlestick bigCandlestick1 = getLastCandlestick(bigPeriodTypeEnum, -1);
////        Candlestick bigCandlestick2 = getLastCandlestick(bigPeriodTypeEnum, -2);
////
////        BigDecimal minLow = MathUtil.min(bigCandlestick1==null?null:bigCandlestick1.getLow(), bigCandlestick2==null?null:bigCandlestick2.getLow());
////        BigDecimal maxLow = MathUtil.max(bigCandlestick1==null?null:bigCandlestick1.getLow(), bigCandlestick2==null?null:bigCandlestick2.getLow());
////
////        if (bigCandlestick1 != null
////
////                //小周期 突破大周期
////                && smallCandlestick0.getClose().compareTo(minLow) < 0
////
////                && smallCandlestick1.getClose().compareTo(minLow) >= 0
////
////                //回踩
//////                && smallCandlestick0.getHigh().compareTo(MathUtil.max(smallCandlestick1.getLow(), smallCandlestick2.getLow())) >= 0
////                //基本盘
////                && min30Candlestick0.getClose().compareTo(MathUtil.max(min30Candlestick1.getLow(), min30Candlestick2.getLow())) < 0
////
////        ){
////            return true;
////        }
////
////        if (bigCandlestick1 != null
////
////                //小周期 突破大周期
////                && smallCandlestick0.getClose().compareTo(maxLow) < 0
////
////                && smallCandlestick1.getClose().compareTo(maxLow) >= 0
////
////                //回踩
//////                && smallCandlestick0.getHigh().compareTo(MathUtil.max(smallCandlestick1.getLow(), smallCandlestick2.getLow())) >= 0
////                //基本盘
////                && min30Candlestick0.getClose().compareTo(MathUtil.max(min30Candlestick1.getLow(), min30Candlestick2.getLow())) < 0
////
////        ){
////            return true;
////        }
////
////        return false;
////    }
//
//    private boolean checkChangeRate(BigDecimal changeRateThreshold, Candlestick ... candlesticks) {
//        return Arrays.stream(candlesticks).anyMatch(candlestick -> candlestick.getChangeRate().compareTo(changeRateThreshold) < 0);
//    }
//
//}
