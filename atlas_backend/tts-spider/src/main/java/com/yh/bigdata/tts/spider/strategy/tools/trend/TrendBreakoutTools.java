package com.yh.bigdata.tts.spider.strategy.tools.trend;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.TrendV2StrategyParams;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.BreakoutBarTools;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.BreakoutBucketTools;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.BreakoutScanWindowTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 短线策略 · 前 N 自然周日K 基准（A1 周内局部新高强K + A2 最近强K）+ 本周日K 突破
 */
public final class TrendBreakoutTools {

    private static final double HIGH_EPS = 1e-6;
    private static final int FETCH_BARS = 80;

    private TrendBreakoutTools() {
    }

    @Getter
    public static final class Hit {
        private final Trade referenceBar;
        private final Trade signalBar;
        private final int scanWindowSize;
        private final int signalBarCount;

        Hit(Trade referenceBar, Trade signalBar, int scanWindowSize, int signalBarCount) {
            this.referenceBar = referenceBar;
            this.signalBar = signalBar;
            this.scanWindowSize = scanWindowSize;
            this.signalBarCount = signalBarCount;
        }
    }

    public static Hit findHit(StockBase stock, TrendV2StrategyParams params) {
        TrendV2StrategyParams p = params != null ? params : TrendV2StrategyParams.defaults();
        List<Trade> allBars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.DAY, FETCH_BARS);
        if (CollectionUtils.isEmpty(allBars)) {
            return null;
        }

        BreakoutScanWindowTools.ScanWindow window = BreakoutScanWindowTools.buildScanWindow(
                allBars,
                BreakoutBucketTools::weekKey,
                p.getPrevWeeks(),
                p.getMaxDaysPerWeek());
        List<Trade> priorBars = window.getPriorBars();
        List<Trade> signalBars = window.getSignalBars();
        if (priorBars.isEmpty() || signalBars.isEmpty()) {
            return null;
        }

        Trade refBar = findReferenceBar(priorBars, p.getMinStrongPct());
        if (refBar == null || refBar.getLow() == null || refBar.getHigh() == null) {
            return null;
        }

        Trade signalBar = findSignalBar(allBars, signalBars, refBar, p.getMinStrongPct(), p.isRequireCurrentBreakout());
        if (signalBar == null) {
            return null;
        }

        return new Hit(refBar, signalBar, window.totalSize(), signalBars.size());
    }

    static Trade findReferenceBar(List<Trade> priorBars, double minStrongPct) {
        Trade ref = findLastLocalHighStrongRef(priorBars, minStrongPct);
        if (ref != null) {
            return ref;
        }
        return findNearestStrongRef(priorBars, minStrongPct);
    }

    /** A1：按自然周分组，强K 且 high 严格高于该周已出现最高价 → 取时间上最后一根 */
    static Trade findLastLocalHighStrongRef(List<Trade> priorBars, double minStrongPct) {
        Trade ref = null;
        String currentWeek = null;
        double weekHighSoFar = -Double.MAX_VALUE;

        for (Trade bar : priorBars) {
            String week = BreakoutBucketTools.weekKey(bar);
            if (week == null || bar.getHigh() == null) {
                continue;
            }
            if (!week.equals(currentWeek)) {
                currentWeek = week;
                weekHighSoFar = -Double.MAX_VALUE;
            }
            double high = bar.getHigh();
            if (BreakoutBarTools.isStrongBar(bar, minStrongPct) && high > weekHighSoFar + HIGH_EPS) {
                ref = bar;
            }
            weekHighSoFar = Math.max(weekHighSoFar, high);
        }
        return ref;
    }

    static Trade findNearestStrongRef(List<Trade> priorBars, double minStrongPct) {
        if (CollectionUtils.isEmpty(priorBars)) {
            return null;
        }
        for (int i = priorBars.size() - 1; i >= 0; i--) {
            Trade bar = priorBars.get(i);
            if (BreakoutBarTools.isStrongBar(bar, minStrongPct)) {
                return bar;
            }
        }
        return null;
    }

    static Trade findSignalBar(List<Trade> allBars, List<Trade> signalBars, Trade refBar,
                               double minStrongPct, boolean requireCurrentBreakout) {
        if (CollectionUtils.isEmpty(allBars) || CollectionUtils.isEmpty(signalBars)
                || refBar == null || refBar.getLow() == null || refBar.getHigh() == null) {
            return null;
        }
        double refLow = refBar.getLow();
        double refHigh = refBar.getHigh();
        if (requireCurrentBreakout) {
            Trade currentBar = allBars.get(allBars.size() - 1);
            return isSignalCandidate(currentBar, allBars, refLow, refHigh, minStrongPct) ? currentBar : null;
        }
        Trade lastMatch = null;
        for (Trade bar : signalBars) {
            if (isSignalCandidate(bar, allBars, refLow, refHigh, minStrongPct)) {
                lastMatch = bar;
            }
        }
        return lastMatch;
    }

    private static boolean isSignalCandidate(Trade bar, List<Trade> allBars,
                                             double refLow, double refHigh, double minStrongPct) {
        if (!BreakoutBarTools.isStrongBar(bar, minStrongPct)) {
            return false;
        }
        if (bar.getClose() == null || bar.getClose() <= refLow + HIGH_EPS) {
            return false;
        }
        Trade prev = previousBar(allBars, bar);
        if (prev == null || prev.getHigh() == null) {
            return false;
        }
        if (bar.getClose() <= prev.getHigh() + HIGH_EPS) {
            return false;
        }
        return belowRefHigh(bar, prev, refHigh);
    }

    private static boolean belowRefHigh(Trade signalBar, Trade prevBar, double refHigh) {
        boolean signalLowBelow = signalBar.getLow() != null && signalBar.getLow() < refHigh - HIGH_EPS;
        boolean prevCloseBelow = prevBar.getClose() != null && prevBar.getClose() < refHigh - HIGH_EPS;
        return signalLowBelow || prevCloseBelow;
    }

    private static Trade previousBar(List<Trade> allBars, Trade bar) {
        for (int i = 1; i < allBars.size(); i++) {
            if (sameBar(allBars.get(i), bar)) {
                return allBars.get(i - 1);
            }
        }
        return null;
    }

    private static boolean sameBar(Trade a, Trade b) {
        return a != null && b != null && a.getDay() != null && a.getDay().equals(b.getDay());
    }
}
