package com.binance.client.utils;

import com.binance.client.enums.SideTypeEnum;
import com.binance.client.model.market.Candlestick;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class CheckValidator {

    public static boolean checkShunshi(List<Candlestick> candlesticks, SideTypeEnum sideTypeEnum) {
        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);
        if (sideTypeEnum == SideTypeEnum.LONG && candlestick0.getShiTiRate().compareTo(BigDecimal.ZERO) > 0) {
            if (candlesticks.size() < 2) {
                return candlestick0.getShiTiRate().compareTo(BigDecimal.ZERO) > 0;
            }
            Candlestick candlestick1 = candlesticks.get(candlesticks.size() - 2);
            if (candlestick1.getShiTiRate().compareTo(BigDecimal.ZERO) <= 0) {
                return candlestick0.getClose().compareTo(candlestick1.getShiTiMax()) > 0;
            }else{
                return candlestick0.getClose().compareTo(candlestick1.getShiTiMin()) > 0;
            }
        } else if (sideTypeEnum == SideTypeEnum.SHORT) {
            if (candlesticks.size() < 2) {
                return candlestick0.getShiTiRate().compareTo(BigDecimal.ZERO) < 0;
            }
            Candlestick candlestick1 = candlesticks.get(candlesticks.size() - 2);
            if (candlestick1.getShiTiRate().compareTo(BigDecimal.ZERO) >= 0) {
                return candlestick0.getClose().compareTo(candlestick1.getShiTiMin()) < 0;
            }else{
                return candlestick0.getClose().compareTo(candlestick1.getShiTiMax()) < 0;
            }
        }
        return false;
    }

    public static boolean checkMACrossMA(List<Candlestick> candlesticks, int crossMA, int baseMA, SideTypeEnum sideTypeEnum) {
        List<Candlestick> lastCandlesticks = candlesticks.subList(0, candlesticks.size() - 1);
        BigDecimal lastBaseMA = IndicatorCaculater.calMA(lastCandlesticks, baseMA);
        BigDecimal lastCrossMA = IndicatorCaculater.calMA(lastCandlesticks, crossMA);

        BigDecimal curBaseMA = IndicatorCaculater.calMA(candlesticks, baseMA);
        BigDecimal curCrossMA = IndicatorCaculater.calMA(candlesticks, crossMA);
        if (sideTypeEnum == SideTypeEnum.LONG) {
            return lastCrossMA.compareTo(lastBaseMA) < 0 && curCrossMA.compareTo(curBaseMA) > 0;
        }else {
            return lastCrossMA.compareTo(lastBaseMA) > 0 && curCrossMA.compareTo(curBaseMA) < 0;
        }
    }


    public static boolean checkOverMA(List<Candlestick> candlesticks, List<Integer> MAs, SideTypeEnum sideTypeEnum, int num) {
        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);

        if (sideTypeEnum == SideTypeEnum.LONG) {
            List<BigDecimal> MAValues = MAs.stream().map(x -> IndicatorCaculater.calMA(candlesticks, x))
                    .sorted(Comparator.comparingDouble(BigDecimal::doubleValue))
                    .collect(Collectors.toList());

            return MAValues.subList(0, num).stream().allMatch(x -> candlestick0.getClose().compareTo(x) > 0);

        }else {
            List<BigDecimal> MAValues = MAs.stream().map(x -> IndicatorCaculater.calMA(candlesticks, x))
                    .sorted(Comparator.comparingDouble(BigDecimal::doubleValue).reversed())
                    .collect(Collectors.toList());

            return MAValues.subList(0, num).stream().allMatch(x -> candlestick0.getClose().compareTo(x) < 0);

        }

    }


    public static boolean checkOverMA(List<Candlestick> candlesticks, List<Integer> MAs, SideTypeEnum sideTypeEnum, boolean andOr) {
        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);
        boolean flag = true;
        if (andOr) {
            flag = true;
            for (Integer MA : MAs) {
                if (sideTypeEnum == SideTypeEnum.LONG) {
                    flag = flag && candlestick0.getClose().compareTo(IndicatorCaculater.calMA(candlesticks, MA)) > 0;
                }else {
                    flag = flag && candlestick0.getClose().compareTo(IndicatorCaculater.calMA(candlesticks, MA)) < 0;
                }
                if (!flag) {
                    break;
                }
            }
        }else{
            flag = false;
            for (Integer MA : MAs) {
                if (sideTypeEnum == SideTypeEnum.LONG) {
                    if (candlestick0.getClose().compareTo(IndicatorCaculater.calMA(candlesticks, MA)) > 0) {
                        flag = true;
                    }
                }else {
                    if (candlestick0.getClose().compareTo(IndicatorCaculater.calMA(candlesticks, MA)) < 0) {
                        flag = true;
                    }
                }
                if (flag) {
                    break;
                }
            }
        }

        return flag;
    }

    public static boolean checkMAGongzhen(List<Candlestick> candlesticks, List<Integer> MAs, SideTypeEnum sideTypeEnum) {
        List<BigDecimal> MAValues = MAs.stream().map(x -> IndicatorCaculater.calMA(candlesticks, x)).collect(Collectors.toList());

        if (sideTypeEnum == SideTypeEnum.LONG) {
            return IntStream.range(0, MAValues.size() - 1).allMatch(i -> MAValues.get(i).compareTo(MAValues.get(i + 1)) >= 0);
        }else {
            return IntStream.range(0, MAValues.size() - 1).allMatch(i -> MAValues.get(i).compareTo(MAValues.get(i + 1)) <= 0);
        }
    }

    public static boolean checkShangyi(List<Candlestick> candlesticks, SideTypeEnum sideTypeEnum) {
        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);
        if (sideTypeEnum == SideTypeEnum.LONG) {
            if (candlesticks.size() < 2){
                return candlestick0.getShiTiRate().compareTo(BigDecimal.ZERO) > 0;
            }

            Candlestick candlestick1 = candlesticks.get(candlesticks.size() - 2);
            //本周期最新价突破
            if (candlestick0.getHigh().compareTo(candlestick1.getHigh()) >= 0 && candlestick0.getClose().compareTo(candlestick1.getShiTiMin()) > 0){
                return true;
            }
            if (candlesticks.size() < 3) {
                return false;
            }

            //前1周期最新价突破
            Candlestick candlestick2 = candlesticks.get(candlesticks.size() - 3);
            if (candlestick1.getHigh().compareTo(candlestick2.getHigh()) >= 0
                    && candlestick1.getClose().compareTo(candlestick2.getClose()) >= 0
                    && candlestick0.getClose().compareTo(candlestick1.getLow()) > 0
                    && candlestick0.getClose().compareTo(candlestick2.getLow()) > 0){
                return true;
            }
            //本周期斜率突破
//            if (candlestick2.getHigh().add(candlestick0.getClose()).compareTo(new BigDecimal(2).multiply(candlestick1.getHigh())) > 0
//                    && candlestick0.getClose().compareTo(candlestick1.getLow()) > 0
//                    && candlestick0.getClose().compareTo(candlestick2.getLow()) > 0) {
//                return true;
//            }


        }else {
            if (candlesticks.size() < 2){
                return candlestick0.getShiTiRate().compareTo(BigDecimal.ZERO) < 0;
            }

            Candlestick candlestick1 = candlesticks.get(candlesticks.size() - 2);
            //本周期最新价突破
            if (candlestick0.getLow().compareTo(candlestick1.getLow()) <= 0 && candlestick0.getClose().compareTo(candlestick1.getShiTiMax()) < 0){
                return true;
            }
            if (candlesticks.size() < 3) {
                return false;
            }

            //前1周期最新价突破
            Candlestick candlestick2 = candlesticks.get(candlesticks.size() - 3);
            if (candlestick1.getLow().compareTo(candlestick2.getLow()) <= 0
                    && candlestick1.getClose().compareTo(candlestick2.getClose()) <= 0
                    && candlestick0.getClose().compareTo(candlestick1.getHigh()) < 0
                    && candlestick0.getClose().compareTo(candlestick2.getHigh()) < 0){
                return true;
            }
            //本周期斜率突破
//            if (candlestick2.getLow().add(candlestick0.getClose()).compareTo(new BigDecimal(2).multiply(candlestick1.getLow())) < 0
//                    && candlestick0.getClose().compareTo(candlestick1.getHigh()) < 0
//                    && candlestick0.getClose().compareTo(candlestick2.getHigh()) < 0) {
//                return true;
//            }
        }
        return false;
    }


    public static boolean checkDownUpCrossMAOverAll(List<Candlestick> candlesticks, List<Integer> MAs, SideTypeEnum sideTypeEnum, BigDecimal crossRate) {
        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);
        if (candlestick0.getShiTiRateAbs().compareTo(crossRate) < 0) {
            return false;
        }

        List<BigDecimal> MAValues1 = new ArrayList<>();
        for (Integer MA : MAs) {
            MAValues1.add(IndicatorCaculater.calMA(candlesticks.subList(0, candlesticks.size() - 1), MA));
        }

        List<BigDecimal> MAValues0 = new ArrayList<>();
        for (Integer MA : MAs) {
            MAValues0.add(IndicatorCaculater.calMA(candlesticks, MA));
        }

        Candlestick candlestick1 = candlesticks.get(candlesticks.size() - 2);
        if (sideTypeEnum == SideTypeEnum.LONG) {
            BigDecimal maxMAValue1 = MathUtil.max(MAValues1);
            BigDecimal maxMAValue0 = MathUtil.max(MAValues0);
            return candlestick1.getClose().compareTo(maxMAValue1) <= 0 && candlestick0.getClose().compareTo(maxMAValue0) > 0;
        } else {
            BigDecimal minMAValue1 = MathUtil.min(MAValues1);
            BigDecimal minMAValue0 = MathUtil.min(MAValues0);
            return candlestick1.getClose().compareTo(minMAValue1) >= 0 && candlestick0.getClose().compareTo(minMAValue0) < 0;
        }
    }



    public static boolean checkDownUpCrossFanBao(List<Candlestick> bigCandlesticks, List<Candlestick> smallCandlesticks, SideTypeEnum sideTypeEnum, BigDecimal crossRate) {
        Candlestick bigCandlestick0 = bigCandlesticks.get(bigCandlesticks.size() - 1);
        Candlestick bigCandlestick1 = bigCandlesticks.size() > 0 ? bigCandlesticks.get(bigCandlesticks.size() - 2) : null;

        Candlestick smallCandlestick0 = smallCandlesticks.get(smallCandlesticks.size() - 1);
        Candlestick smallCandlestick1 = smallCandlesticks.size() > 0 ? smallCandlesticks.get(smallCandlesticks.size() - 2) : null;

        if (bigCandlestick1 == null) {
            if (sideTypeEnum == SideTypeEnum.LONG) {
                return bigCandlestick0.getShiTiRate().compareTo(crossRate) > 0;
            } else {
                return bigCandlestick0.getShiTiRateAbs().compareTo(crossRate.abs()) > 0;
            }
        }

        if (sideTypeEnum == SideTypeEnum.LONG) {
            return bigCandlestick0.getShiTiRate().compareTo(crossRate) > 0
                    && smallCandlestick1.getClose().compareTo(bigCandlestick1.getShiTiMax()) <= 0
                    && smallCandlestick0.getClose().compareTo(bigCandlestick1.getShiTiMax()) > 0
                    ;
        } else {
            return bigCandlestick0.getShiTiRateAbs().compareTo(crossRate.abs()) > 0
                    && smallCandlestick1.getClose().compareTo(bigCandlestick1.getShiTiMin()) >= 0
                    && smallCandlestick0.getClose().compareTo(bigCandlestick1.getShiTiMin()) < 0
                    ;
        }
    }
}
