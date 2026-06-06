package com.yh.bigdata.tts.spider.strategy.group.band;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.dto.CheckResponse;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.spider.response.CheckResult;

import java.util.List;

public final class SignalBandToolsWrapper {

    /**
     * 突破 趋势波段
     */
    public static boolean checkCrossBandHighSignal(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, PeriodTypeEnum opPeriodType, CheckResult checkResult) {
        long checkTrendCount = trendPeriodTypes.stream()
                .map(trendPeriodType -> {
                            return SignalBandTools.checkCrossBandHighSignal(stockBase, trendPeriodType, opPeriodType);

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
    public static boolean checkRevertBandHighSignal(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, PeriodTypeEnum opPeriodType, CheckResult checkResult) {
        long checkTrendCount = trendPeriodTypes.stream()
                .map(trendPeriodType -> {
                            return SignalBandTools.checkRevertBandHighSignal(stockBase, trendPeriodType, opPeriodType);
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
