package com.yh.bigdata.tts.spider.strategy.tools.ultralow;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.UltraShortStrategyParams;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 超短线策略 · 前两日 min30 基准K（A1 局部新高强K + A2 最近强K 回退）+ 当日突破K
 */
public final class UltraShortBreakoutTools {

    private static final double HIGH_EPS = 1e-6;
    private static final int FETCH_BARS = 50;

    private UltraShortBreakoutTools() {
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

    public static Hit findHit(StockBase stock, UltraShortStrategyParams params) {
        UltraShortStrategyParams p = params != null ? params : UltraShortStrategyParams.defaults();
        List<Trade> allBars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.MIN30, FETCH_BARS);
        if (CollectionUtils.isEmpty(allBars)) {
            return null;
        }

        BreakoutScanWindowTools.ScanWindow window = BreakoutScanWindowTools.buildScanWindow(
                allBars,
                BreakoutBucketTools::dayKey,
                p.getPrevDays(),
                p.getMaxBarsPerDay());
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

    /**
     * A1：突破日前各交易日内，最后一根「强K + 当日局部新高」；
     * A2：若 A1 无结果，取 prior 窗口内离 signal 最近的一根强K（自后向前）。
     */
    static Trade findReferenceBar(List<Trade> priorBars, double minStrongPct) {
        Trade ref = findLastLocalHighStrongRef(priorBars, minStrongPct);
        if (ref != null) {
            return ref;
        }
        return findNearestStrongRef(priorBars, minStrongPct);
    }

    /** A1：按交易日分组，强K 且 high 严格高于当日已出现最高价 → 取时间上最后一根 */
    static Trade findLastLocalHighStrongRef(List<Trade> priorBars, double minStrongPct) {
        Trade ref = null;
        String currentDay = null;
        double dayHighSoFar = -Double.MAX_VALUE;

        for (Trade bar : priorBars) {
            String day = BreakoutBucketTools.dayKey(bar);
            if (day == null || bar.getHigh() == null) {
                continue;
            }
            if (!day.equals(currentDay)) {
                currentDay = day;
                dayHighSoFar = -Double.MAX_VALUE;
            }
            double high = bar.getHigh();
            if (BreakoutBarTools.isStrongBar(bar, minStrongPct) && high > dayHighSoFar + HIGH_EPS) {
                ref = bar;
            }
            dayHighSoFar = Math.max(dayHighSoFar, high);
        }
        return ref;
    }

    /** A2：自 signal 日前一根起向前，最近的一根强K */
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

    /**
     * 当日 30m 强K：close &gt; 前K.high、close &gt; ref.low，
     * 且 sig.low 或 前K.close 至少其一 &lt; ref.high。
     * {@code requireCurrentBreakout=true} 时仅判定最新一根；否则当日任一根满足即可（多根取最后一根）。
     */
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

    /** 突破K.low 或 前K.close 至少其一低于 ref.high */
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
