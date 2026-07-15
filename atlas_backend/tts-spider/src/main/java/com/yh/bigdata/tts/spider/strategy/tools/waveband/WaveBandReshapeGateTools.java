package com.yh.bigdata.tts.spider.strategy.tools.waveband;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 波段策略形态门：月/周 K 按凸凹形态约束当前 K 收阳与收盘价区间；日 K 收阳为信号。
 */
public final class WaveBandReshapeGateTools {

    private static final double EPS = 1e-6;

    private WaveBandReshapeGateTools() {
    }

    public static boolean passesPeriodGate(StockBase stock, CheckResult checkResult,
                                           PeriodTypeEnum period, int lookback) {
        if (stock == null || period == null || lookback < 3) {
            return false;
        }
        int fetchBars = Math.max(lookback + 2, lookback);
        List<Trade> periodBars = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        if (CollectionUtils.isEmpty(periodBars) || periodBars.size() < 2) {
            return false;
        }
        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(periodBars, lookback);
        if (bands.size() < 2) {
            appendPeriodMessage(checkResult, period, periodLabel(period) + "波段不足");
            return false;
        }
        Trade current = periodBars.get(periodBars.size() - 1);
        Trade prevBar = periodBars.get(periodBars.size() - 2);
        if (!YangBandTools.isStrictYang(current)) {
            appendPeriodMessage(checkResult, period, periodLabel(period) + "K未收阳");
            return false;
        }
        Double close = resolveClose(stock, period, current);
        if (close == null) {
            return false;
        }
        YangBandTools.CompleteYangBand lastBand = bands.get(bands.size() - 1);
        YangBandTools.CompleteYangBand prevBand = bands.get(bands.size() - 2);
        boolean convex = WaveShapeTools.isConvex(lastBand, prevBand);
        boolean pass = convex
                ? passesConvexPeriod(close, prevBar, lastBand, prevBand)
                : passesConcavePeriod(close, prevBar, lastBand, prevBand);
        if (checkResult != null) {
            String shape = convex ? "凸" : "凹";
            appendPeriodMessage(checkResult, period,
                    pass ? periodLabel(period) + shape + "波段门" : periodLabel(period) + shape + "波段门未过");
        }
        return pass;
    }

    public static boolean passesDayYangSignal(StockBase stock, CheckResult checkResult) {
        if (stock == null) {
            return false;
        }
        List<Trade> dayBars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.DAY, 1);
        Trade today = CollectionUtils.isEmpty(dayBars) ? null : dayBars.get(dayBars.size() - 1);
        boolean pass = YangBandTools.isStrictYang(today);
        if (checkResult != null) {
            checkResult.addSignal(PeriodTypeEnum.DAY, pass ? "日K收阳" : "日K未收阳");
        }
        return pass;
    }

    private static boolean passesConvexPeriod(double close, Trade prevBar,
                                              YangBandTools.CompleteYangBand lastBand,
                                              YangBandTools.CompleteYangBand prevBand) {
        Double prevHigh = prevBar != null ? prevBar.getHigh() : null;
        if (prevHigh == null || close <= prevHigh + EPS) {
            return false;
        }
        if (!aboveLow(close, lastBand) || !aboveLow(close, prevBand)) {
            return false;
        }
        return belowMaxBandHigh(close, lastBand, prevBand);
    }

    private static boolean passesConcavePeriod(double close, Trade prevBar,
                                               YangBandTools.CompleteYangBand lastBand,
                                               YangBandTools.CompleteYangBand prevBand) {
        Double prevHigh = prevBar != null ? prevBar.getHigh() : null;
        if (prevHigh == null || close <= prevHigh + EPS) {
            return false;
        }
        if (!aboveLow(close, lastBand) || !aboveLow(close, prevBand)) {
            return false;
        }
        return belowMaxBandHigh(close, lastBand, prevBand);
    }

    private static boolean belowMaxBandHigh(double close,
                                            YangBandTools.CompleteYangBand lastBand,
                                            YangBandTools.CompleteYangBand prevBand) {
        if (lastBand == null || prevBand == null) {
            return false;
        }
        double lastHigh = lastBand.getBandHigh();
        double prevHigh = prevBand.getBandHigh();
        if (Double.isNaN(lastHigh) || Double.isNaN(prevHigh)) {
            return false;
        }
        return close < Math.max(lastHigh, prevHigh) - EPS;
    }

    private static boolean aboveLow(double close, YangBandTools.CompleteYangBand band) {
        if (band == null || Double.isNaN(band.getBandLow())) {
            return false;
        }
        return close > band.getBandLow() + EPS;
    }

    private static Double resolveClose(StockBase stock, PeriodTypeEnum period, Trade current) {
        if (current != null && current.getClose() != null) {
            return current.getClose();
        }
        if (period == PeriodTypeEnum.DAY && stock != null && stock.getClose() != null) {
            return stock.getClose();
        }
        return null;
    }

    private static void appendPeriodMessage(CheckResult checkResult, PeriodTypeEnum period, String msg) {
        if (checkResult != null) {
            checkResult.addTrendPeriod(period, msg);
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
