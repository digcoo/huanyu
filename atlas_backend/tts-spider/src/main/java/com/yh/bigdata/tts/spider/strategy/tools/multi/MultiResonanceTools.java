package com.yh.bigdata.tts.spider.strategy.tools.multi;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.indicator.TiZiIndicatorUtils;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;

import java.util.Arrays;
import java.util.List;

/**
 * 多周期共振 Context：年/月/周/日同时满足
 * 1. 当前 K 收盘 &gt; 开盘（阳线）
 * 2. 上周期阳线，或 上周期最高价 &gt; 上上周期的座架高（梯子 High，无梯子时取实体上沿）
 */
public final class MultiResonanceTools {

    private static final List<PeriodTypeEnum> RESONANCE_PERIODS =
            Arrays.asList(
                    PeriodTypeEnum.YEAR,
                    PeriodTypeEnum.MONTH,
                    PeriodTypeEnum.WEEK,
                    PeriodTypeEnum.DAY);

    private MultiResonanceTools() {
    }

    public static ResonanceSnapshot evaluate(StockBase stock) {
        ResonanceSnapshot snap = new ResonanceSnapshot();
        for (PeriodTypeEnum period : RESONANCE_PERIODS) {
            PeriodResonance pr = evaluatePeriod(stock, period);
            snap.put(period, pr);
        }
        return snap;
    }

    private static PeriodResonance evaluatePeriod(StockBase stock, PeriodTypeEnum period) {
        PeriodResonance pr = new PeriodResonance();
        pr.period = period;

        Trade current = RealtimeStockCache.getLastTrade(stock, period, 0);
        Trade prev = RealtimeStockCache.getLastTrade(stock, period, 1);
        Trade prev2 = RealtimeStockCache.getLastTrade(stock, period, 2);

        if (current == null || prev == null || prev2 == null
                || current.getClose() == null || current.getOpen() == null
                || prev.getClose() == null || prev.getOpen() == null
                || prev.getHigh() == null) {
            pr.valid = false;
            return pr;
        }

        pr.valid = true;
        pr.currentYang = current.getClose() > current.getOpen();
        pr.prevYang = prev.getClose() > prev.getOpen();
        double zuojiaHigh = resolveZuojiaHigh(stock, period, prev2);
        pr.prevHighAboveZuojia = prev.getHigh() > zuojiaHigh;
        pr.structureOk = pr.prevYang || pr.prevHighAboveZuojia;
        return pr;
    }

    /**
     * 座架高：上上周期的梯子 High；该根非梯子时取实体上沿，再 fallback 到最高价
     */
    static double resolveZuojiaHigh(StockBase stock, PeriodTypeEnum period, Trade barAtOffset2) {
        if (barAtOffset2 == null) {
            return 0;
        }
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, 60);
        if (trades != null && !trades.isEmpty()) {
            for (TiZiIndicatorUtils.TiZiPoint point
                    : TiZiIndicatorUtils.getTiZiPointsNoMerge(trades, period)) {
                if (point.getTrade() != null
                        && barAtOffset2.getDay().equals(point.getTrade().getDay())
                        && point.getTrade().getHigh() != null) {
                    return point.getTrade().getHigh();
                }
            }
        }
        Double shitiMax = barAtOffset2.getShitiMax();
        if (shitiMax != null && shitiMax > 0) {
            return shitiMax;
        }
        return barAtOffset2.getHigh() != null ? barAtOffset2.getHigh() : 0;
    }

    public static final class PeriodResonance {
        public PeriodTypeEnum period;
        public boolean valid;
        /** 当前周期：收盘 &gt; 开盘 */
        public boolean currentYang;
        /** 上周期：收盘 &gt; 开盘 */
        public boolean prevYang;
        /** 上周期：最高 &gt; 上上周期的座架高 */
        public boolean prevHighAboveZuojia;
        /** 条件 2 */
        public boolean structureOk;

        public boolean coreResonance() {
            return valid && currentYang && structureOk;
        }
    }

    public static final class ResonanceSnapshot {
        public PeriodResonance year;
        public PeriodResonance month;
        public PeriodResonance week;
        public PeriodResonance day;

        void put(PeriodTypeEnum period, PeriodResonance pr) {
            if (period == PeriodTypeEnum.YEAR) {
                year = pr;
            } else if (period == PeriodTypeEnum.MONTH) {
                month = pr;
            } else if (period == PeriodTypeEnum.WEEK) {
                week = pr;
            } else if (period == PeriodTypeEnum.DAY) {
                day = pr;
            }
        }

        public int coreCount() {
            int n = 0;
            if (year != null && year.coreResonance()) {
                n++;
            }
            if (month != null && month.coreResonance()) {
                n++;
            }
            if (week != null && week.coreResonance()) {
                n++;
            }
            if (day != null && day.coreResonance()) {
                n++;
            }
            return n;
        }

        public boolean fullResonance() {
            return coreCount() >= RESONANCE_PERIODS.size();
        }
    }
}
