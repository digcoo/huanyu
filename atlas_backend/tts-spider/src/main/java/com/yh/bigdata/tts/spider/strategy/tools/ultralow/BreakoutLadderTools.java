package com.yh.bigdata.tts.spider.strategy.tools.ultralow;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.UltraLowReboundStrategyParams;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.function.Function;

/**
 * 突破阶梯 · 通用「基准K + 首根突破K」引擎（30m / 日 / 周 / 月）
 * 突破K：信号桶内首根强K，且 close &gt; 前一根 K 的 high，且 close &gt; 基准K的 low
 */
public final class BreakoutLadderTools {

    private static final double HIGH_EPS = 1e-6;

    private BreakoutLadderTools() {
    }

    public enum LadderTierKind {
        ULTRA, SHORT, MEDIUM, LONG
    }

    @Getter
    public static final class TierHit {
        private final LadderTierKind kind;
        private final Trade referenceBar;
        private final Trade signalBar;
        private final int scanWindowSize;
        private final int signalBarCount;

        TierHit(LadderTierKind kind, Trade referenceBar, Trade signalBar,
                int scanWindowSize, int signalBarCount) {
            this.kind = kind;
            this.referenceBar = referenceBar;
            this.signalBar = signalBar;
            this.scanWindowSize = scanWindowSize;
            this.signalBarCount = signalBarCount;
        }
    }

    public static TierHit findUltraHit(StockBase stock, UltraLowReboundStrategyParams p) {
        return findHit(stock, LadderTierKind.ULTRA, PeriodTypeEnum.MIN30, 50,
                BreakoutBucketTools::dayKey, BreakoutBucketTools::dayKey,
                p.getMin30PrevDays(), p.getMin30BarsPerDay(), p.getMin30BodyGainPct());
    }

    public static TierHit findShortHit(StockBase stock, UltraLowReboundStrategyParams p) {
        return findHit(stock, LadderTierKind.SHORT, PeriodTypeEnum.DAY, 80,
                BreakoutBucketTools::tradeWeekKey, BreakoutBucketTools::tradeWeekKey,
                p.getDayPrevWeeks(), p.getDayBarsPerWeek(), p.getDayBodyGainPct());
    }

    public static TierHit findMediumHit(StockBase stock, UltraLowReboundStrategyParams p) {
        return findHit(stock, LadderTierKind.MEDIUM, PeriodTypeEnum.WEEK, 60,
                BreakoutBucketTools::monthKey, BreakoutBucketTools::monthKey,
                p.getWeekPrevMonths(), p.getWeekBarsPerMonth(), p.getWeekBodyGainPct());
    }

    public static TierHit findLongHit(StockBase stock, UltraLowReboundStrategyParams p) {
        if (!p.isEnableLongPriceFilter()) {
            return findHitNoPriceFilter(stock, LadderTierKind.LONG, PeriodTypeEnum.MONTH, 36,
                    BreakoutBucketTools::yearKey, BreakoutBucketTools::yearKey,
                    p.getMonthPrevYears(), p.getMonthBarsPerYear(), p.getMonthBodyGainPct());
        }
        return findHit(stock, LadderTierKind.LONG, PeriodTypeEnum.MONTH, 36,
                BreakoutBucketTools::yearKey, BreakoutBucketTools::yearKey,
                p.getMonthPrevYears(), p.getMonthBarsPerYear(), p.getMonthBodyGainPct());
    }

    private static TierHit findHit(StockBase stock, LadderTierKind kind, PeriodTypeEnum period,
                                   int fetchBars, Function<Trade, String> scanUnitKey,
                                   Function<Trade, String> resetUnitKey,
                                   int prevUnits, int maxBarsPerUnit, double bodyPct) {
        TierHit raw = findHitCore(stock, kind, period, fetchBars, scanUnitKey, resetUnitKey,
                prevUnits, maxBarsPerUnit, bodyPct);
        if (raw == null || raw.getSignalBar() == null) {
            return null;
        }
        if (!isCurrentCloseNotAboveBreakoutHigh(stock, raw.getSignalBar())) {
            return null;
        }
        return raw;
    }

