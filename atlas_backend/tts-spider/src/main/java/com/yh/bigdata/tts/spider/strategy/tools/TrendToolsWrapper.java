package com.yh.bigdata.tts.spider.strategy.tools;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.dto.CheckResponse;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.spider.response.CheckResult;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TrendToolsWrapper {

    /**
     * 趋势： 无阻力
     */
    public static boolean checkNoPressureTrend(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult, boolean isCheckNoPressureTrend) {
        long checkTrendCount = trendPeriodTypes.stream()
                .map(periodType -> TrendTools.checkNoPressureTrend(stockBase, periodType))
                .filter(CheckResponse::isSuccess)
                .map(checkResponse -> {
                    checkResult.addTrendPeriod(checkResponse.getPeriodType(), checkResponse.getMessage());
                    checkResult.setSortValue(checkResponse.getScore());
                    return checkResponse;
                })
                .count();

        if (!isCheckNoPressureTrend) {
//            return checkTrendCount > 0;
            return checkTrendCount == trendPeriodTypes.size();
        }

        List<PeriodTypeEnum> noTrendPeriodTypes = Stream.of(PeriodTypeEnum.YEAR, PeriodTypeEnum.QUARTER, PeriodTypeEnum.MONTH, PeriodTypeEnum.WEEK)
                .filter(periodType -> !trendPeriodTypes.contains(periodType)).collect(Collectors.toList());
        long checkTrendCountFalse = noTrendPeriodTypes.stream()
                .map(periodType -> TrendTools.checkNoPressureTrend(stockBase, periodType))
                .filter(x -> !x.isSuccess())
                .map(checkResponse -> {
                    checkResult.addTrendPeriod(checkResponse.getPeriodType(), checkResponse.getMessage());
                    checkResult.setSortValue(checkResponse.getScore());
                    return checkResponse;
                })
                .count();


        return checkTrendCount == trendPeriodTypes.size() && checkTrendCountFalse == noTrendPeriodTypes.size();
    }

    /**
     * 趋势： 有支撑
     */
    public static boolean checkHasSupportTrend(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
        long checkTrendCount = trendPeriodTypes.stream()
                .map(periodType -> TrendTools.checkHasSupportTrend(stockBase, periodType))
                .filter(CheckResponse::isSuccess)
                .map(checkResponse -> {
                    checkResult.addTrendPeriod(checkResponse.getPeriodType(), checkResponse.getMessage());
                    checkResult.setSortValue(checkResponse.getScore());
                    return checkResponse;
                })
                .count();
        return checkTrendCount > 0;
    }

    /**
     * 趋势： red
     */
    public static boolean checkRedTrend(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
        long checkTrendCount = trendPeriodTypes.stream()
                .map(periodType -> TrendTools.checkRedTrend(stockBase, periodType))
                .filter(CheckResponse::isSuccess)
                .map(checkResponse -> {
                    checkResult.addTrendPeriod(checkResponse.getPeriodType(), checkResponse.getMessage());
                    checkResult.setSortValue(checkResponse.getScore());
                    return checkResponse;
                })
                .count();
        return checkTrendCount == trendPeriodTypes.size();
    }


    /**
     * 趋势： MACD金叉之上
     */
    public static boolean checkOverMACDTrend(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
        long checkTrendCount = trendPeriodTypes.stream()
                .map(periodType -> TrendTools.checkOverMACDTrend(stockBase, periodType))
                .filter(CheckResponse::isSuccess)
                .map(checkResponse -> {
                    checkResult.addTrendPeriod(checkResponse.getPeriodType(), checkResponse.getMessage());
                    checkResult.setSortValue(checkResponse.getScore());
                    return checkResponse;
                })
                .count();
        return checkTrendCount > 0;
    }


    /**
     * 趋势： MACD金叉之上
     */
    public static boolean checkOverMA20Trend(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
        long checkTrendCount = trendPeriodTypes.stream()
                .map(periodType -> TrendTools.checkOverMA20Trend(stockBase, periodType))
                .filter(CheckResponse::isSuccess)
                .map(checkResponse -> {
                    checkResult.addTrendPeriod(checkResponse.getPeriodType(), checkResponse.getMessage());
                    checkResult.setSortValue(checkResponse.getScore());
                    return checkResponse;
                })
                .count();
        return checkTrendCount == trendPeriodTypes.size();
    }

    /**
     * 趋势： MA5>MA20
     */
    public static boolean checkMA5OverMA20Trend(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
        long checkTrendCount = trendPeriodTypes.stream()
                .map(periodType -> TrendTools.checkMA5OverMA20Trend(stockBase, periodType))
                .filter(CheckResponse::isSuccess)
                .map(checkResponse -> {
                    checkResult.addTrendPeriod(checkResponse.getPeriodType(), checkResponse.getMessage());
                    checkResult.setSortValue(checkResponse.getScore());
                    return checkResponse;
                })
                .count();
        return checkTrendCount == trendPeriodTypes.size();
    }


    /**
     * 趋势： 波段底上(High)
     */
    public static boolean checkOverBandBottomHighTrend(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
        long checkTrendCount = trendPeriodTypes.stream()
                .map(periodType -> TrendTools.checkOverBandBottomHighTrend(stockBase, periodType))
                .filter(CheckResponse::isSuccess)
                .map(checkResponse -> {
                    checkResult.addTrendPeriod(checkResponse.getPeriodType(), checkResponse.getMessage());
                    checkResult.setSortValue(checkResponse.getScore());
                    return checkResponse;
                })
                .count();
        return checkTrendCount == trendPeriodTypes.size();
    }

    /**
     * 趋势： 波段底上(High)
     */
    public static boolean checkOverBandBottomLowTrend(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
        long checkTrendCount = trendPeriodTypes.stream()
                .map(periodType -> TrendTools.checkOverBandBottomLowTrend(stockBase, periodType))
                .filter(CheckResponse::isSuccess)
                .map(checkResponse -> {
                    checkResult.addTrendPeriod(checkResponse.getPeriodType(), checkResponse.getMessage());
                    checkResult.setSortValue(checkResponse.getScore());
                    return checkResponse;
                })
                .count();
        return checkTrendCount == trendPeriodTypes.size();
    }


    /**
     * 趋势： 波段底上(High)
     */
    public static boolean checkOverLastRedLowTrend(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
        long checkTrendCount = trendPeriodTypes.stream()
                .map(periodType -> TrendTools.checkOverLastRedLowTrend(stockBase, periodType))
                .filter(CheckResponse::isSuccess)
                .map(checkResponse -> {
                    checkResult.addTrendPeriod(checkResponse.getPeriodType(), checkResponse.getMessage());
                    checkResult.setSortValue(checkResponse.getScore());
                    return checkResponse;
                })
                .count();
        return checkTrendCount == trendPeriodTypes.size();
    }


    /**
     * 趋势： 上周期Low之上
     */
    public static boolean checkOverLastLowTrend(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
        long checkTrendCount = trendPeriodTypes.stream()
                .map(periodType -> TrendTools.checkOverLastLowTrend(stockBase, periodType))
                .filter(CheckResponse::isSuccess)
                .map(checkResponse -> {
                    checkResult.addTrendPeriod(checkResponse.getPeriodType(), checkResponse.getMessage());
                    checkResult.setSortValue(checkResponse.getScore());
                    return checkResponse;
                })
                .count();
        return checkTrendCount == trendPeriodTypes.size();
    }


    /**
     * 趋势： 波段底上
     */
    public static boolean checkOverBandLowTrend(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
        long checkTrendCount = trendPeriodTypes.stream()
                .map(periodType -> TrendTools.checkOverBandLowTrend(stockBase, periodType))
                .filter(CheckResponse::isSuccess)
                .map(checkResponse -> {
                    checkResult.addTrendPeriod(checkResponse.getPeriodType(), checkResponse.getMessage());
                    checkResult.setSortValue(checkResponse.getScore());
                    return checkResponse;
                })
                .count();
        return checkTrendCount == trendPeriodTypes.size();
    }


    /**
     * 趋势： 反转波段底上
     */
    public static boolean checkOverRevertBandLowTrend(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
        long checkTrendCount = trendPeriodTypes.stream()
                .map(periodType -> TrendTools.checkOverRevertBandLowTrend(stockBase, periodType))
                .filter(CheckResponse::isSuccess)
                .map(checkResponse -> {
                    checkResult.addTrendPeriod(checkResponse.getPeriodType(), checkResponse.getMessage());
                    checkResult.setSortValue(checkResponse.getScore());
                    return checkResponse;
                })
                .count();
        return checkTrendCount == trendPeriodTypes.size();
    }
}
