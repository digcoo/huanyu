package com.yh.bigdata.tts.spider.strategy.tools.macd;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.strategy.tools.StockEvaluationScratchpad;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * MACD 交叉 K 解析 · 单股评估内复用 MACD 序列。
 */
public final class MacdCrossBarResolver {

    private static final int FETCH_PADDING = 40;
    private static final int LOOKBACK_DAY = 120;
    private static final int LOOKBACK_WEEK = 104;
    private static final int LOOKBACK_MONTH = 60;

    private MacdCrossBarResolver() {
    }

    public static final class MacdSeries {
        private final List<Trade> trades;
        private final List<MACDIndicatorUtils.MACDPoint> points;

        MacdSeries(List<Trade> trades, List<MACDIndicatorUtils.MACDPoint> points) {
            this.trades = trades;
            this.points = points;
        }

        public List<Trade> getTrades() {
            return trades;
        }

        public List<MACDIndicatorUtils.MACDPoint> getPoints() {
            return points;
        }
    }

    public static MacdCrossStructureTools.CrossBar findLatestCrossBar(StockBase stock, PeriodTypeEnum period,
                                                                      int lookback) {
        if (stock == null || period == null || lookback < 1) {
            return null;
        }
        MacdSeries series = loadSeries(stock, period);
        if (series == null) {
            return null;
        }
        return MacdCrossStructureTools.findLatestCrossBar(series.getTrades(), series.getPoints(), lookback);
    }

    public static MacdCrossStructureTools.CrossBar findLatestCrossBar(List<Trade> trades, int lookback) {
        if (CollectionUtils.isEmpty(trades) || lookback < 1) {
            return null;
        }
        List<MACDIndicatorUtils.MACDPoint> points = MACDIndicatorUtils.calculateMACD(Ticker.from(trades));
        return MacdCrossStructureTools.findLatestCrossBar(trades, points, lookback);
    }

    static MacdSeries loadSeries(StockBase stock, PeriodTypeEnum period) {
        int fetchBars = fetchBarsFor(period);
        String key = stock.getCode() + "|" + period.name() + "|" + fetchBars;
        StockEvaluationScratchpad.State scratch = StockEvaluationScratchpad.get();
        MacdSeries cached = scratch.macdSeries(key);
        if (cached != null) {
            return cached;
        }
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        if (CollectionUtils.isEmpty(trades)) {
            return null;
        }
        List<MACDIndicatorUtils.MACDPoint> points = MACDIndicatorUtils.calculateMACD(Ticker.from(trades));
        if (CollectionUtils.isEmpty(points) || points.size() != trades.size()) {
            return null;
        }
        MacdSeries series = new MacdSeries(trades, points);
        scratch.putMacdSeries(key, series);
        return series;
    }

    static int fetchBarsFor(PeriodTypeEnum period) {
        return gateLookbackFor(period) + FETCH_PADDING;
    }

    public static int gateLookbackFor(PeriodTypeEnum period) {
        if (period == PeriodTypeEnum.MONTH) {
            return LOOKBACK_MONTH;
        }
        if (period == PeriodTypeEnum.WEEK) {
            return LOOKBACK_WEEK;
        }
        return LOOKBACK_DAY;
    }
}
