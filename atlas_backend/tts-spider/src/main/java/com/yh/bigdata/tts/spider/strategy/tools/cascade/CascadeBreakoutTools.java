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

    private CascadeBreakoutTools() {
    }

    @Getter
    public static final class TierHit {
        private final PeriodTypeEnum signalTier;
        private final MacdCrossStructureTools.CrossBar crossBar;
        private final Trade todayDayBar;
        private final Trade prevDayBar;

        TierHit(PeriodTypeEnum signalTier, MacdCrossStructureTools.CrossBar crossBar,
                Trade todayDayBar, Trade prevDayBar) {
            this.signalTier = signalTier;
            this.crossBar = crossBar;
            this.todayDayBar = todayDayBar;
            this.prevDayBar = prevDayBar;
        }

        public Trade getReferenceBar() {
            return crossBar != null ? crossBar.getBar() : null;
        }

        public MacdCrossStructureTools.CrossKind getCrossKind() {
            return crossBar != null ? crossBar.getKind() : null;
        }
    }

    public static TierHit findDayTierHit(StockBase stock, int lookbackDay, int lookbackWeek) {
        return findTierHit(stock, PeriodTypeEnum.DAY, lookbackDay, PeriodTypeEnum.WEEK, lookbackWeek, 0);
    }

    public static TierHit findWeekTierHit(StockBase stock, int lookbackWeek, int lookbackMonth, int lookbackDay) {
        return findTierHit(stock, PeriodTypeEnum.WEEK, lookbackWeek, PeriodTypeEnum.MONTH, lookbackMonth, lookbackDay);
    }

    public static TierHit findMonthTierHit(StockBase stock, int lookbackMonth, int lookbackDay) {
        return findTierHit(stock, PeriodTypeEnum.MONTH, lookbackMonth, null, 0, lookbackDay);
    }

    private static TierHit findTierHit(StockBase stock, PeriodTypeEnum refPeriod, int refLookback,
                                       PeriodTypeEnum cascadePeriod, int cascadeLookback, int lookbackDay) {
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
        double refHigh = ref.getHigh();
        if (edge.prevClose > refHigh + HIGH_EPS) {
            return null;
        }
        if (edge.todayClose <= refHigh + HIGH_EPS) {
            return null;
        }
        if (sameBar(ref, edge.todayDayBar)) {
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
        return new TierHit(refPeriod, cross, edge.todayDayBar, edge.prevDayBar);
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
