package com.yh.bigdata.tts.spider.strategy.tools.cascade;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.strategy.tools.macd.MacdCrossBarResolver;
import com.yh.bigdata.tts.spider.strategy.tools.macd.MacdCrossStructureTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 级联交叉突破 · 日 K 边沿触发 + 上级周期 close 级联确认
 */
public final class CascadeBreakoutTools {

    private static final double HIGH_EPS = 1e-6;
    private static final double LOW_EPS = 1e-6;

    private CascadeBreakoutTools() {
    }

    public enum BreakoutPath {
        REF_HIGH,
        PREV_HIGH_LOW
    }

    @Getter
    public static final class TierHit {
        private final PeriodTypeEnum signalTier;
        private final MacdCrossStructureTools.CrossBar crossBar;
        private final Trade todayDayBar;
        private final Trade prevDayBar;
        private final BreakoutPath breakoutPath;

        TierHit(PeriodTypeEnum signalTier, MacdCrossStructureTools.CrossBar crossBar,
                Trade todayDayBar, Trade prevDayBar, BreakoutPath breakoutPath) {
            this.signalTier = signalTier;
            this.crossBar = crossBar;
            this.todayDayBar = todayDayBar;
            this.prevDayBar = prevDayBar;
            this.breakoutPath = breakoutPath != null ? breakoutPath : BreakoutPath.REF_HIGH;
        }

        public Trade getReferenceBar() {
            return crossBar != null ? crossBar.getBar() : null;
        }

        public MacdCrossStructureTools.CrossKind getCrossKind() {
            return crossBar != null ? crossBar.getKind() : null;
        }
    }

    public static TierHit findDayTierHit(StockBase stock, int lookbackDay, int lookbackWeek, boolean enableAltBreakout) {
        return findTierHit(stock, PeriodTypeEnum.DAY, lookbackDay, PeriodTypeEnum.WEEK, lookbackWeek, 0,
                enableAltBreakout);
    }

    public static TierHit findWeekTierHit(StockBase stock, int lookbackWeek, int lookbackMonth, int lookbackDay,
                                          boolean enableAltBreakout) {
        return findTierHit(stock, PeriodTypeEnum.WEEK, lookbackWeek, PeriodTypeEnum.MONTH, lookbackMonth, lookbackDay,
                enableAltBreakout);
    }

    public static TierHit findMonthTierHit(StockBase stock, int lookbackMonth, int lookbackDay,
                                           boolean enableAltBreakout) {
        return findTierHit(stock, PeriodTypeEnum.MONTH, lookbackMonth, null, 0, lookbackDay, enableAltBreakout);
    }

    private static TierHit findTierHit(StockBase stock, PeriodTypeEnum refPeriod, int refLookback,
                                       PeriodTypeEnum cascadePeriod, int cascadeLookback, int lookbackDay,
                                       boolean enableAltBreakout) {
        if (stock == null || refPeriod == null || refLookback < 1) {
            return null;
        }
        MacdCrossStructureTools.CrossBar cross = MacdCrossBarResolver.findLatestCrossBar(stock, refPeriod, refLookback);
        if (cross == null || cross.getBar() == null || cross.getBar().getHigh() == null) {
            return null;
        }
        Trade ref = cross.getBar();
        DayEdge edge = resolveDayEdge(stock);
        if (edge == null) {
            return null;
        }
        if (sameBar(ref, edge.todayDayBar)) {
            return null;
        }
        BreakoutPath path = resolveBreakoutPath(stock, refPeriod, refLookback, ref, edge, enableAltBreakout);
        if (path == null) {
            return null;
        }
        if (cascadePeriod != null) {
            if (!upperPeriodAboveCrossHigh(stock, cascadePeriod, cascadeLookback)) {
                return null;
            }
        }
        if (requiresDayRefHighFloor(refPeriod)) {
            if (!dayCloseAboveDayRefHigh(stock, lookbackDay, edge)) {
                return null;
            }
        }
        return new TierHit(refPeriod, cross, edge.todayDayBar, edge.prevDayBar, path);
    }

    /**
     * 突破路径并集：基准 high 边沿，或（本档前 K high 边沿 + 日 K low/high 约束）
     */
    private static BreakoutPath resolveBreakoutPath(StockBase stock, PeriodTypeEnum refPeriod, int refLookback,
                                                    Trade ref, DayEdge edge, boolean enableAltBreakout) {
        if (passesRefHighEdge(edge, ref)) {
            return BreakoutPath.REF_HIGH;
        }
        if (enableAltBreakout
                && passesPrevBarHighEdge(stock, refPeriod, refLookback, edge)
                && passesAltPathDayCloseRules(edge, ref)) {
            return BreakoutPath.PREV_HIGH_LOW;
        }
        return null;
    }

    private static boolean passesRefHighEdge(DayEdge edge, Trade ref) {
        if (edge == null || ref == null || ref.getHigh() == null) {
            return false;
        }
        double refHigh = ref.getHigh();
        return edge.prevClose <= refHigh + HIGH_EPS && edge.todayClose > refHigh + HIGH_EPS;
    }

