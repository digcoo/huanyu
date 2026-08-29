package com.binance.client.strategy.macross;

import com.binance.client.model.market.LongCandlestickMA;

import java.math.BigDecimal;
import java.util.List;

/**
 * MA5/MA10 金叉/死叉交叉点价格，及边沿突破、父级均价之上判定。
 */
public final class MaCrossPointCore {

    static final double EPS = 1e-6;

    private MaCrossPointCore() {
    }

    public enum CrossKind {
        GOLDEN,
        DEATH
    }

    /** 金叉：i 满足 MA5≥MA10，且 i-1 不满足 */
    public static boolean isGoldenCrossAt(List<LongCandlestickMA> bars, int i) {
        if (bars == null || i < 1 || i >= bars.size()) {
            return false;
        }
        return isMa5GeMa10(bars.get(i)) && !isMa5GeMa10(bars.get(i - 1));
    }

    /** 死叉：i 满足 MA5&lt;MA10，且 i-1 满足 MA5≥MA10 */
    public static boolean isDeathCrossAt(List<LongCandlestickMA> bars, int i) {
        if (bars == null || i < 1 || i >= bars.size()) {
            return false;
        }
        return !isMa5GeMa10(bars.get(i)) && isMa5GeMa10(bars.get(i - 1));
    }

    public static boolean isMa5GeMa10(LongCandlestickMA bar) {
        if (bar == null || bar.getMa5() == null || bar.getMa10() == null) {
            return false;
        }
        return bar.getMa5().doubleValue() + EPS >= bar.getMa10().doubleValue();
    }

    /** 从 lastIdx 往前找最近交叉 K（不含 lastIdx 本身） */
    public static int findLatestCrossIndex(List<LongCandlestickMA> bars, int lastIdx, CrossKind kind) {
        if (bars == null || lastIdx < 2 || kind == null) {
            return -1;
        }
        for (int i = lastIdx - 1; i >= 1; i--) {
            if (kind == CrossKind.GOLDEN && isGoldenCrossAt(bars, i)) {
                return i;
            }
            if (kind == CrossKind.DEATH && isDeathCrossAt(bars, i)) {
                return i;
            }
        }
        return -1;
    }

    /** 交叉点位：交叉 K 与前一根之间，对 MA5/MA10 线性插值。 */
    public static Double crossPriceAt(List<LongCandlestickMA> bars, int crossIdx) {
        if (bars == null || crossIdx < 1 || crossIdx >= bars.size()) {
            return null;
        }
        LongCandlestickMA prev = bars.get(crossIdx - 1);
        LongCandlestickMA cur = bars.get(crossIdx);
        if (prev == null || cur == null
                || prev.getMa5() == null || prev.getMa10() == null
                || cur.getMa5() == null || cur.getMa10() == null) {
            return null;
        }
        double a0 = prev.getMa5().doubleValue();
        double a1 = cur.getMa5().doubleValue();
        double b0 = prev.getMa10().doubleValue();
        double b1 = cur.getMa10().doubleValue();
        double denom = (a1 - a0) - (b1 - b0);
        if (Math.abs(denom) < EPS) {
            return (a0 + b0) / 2.0;
        }
        double t = (b0 - a0) / denom;
        if (t < -EPS || t > 1.0 + EPS) {
            t = Math.max(0.0, Math.min(1.0, t));
        }
        return a0 + t * (a1 - a0);
    }

    /** 边沿突破：prev.close ≤ 基准价，signal.close &gt; 基准价 */
    public static boolean passesEdgeBreak(LongCandlestickMA prevBar, LongCandlestickMA signalBar, double breakLine) {
        Double prevClose = closeOf(prevBar);
        Double signalClose = closeOf(signalBar);
        if (prevClose == null || signalClose == null) {
            return false;
        }
        return prevClose <= breakLine + EPS && signalClose > breakLine + EPS;
    }

    /** 边沿跌破：prev.close ≥ 基准价，signal.close &lt; 基准价 */
    public static boolean passesEdgeBreakDown(LongCandlestickMA prevBar, LongCandlestickMA signalBar, double breakLine) {
        Double prevClose = closeOf(prevBar);
        Double signalClose = closeOf(signalBar);
        if (prevClose == null || signalClose == null) {
            return false;
        }
        return prevClose >= breakLine - EPS && signalClose < breakLine - EPS;
    }

    /** 本档：MA5 &gt; MA10 */
    public static boolean passesMa5AboveMa10(LongCandlestickMA bar) {
        if (bar == null || bar.getMa5() == null || bar.getMa10() == null) {
            return false;
        }
        return bar.getMa5().doubleValue() > bar.getMa10().doubleValue() + EPS;
    }

    /** 本档：MA5 &lt; MA10 */
    public static boolean passesMa5BelowMa10(LongCandlestickMA bar) {
        if (bar == null || bar.getMa5() == null || bar.getMa10() == null) {
            return false;
        }
        return bar.getMa5().doubleValue() < bar.getMa10().doubleValue() - EPS;
    }

    /** 父级均价之上：close &gt; max(MA5, MA10) */
    public static boolean passesAboveMa(LongCandlestickMA bar) {
        Double close = closeOf(bar);
        if (close == null || bar.getMa5() == null || bar.getMa10() == null) {
            return false;
        }
        double maxMa = Math.max(bar.getMa5().doubleValue(), bar.getMa10().doubleValue());
        return close > maxMa + EPS;
    }

    /** 父级均价之下：close &lt; min(MA5, MA10) */
    public static boolean passesBelowMa(LongCandlestickMA bar) {
        Double close = closeOf(bar);
        if (close == null || bar.getMa5() == null || bar.getMa10() == null) {
            return false;
        }
        double minMa = Math.min(bar.getMa5().doubleValue(), bar.getMa10().doubleValue());
        return close < minMa - EPS;
    }

    private static Double closeOf(LongCandlestickMA bar) {
        if (bar == null || bar.getClose() == null) {
            return null;
        }
        return bar.getClose().doubleValue();
    }

    static double bd(BigDecimal v) {
        return v == null ? Double.NaN : v.doubleValue();
    }
}
