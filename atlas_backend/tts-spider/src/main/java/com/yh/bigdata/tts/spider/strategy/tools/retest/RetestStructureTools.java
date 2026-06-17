package com.yh.bigdata.tts.spider.strategy.tools.retest;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.RetestStrategyParams;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.BreakoutBarTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * L0 → H1 → L1 → signal 结构扫描
 */
public final class RetestStructureTools {

    private RetestStructureTools() {
    }

    public enum RetestTierKind {
        ULTRA, SHORT, MEDIUM, LONG
    }

    @Getter
    public static final class StructureHit {
        private final RetestTierKind kind;
        private final Trade l0;
        private final Trade h1;
        private final Trade l1;
        private final Trade signal;
        private final double pullbackHigh;

        StructureHit(RetestTierKind kind, Trade l0, Trade h1, Trade l1, Trade signal, double pullbackHigh) {
            this.kind = kind;
            this.l0 = l0;
            this.h1 = h1;
            this.l1 = l1;
            this.signal = signal;
            this.pullbackHigh = pullbackHigh;
        }
    }

    public static StructureHit findUltraHit(StockBase stock, RetestStrategyParams p) {
        return findHit(stock, RetestTierKind.ULTRA, PeriodTypeEnum.MIN30, 50, p);
    }

    public static StructureHit findShortHit(StockBase stock, RetestStrategyParams p) {
        return findHit(stock, RetestTierKind.SHORT, PeriodTypeEnum.DAY, 50, p);
    }

    public static StructureHit findMediumHit(StockBase stock, RetestStrategyParams p) {
        return findHit(stock, RetestTierKind.MEDIUM, PeriodTypeEnum.WEEK, 30, p);
    }

    public static StructureHit findLongHit(StockBase stock, RetestStrategyParams p) {
        return findHit(stock, RetestTierKind.LONG, PeriodTypeEnum.MONTH, 30, p);
    }

    private static StructureHit findHit(StockBase stock, RetestTierKind kind, PeriodTypeEnum period,
                                        int fetchBars, RetestStrategyParams params) {
        RetestStrategyParams p = params != null ? params : RetestStrategyParams.defaults();
        List<Trade> bars = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        if (CollectionUtils.isEmpty(bars) || bars.size() < 12) {
            return null;
        }

        double bodyPct = period.getCrossMaxHighRate();
        List<SwingPointTools.SwingPoint> swings = SwingPointTools.findFractalSwings(bars, p.getFractalBars());

        StructureHit best = null;
        int bestSignalIdx = -1;

        for (SwingPointTools.SwingPoint l0Sp : swings) {
            if (l0Sp.getKind() != SwingPointTools.SwingKind.LOW) {
                continue;
            }
            SwingPointTools.SwingPoint h1Sp = SwingPointTools.firstAfter(swings, l0Sp.getIndex(),
                    SwingPointTools.SwingKind.HIGH);
            if (h1Sp == null || !validImpulse(l0Sp, h1Sp, bars, p, bodyPct)) {
                continue;
            }
            SwingPointTools.SwingPoint l1Sp = SwingPointTools.firstAfter(swings, h1Sp.getIndex(),
                    SwingPointTools.SwingKind.LOW);
            if (l1Sp == null || !validPullback(l0Sp, h1Sp, l1Sp, p)) {
                continue;
            }
            double pullbackHigh = resolvePullbackHigh(bars, l0Sp, h1Sp, l1Sp, p);
            SignalFind signalFind = findSignalBar(bars, l1Sp.getIndex(), pullbackHigh, p, bodyPct);
            if (signalFind == null) {
                continue;
            }
            if (!isCurrentCloseNotAboveSignalHigh(stock, signalFind.signal)) {
                continue;
            }
            if (signalFind.index > bestSignalIdx) {
                bestSignalIdx = signalFind.index;
                best = new StructureHit(kind, l0Sp.getBar(), h1Sp.getBar(), l1Sp.getBar(),
                        signalFind.signal, pullbackHigh);
            }
        }
        return best;
    }

    static boolean validImpulse(SwingPointTools.SwingPoint l0, SwingPointTools.SwingPoint h1,
                                List<Trade> bars, RetestStrategyParams p, double bodyPct) {
        Double l0Low = l0.getBar().getLow();
        Double h1High = h1.getBar().getHigh();
        if (l0Low == null || h1High == null || l0Low <= 0) {
            return false;
        }
        double gain = (h1High - l0Low) / l0Low;
        if (gain < p.getImpulseMinGainPct()) {
            return false;
        }
        int barCount = h1.getIndex() - l0.getIndex() + 1;
        if (barCount < p.getImpulseMinBars() || barCount > p.getImpulseMaxBars()) {
            return false;
        }
        int strongCount = 0;
        for (int i = l0.getIndex(); i <= h1.getIndex(); i++) {
            if (BreakoutBarTools.isStrongBar(bars.get(i), bodyPct)) {
                strongCount++;
            }
        }
        return strongCount >= p.getImpulseMinStrongBars();
    }

