package com.yh.bigdata.tts.spider.strategy.tools.cladder;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.BreakoutBarTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 级联梯子突破 · 基准 K 压顶 + 最后一根大涨幅（日/周/月交集）
 */
public final class CascadeLadderBreakoutTools {

    private static final double HIGH_EPS = 1e-6;
    private static final double LOW_EPS = 1e-6;

    private static final double REF_BODY_DAY = 0.03;
    private static final double REF_BODY_WEEK = 0.04;
    private static final double REF_BODY_MONTH = 0.05;
    /** 最后一根 K：日周月统一涨幅门槛 */
    private static final double LAST_BAR_GAIN_MIN = 0.02;

    private CascadeLadderBreakoutTools() {
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

    private static PeriodHit findHit(StockBase stock, PeriodTypeEnum period, int lookback,
                                       double refBodyMin) {
        if (stock == null || period == null || lookback < 3) {
            return null;
        }
        int fetchBars = Math.max(lookback + 2, period == PeriodTypeEnum.MONTH ? 40 : 80);
        List<Trade> bars = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        if (CollectionUtils.isEmpty(bars) || bars.size() < 3) {
            return null;
        }
        int lastIdx = bars.size() - 1;
        Trade last = bars.get(lastIdx);
        Trade prev = bars.get(lastIdx - 1);

        if (!BreakoutBarTools.isLargeGain(last, LAST_BAR_GAIN_MIN)) {
            return null;
        }

        Trade ref = findNearestReference(bars, lastIdx, refBodyMin, lookback);
        if (ref == null || ref.getLow() == null) {
            return null;
        }
        Double lastClose = last.getClose();
        if (lastClose == null || lastClose <= ref.getLow() + LOW_EPS) {
            return null;
        }
        return new PeriodHit(period, ref, prev, last);
    }

    /** 自最后一根前一根向前，取最近一根合格基准 K */
    private static Trade findNearestReference(List<Trade> bars, int lastIdx, double refBodyMin, int lookback) {
        int minIdx = Math.max(0, lastIdx - lookback);
        for (int i = lastIdx - 1; i >= minIdx; i--) {
            Trade candidate = bars.get(i);
            if (!BreakoutBarTools.isLargeYang(candidate, refBodyMin)) {
                continue;
            }
            Double refHigh = candidate.getHigh();
            if (refHigh == null) {
                continue;
            }
            if (!intermediateClosesAtMostRefHigh(bars, i, lastIdx, refHigh)) {
                continue;
            }
            return candidate;
        }
        return null;
    }

    private static boolean intermediateClosesAtMostRefHigh(List<Trade> bars, int refIdx, int lastIdx,
                                                           double refHigh) {
        for (int i = refIdx + 1; i < lastIdx; i++) {
            Double close = bars.get(i).getClose();
            if (close == null || close > refHigh + HIGH_EPS) {
                return false;
            }
        }
        return true;
    }
}
