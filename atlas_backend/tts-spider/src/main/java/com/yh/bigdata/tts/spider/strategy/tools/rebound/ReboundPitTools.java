package com.yh.bigdata.tts.spider.strategy.tools.rebound;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.dto.CheckResponse;
import com.yh.bigdata.tts.common.indicator.BandRevertIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.MAIndicatorUtils;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.ReboundStrategyParams;
import com.yh.bigdata.tts.spider.strategy.tools.IndicatorCommonUtils;

import java.util.List;

/**
 * 深坑反弹 · 叙事结构：长期下跌 → 恐慌大跌（情绪释放）→ 坑底企稳
 */
public final class ReboundPitTools {

    private static final PeriodTypeEnum DAY = PeriodTypeEnum.DAY;
    private static final PeriodTypeEnum WEEK = PeriodTypeEnum.WEEK;
    private static final PeriodTypeEnum MONTH = PeriodTypeEnum.MONTH;

    private ReboundPitTools() {
    }

    public static PitSnapshot evaluateWeek(StockBase stock) {
        return evaluatePeriod(stock, WEEK);
    }

    public static PitSnapshot evaluateMonth(StockBase stock) {
        return evaluatePeriod(stock, MONTH);
    }

    /**
     * 深坑叙事是否成立（接入前置条件，不含反弹触发）
     */
    public static boolean passesDeepPitNarrative(StockBase stock, ReboundStrategyParams params) {
        DeepPitNarrative n = evaluateNarrative(stock, params);
        return n.isComplete();
    }

    public static DeepPitNarrative evaluateNarrative(StockBase stock, ReboundStrategyParams params) {
        ReboundStrategyParams p = params != null ? params : ReboundStrategyParams.defaults();
        DeepPitNarrative n = new DeepPitNarrative();
        n.longDecline = checkLongTermDecline(stock, p);
        n.capitulation = checkCapitulationRelease(stock, p);
        if (n.capitulation) {
            n.capitulationLow = findCapitulationLow(stock, p);
        }
        n.pitFloorHeld = hit(IndicatorCommonUtils.checkOverRevertBandLow(stock, WEEK))
                || hit(IndicatorCommonUtils.checkOverRevertBandLow(stock, MONTH));
        n.abovePanicLow = checkAbovePanicLow(stock, n.capitulationLow);
        n.stillRecoveryZone = !hit(IndicatorCommonUtils.checkOverMA20(stock, WEEK))
                || !hit(IndicatorCommonUtils.checkOverMA20(stock, MONTH));
        return n;
    }

    public static PitSnapshot evaluatePeriod(StockBase stock, PeriodTypeEnum period) {
        PitSnapshot snap = new PitSnapshot(period);
        snap.deepRevertBand = hasDeepRevertBand(stock, period);
        snap.overRevertBandLow = hit(IndicatorCommonUtils.checkOverRevertBandLow(stock, period));
        snap.underMa20 = !hit(IndicatorCommonUtils.checkOverMA20(stock, period));
        return snap;
    }

    /** 长期下跌：月 K 弱势 +（周 K 弱势 或 相对高位深度回撤） */
    private static boolean checkLongTermDecline(StockBase stock, ReboundStrategyParams p) {
        if (hit(IndicatorCommonUtils.checkOverMA20(stock, MONTH))) {
            return false;
        }

        boolean weekWeak = !hit(IndicatorCommonUtils.checkOverMA20(stock, WEEK));
        boolean maBearish = checkMonthMa5BelowMa20(stock);

        Trade day = RealtimeStockCache.getLastTrade(stock, DAY, 0);
        boolean deepDrawdown = false;
        if (day != null && day.getClose() != null && stock.getHigh52w() != null && stock.getHigh52w() > 0) {
            deepDrawdown = day.getClose() <= stock.getHigh52w() * (1 - p.getMinDrawdownFrom52w());
        }

        boolean longBandDecline = hasDeepRevertBand(stock, MONTH) || hasDeepRevertBand(stock, WEEK);
        return weekWeak || maBearish || deepDrawdown || longBandDecline;
    }

    private static boolean checkMonthMa5BelowMa20(StockBase stock) {
        List<Trade> months = RealtimeStockCache.getLastTrades(stock, MONTH, 30);
        if (months == null || months.size() < 21) {
            return false;
        }
        Double ma5 = MAIndicatorUtils.calLatestMA(months, 5);
        Double ma20 = MAIndicatorUtils.calLatestMA(months, 20);
        return ma5 != null && ma20 != null && ma5 < ma20;
    }

    /**
     * 恐慌大跌：近端出现一根（或一段）显著下跌 K，完成情绪集中释放
     */
    private static boolean checkCapitulationRelease(StockBase stock, ReboundStrategyParams p) {
        if (hasCapitulationBar(stock, DAY, p.getCapitulationLookbackDays(), p.getCapitulationDayRate())) {
            return true;
        }
        if (hasCapitulationBar(stock, WEEK, p.getCapitulationLookbackWeeks(), p.getCapitulationWeekRate())) {
            return true;
        }
        return hasBandCapitulation(stock, WEEK) || hasBandCapitulation(stock, MONTH);
    }

