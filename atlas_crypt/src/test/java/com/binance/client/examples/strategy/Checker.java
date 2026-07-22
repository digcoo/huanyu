package com.binance.client.examples.strategy;

import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.enums.SideTypeEnum;
import com.binance.client.model.market.Candlestick;
import com.binance.client.utils.MathUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Data
@Slf4j
public abstract class Checker {

    private final String symbol;
    private final Map<PeriodTypeEnum, List<Candlestick>> periodTypeCandlesticksMap;
    private SideTypeEnum side = SideTypeEnum.LONG;

    //回测的偏移
    private int offset = 0;
    List<PeriodTypeEnum> trendPeriodTypes;
    PeriodTypeEnum opPeriodType;

    public Checker(String symbol
            , Map<PeriodTypeEnum, List<Candlestick>> periodTypeCandlesticksMap
            , List<PeriodTypeEnum> trendPeriodTypes
            , PeriodTypeEnum opPeriodType
           , int offset
    ) {
        this.symbol = symbol;
        this.periodTypeCandlesticksMap = periodTypeCandlesticksMap;
        this.trendPeriodTypes = trendPeriodTypes;
        this.opPeriodType = opPeriodType;
        this.offset = offset;
    }

    public void check(CheckResult checkResult) {

        try {

            Candlestick dayCandlestick0 = getLastCandlestick(opPeriodType, 0);
            checkResult.setChangeRate(dayCandlestick0.getChangeRate());
            checkResult.setClose(dayCandlestick0.getClose());
            checkResult.setSortValue(dayCandlestick0.getCrestShockRate());
            checkResult.setOpPeriodType(opPeriodType);
            checkResult.setTrendPeriodType(trendPeriodTypes.get(0));
            checkResult.setSideType(getSide());


            boolean checkRiskSignal = checkRisk();
            if (!checkRiskSignal) {
                return;
            }

            boolean checkTrend = checkTrend(trendPeriodTypes, checkResult);
            if (!checkTrend) {
                return;
            }
            checkResult.setHasTrend(true);

            boolean checkSignal = checkSignal(trendPeriodTypes, opPeriodType, checkResult);
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
//
//    //------------------------------------  Risk  ------------------------------------
//    abstract boolean checkNoXieSanJiaoRisk(PeriodTypeEnum trendPeriodType);
//    //------------------------------------  Risk  ------------------------------------
//
//
//
//    abstract CheckResponse checkMACDTrend(PeriodTypeEnum trendPeriodType);
//
//    abstract CheckResponse checkCrossGoldMACD(PeriodTypeEnum opPeriodType);
//
//    abstract CheckResponse checkDownCrossGoldMACD(PeriodTypeEnum opPeriodType);
//
//    abstract CheckResponse checkTurnRoundKDJ(PeriodTypeEnum opPeriodType);
//
//    abstract CheckResponse checkExtremeReverse(PeriodTypeEnum opPeriodType);
//
//    abstract CheckResponse checkFanBao(PeriodTypeEnum opPeriodType, boolean lastK);
//
//    abstract CheckResponse checkBetweenTiZiAndOverMACD(List<PeriodTypeEnum> trendPeriodType, PeriodTypeEnum opPeriodTyp);

    abstract CheckResponse checkCrossKeyPressureHighSignal(List<PeriodTypeEnum> trendPeriodType, PeriodTypeEnum opPeriodTyp);

//    abstract CheckResponse checkShangYiBetweenTiZiSignal(List<PeriodTypeEnum> trendPeriodType, PeriodTypeEnum opPeriodTyp);



    public Candlestick getLastCandlestick(PeriodTypeEnum periodTypeEnum, int lastN) {
        lastN = Math.abs(lastN);

        List<Candlestick> candlesticks = this.periodTypeCandlesticksMap.get(periodTypeEnum);
        if (candlesticks.size() - getOffset() > lastN) {
            return candlesticks.get(candlesticks.size() - getOffset() - lastN - 1);
        } else {
            return null;
        }
    }

    public List<Candlestick> getLastCandlesticks(PeriodTypeEnum periodTypeEnum, int n) {
        List<Candlestick> candlesticks = this.periodTypeCandlesticksMap.get(periodTypeEnum);
        return candlesticks.subList(MathUtil.max(0, candlesticks.size() - getOffset() - n), candlesticks.size() - getOffset());
    }


    /**
     * 风险规避
     *
     * @return
     */
    private boolean checkRisk() {
//
//        boolean checkNoXieSanJiaoRisk = Arrays.asList(PeriodTypeEnum.DAY1, PeriodTypeEnum.WEEK).stream()
//                .allMatch(periodType -> checkNoXieSanJiaoRisk(periodType));
//
//        if (checkNoXieSanJiaoRisk) {
//            return true;
//        }

        return true;

    }


    private boolean checkTrend(List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {

//        if ("ARCUSDT".equals(getSymbol())) {
//            System.out.println("NEARUSDT");
//        }

//        long checkTrendCount = trendPeriodTypes.stream()
//                .map(periodType -> checkMACDTrend(periodType))
//                .filter(checkResponse -> checkResponse.isSuccess())
//                .map(checkResponse -> {
//                    checkResult.addTrendPeriod(checkResponse.getPeriodType(), checkResponse.getMessage());
//                    return checkResponse;
//                })
//                .count();
//
//        if (checkTrendCount == trendPeriodTypes.size()) {
//            return true;
//        }
        return true;
    }


    private boolean checkSignal(List<PeriodTypeEnum> trendPeriodTypes, PeriodTypeEnum opPeriodType, CheckResult checkResult){

//        if ("ARCUSDT".equals(getSymbol())) {
//            System.out.println("NEARUSDT");
//        }

//        //KDJ反转
//        CheckResponse checkTurnRoundKDJ = checkTurnRoundKDJ(opPeriodType);
//        if (checkTurnRoundKDJ.isSuccess()) {
//            checkResult.addTurnRoundKDJ(checkTurnRoundKDJ.getPeriodType(), checkTurnRoundKDJ.getMessage());
//            return true;
//        }
//
//        //突破MACD阻力位
//        CheckResponse checkCrossGoldMACD = checkCrossGoldMACD(opPeriodType);
//        if (checkCrossGoldMACD.isSuccess()) {
//            checkResult.addCrossGoldMACD(checkCrossGoldMACD.getPeriodType(), checkCrossGoldMACD.getMessage());
//            return true;
//        }
//
//        //回踩MACD阻力位
//        CheckResponse checkDownCrossGoldMACD = checkDownCrossGoldMACD(opPeriodType);
//        if (checkDownCrossGoldMACD.isSuccess()) {
//            checkResult.addCrossGoldMACD(checkDownCrossGoldMACD.getPeriodType(), checkDownCrossGoldMACD.getMessage());
//            return true;
//        }

//        //凹突破
//        CheckResponse checkFanBao = checkFanBao(opPeriodType);
//        if (checkFanBao.isSuccess()) {
//            checkResult.addFanBao(checkFanBao.getPeriodType(), checkFanBao.getMessage());
//            return true;
//        }

//        //反包
//        boolean lastKFanBao = false;
//        CheckResponse checkFanBao = checkFanBao(opPeriodType, lastKFanBao);
//        if (checkFanBao.isSuccess()) {
//            checkResult.addFanBao(checkFanBao.getPeriodType(), checkFanBao.getMessage());
//            return true;
//        }

        // 梯子: cross[high]
        CheckResponse checkCrossTiZiHighSignal = checkCrossKeyPressureHighSignal(trendPeriodTypes, opPeriodType);
        if (checkCrossTiZiHighSignal.isSuccess()) {
            checkResult.addFanBao(checkCrossTiZiHighSignal.getOpPeriodType(), checkCrossTiZiHighSignal.getMessage());
            return true;
        }
//
//        // 梯子: 回踩[min]
//        CheckResponse checkBackCrossTiZiMinSignal = checkBackCrossTiZiMinSignal(trendPeriodTypes, opPeriodType);
//        if (checkBackCrossTiZiMinSignal.isSuccess()) {
//            checkResult.addFanBao(checkBackCrossTiZiMinSignal.getPeriodType(), checkBackCrossTiZiMinSignal.getMessage());
//            return true;
//        }
//
//
//        // 梯子: 内部上移[min : max]：包含cross
//        CheckResponse checkShangYiBetweenTiZiSignal = checkShangYiBetweenTiZiSignal(trendPeriodTypes, opPeriodType);
//        if (checkShangYiBetweenTiZiSignal.isSuccess()) {
//            checkResult.addFanBao(checkShangYiBetweenTiZiSignal.getPeriodType(), checkShangYiBetweenTiZiSignal.getMessage());
//            return true;
//        }

        return false;
    }

}
