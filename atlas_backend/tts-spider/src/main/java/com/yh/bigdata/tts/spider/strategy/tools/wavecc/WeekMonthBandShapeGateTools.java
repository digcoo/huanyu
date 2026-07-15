package com.yh.bigdata.tts.spider.strategy.tools.wavecc;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.response.CheckResult;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 周/月凹凸形态门：凹收阳且现价&gt;末波段 high，凸收阳且现价&gt;前 K high。
 */
public final class WeekMonthBandShapeGateTools {

    private static final double EPS = 1e-6;

    private WeekMonthBandShapeGateTools() {
    }

    public static boolean passesWeekMonthGate(StockBase stock, CheckResult checkResult,
                                              int lookbackWeek, int lookbackMonth, String logPrefix) {
        boolean weekOk = passesPeriodBandShape(stock, checkResult, PeriodTypeEnum.WEEK,
                lookbackWeek, logPrefix);
        boolean monthOk = passesPeriodBandShape(stock, checkResult, PeriodTypeEnum.MONTH,
                lookbackMonth, logPrefix);
        return weekOk && monthOk;
    }

    static boolean passesPeriodBandShapeOnBars(List<Trade> periodBars, CheckResult checkResult,
                                               PeriodTypeEnum period, int lookback, String logPrefix) {
        if (period == null || lookback < 3) {
            return false;
        }
        if (CollectionUtils.isEmpty(periodBars) || periodBars.size() < 2) {
            appendMessage(checkResult, period, logPrefix, periodLabel(period) + "无K");
            return false;
        }
        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(periodBars, lookback);
        if (bands.size() < 2) {
            appendMessage(checkResult, period, logPrefix, periodLabel(period) + "波段不足");
            return false;
        }
        YangBandTools.CompleteYangBand lastBand = bands.get(bands.size() - 1);
        YangBandTools.CompleteYangBand prevBand = bands.get(bands.size() - 2);
        Trade lastBar = periodBars.get(periodBars.size() - 1);
        Trade prevBar = periodBars.get(periodBars.size() - 2);
        if (!YangBandTools.isStrictYang(lastBar)) {
            appendMessage(checkResult, period, logPrefix, periodLabel(period) + "未收阳");
            return false;
        }
        Double close = resolveClose(periodBars);
        if (close == null) {
            return false;
        }
        boolean convex = WaveShapeTools.isConvex(lastBand, prevBand);
        if (convex) {
            Double prevHigh = prevBar != null ? prevBar.getHigh() : null;
            if (prevHigh == null || close <= prevHigh + EPS) {
                appendMessage(checkResult, period, logPrefix, periodLabel(period) + "凸未过前K高");
                return false;
            }
            appendMessage(checkResult, period, logPrefix, periodLabel(period) + "凸收阳破前K高");
            return true;
        }
        double bandHigh = lastBand.getBandHigh();
        if (Double.isNaN(bandHigh) || close <= bandHigh + EPS) {
            appendMessage(checkResult, period, logPrefix, periodLabel(period) + "凹未过末波段顶");
            return false;
        }
        appendMessage(checkResult, period, logPrefix, periodLabel(period) + "凹收阳破末波段顶");
        return true;
    }

    private static boolean passesPeriodBandShape(StockBase stock, CheckResult checkResult,
                                                 PeriodTypeEnum period, int lookback, String logPrefix) {
        if (stock == null || period == null || lookback < 3) {
            return false;
        }
        int fetchBars = Math.max(lookback + 2, lookback);
        List<Trade> periodBars = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        return passesPeriodBandShapeOnBars(periodBars, checkResult, period, lookback, logPrefix);
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
