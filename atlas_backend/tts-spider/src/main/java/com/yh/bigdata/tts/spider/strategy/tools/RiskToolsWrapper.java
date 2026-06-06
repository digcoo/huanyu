package com.yh.bigdata.tts.spider.strategy.tools;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.dto.CheckResponse;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.spider.response.CheckResult;

import java.util.List;

public final class RiskToolsWrapper {
    /**
     * 风险： 跌破支撑（Low）
     */
    public static boolean checkNotUnderLowRisk(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
        long checkRiskCount = trendPeriodTypes.stream()
                .map(periodType -> RiskTools.checkNotUnderLowRisk(stockBase, periodType))
                .filter(CheckResponse::isSuccess)
                .map(checkResponse -> {
                    checkResult.addRiskPeriod(checkResponse.getPeriodType(), checkResponse.getMessage());
                    return checkResponse;
                })
                .count();

        if (checkRiskCount >= 2) {
            return true;
        }

        return false;

    }

    /**
     * 风险： 斜三角风险
     */
    public static boolean checkNoXieSanJiaoRisk(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
        long checkRiskCount = trendPeriodTypes.stream()
                .map(periodType -> RiskTools.checkNoXieSanJiaoRisk(stockBase, periodType))
                .filter(CheckResponse::isSuccess)
                .map(checkResponse -> {
                    checkResult.addRiskPeriod(checkResponse.getPeriodType(), checkResponse.getMessage());
                    return checkResponse;
                })
                .count();

        return checkRiskCount > 0;

    }
}
