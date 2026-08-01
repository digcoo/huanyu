package com.yh.bigdata.tts.spider.strategy.tools;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;

/**
 * 信号档位对应的父周期 MACD&gt;0：日→周，周→月，月→年。
 */
public final class ParentPeriodMacdPositiveGateTools {

    private ParentPeriodMacdPositiveGateTools() {
    }

    public static PeriodTypeEnum parentPeriod(PeriodTypeEnum signalPeriod) {
        if (signalPeriod == PeriodTypeEnum.DAY) {
            return PeriodTypeEnum.WEEK;
        }
        if (signalPeriod == PeriodTypeEnum.WEEK) {
            return PeriodTypeEnum.MONTH;
        }
        if (signalPeriod == PeriodTypeEnum.MONTH) {
            return PeriodTypeEnum.YEAR;
        }
        return null;
    }

    public static boolean passes(StockBase stock, PeriodTypeEnum signalPeriod) {
        PeriodTypeEnum parent = parentPeriod(signalPeriod);
        if (stock == null || parent == null) {
            return false;
        }
        return UnilateralMacdTools.isMacdPositive(stock, parent);
    }

    public static boolean passWithMessage(StockBase stock, CheckResult checkResult,
                                          PeriodTypeEnum signalPeriod, String strategyTag) {
        PeriodTypeEnum parent = parentPeriod(signalPeriod);
        if (parent == null) {
            return false;
        }
        if (passes(stock, signalPeriod)) {
            return true;
        }
        if (checkResult != null && signalPeriod != null) {
            String tag = strategyTag != null ? strategyTag : "";
            checkResult.addTrendPeriod(signalPeriod,
                    tag + "父" + periodLabel(parent) + "MACD≤0");
        }
        return false;
    }

    private static String periodLabel(PeriodTypeEnum period) {
        if (period == PeriodTypeEnum.YEAR) {
            return "年";
        }
        if (period == PeriodTypeEnum.MONTH) {
            return "月";
        }
        if (period == PeriodTypeEnum.WEEK) {
            return "周";
        }
        return "日";
    }
}
