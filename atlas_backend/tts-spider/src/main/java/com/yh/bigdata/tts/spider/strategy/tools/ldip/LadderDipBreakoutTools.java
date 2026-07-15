package com.yh.bigdata.tts.spider.strategy.tools.ldip;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.BreakoutBarTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 级联梯子探底回升 · 基准大阳 + 中间破 low + 最后一根 high 抬升
 */
public final class LadderDipBreakoutTools {

    private static final double HIGH_EPS = 1e-6;
    private static final double LOW_EPS = 1e-6;

    private static final double REF_BODY_DAY = 0.025;
    private static final double REF_BODY_WEEK = 0.035;
    private static final double REF_BODY_MONTH = 0.045;
    /** 回升 K（最后一根）最低涨幅 */
    private static final double SIG_BAR_GAIN_MIN = 0.02;

    private LadderDipBreakoutTools() {
    }

    @Getter
    public static final class PeriodHit {
        private final PeriodTypeEnum period;
        private final Trade referenceBar;
        private final Trade prevBar;
        private final Trade lastBar;

        PeriodHit(PeriodTypeEnum period, Trade referenceBar, Trade prevBar, Trade lastBar) {
            this.period = period;
            this.referenceBar = referenceBar;
            this.prevBar = prevBar;
            this.lastBar = lastBar;
        }
    }

    public static PeriodHit findDayHit(StockBase stock, int lookback) {
        return findHit(stock, PeriodTypeEnum.DAY, lookback, REF_BODY_DAY);
    }

    public static PeriodHit findWeekHit(StockBase stock, int lookback) {
        return findHit(stock, PeriodTypeEnum.WEEK, lookback, REF_BODY_WEEK);
    }

    public static PeriodHit findMonthHit(StockBase stock, int lookback) {
        return findHit(stock, PeriodTypeEnum.MONTH, lookback, REF_BODY_MONTH);
    }

    /** 本档 low：配对回升 K close &gt; 配对 §1.3 基准 K low */
    public static boolean passesHitCloseAboveRefLow(PeriodHit hit) {
        if (hit == null || hit.getReferenceBar() == null || hit.getLastBar() == null) {
            return false;
        }
        Double refLow = hit.getReferenceBar().getLow();
        Double lastClose = hit.getLastBar().getClose();
        if (refLow == null || lastClose == null) {
            return false;
        }
        return lastClose > refLow + LOW_EPS;
    }

    /** 日档附加 · 周跨档 low：最后一根周 K close &gt; 周 §1.3 基准 low */
    public static boolean passesDayCrossWeekConfirm(StockBase stock, int weekLookback) {
        return passesUpperPeriodLowConfirm(stock, PeriodTypeEnum.WEEK, weekLookback, REF_BODY_WEEK);
    }

    /** 日档附加 · 月跨档 low：最后一根月 K close &gt; 月 §1.3 基准 low */
    public static boolean passesDayCrossMonthConfirm(StockBase stock, int monthLookback) {
        return passesUpperPeriodLowConfirm(stock, PeriodTypeEnum.MONTH, monthLookback, REF_BODY_MONTH);
    }

    /** 周档附加 · 日跨档 low：最后一根日 K close &gt; 日 §1.3 基准 low */
    public static boolean passesWeekCrossDayConfirm(StockBase stock, int dayLookback) {
        return passesUpperPeriodLowConfirm(stock, PeriodTypeEnum.DAY, dayLookback, REF_BODY_DAY);
    }

    /** 周档附加 · 月跨档 low：最后一根月 K close &gt; 月 §1.3 基准 low */
    public static boolean passesWeekCrossMonthConfirm(StockBase stock, int monthLookback) {
        return passesUpperPeriodLowConfirm(stock, PeriodTypeEnum.MONTH, monthLookback, REF_BODY_MONTH);
    }

    /** 月档附加 · 日跨档 low：最后一根日 K close &gt; 日 §1.3 基准 low */
    public static boolean passesMonthCrossDayConfirm(StockBase stock, int dayLookback) {
        return passesUpperPeriodLowConfirm(stock, PeriodTypeEnum.DAY, dayLookback, REF_BODY_DAY);
    }

    /** 月档附加 · 周跨档 low：最后一根周 K close &gt; 周 §1.3 基准 low */
    public static boolean passesMonthCrossWeekConfirm(StockBase stock, int weekLookback) {
        return passesUpperPeriodLowConfirm(stock, PeriodTypeEnum.WEEK, weekLookback, REF_BODY_WEEK);
    }

