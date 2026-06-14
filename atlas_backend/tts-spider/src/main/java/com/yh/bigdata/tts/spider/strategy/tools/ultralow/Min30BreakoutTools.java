package com.yh.bigdata.tts.spider.strategy.tools.ultralow;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.UltraLowReboundStrategyParams;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 超短线 · 前日/前2日新高强K + 当日 30m 突破前高且收盘站上前实体
 */
public final class Min30BreakoutTools {

    private static final int FETCH_BARS = 50;
    private static final double HIGH_EPS = 1e-6;

    private Min30BreakoutTools() {
    }

    public static BreakoutHit findBreakout(StockBase stock, UltraLowReboundStrategyParams params) {
        UltraLowReboundStrategyParams p = params != null ? params : UltraLowReboundStrategyParams.defaults();
        List<Trade> allBars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.MIN30, FETCH_BARS);
        if (CollectionUtils.isEmpty(allBars)) {
            return null;
        }

        List<Trade> window = Min30ScanWindowTools.buildScanWindow(
                allBars, p.getMin30PrevDays(), p.getMin30BarsPerDay());
        if (CollectionUtils.isEmpty(window)) {
            return null;
        }

        String todayKey = Min30ScanWindowTools.dayKey(allBars.get(allBars.size() - 1));
        List<Trade> priorBars = new ArrayList<>();
        List<Trade> todayBars = new ArrayList<>();
        for (Trade bar : window) {
            if (todayKey.equals(Min30ScanWindowTools.dayKey(bar))) {
                todayBars.add(bar);
            } else {
                priorBars.add(bar);
            }
        }
        if (priorBars.isEmpty() || todayBars.isEmpty()) {
            return null;
        }

        double priorPeakHigh = priorBars.stream()
                .filter(b -> b.getHigh() != null)
                .mapToDouble(Trade::getHigh)
                .max()
                .orElse(0);
        if (priorPeakHigh <= 0) {
            return null;
        }

        Trade refBar = findReferenceBar(priorBars, priorPeakHigh, p.getMin30BodyGainPct());
        if (refBar == null) {
            return null;
        }

        Trade signalBar = findTodaySignalBar(todayBars, refBar, p.getMin30BodyGainPct());
        if (signalBar == null) {
            return null;
        }

        return new BreakoutHit(refBar, signalBar, priorPeakHigh, window.size(),
                Min30ScanWindowTools.countTodayBars(allBars));
    }

    /** 前1~2日：大阳线或大涨幅，且 high 为区间新高（不含当日） */
    static Trade findReferenceBar(List<Trade> priorBars, double priorPeakHigh, double minPct) {
        Trade ref = null;
        for (Trade bar : priorBars) {
            if (bar.getHigh() == null || bar.getHigh() < priorPeakHigh - HIGH_EPS) {
                continue;
            }
            if (!Min30BarTools.isStrongBar(bar, minPct)) {
                continue;
            }
            ref = bar;
        }
        return ref;
    }

    /** 当日：大阳线或大涨幅，high > 前 high，close > 前 bodyMax */
    static Trade findTodaySignalBar(List<Trade> todayBars, Trade refBar, double minPct) {
        Double refHigh = refBar.getHigh();
        Double refBodyMax = refBar.getShitiMax();
        if (refHigh == null || refBodyMax == null) {
            return null;
        }
        for (int i = todayBars.size() - 1; i >= 0; i--) {
            Trade bar = todayBars.get(i);
            if (!Min30BarTools.isStrongBar(bar, minPct)) {
                continue;
            }
            if (bar.getHigh() == null || bar.getClose() == null) {
                continue;
            }
            if (bar.getHigh() > refHigh && bar.getClose() > refBodyMax) {
                return bar;
            }
        }
        return null;
    }

    @Getter
    public static final class BreakoutHit {
        private final Trade referenceBar;
        private final Trade signalBar;
        private final double priorPeakHigh;
        private final int scanWindowSize;
        private final int todayBarCount;

        public BreakoutHit(Trade referenceBar, Trade signalBar, double priorPeakHigh,
                           int scanWindowSize, int todayBarCount) {
            this.referenceBar = referenceBar;
            this.signalBar = signalBar;
            this.priorPeakHigh = priorPeakHigh;
            this.scanWindowSize = scanWindowSize;
            this.todayBarCount = todayBarCount;
        }
    }
}