    private static boolean hasCapitulationBar(StockBase stock, PeriodTypeEnum period,
                                              int lookback, double minDropRate) {
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, lookback + 1);
        if (trades == null || trades.size() < 2) {
            return false;
        }
        int start = Math.max(0, trades.size() - lookback - 1);
        for (int i = start; i < trades.size() - 1; i++) {
            Trade t = trades.get(i);
            if (t.getChangeRate() != null && t.getChangeRate() <= -minDropRate) {
                return true;
            }
            if (t.getShitiRate() != null && t.getShitiRate() <= -minDropRate * 0.85) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasBandCapitulation(StockBase stock, PeriodTypeEnum period) {
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, 60);
        if (trades == null || trades.size() < 3) {
            return false;
        }
        BandRevertIndicatorUtils.BandSegment seg =
                BandRevertIndicatorUtils.getLastBandSegmentWithCurrent(period, trades, 0);
        if (seg == null) {
            return false;
        }
        double amplified = period.getBandChangeRate() * 1.2;
        return seg.getBandChangeRate() <= -amplified || seg.getBandShiTiRate() <= -period.getBandShiTiRate();
    }

    private static Double findCapitulationLow(StockBase stock, ReboundStrategyParams p) {
        double low = Double.POSITIVE_INFINITY;
        List<Trade> days = RealtimeStockCache.getLastTrades(stock, DAY, p.getCapitulationLookbackDays() + 1);
        if (days != null && days.size() >= 2) {
            int start = Math.max(0, days.size() - p.getCapitulationLookbackDays() - 1);
            for (int i = start; i < days.size() - 1; i++) {
                Trade t = days.get(i);
                if (isCapitulationBar(t, p.getCapitulationDayRate()) && t.getLow() != null) {
                    low = Math.min(low, t.getLow());
                }
            }
        }
        List<Trade> weeks = RealtimeStockCache.getLastTrades(stock, WEEK, p.getCapitulationLookbackWeeks() + 1);
        if (weeks != null && weeks.size() >= 2) {
            int start = Math.max(0, weeks.size() - p.getCapitulationLookbackWeeks() - 1);
            for (int i = start; i < weeks.size() - 1; i++) {
                Trade t = weeks.get(i);
                if (isCapitulationBar(t, p.getCapitulationWeekRate()) && t.getLow() != null) {
                    low = Math.min(low, t.getLow());
                }
            }
        }
        return low == Double.POSITIVE_INFINITY ? null : low;
    }

    private static boolean isCapitulationBar(Trade t, double minDropRate) {
        if (t == null) {
            return false;
        }
        if (t.getChangeRate() != null && t.getChangeRate() <= -minDropRate) {
            return true;
        }
        return t.getShitiRate() != null && t.getShitiRate() <= -minDropRate * 0.85;
    }

    /** 现价仍站在恐慌低点之上（不接飞刀） */
    private static boolean checkAbovePanicLow(StockBase stock, Double capitulationLow) {
        Trade day = RealtimeStockCache.getLastTrade(stock, DAY, 0);
        if (day == null || day.getClose() == null) {
            return false;
        }
        if (capitulationLow == null) {
            return true;
        }
        return day.getClose() >= capitulationLow;
    }

    private static boolean hasDeepRevertBand(StockBase stock, PeriodTypeEnum period) {
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, 60);
        if (trades == null || trades.size() < 3) {
            return false;
        }
        BandRevertIndicatorUtils.BandSegment seg =
                BandRevertIndicatorUtils.getLastBandSegmentWithCurrent(period, trades, 0);
        if (seg == null) {
            return false;
        }
        double change = seg.getBandChangeRate();
        double shiti = seg.getBandShiTiRate();
        return change <= -period.getBandChangeRate()
                || shiti <= -period.getBandShiTiRate();
    }

    private static boolean hit(CheckResponse resp) {
        return resp != null && resp.isSuccess();
    }

    /** 深坑三段式叙事快照 */
    public static final class DeepPitNarrative {
        public boolean longDecline;
        public boolean capitulation;
        public boolean pitFloorHeld;
        public boolean abovePanicLow;
        public boolean stillRecoveryZone;
        public Double capitulationLow;

        public boolean isComplete() {
            return longDecline && capitulation && pitFloorHeld && abovePanicLow;
        }

        public int stageCount() {
            int n = 0;
            if (longDecline) n++;
            if (capitulation) n++;
            if (pitFloorHeld && abovePanicLow) n++;
            return n;
        }
    }

    public static final class PitSnapshot {
        public final PeriodTypeEnum period;
        public boolean deepRevertBand;
        public boolean overRevertBandLow;
        public boolean underMa20;

        PitSnapshot(PeriodTypeEnum period) {
            this.period = period;
        }

        public int contextCount() {
            int n = 0;
            if (deepRevertBand) n++;
            if (overRevertBandLow) n++;
            if (underMa20) n++;
            return n;
        }

        public boolean isFullContext(int min) {
            return contextCount() >= min;
        }
    }
}
