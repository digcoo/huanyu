package com.yh.bigdata.tts.spider.strategy.tools.frictionless;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.BreakoutBarTools;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.BreakoutBucketTools;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.BreakoutScanWindowTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 跨周期内梯子上移 · 背景桶强K基准 + 柱内中间K约束 + 柱内突破信号
 */
public final class CrossPeriodInBarBreakoutTools {

    private static final double EPS = 1e-6;

    private CrossPeriodInBarBreakoutTools() {
    }

    @Getter
    public static final class TierSpec {
        private final PeriodTypeEnum period;
        private final Function<Trade, String> bucketKeyFn;
        private final int prevUnits;
        private final int maxBarsPerUnit;
        private final int fetchBars;
        private final double minStrongPct;
        private final boolean requireCurrentBreakout;

        public TierSpec(PeriodTypeEnum period, Function<Trade, String> bucketKeyFn,
                        int prevUnits, int maxBarsPerUnit, int fetchBars,
                        double minStrongPct, boolean requireCurrentBreakout) {
            this.period = period;
            this.bucketKeyFn = bucketKeyFn;
            this.prevUnits = prevUnits;
            this.maxBarsPerUnit = maxBarsPerUnit;
            this.fetchBars = fetchBars;
            this.minStrongPct = minStrongPct;
            this.requireCurrentBreakout = requireCurrentBreakout;
        }
    }

    @Getter
    public static final class Hit {
        private final PeriodTypeEnum period;
        private final Trade referenceBar;
        private final Trade signalBar;
        private final int scanWindowSize;
        private final int signalBarCount;

        Hit(PeriodTypeEnum period, Trade referenceBar, Trade signalBar,
            int scanWindowSize, int signalBarCount) {
            this.period = period;
            this.referenceBar = referenceBar;
            this.signalBar = signalBar;
            this.scanWindowSize = scanWindowSize;
            this.signalBarCount = signalBarCount;
        }
    }

    public static TierSpec dayTier(int prevWeeks, int maxDaysPerWeek, double minStrongPct,
                                   boolean requireCurrentBreakout) {
        return new TierSpec(
                PeriodTypeEnum.DAY,
                BreakoutBucketTools::weekKey,
                prevWeeks,
                maxDaysPerWeek,
                80,
                minStrongPct,
                requireCurrentBreakout);
    }

    public static TierSpec weekTier(int prevMonths, int maxWeeksPerMonth, double minStrongPct,
                                    boolean requireCurrentBreakout) {
        return new TierSpec(
                PeriodTypeEnum.WEEK,
                BreakoutBucketTools::monthKey,
                prevMonths,
                maxWeeksPerMonth,
                80,
                minStrongPct,
                requireCurrentBreakout);
    }

    public static TierSpec monthTier(int prevYears, int maxMonthsPerYear, double minStrongPct,
                                     boolean requireCurrentBreakout) {
        return new TierSpec(
                PeriodTypeEnum.MONTH,
                BreakoutBucketTools::yearKey,
                prevYears,
                maxMonthsPerYear,
                36,
                minStrongPct,
                requireCurrentBreakout);
    }

    /**
     * Min30 跨日桶：背景=前 N 个交易日 30m，信号=当日 30m（ul.prevDays / ul.maxBarsPerDay）
     */
    public static TierSpec min30DayBucketTier(int prevDays, int maxBarsPerDay, double minStrongPct,
                                              boolean requireCurrentBreakout) {
        return new TierSpec(
                PeriodTypeEnum.MIN30,
                BreakoutBucketTools::dayKey,
                prevDays,
                maxBarsPerDay,
                50,
                minStrongPct,
                requireCurrentBreakout);
    }

