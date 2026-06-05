package com.yh.bigdata.tts.spider.strategy.group.min30;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.dto.CheckResponse;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.spider.response.CheckResult;

import java.util.List;

public final class SignalMin30ToolsWrapper {

    /**
     * min30突破关键阻力位：非实时突破
     */
    public static boolean checkCrossZaoPanMin30HighSignal(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
        return SignalMin30Tools.checkCrossZaoPanMin30Signal(stockBase).isSuccess();
    }


    /**
     * 突破 趋势波段
     */
    public static boolean checkCrossBandStepSignal(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, PeriodTypeEnum opPeriodType, CheckResult checkResult, boolean realtimeFlag) {
        long checkTrendCount = trendPeriodTypes.stream()
                .map(trendPeriodType -> {
                            return SignalMin30Tools.checkCrossBandStepHighSignal(stockBase, trendPeriodType, realtimeFlag);

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
     * 突破 反转波段
     */
    public static boolean checkRevertBandStepSignal(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, PeriodTypeEnum opPeriodType, CheckResult checkResult, boolean realtimeFlag) {
        long checkTrendCount = trendPeriodTypes.stream()
                .map(trendPeriodType -> {
                            return SignalMin30Tools.checkRevertBandStepHighSignal(stockBase, trendPeriodType, realtimeFlag);
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
     * 新低翻红
     */
    public static boolean checkNewBottomRedSignal(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
        long checkTrendCount = trendPeriodTypes.stream()
                .map(trendPeriodType -> {
                            return SignalMin30Tools.checkNewBottomRedSignal(stockBase, trendPeriodType);
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
}