    private static boolean passesPrevBarHighEdge(StockBase stock, PeriodTypeEnum refPeriod, int refLookback,
                                                 DayEdge edge) {
        if (stock == null || refPeriod == null || edge == null) {
            return false;
        }
        if (refPeriod == PeriodTypeEnum.DAY) {
            return passesDayCloseEdgeCrossPrevDayHigh(edge);
        }
        return passesDayCloseEdgeCrossPeriodBarBeforeLast(stock, refPeriod, refLookback, edge);
    }

    /** 日档：日 K 边沿突破前一根日 K high */
    private static boolean passesDayCloseEdgeCrossPrevDayHigh(DayEdge edge) {
        Trade prevDay = edge.prevDayBar;
        if (prevDay == null || prevDay.getHigh() == null) {
            return false;
        }
        double prevHigh = prevDay.getHigh();
        return edge.prevClose <= prevHigh + HIGH_EPS && edge.todayClose > prevHigh + HIGH_EPS;
    }

    /**
     * 周/月档：日 K 边沿突破「本周期最后一根 K 的前一根」high
     */
    private static boolean passesDayCloseEdgeCrossPeriodBarBeforeLast(StockBase stock, PeriodTypeEnum period,
                                                                      int lookback, DayEdge edge) {
        int fetchBars = Math.max(lookback + 3, period == PeriodTypeEnum.MONTH ? 40 : 80);
        List<Trade> periodBars = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        if (CollectionUtils.isEmpty(periodBars) || periodBars.size() < 2) {
            return false;
        }
        Trade anchor = periodBars.get(periodBars.size() - 2);
        if (anchor.getHigh() == null) {
            return false;
        }
        double anchorHigh = anchor.getHigh();
        return edge.prevClose <= anchorHigh + HIGH_EPS && edge.todayClose > anchorHigh + HIGH_EPS;
    }

    /**
     * 路径 B 附加：最后一根日 K close &gt; 本档 MACD 基准 low，且倒数第二根日 K close &lt;= 基准 high
     */
    private static boolean passesAltPathDayCloseRules(DayEdge edge, Trade ref) {
        if (edge == null || ref == null) {
            return false;
        }
        if (ref.getLow() == null || ref.getHigh() == null) {
            return false;
        }
        return edge.todayClose > ref.getLow() + LOW_EPS
                && edge.prevClose <= ref.getHigh() + HIGH_EPS;
    }

    /** 周/月档须日 close 已站上日基准 high，过滤纯大周期反弹 */
    private static boolean requiresDayRefHighFloor(PeriodTypeEnum refPeriod) {
        return refPeriod == PeriodTypeEnum.WEEK || refPeriod == PeriodTypeEnum.MONTH;
    }

    private static boolean dayCloseAboveDayRefHigh(StockBase stock, int lookbackDay, DayEdge edge) {
        if (stock == null || edge == null || lookbackDay < 1) {
            return false;
        }
        MacdCrossStructureTools.CrossBar dayCross =
                MacdCrossBarResolver.findLatestCrossBar(stock, PeriodTypeEnum.DAY, lookbackDay);
        if (dayCross == null || dayCross.getBar() == null || dayCross.getBar().getHigh() == null) {
            return false;
        }
        return edge.todayClose > dayCross.getBar().getHigh() + HIGH_EPS;
    }

    private static boolean upperPeriodAboveCrossHigh(StockBase stock, PeriodTypeEnum period, int lookback) {
        MacdCrossStructureTools.CrossBar cross = MacdCrossBarResolver.findLatestCrossBar(stock, period, lookback);
        if (cross == null || cross.getBar() == null || cross.getBar().getHigh() == null) {
            return false;
        }
        Trade current = RealtimeStockCache.getLastTrade(stock, period, 0);
        if (current == null || current.getClose() == null) {
            return false;
        }
        return current.getClose() > cross.getBar().getHigh() + HIGH_EPS;
    }

    private static DayEdge resolveDayEdge(StockBase stock) {
        List<Trade> dayBars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.DAY, 2);
        if (CollectionUtils.isEmpty(dayBars) || dayBars.size() < 2) {
            return null;
        }
        Trade prev = dayBars.get(dayBars.size() - 2);
        Trade today = dayBars.get(dayBars.size() - 1);
        Double prevClose = prev != null ? prev.getClose() : null;
        Double todayClose = resolveTodayClose(stock, today);
        if (prevClose == null || todayClose == null) {
            return null;
        }
        return new DayEdge(prev, today, prevClose, todayClose);
    }

    private static Double resolveTodayClose(StockBase stock, Trade todayBar) {
        if (todayBar != null && todayBar.getClose() != null) {
            return todayBar.getClose();
        }
        return stock != null ? stock.getClose() : null;
    }

    private static boolean sameBar(Trade a, Trade b) {
        return a != null && b != null && a.getDay() != null && a.getDay().equals(b.getDay());
    }

    private static final class DayEdge {
        private final Trade prevDayBar;
        private final Trade todayDayBar;
        private final double prevClose;
        private final double todayClose;

        DayEdge(Trade prevDayBar, Trade todayDayBar, double prevClose, double todayClose) {
            this.prevDayBar = prevDayBar;
            this.todayDayBar = todayDayBar;
            this.prevClose = prevClose;
            this.todayClose = todayClose;
        }
    }
}
