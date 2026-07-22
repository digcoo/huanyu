package com.binance.client.examples.backup2;

import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.enums.SideTypeEnum;
import com.binance.client.model.market.Candlestick;
import com.binance.client.utils.MathUtil;
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
    private final Map<PeriodTypeEnum, List<Candlestick>> periodTypeCandlesticksMap;
    SideTypeEnum side = SideTypeEnum.LONG;

    public Checker(String symbol
            , Map<PeriodTypeEnum, List<Candlestick>> periodTypeCandlesticksMap
    ) {
        this.symbol = symbol;
        this.periodTypeCandlesticksMap = periodTypeCandlesticksMap;
    }

    public void check(CheckResult checkResult) {

        try {

            PeriodTypeEnum trendPeriodType = PeriodTypeEnum.HOUR4;
            PeriodTypeEnum opPeriodType = PeriodTypeEnum.HOUR4;

            Candlestick dayCandlestick0 = getLastCandlestick(trendPeriodType, 0);
            checkResult.setChangeRate(dayCandlestick0.getChangeRate());
            checkResult.setClose(dayCandlestick0.getClose());
            checkResult.setSortValue(dayCandlestick0.getCrestShockRate());
            checkResult.setOpPeriodType(opPeriodType);
            checkResult.setTrendPeriodType(trendPeriodType);
            checkResult.setSideType(getSide());


            boolean checkRiskSignal = checkRisk();
            if (!checkRiskSignal) {
                return;
            }

            boolean checkTrend = checkTrend(Arrays.asList(trendPeriodType, opPeriodType), checkResult);
            if (!checkTrend) {
                return;
            }
            checkResult.setHasTrend(true);

            boolean checkSignal = checkSignal(trendPeriodType, opPeriodType, checkResult);
            if (!checkSignal) {
                return;
            }
            checkResult.setHasSignal(true);

        } catch (Exception ex) {
            log.error("{} exception...symbol={}", this.getClass().getName(), getSymbol(), ex);
            ex.printStackTrace();
        } finally {

        }
    }

    abstract SideTypeEnum getSide();

    abstract Pair<Boolean, String> checkFanBaoSignal(PeriodTypeEnum opPeriodType);

    abstract Pair<Boolean, String> checkTurnRound(PeriodTypeEnum opPeriodType);

    abstract CheckResponse checkCrossKeyResistanceSignal(PeriodTypeEnum keyPeriodType, PeriodTypeEnum opPeriodType);

    abstract CheckResponse checkOverMACDTrend(PeriodTypeEnum trendPeriodType);

    abstract boolean checkOverLowTrend(PeriodTypeEnum opPeriodType);

    /**
     * 凹突破
     * @param opPeriodType
     * @return
     */
    abstract CheckResponse checkConcaveCrossSignal(PeriodTypeEnum opPeriodType);


    /**
     * 黄金实体位
     * @param opPeriodType
     * @return
     */
    abstract CheckResponse checkCrossGoldSignal(PeriodTypeEnum trendPeriodType, PeriodTypeEnum opPeriodType);

    public Candlestick getLastCandlestick(PeriodTypeEnum periodTypeEnum, int lastN) {
        lastN = Math.abs(lastN);

        List<Candlestick> candlesticks = this.periodTypeCandlesticksMap.get(periodTypeEnum);
        if (candlesticks.size() > lastN) {
            return candlesticks.get(candlesticks.size() - lastN - 1);
        } else {
            return null;
        }
    }

    public List<Candlestick> getLastCandlesticks(PeriodTypeEnum periodTypeEnum, long startTime, long endTime) {
        return this.periodTypeCandlesticksMap.get(periodTypeEnum).stream()
                .filter(candlestick -> candlestick.getOpenTime() >= startTime
                        && candlestick.getCloseTime() <= endTime)
                .collect(Collectors.toList());
    }

    public List<Candlestick> getLastCandlesticks(PeriodTypeEnum periodTypeEnum, int n) {
        List<Candlestick> candlesticks = this.periodTypeCandlesticksMap.get(periodTypeEnum);
        return candlesticks.subList(MathUtil.max(0, candlesticks.size() - n), candlesticks.size());
    }

    /**
     * 日内信号分析
     *
     * @param checkResult
     * @return
     */
    private boolean checkSignalPeriod(PeriodTypeEnum opPeriodType, CheckResult checkResult) {

        //反包信号
        Pair<Boolean, String> checkFanBaoSignal = checkFanBaoSignal(opPeriodType);
        if (checkFanBaoSignal.getLeft()) {
            checkResult.addFanBao(opPeriodType, checkFanBaoSignal.getRight());
            checkResult.setHasSignal(true);
            return true;
        }

        //掉头信号
        Pair<Boolean, String> checkTurnRound = checkTurnRound(opPeriodType);
        if (checkTurnRound.getLeft()) {
            checkResult.addTurnRound(opPeriodType, checkTurnRound.getRight());
            checkResult.setHasSignal(true);
            return true;
        }


        return false;
    }


    /**
     * 风险规避
     *
     * @return
     */
    private boolean checkRisk() {

        boolean checkOverLowTrend = Arrays.asList(PeriodTypeEnum.DAY1).stream()
                .allMatch(periodType -> checkOverLowTrend(periodType));

        if (!checkOverLowTrend) {
            return false;
        }

        return true;

    }


    private boolean checkTrend(List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {

//        if ("ARCUSDT".equals(getSymbol())) {
//            System.out.println("NEARUSDT");
//        }

        long checkTrendCount = trendPeriodTypes.stream()
                .map(periodType -> checkOverMACDTrend(periodType))
                .filter(checkResponse -> checkResponse.isSuccess())
                .map(checkResponse -> {
                    checkResult.addTrendPeriod(checkResponse.getPeriodType(), checkResponse.getMessage());
                    return checkResponse;
                })
                .count();
        if (checkTrendCount == trendPeriodTypes.size()) {
            return true;
        }
        return false;
    }


    private boolean checkSignal(PeriodTypeEnum trendPeriodType, PeriodTypeEnum opPeriodType, CheckResult checkResult){

//        if ("ARCUSDT".equals(getSymbol())) {
//            System.out.println("NEARUSDT");
//        }

        //黄金实体位-操作周期
        CheckResponse checkCrossOpPeriodGoldSignalResponse = checkCrossGoldSignal(opPeriodType, opPeriodType);
        if (checkCrossOpPeriodGoldSignalResponse.isSuccess()) {
            checkResult.addCrossGold(checkCrossOpPeriodGoldSignalResponse.getPeriodType(), checkCrossOpPeriodGoldSignalResponse.getMessage());
            return true;
        }

//        //黄金实体位-趋势周期
//        CheckResponse checkCrossTrendGoldSignalResponse = checkCrossGoldSignal(trendPeriodType, opPeriodType);
//        if (checkCrossTrendGoldSignalResponse.isSuccess()) {
//            checkResult.addCrossGold(checkCrossTrendGoldSignalResponse.getPeriodType(), checkCrossTrendGoldSignalResponse.getMessage());
//            return true;
//        }

        //凹突破
        CheckResponse checkConcaveCrossSignalResponse = checkConcaveCrossSignal(opPeriodType);
        if (checkConcaveCrossSignalResponse.isSuccess()) {
            checkResult.addCrossResistance(checkConcaveCrossSignalResponse.getPeriodType(), checkConcaveCrossSignalResponse.getMessage());
            return true;
        }


//        //突破关键阻力位
//        Optional<CheckResponse> checkCrossKeyResistanceSignalResponseOptional = Arrays.asList(PeriodTypeEnum.WEEK, PeriodTypeEnum.MONTH).stream()
//                .map(periodType -> checkCrossKeyResistanceSignal(periodType, opPeriodType))
//                .filter(checkResponse -> checkResponse.isSuccess())
//                .findFirst();
//        if (checkCrossKeyResistanceSignalResponseOptional.isPresent()) {
//            checkResult.setHasSignal(true);
//            checkResult.addCrossKeyResistance(checkCrossKeyResistanceSignalResponseOptional.get().getPeriodType(), checkCrossKeyResistanceSignalResponseOptional.get().getMessage());
//            return true;
//        }

        return false;
    }

}
