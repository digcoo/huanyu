package com.yh.bigdata.tts.spider.strategy.tools.wavecc;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 趋势门：按信号档（日/周/月）判定末波段 High 最高阳 K 与次末波段的相对强弱及现价突破。
 * <p>
 * 末波段 peak 收盘 &gt; 次末 peak 收盘：现价须 &gt; 次末 peak 的 high；
 * 否则现价须 &gt; 末波段 peak 的 high。
 */
public final class YangBandTrendGateTools {

    private static final double EPS = 1e-6;

    private YangBandTrendGateTools() {
    }

    public static PeriodTypeEnum resolveSignalTier(boolean enableDay, boolean enableWeek, boolean enableMonth) {
        if (enableMonth && !enableDay && !enableWeek) {
            return PeriodTypeEnum.MONTH;
        }
        if (enableWeek && !enableDay && !enableMonth) {
            return PeriodTypeEnum.WEEK;
        }
        return PeriodTypeEnum.DAY;
    }

    public static int resolveLookback(PeriodTypeEnum period, int lookbackDay, int lookbackWeek, int lookbackMonth) {
        if (period == PeriodTypeEnum.MONTH) {
            return lookbackMonth;
        }
        if (period == PeriodTypeEnum.WEEK) {
            return lookbackWeek;
        }
        return lookbackDay;
    }

    public static boolean passesSignalTier(StockBase stock, PeriodTypeEnum signalPeriod,
                                           int lookbackDay, int lookbackWeek, int lookbackMonth) {
        if (signalPeriod == null) {
            return false;
        }
        int lookback = resolveLookback(signalPeriod, lookbackDay, lookbackWeek, lookbackMonth);
        return passesPeriod(stock, signalPeriod, lookback);
    }

    static boolean passesPeriod(StockBase stock, PeriodTypeEnum period, int lookback) {
        if (stock == null || period == null || lookback < 3) {
            return false;
        }
        int fetchBars = Math.max(lookback + 2, lookback);
        List<Trade> periodBars = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        if (CollectionUtils.isEmpty(periodBars)) {
            return false;
        }
        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(periodBars, lookback);
        if (bands.size() < 2) {
            return false;
        }
        YangBandTools.CompleteYangBand lastBand = bands.get(bands.size() - 1);
        YangBandTools.CompleteYangBand prevBand = bands.get(bands.size() - 2);
        Trade lastPeakYang = YangBandTools.findHighestHighYangInBand(periodBars, lastBand);
        Trade prevPeakYang = YangBandTools.findHighestHighYangInBand(periodBars, prevBand);
        if (lastPeakYang == null || prevPeakYang == null
                || lastPeakYang.getClose() == null || prevPeakYang.getClose() == null
                || lastPeakYang.getHigh() == null || prevPeakYang.getHigh() == null) {
            return false;
        }

        Double currentClose = resolveCurrentClose(stock, period, periodBars);
        if (currentClose == null) {
            return false;
        }

        boolean peakCloseUplift = lastPeakYang.getClose() > prevPeakYang.getClose() + EPS;
        if (peakCloseUplift) {
            return currentClose > prevPeakYang.getHigh() + EPS;
        }
        return currentClose > lastPeakYang.getHigh() + EPS;
    }

    private static Double resolveCurrentClose(StockBase stock, PeriodTypeEnum period, List<Trade> periodBars) {
        Trade current = periodBars.get(periodBars.size() - 1);
        if (current != null && current.getClose() != null) {
            return current.getClose();
        }
        if (period == PeriodTypeEnum.DAY && stock != null && stock.getClose() != null) {
            return stock.getClose();
        }
        return null;
    }
}