    /** 日档附加：最后一根日 K close &gt; 前一根日 K high */
    public static boolean passesDayCloseCrossPrevDayHigh(StockBase stock) {
        if (stock == null) {
            return false;
        }
        List<Trade> dayBars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.DAY, 4);
        if (CollectionUtils.isEmpty(dayBars) || dayBars.size() < 2) {
            return false;
        }
        int lastIdx = dayBars.size() - 1;
        Trade lastDay = dayBars.get(lastIdx);
        Trade prevDay = dayBars.get(lastIdx - 1);
        if (lastDay.getClose() == null || prevDay.getHigh() == null) {
            return false;
        }
        return lastDay.getClose() > prevDay.getHigh() + HIGH_EPS;
    }

    /**
     * 周档附加：日 K 边沿突破「周周期最后一根 K 的前一根」high
     * （最后一根日 close &gt; anchor high，倒数第二根日 close &lt;= anchor high）
     */
    public static boolean passesDayCloseEdgeCrossPrevWeekHigh(StockBase stock, int weekLookback) {
        return passesDayCloseEdgeCrossPeriodBarBeforeLast(stock, PeriodTypeEnum.WEEK, weekLookback);
    }

    /**
     * 月档附加：日 K 边沿突破「月周期最后一根 K 的前一根」high
     */
    public static boolean passesDayCloseEdgeCrossPrevMonthHigh(StockBase stock, int monthLookback) {
        return passesDayCloseEdgeCrossPeriodBarBeforeLast(stock, PeriodTypeEnum.MONTH, monthLookback);
    }

    /**
     * 各档附加：倒数第二根日 K close &lt; 本档配对 §1.3 基准 K high
     */
    public static boolean passesSecondDayCloseBelowRefHigh(StockBase stock, Trade referenceBar) {
        if (stock == null || referenceBar == null || referenceBar.getHigh() == null) {
            return false;
        }
        List<Trade> dayBars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.DAY, 4);
        if (CollectionUtils.isEmpty(dayBars) || dayBars.size() < 2) {
            return false;
        }
        Trade secondLastDay = dayBars.get(dayBars.size() - 2);
        if (secondLastDay.getClose() == null) {
            return false;
        }
        return secondLastDay.getClose() < referenceBar.getHigh() - HIGH_EPS;
    }

    private static boolean passesDayCloseEdgeCrossPeriodBarBeforeLast(StockBase stock, PeriodTypeEnum period,
                                                                      int lookback) {
        if (stock == null || period == null) {
            return false;
        }
        int fetchBars = Math.max(lookback + 3, period == PeriodTypeEnum.MONTH ? 40 : 80);
        List<Trade> periodBars = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        if (CollectionUtils.isEmpty(periodBars) || periodBars.size() < 2) {
            return false;
        }
        Trade anchor = periodBars.get(periodBars.size() - 2);
        if (anchor.getHigh() == null) {
            return false;
        }
        List<Trade> dayBars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.DAY, 4);
        if (CollectionUtils.isEmpty(dayBars) || dayBars.size() < 2) {
            return false;
        }
        int lastDayIdx = dayBars.size() - 1;
        Trade lastDay = dayBars.get(lastDayIdx);
        Trade prevDay = dayBars.get(lastDayIdx - 1);
        if (lastDay.getClose() == null || prevDay.getClose() == null) {
            return false;
        }
        double anchorHigh = anchor.getHigh();
        return lastDay.getClose() > anchorHigh + HIGH_EPS
                && prevDay.getClose() <= anchorHigh + HIGH_EPS;
    }

    private static boolean passesUpperPeriodLowConfirm(StockBase stock, PeriodTypeEnum period,
                                                       int lookback, double refBodyMin) {
        if (stock == null || period == null || lookback < 3) {
            return false;
        }
        int fetchBars = Math.max(lookback + 3, period == PeriodTypeEnum.MONTH ? 40 : 80);
        List<Trade> bars = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        if (CollectionUtils.isEmpty(bars) || bars.size() < 3) {
            return false;
        }
        int lastIdx = bars.size() - 1;
        Trade last = bars.get(lastIdx);
        Trade ref = findReferenceBar(bars, lastIdx, refBodyMin, lookback);
        if (ref == null || last.getClose() == null || ref.getLow() == null) {
            return false;
        }
        return last.getClose() > ref.getLow() + LOW_EPS;
    }

    private static PeriodHit findHit(StockBase stock, PeriodTypeEnum period, int lookback,
                                       double refBodyMin) {
        if (stock == null || period == null || lookback < 3) {
            return null;
        }
        int fetchBars = Math.max(lookback + 3, period == PeriodTypeEnum.MONTH ? 40 : 80);
        List<Trade> bars = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        if (CollectionUtils.isEmpty(bars) || bars.size() < 4) {
            return null;
        }
        int lastIdx = bars.size() - 1;
        Trade last = bars.get(lastIdx);
        Trade prev = bars.get(lastIdx - 1);
        if (!passesLastBar(last, prev)) {
            return null;
        }

        Trade ref = findNearestReference(bars, lastIdx, refBodyMin, lookback);
        if (ref == null) {
            return null;
        }
        return new PeriodHit(period, ref, prev, last);
    }

    /** §1.3：从倒数第三根起向前取最近一根合格基准 K（最后一根、倒数第二根不参与） */
    private static Trade findReferenceBar(List<Trade> bars, int lastIdx, double refBodyMin, int lookback) {
        if (lastIdx < 2) {
            return null;
        }
        int minIdx = Math.max(0, lastIdx - lookback);
        for (int i = lastIdx - 2; i >= minIdx; i--) {
            if (passesReferenceBarRules(bars, i, refBodyMin)) {
                return bars.get(i);
            }
        }
        return null;
    }

    private static boolean passesReferenceBarRules(List<Trade> bars, int refIdx, double refBodyMin) {
        Trade candidate = bars.get(refIdx);
        if (!BreakoutBarTools.isLargeYang(candidate, refBodyMin)) {
            return false;
        }
        if (candidate.getHigh() == null || candidate.getLow() == null) {
            return false;
        }
        return passesReferenceHighStructure(bars, refIdx);
    }

    /** §1.3 基准 K + §1.4 中间破 low 配对校验 */
    private static Trade findNearestReference(List<Trade> bars, int lastIdx, double refBodyMin, int lookback) {
        Trade ref = findReferenceBar(bars, lastIdx, refBodyMin, lookback);
        if (ref == null) {
            return null;
        }
        int refIdx = indexOfBarByDay(bars, ref);
        if (refIdx < 0) {
            return null;
        }
        Double refLow = ref.getLow();
        if (refLow == null || !intermediateDippedBelowRefLow(bars, refIdx, lastIdx, refLow)) {
            return null;
        }
        return ref;
    }

    private static boolean passesLastBar(Trade last, Trade prev) {
        if (last == null || prev == null
                || last.getHigh() == null || prev.getHigh() == null) {
            return false;
        }
        if (!BreakoutBarTools.isLargeGain(last, SIG_BAR_GAIN_MIN)) {
            return false;
        }
        return last.getHigh() > prev.getHigh() + HIGH_EPS;
    }

    private static int indexOfBarByDay(List<Trade> bars, Trade bar) {
        if (bar == null || bar.getDay() == null) {
            return -1;
        }
        for (int i = bars.size() - 1; i >= 0; i--) {
            if (bar.getDay().equals(bars.get(i).getDay())) {
                return i;
            }
        }
        return -1;
    }

    private static boolean passesReferenceHighStructure(List<Trade> bars, int refIdx) {
        if (refIdx < 1) {
            return false;
        }
        Trade beforeRef = bars.get(refIdx - 1);
        Trade ref = bars.get(refIdx);
        if (beforeRef.getHigh() == null || ref.getHigh() == null) {
            return false;
        }
        if (ref.getHigh() > beforeRef.getHigh() + HIGH_EPS) {
            return true;
        }
        if (refIdx + 1 >= bars.size()) {
            return false;
        }
        Trade afterRef = bars.get(refIdx + 1);
        return afterRef.getHigh() != null
                && afterRef.getHigh() > beforeRef.getHigh() + HIGH_EPS;
    }

    /** ref 之后至最后一根（含最后一根，不含 ref）至少一根 low 破 ref.low */
    private static boolean intermediateDippedBelowRefLow(List<Trade> bars, int refIdx, int lastIdx,
                                                         double refLow) {
        if (refIdx + 1 > lastIdx) {
            return false;
        }
        for (int j = refIdx + 1; j <= lastIdx; j++) {
            Double low = bars.get(j).getLow();
            if (low != null && low < refLow - LOW_EPS) {
                return true;
            }
        }
        return false;
    }
}
