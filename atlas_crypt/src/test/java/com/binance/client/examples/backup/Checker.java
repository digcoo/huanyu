package com.binance.client.examples.backup;

import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.enums.SideTypeEnum;
import com.binance.client.model.market.Candlestick;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Slf4j
public abstract class Checker {

    private final String symbol;
    private final PeriodTypeEnum opPeriodType;
    private final Map<PeriodTypeEnum, List<Candlestick>> periodTypeCandlesticksMap;
    SideTypeEnum side = SideTypeEnum.LONG;

    public Checker(String symbol
                   , PeriodTypeEnum opPeriodType
                   , Map<PeriodTypeEnum, List<Candlestick>> periodTypeCandlesticksMap) {
        this.symbol = symbol;
        this.opPeriodType = opPeriodType;
        this.periodTypeCandlesticksMap = periodTypeCandlesticksMap;
    }

    public Candlestick getLastCandlestick(PeriodTypeEnum periodTypeEnum, int lastN) {
        lastN = Math.abs(lastN);

        List<Candlestick> candlesticks = this.periodTypeCandlesticksMap.get(periodTypeEnum);
        if (candlesticks.size() > lastN) {
            return candlesticks.get(candlesticks.size() - lastN - 1);
        }else {
            return null;
        }
    }

    public void check(CheckResult checkResult) {

        try {
//            checkCrossStrategy(checkResult);

            checkCrossTiZiMinStrategy(checkResult);

        } catch (Exception ex) {
            log.error("{} exception...symbol={}", this.getClass().getName(), getSymbol(), ex);
            ex.printStackTrace();
        }

    }

    abstract SideTypeEnum getSide();

    /**
     * 顺周期：阻力位之上
     * @param periodTypeEnum
     * @return
     */
    abstract boolean checkOverResistanceTrend(PeriodTypeEnum periodTypeEnum);


    /**
     * 顺周期：阻力位之上
     * @param periodTypeEnum
     * @return
     */
    abstract boolean checkSameDirectionTrend(PeriodTypeEnum periodTypeEnum);


    /**
     * 前1周期突破High
     * @param periodTypeEnum
     * @return
     */
    abstract boolean checkPreCrossResistanceTrend(PeriodTypeEnum periodTypeEnum);


    /**
     * 大周期突破，调整机会
     * @param periodTypeEnum
     * @return
     */
    abstract public boolean checkCrossPeriodTrend(PeriodTypeEnum periodTypeEnum);


    /**
     * 突破TiZi Min
     */
    abstract public boolean checkCrossTiZiMinTrend(PeriodTypeEnum periodTypeEnum);


    /**
     * 突破TiZi Max
     */
    abstract public boolean checkCrossTiZiMaxTrend(PeriodTypeEnum periodTypeEnum);

        /**
         * 突破阻力位
         * @param trendPeriodTypeEnum
         * @return
         */
    abstract Pair<Boolean, String> checkCrossResistanceSignal(PeriodTypeEnum trendPeriodTypeEnum);

    /**
     * 反包突破阻力位
     * @param trendPeriodTypeEnum
     * @return
     */
    abstract Pair<Boolean, String> checkFanBaoCrossResistanceSignal(PeriodTypeEnum trendPeriodTypeEnum);

    /**
     * 掉头
     * @param trendPeriodTypeEnum
     * @return
     */
    abstract Pair<Boolean, String> checkTurnRoundSignal(PeriodTypeEnum trendPeriodTypeEnum);


    /**
     * 趋势分析
     * @param checkResult
     * @param periodTypeEnum
     */
    public boolean checkTrendPeriod(CheckResult checkResult, PeriodTypeEnum periodTypeEnum) {

        boolean checkTrend = checkOverResistanceTrend(periodTypeEnum) || checkPreCrossResistanceTrend(periodTypeEnum);
        if (checkTrend) {
            checkResult.addTrendPeriod(periodTypeEnum, getSide());
            return true;
        }
        return false;
    }

