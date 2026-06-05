package com.yh.bigdata.tts.spider.strategy.tools;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.dto.CheckResponse;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.spider.response.CheckResult;

import java.util.List;

public final class PriorityToolsWrapper {

    /**
     * 偏好： 中位数以上
     */
    public static boolean checkBaseLinePriority(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
        long checkTrendCount = trendPeriodTypes.stream()
                .map(periodType -> PriorityTools.checkBaseLinePriority(stockBase, periodType))
                .filter(CheckResponse::isSuccess)
                .map(checkResponse -> {
                    checkResult.addTrendPeriod(checkResponse.getPeriodType(), checkResponse.getMessage());
                    checkResult.setSortValue(checkResponse.getScore());
                    return checkResponse;
                })
                .count();

        if (checkTrendCount >= 2) {
            return true;
        }

        return false;
    }


    /**
     * 偏好： 平均成交额>8000万
     */
    public static boolean checkBaseAmountPriority(StockBase stockBase, CheckResult checkResult) {
        return PriorityTools.checkBaseAmountPriority(stockBase).isSuccess();
    }

}