    static boolean validPullback(SwingPointTools.SwingPoint l0, SwingPointTools.SwingPoint h1,
                                 SwingPointTools.SwingPoint l1, RetestStrategyParams p) {
        Double h1High = h1.getBar().getHigh();
        Double l1Low = l1.getBar().getLow();
        Double l0Low = l0.getBar().getLow();
        if (h1High == null || l1Low == null || l0Low == null) {
            return false;
        }
        double range = h1High - l0Low;
        if (range <= 0) {
            return false;
        }
        double ratio = (h1High - l1Low) / range;
        if (ratio < p.getPullbackMinRatio() || ratio > p.getPullbackMaxRatio()) {
            return false;
        }
        if (l1Low < l0Low * (1 - p.getLowEqualTolerance())) {
            return false;
        }
        int impulseBars = h1.getIndex() - l0.getIndex() + 1;
        int pullbackBars = l1.getIndex() - h1.getIndex() + 1;
        return pullbackBars <= (int) (impulseBars * 1.5);
    }

    static double resolvePullbackHigh(List<Trade> bars, SwingPointTools.SwingPoint l0,
                                      SwingPointTools.SwingPoint h1, SwingPointTools.SwingPoint l1,
                                      RetestStrategyParams p) {
        Double localHigh = null;
        for (int i = h1.getIndex() + 1; i < l1.getIndex(); i++) {
            Double high = bars.get(i).getHigh();
            if (high != null) {
                localHigh = localHigh == null ? high : Math.max(localHigh, high);
            }
        }
        if (localHigh != null && localHigh > 0) {
            return localHigh;
        }
        Double h1High = h1.getBar().getHigh();
        Double l0Low = l0.getBar().getLow();
        if (h1High == null || l0Low == null) {
            return h1High != null ? h1High : 0;
        }
        return h1High - p.getPullbackMinRatio() * (h1High - l0Low);
    }

    private static final class SignalFind {
        private final Trade signal;
        private final int index;

        SignalFind(Trade signal, int index) {
            this.signal = signal;
            this.index = index;
        }
    }

    static SignalFind findSignalBar(List<Trade> bars, int l1Index, double pullbackHigh,
                                    RetestStrategyParams p, double bodyPct) {
        int start = l1Index + p.getStabilizeBars();
        if (start >= bars.size()) {
            return null;
        }
        double floor = bars.get(l1Index).getLow() != null
                ? bars.get(l1Index).getLow() * (1 - p.getLowEqualTolerance()) : 0;

        for (int i = start; i < bars.size(); i++) {
            boolean stable = true;
            for (int j = l1Index + 1; j < i; j++) {
                Double low = bars.get(j).getLow();
                if (low != null && low < floor) {
                    stable = false;
                    break;
                }
            }
            if (!stable) {
                continue;
            }
            Trade bar = bars.get(i);
            Trade prev = bars.get(i - 1);
            if (bar.getHigh() == null || bar.getClose() == null
                    || prev.getOpen() == null || prev.getClose() == null) {
                continue;
            }
            double prevBodyTop = Math.max(prev.getOpen(), prev.getClose());
            if (!BreakoutBarTools.isStrongBar(bar, bodyPct)) {
                continue;
            }
            if (bar.getHigh() > pullbackHigh && bar.getClose() > prevBodyTop) {
                return new SignalFind(bar, i);
            }
        }
        return null;
    }

    static boolean isCurrentCloseNotAboveSignalHigh(StockBase stock, Trade signal) {
        if (stock == null || signal == null || stock.getClose() == null || signal.getHigh() == null) {
            return false;
        }
        return stock.getClose() <= signal.getHigh();
    }

    public static PeriodTypeEnum macroPeriodFor(RetestTierKind kind) {
        switch (kind) {
            case ULTRA:
                return PeriodTypeEnum.DAY;
            case SHORT:
                return PeriodTypeEnum.WEEK;
            case MEDIUM:
                return PeriodTypeEnum.MONTH;
            case LONG:
                return PeriodTypeEnum.YEAR;
            default:
                return PeriodTypeEnum.WEEK;
        }
    }
}