    /**
     * 回踩趋势分析
     * @param checkResult
     * @param periodTypeEnum
     */
    public boolean checkBackCrossTrendPeriod(CheckResult checkResult, PeriodTypeEnum periodTypeEnum) {
        boolean checkTrend = checkCrossPeriodTrend(periodTypeEnum);

        return checkTrend;
    }


    /**
     * 信号分析
     * @param checkResult
     * @param periodTypeEnum
     */
    public boolean checkSignalPeriod(CheckResult checkResult, PeriodTypeEnum periodTypeEnum) {
        boolean checkSignal = false;

        Pair<Boolean, String> fanBaoCrossResistance = checkFanBaoCrossResistanceSignal(periodTypeEnum);
        if (fanBaoCrossResistance.getLeft()) {
            checkResult.addFanBaoCrossResistancePeriod(periodTypeEnum, fanBaoCrossResistance.getRight());
            checkSignal = true;
        }

        if (!fanBaoCrossResistance.getLeft()) {
            Pair<Boolean, String> crossResistance = checkCrossResistanceSignal(periodTypeEnum);
            if (crossResistance.getLeft()) {
                checkResult.addCrossResistancePeriod(periodTypeEnum, crossResistance.getRight());
                checkSignal = true;
            }
        }

        Pair<Boolean, String> turnRound = checkTurnRoundSignal(periodTypeEnum);
        if (turnRound.getLeft()) {
            checkResult.addTurnRoundPeriod(periodTypeEnum, getSide());
            checkSignal = true;
        }

        if (checkOverResistanceTrend(periodTypeEnum)
                && checkSameDirectionTrend(periodTypeEnum)) {
            checkSignal = true;
        }

        if (checkSignal) {
            checkResult.addSignalPeriod(periodTypeEnum, getSide());
            return true;
        }
        return false;
    }

    public void checkCrossStrategy(CheckResult checkResult) {

        PeriodTypeEnum secondOpPeriod = GlobalConstants.LIMIT_TREND_PERIOD_MAP.get(opPeriodType).getLeft();
        List<PeriodTypeEnum> trendPeriods = GlobalConstants.LIMIT_TREND_PERIOD_MAP.get(opPeriodType).getRight();

        Candlestick opCandlestick0 = getLastCandlestick(opPeriodType, 0);

        checkResult.setOpPeriodTypeEnum(opPeriodType);
        checkResult.setChangeRate(opCandlestick0.getChangeRate());
        checkResult.setClose(opCandlestick0.getClose());

        //趋势分析：仅做展示
        GlobalConstants.getGlobalTrendPeriodList().forEach(periodTypeEnum -> {
            checkTrendPeriod(checkResult, periodTypeEnum);
        });

        //分析趋势周期: 顺势，且不可出现方向相悖
        boolean checkTrend = trendPeriods.stream()
                .map(periodTypeEnum ->  checkTrendPeriod(checkResult, periodTypeEnum))
                .collect(Collectors.toList())
                .stream()
                .anyMatch(x -> x);

        boolean checkOppositeTrend = checkResult.getTrendPeriodMap().entrySet().stream()
                .filter(x -> trendPeriods.contains(x.getKey()))
                .anyMatch(x -> x.getValue().equals(getSide().getOppositeSide()));

        if (checkTrend && checkOppositeTrend) {
            checkResult.setSuccess(false);
            return;
        }

        //信号
        boolean checkSignal = Arrays.asList(opPeriodType, secondOpPeriod).stream()
                .allMatch(periodTypeEnum -> checkSignalPeriod(checkResult, periodTypeEnum));

        boolean checkOppositeSignal = checkResult.getSignalPeriodMap().entrySet().stream()
                .filter(x -> trendPeriods.contains(x.getKey()))
                .anyMatch(x -> x.getValue().equals(getSide().getOppositeSide()));

        if (checkSignal && checkOppositeSignal) {
            checkResult.setSuccess(false);
            return;
        }


        if (checkTrend && checkSignal
                && (opPeriodType == GlobalConstants.SECOND_OP_PERIOD)
        ) {

            checkResult.setSuccess(true);
            checkResult.setOpSideTypeEnum(getSide());
        }
    }

//
//    public void checkBackCrossStrategy(CheckResult checkResult) {
//
//        PeriodTypeEnum secondOpPeriod = GlobalConstants.LIMIT_TREND_PERIOD_MAP.get(opPeriodType).getLeft();
//        List<PeriodTypeEnum> trendPeriods = GlobalConstants.LIMIT_TREND_PERIOD_MAP.get(opPeriodType).getRight();
//
//        Candlestick opCandlestick0 = getLastCandlestick(opPeriodType, 0);
//
//        checkResult.setOpPeriodTypeEnum(opPeriodType);
//        checkResult.setChangeRate(opCandlestick0.getChangeRate());
//        checkResult.setClose(opCandlestick0.getClose());
//
//        //趋势分析：仅做展示
//        GlobalConstants.getGlobalTrendPeriodList().forEach(periodTypeEnum -> {
//            checkTrendPeriod(checkResult, periodTypeEnum);
//        });
//
//        //分析趋势周期: 顺势，且不可出现方向相悖
//        boolean checkTrend = trendPeriods.stream()
//                .map(periodTypeEnum ->  checkBackCrossTrendPeriod(checkResult, periodTypeEnum))
//                .collect(Collectors.toList())
//                .stream()
//                .anyMatch(x -> x);
//
//
//        if (checkTrend) {
//            checkResult.setSuccess(true);
//            checkResult.setOpSideTypeEnum(getSide());
//        }
//    }


