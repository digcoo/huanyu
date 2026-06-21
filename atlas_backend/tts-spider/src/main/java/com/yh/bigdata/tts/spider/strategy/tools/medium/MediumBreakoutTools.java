package com.yh.bigdata.tts.spider.strategy.tools.medium;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MediumStrategyParams;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.BreakoutBarTools;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.BreakoutBucketTools;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.BreakoutPositionContextTools;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.BreakoutScanWindowTools;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.BreakoutSignalTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 中线策略 · 前 N 自然月周K 基准（A1 月内局部新高强K + A2 最近强K）+ 本月周K 突破
 */
public final class MediumBreakoutTools {

    private static final double HIGH_EPS = 1e-6;
    private static final int FETCH_BARS = 80;

    private MediumBreakoutTools() {
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

    public static Hit findHit(StockBase stock, MediumStrategyParams params) {
        MediumStrategyParams p = params != null ? params : MediumStrategyParams.defaults();
        List<Trade> allBars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.WEEK, FETCH_BARS);
        if (CollectionUtils.isEmpty(allBars)) {
            return null;
        }

        BreakoutScanWindowTools.ScanWindow window = BreakoutScanWindowTools.buildScanWindow(
                allBars,
                BreakoutBucketTools::monthKey,
                p.getPrevMonths(),
                p.getMaxWeeksPerMonth());
        List<Trade> priorBars = window.getPriorBars();
        List<Trade> signalBars = window.getSignalBars();
        if (priorBars.isEmpty() || signalBars.isEmpty()) {
            return null;
        }

        Trade refBar = findReferenceBar(priorBars, p.getMinStrongPct());
        if (refBar == null || refBar.getLow() == null || refBar.getHigh() == null) {
            return null;
        }

        Trade signalBar = findSignalBar(stock, allBars, signalBars, refBar, p.getMinStrongPct(), p.isRequireCurrentBreakout());
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

    /** A1：按自然月分组，强K 且 high 严格高于该月已出现最高价 → 取时间上最后一根 */
    static Trade findLastLocalHighStrongRef(List<Trade> priorBars, double minStrongPct) {
        Trade ref = null;
        String currentMonth = null;
        double monthHighSoFar = -Double.MAX_VALUE;

        for (Trade bar : priorBars) {
            String month = BreakoutBucketTools.monthKey(bar);
            if (month == null || bar.getHigh() == null) {
                continue;
            }
            if (!month.equals(currentMonth)) {
                currentMonth = month;
                monthHighSoFar = -Double.MAX_VALUE;
            }
            double high = bar.getHigh();
            if (BreakoutBarTools.isStrongBar(bar, minStrongPct) && high > monthHighSoFar + HIGH_EPS) {
                ref = bar;
            }
            monthHighSoFar = Math.max(monthHighSoFar, high);
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

    static Trade findSignalBar(StockBase stock, List<Trade> allBars, List<Trade> signalBars, Trade refBar,
                               double minStrongPct, boolean requireCurrentBreakout) {
        if (CollectionUtils.isEmpty(allBars) || CollectionUtils.isEmpty(signalBars)
                || refBar == null || refBar.getLow() == null || refBar.getHigh() == null) {
            return null;
        }
        double refLow = refBar.getLow();
        double refHigh = refBar.getHigh();
        if (requireCurrentBreakout) {
            Trade currentBar = allBars.get(allBars.size() - 1);
            return BreakoutSignalTools.isSignalCandidate(stock, currentBar, allBars, refLow, refHigh,
                    minStrongPct, BreakoutPositionContextTools.MacroTier.MEDIUM) ? currentBar : null;
        }
        Trade lastMatch = null;
        for (Trade bar : signalBars) {
            if (BreakoutSignalTools.isSignalCandidate(stock, bar, allBars, refLow, refHigh, minStrongPct,
                    BreakoutPositionContextTools.MacroTier.MEDIUM)) {
                lastMatch = bar;
            }
        }
        return lastMatch;
    }
}
