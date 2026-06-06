package com.yh.bigdata.tts.spider.strategy.tools;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.dto.CheckResponse;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.spider.response.CheckResult;

import java.util.List;

public final class SignalToolsWrapper {

    /**
     * 突破梯子High(底上移)
     */
    public static boolean checkCrossTiZiHighSignal(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, PeriodTypeEnum opPeriodType, CheckResult checkResult) {
        long checkTrendCount = trendPeriodTypes.stream()
                .map(periodType -> {
                            CheckResponse checkCrossPress1Signal = SignalTools.checkCrossTiZiHighSignal(stockBase, periodType, opPeriodType);

//                            CheckResponse checkOverMACDTrend = TrendTools.checkOverMACDTrend(stockBase, periodType);
//                            CheckResponse checkNoPressureTrend = TrendTools.checkNoPressureTrend(stockBase, periodType);
//                            CheckResponse checkRedTrend = TrendTools.checkRedTrend(stockBase, periodType);

                            checkCrossPress1Signal.setSuccess(checkCrossPress1Signal.isSuccess()
//                                    && checkOverMACDTrend.isSuccess()
//                                    && checkNoPressureTrend.isSuccess()
//                                    && checkRedTrend.isSuccess()
                            )
                            ;

                            return checkCrossPress1Signal;
                        }
                )
                .filter(CheckResponse::isSuccess)
                .map(checkResponse -> {
                    checkResult.addSignal(checkResponse.getPeriodType(), checkResponse.getMessage());
                    checkResult.setSortValue(checkResponse.getScore());
                    return checkResponse;
                })
                .count();

        if (checkTrendCount > 0) {
            return true;
        }

        return false;
    }

    /**
     * MACD金叉
     */
    public static boolean checkCrossGoldMACDSignal(StockBase stockBase, PeriodTypeEnum opPeriodType, CheckResult checkResult) {
        return SignalTools.checkCrossGoldMACDSignal(stockBase, opPeriodType).isSuccess();
    }

}