    public void checkCrossTiZiMinStrategy(CheckResult checkResult) {

        List<PeriodTypeEnum> trendPeriods = GlobalConstants.LIMIT_TREND_PERIOD_MAP.get(opPeriodType).getRight();

        Candlestick opCandlestick0 = getLastCandlestick(opPeriodType, 0);

        checkResult.setOpPeriodTypeEnum(opPeriodType);
        checkResult.setChangeRate(opCandlestick0.getChangeRate());
        checkResult.setClose(opCandlestick0.getClose());

        //趋势分析：仅做展示
        GlobalConstants.getGlobalTrendPeriodList().forEach(periodTypeEnum -> {
            checkTrendPeriod(checkResult, periodTypeEnum);
        });

        //分析趋势周期: 顺势，且不可出现方向相悖
        boolean checkTrend = trendPeriods.stream()
                .map(periodTypeEnum ->  checkBackCrossTrendPeriod(checkResult, periodTypeEnum))
                .collect(Collectors.toList())
                .stream()
                .anyMatch(x -> x);


        if (checkTrend) {
            checkResult.setSuccess(true);
            checkResult.setOpSideTypeEnum(getSide());
        }
    }


    public void checkCrossShitiMaxStrategy(CheckResult checkResult) {

        PeriodTypeEnum secondOpPeriod = GlobalConstants.LIMIT_TREND_PERIOD_MAP.get(opPeriodType).getLeft();
        List<PeriodTypeEnum> trendPeriods = GlobalConstants.LIMIT_TREND_PERIOD_MAP.get(opPeriodType).getRight();

        Candlestick opCandlestick0 = getLastCandlestick(opPeriodType, 0);

        checkResult.setOpPeriodTypeEnum(opPeriodType);
        checkResult.setChangeRate(opCandlestick0.getChangeRate());
        checkResult.setClose(opCandlestick0.getClose());

        //趋势分析：仅做展示
        GlobalConstants.getGlobalTrendPeriodList().forEach(periodTypeEnum -> {
            checkTrendPeriod(checkResult, periodTypeEnum);
        });

        //分析趋势周期: 顺势，且不可出现方向相悖
        boolean checkTrend = trendPeriods.stream()
                .map(periodTypeEnum ->  checkBackCrossTrendPeriod(checkResult, periodTypeEnum))
                .collect(Collectors.toList())
                .stream()
                .anyMatch(x -> x);


        if (checkTrend) {
            checkResult.setSuccess(true);
            checkResult.setOpSideTypeEnum(getSide());
        }
    }



}
