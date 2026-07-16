package com.yh.bigdata.tts.spider.strategy.tools.macd;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MacdPositiveGateParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;

/**
 * 日/周/月 MACD&gt;0 可选门。
 */
public final class MacdPositiveGateTools {

    private MacdPositiveGateTools() {
    }

    public static boolean passGate(StockBase stock, CheckResult checkResult, MacdPositiveGateParams params) {
        MacdPositiveGateParams p = params != null ? params : MacdPositiveGateParams.defaults();
        if (!p.isRequireMonthMacd() && !p.isRequireWeekMacd() && !p.isRequireDayMacd()) {
            return true;
        }
        if (p.isRequireMonthMacd()) {
            if (!UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.MONTH)) {
                appendMessage(checkResult, PeriodTypeEnum.MONTH, "月MACD≤0");
                return false;
            }
            appendMessage(checkResult, PeriodTypeEnum.MONTH, "月MACD>0");
        }
        if (p.isRequireWeekMacd()) {
            if (!UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.WEEK)) {
                appendMessage(checkResult, PeriodTypeEnum.WEEK, "周MACD≤0");
                return false;
            }
            appendMessage(checkResult, PeriodTypeEnum.WEEK, "周MACD>0");
        }
        if (p.isRequireDayMacd()) {
            if (!UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.DAY)) {
                appendMessage(checkResult, PeriodTypeEnum.DAY, "日MACD≤0");
                return false;
            }
            appendMessage(checkResult, PeriodTypeEnum.DAY, "日MACD>0");
        }
        return true;
    }

    private static void appendMessage(CheckResult checkResult, PeriodTypeEnum period, String msg) {
        if (checkResult != null && period != null) {
            checkResult.addTrendPeriod(period, "[MG]" + msg);
        }
    }
}
