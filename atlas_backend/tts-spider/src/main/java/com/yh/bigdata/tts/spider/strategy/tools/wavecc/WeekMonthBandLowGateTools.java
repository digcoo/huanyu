package com.yh.bigdata.tts.spider.strategy.tools.wavecc;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.response.CheckResult;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 周/月末波段底门：收盘价须 &gt; 末完整波段的波段底（波段内阳 K low 最小值）。
 */
public final class WeekMonthBandLowGateTools {

    private static final double EPS = 1e-6;

    private WeekMonthBandLowGateTools() {
    }

    public static boolean passesWeekMonthGate(StockBase stock, CheckResult checkResult,
                                              int lookbackWeek, int lookbackMonth, String logPrefix) {
        boolean weekOk = passesPeriodBandLow(stock, checkResult, PeriodTypeEnum.WEEK,
                lookbackWeek, logPrefix);
        boolean monthOk = passesPeriodBandLow(stock, checkResult, PeriodTypeEnum.MONTH,
                lookbackMonth, logPrefix);
        return weekOk && monthOk;
    }

    public static boolean passesPeriodBandLowOnBars(List<Trade> periodBars, CheckResult checkResult,
                                             PeriodTypeEnum period, int lookback, String logPrefix) {
        if (period == null || lookback < 3) {
            return false;
        }
        if (CollectionUtils.isEmpty(periodBars)) {
            appendMessage(checkResult, period, logPrefix, periodLabel(period) + "无K");
            return false;
        }
        YangBandTools.CompleteYangBand lastBand = YangBandTools.findLastCompleteBand(periodBars, lookback);
        if (lastBand == null || Double.isNaN(lastBand.getBandLow())) {
            appendMessage(checkResult, period, logPrefix, periodLabel(period) + "无末波段");
            return false;
        }
        Double close = resolveClose(periodBars);
        if (close == null) {
            return false;
        }
        if (close <= lastBand.getBandLow() + EPS) {
            appendMessage(checkResult, period, logPrefix, periodLabel(period) + "未过末波段底");
            return false;
        }
        appendMessage(checkResult, period, logPrefix, periodLabel(period) + "末波段底上");
        return true;
    }

    private static boolean passesPeriodBandLow(StockBase stock, CheckResult checkResult,
                                               PeriodTypeEnum period, int lookback, String logPrefix) {
        if (stock == null || period == null || lookback < 3) {
            return false;
        }
        int fetchBars = Math.max(lookback + 2, lookback);
        List<Trade> periodBars = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        return passesPeriodBandLowOnBars(periodBars, checkResult, period, lookback, logPrefix);
    }

    private static Double resolveClose(List<Trade> periodBars) {
        Trade lastBar = periodBars.get(periodBars.size() - 1);
        if (lastBar != null && lastBar.getClose() != null) {
            return lastBar.getClose();
        }
        return null;
    }

    private static void appendMessage(CheckResult checkResult, PeriodTypeEnum period,
                                      String logPrefix, String msg) {
        if (checkResult != null) {
            String prefix = logPrefix != null ? logPrefix : "";
            checkResult.addTrendPeriod(period, prefix + msg);
        }
    }

    private static String periodLabel(PeriodTypeEnum period) {
        if (period == PeriodTypeEnum.MONTH) {
            return "月";
        }
        if (period == PeriodTypeEnum.WEEK) {
            return "周";
        }
        return "日";
    }
}