    private static TierHit findHitNoPriceFilter(StockBase stock, LadderTierKind kind, PeriodTypeEnum period,
                                                int fetchBars, Function<Trade, String> scanUnitKey,
                                                Function<Trade, String> resetUnitKey,
                                                int prevUnits, int maxBarsPerUnit, double bodyPct) {
        return findHitCore(stock, kind, period, fetchBars, scanUnitKey, resetUnitKey,
                prevUnits, maxBarsPerUnit, bodyPct);
    }

    private static TierHit findHitCore(StockBase stock, LadderTierKind kind, PeriodTypeEnum period,
                                       int fetchBars, Function<Trade, String> scanUnitKey,
                                       Function<Trade, String> resetUnitKey,
                                       int prevUnits, int maxBarsPerUnit, double bodyPct) {
        List<Trade> allBars = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        if (CollectionUtils.isEmpty(allBars)) {
            return null;
        }

        BreakoutScanWindowTools.ScanWindow window = BreakoutScanWindowTools.buildScanWindow(
                allBars, scanUnitKey, prevUnits, maxBarsPerUnit);
        List<Trade> priorBars = window.getPriorBars();
        List<Trade> signalBars = window.getSignalBars();
        if (priorBars.isEmpty() || signalBars.isEmpty()) {
            return null;
        }

        Trade refBar = findReferenceBar(priorBars, resetUnitKey, bodyPct);
        if (refBar == null) {
            return null;
        }

        Trade signalBar = findFirstSignalBar(allBars, signalBars, refBar, bodyPct);
        if (signalBar == null) {
            return null;
        }

        return new TierHit(kind, refBar, signalBar, window.totalSize(), signalBars.size());
    }

    static boolean isCurrentCloseNotAboveBreakoutHigh(StockBase stock, Trade signalBar) {
        if (stock == null || signalBar == null) {
            return false;
        }
        Double currentClose = stock.getClose();
        Double breakoutHigh = signalBar.getHigh();
        if (currentClose == null || breakoutHigh == null) {
            return false;
        }
        return currentClose <= breakoutHigh;
    }

    static Trade findReferenceBar(List<Trade> priorBars, Function<Trade, String> resetUnitKey, double minPct) {
        Trade ref = null;
        String currentUnit = null;
        double unitHighSoFar = -Double.MAX_VALUE;

        for (Trade bar : priorBars) {
            String unit = resetUnitKey.apply(bar);
            if (unit == null || bar.getHigh() == null) {
                continue;
            }
            if (!unit.equals(currentUnit)) {
                currentUnit = unit;
                unitHighSoFar = -Double.MAX_VALUE;
            }
            double high = bar.getHigh();
            if (BreakoutBarTools.isStrongBar(bar, minPct) && high > unitHighSoFar + HIGH_EPS) {
                ref = bar;
            }
            unitHighSoFar = Math.max(unitHighSoFar, high);
        }
        return ref;
    }

    static Trade findFirstSignalBar(List<Trade> allBars, List<Trade> signalBars, Trade refBar,
                                      double minPct) {
        if (CollectionUtils.isEmpty(allBars) || CollectionUtils.isEmpty(signalBars)
                || refBar == null || refBar.getLow() == null) {
            return null;
        }
        double refLow = refBar.getLow();
        for (Trade bar : signalBars) {
            if (!BreakoutBarTools.isStrongBar(bar, minPct)) {
                continue;
            }
            if (bar.getClose() == null) {
                continue;
            }
            if (bar.getClose() <= refLow + HIGH_EPS) {
                continue;
            }
            Trade prev = previousBar(allBars, bar);
            if (prev == null || prev.getHigh() == null) {
                continue;
            }
            if (bar.getClose() > prev.getHigh() + HIGH_EPS) {
                return bar;
            }
        }
        return null;
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
