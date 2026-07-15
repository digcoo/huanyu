package com.yh.bigdata.tts.spider.strategy.tools.wavecc;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.AllYangGateTools;

/**
 * 年、周、月最后一根 K 须全部收阳（close &gt; open）。
 */
public final class YearWeekMonthYangGateTools {

    private YearWeekMonthYangGateTools() {
    }

    public static boolean passesGate(StockBase stock, CheckResult checkResult, String logPrefix) {
        if (stock == null) {
            return false;
        }
        boolean yearOk = passesPeriod(stock, PeriodTypeEnum.YEAR);
        boolean monthOk = passesPeriod(stock, PeriodTypeEnum.MONTH);
        boolean weekOk = passesPeriod(stock, PeriodTypeEnum.WEEK);
        if (checkResult != null) {
            appendMessage(checkResult, PeriodTypeEnum.YEAR, logPrefix,
                    yearOk ? "年收阳" : "年未收阳");
            appendMessage(checkResult, PeriodTypeEnum.MONTH, logPrefix,
                    monthOk ? "月收阳" : "月未收阳");
            appendMessage(checkResult, PeriodTypeEnum.WEEK, logPrefix,
                    weekOk ? "周收阳" : "周未收阳");
        }
        return yearOk && monthOk && weekOk;
    }

    static boolean passesPeriod(StockBase stock, PeriodTypeEnum period) {
        return AllYangGateTools.passesPeriod(stock, period);
    }

    static boolean passesPeriodOnLastBar(Trade lastBar) {
        return YangBandTools.isStrictYang(lastBar);
    }

    private static void appendMessage(CheckResult checkResult, PeriodTypeEnum period,
                                      String logPrefix, String msg) {
        if (checkResult != null) {
            String prefix = logPrefix != null ? logPrefix : "";
            checkResult.addTrendPeriod(period, prefix + msg);
        }
    }
}
