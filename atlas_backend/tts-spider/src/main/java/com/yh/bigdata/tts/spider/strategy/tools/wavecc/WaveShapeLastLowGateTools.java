package com.yh.bigdata.tts.spider.strategy.tools.wavecc;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 日/周/月须同时满足：本档最后一根 K 的 close &gt; 本档末完整波段的末阳底。
 */
public final class WaveShapeLastLowGateTools {

    private static final double EPS = 1e-6;

    private WaveShapeLastLowGateTools() {
    }

    public static boolean passesAll(StockBase stock, int lookbackDay, int lookbackWeek, int lookbackMonth) {
        return passesPeriod(stock, PeriodTypeEnum.DAY, lookbackDay)
                && passesPeriod(stock, PeriodTypeEnum.WEEK, lookbackWeek)
                && passesPeriod(stock, PeriodTypeEnum.MONTH, lookbackMonth);
    }

    /** 周或月：收盘价 &gt; 末完整波段末阳 K 的 low（满足其一即可） */
    public static boolean passesWeekOrMonth(StockBase stock, int lookbackWeek, int lookbackMonth) {
        return passesPeriod(stock, PeriodTypeEnum.WEEK, lookbackWeek)
                || passesPeriod(stock, PeriodTypeEnum.MONTH, lookbackMonth);
    }

    public static boolean passesPeriod(StockBase stock, PeriodTypeEnum period, int lookback) {
        if (stock == null || period == null || lookback < 3) {
            return false;
        }
        int fetchBars = Math.max(lookback + 2, lookback);
        List<Trade> periodBars = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        if (CollectionUtils.isEmpty(periodBars)) {
            return false;
        }
        YangBandTools.CompleteYangBand band = YangBandTools.findLastCompleteBand(periodBars, lookback);
        if (band == null || Double.isNaN(band.getLastLow())) {
            return false;
        }
        Double close = resolveCurrentClose(stock, period, periodBars);
        if (close == null) {
            return false;
        }
        return close > band.getLastLow() + EPS;
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