    public static Hit findHit(StockBase stock, TierSpec spec) {
        if (stock == null || spec == null) {
            return null;
        }
        List<Trade> allBars = RealtimeStockCache.getLastTrades(stock, spec.getPeriod(), spec.getFetchBars());
        if (CollectionUtils.isEmpty(allBars) || allBars.size() < 3) {
            return null;
        }
        Map<String, Integer> barIndexByDay = buildBarIndexByDay(allBars);

        BreakoutScanWindowTools.ScanWindow window = BreakoutScanWindowTools.buildScanWindow(
                allBars,
                spec.getBucketKeyFn(),
                spec.getPrevUnits(),
                spec.getMaxBarsPerUnit());
        List<Trade> priorBars = window.getPriorBars();
        List<Trade> signalBars = window.getSignalBars();
        if (priorBars.isEmpty() || signalBars.isEmpty()) {
            return null;
        }

        if (spec.isRequireCurrentBreakout()) {
            Trade signalBar = allBars.get(allBars.size() - 1);
            if (!containsBar(signalBars, signalBar)) {
                return null;
            }
            int signalIdx = allBars.size() - 1;
            Trade prevBar = allBars.get(signalIdx - 1);
            Trade refBar = findNearestRef(allBars, barIndexByDay, priorBars, signalBar, prevBar, signalIdx, spec.getMinStrongPct());
            if (refBar == null) {
                return null;
            }
            return new Hit(spec.getPeriod(), refBar, signalBar, window.totalSize(), signalBars.size());
        }

        Hit lastHit = null;
        for (Trade signalBar : signalBars) {
            int signalIdx = indexOfBar(barIndexByDay, signalBar);
            if (signalIdx <= 0) {
                continue;
            }
            Trade prevBar = allBars.get(signalIdx - 1);
            Trade refBar = findNearestRef(allBars, barIndexByDay, priorBars, signalBar, prevBar, signalIdx, spec.getMinStrongPct());
            if (refBar != null) {
                lastHit = new Hit(spec.getPeriod(), refBar, signalBar, window.totalSize(), signalBars.size());
            }
        }
        return lastHit;
    }

    static Trade findNearestRef(List<Trade> allBars, Map<String, Integer> barIndexByDay,
                                List<Trade> priorBars, Trade signalBar, Trade prevBar,
                                int signalIdx, double minStrongPct) {
        if (signalBar == null || prevBar == null || signalIdx <= 0) {
            return null;
        }
        Trade nearest = null;
        int nearestIdx = -1;
        for (Trade ref : priorBars) {
            if (ref == null || ref.getHigh() == null || ref.getLow() == null) {
                continue;
            }
            if (!BreakoutBarTools.isStrongBar(ref, minStrongPct)) {
                continue;
            }
            int refIdx = indexOfBar(barIndexByDay, ref);
            if (refIdx < 0 || refIdx >= signalIdx - 1) {
                continue;
            }
            if (!intermediateClosesAtMostRefHigh(allBars, refIdx, signalIdx, ref.getHigh())) {
                continue;
            }
            if (!matchesInBarSignal(signalBar, prevBar, ref)) {
                continue;
            }
            if (refIdx > nearestIdx) {
                nearestIdx = refIdx;
                nearest = ref;
            }
        }
        return nearest;
    }

    static boolean matchesInBarSignal(Trade signalBar, Trade prevBar, Trade refBar) {
        Double close = signalBar.getClose();
        Double low = signalBar.getLow();
        Double prevClose = prevBar != null ? prevBar.getClose() : null;
        Double prevHigh = prevBar != null ? prevBar.getHigh() : null;
        Double refLow = refBar.getLow();
        Double refHigh = refBar.getHigh();
        if (close == null || prevHigh == null || refLow == null || refHigh == null) {
            return false;
        }
        if (close <= prevHigh + EPS) {
            return false;
        }
        if (close + EPS < refLow) {
            return false;
        }
        double bound = refHigh;
        if (low != null && low <= bound + EPS) {
            return true;
        }
        return prevClose != null && prevClose <= bound + EPS;
    }

    static boolean intermediateClosesAtMostRefHigh(List<Trade> allBars, int refIdx, int signalIdx,
                                                   double refHigh) {
        for (int i = refIdx + 1; i < signalIdx; i++) {
            Double close = allBars.get(i).getClose();
            if (close == null || close > refHigh + EPS) {
                return false;
            }
        }
        return true;
    }

    private static boolean containsBar(List<Trade> bars, Trade target) {
        if (CollectionUtils.isEmpty(bars) || target == null) {
            return false;
        }
        for (Trade bar : bars) {
            if (sameBar(bar, target)) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, Integer> buildBarIndexByDay(List<Trade> allBars) {
        Map<String, Integer> indexByDay = new HashMap<>(allBars.size() * 2);
        for (int i = 0; i < allBars.size(); i++) {
            Trade bar = allBars.get(i);
            if (bar != null && bar.getDay() != null) {
                indexByDay.putIfAbsent(bar.getDay(), i);
            }
        }
        return indexByDay;
    }

    private static int indexOfBar(Map<String, Integer> barIndexByDay, Trade bar) {
        if (barIndexByDay == null || bar == null || bar.getDay() == null) {
            return -1;
        }
        Integer idx = barIndexByDay.get(bar.getDay());
        return idx != null ? idx : -1;
    }

    private static boolean sameBar(Trade a, Trade b) {
        return a != null && b != null && a.getDay() != null && a.getDay().equals(b.getDay());
    }
}
