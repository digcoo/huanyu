package com.yh.bigdata.tts.spider.strategy.tools.macrosspoint;

import com.yh.bigdata.tts.common.model.Trade;

import java.util.List;

/**
 * MA5/MA10 金叉/死叉交叉点价格，及边沿突破判定。
 */
public final class MaCrossPointCore {

    private static final double EPS = 1e-6;

    private MaCrossPointCore() {
    }

    public enum CrossKind {
        GOLDEN,
        DEATH
    }

    /** 金叉：i 满足 MA5≥MA10，且 i-1 不满足 */
    public static boolean isGoldenCrossAt(List<Trade> trades, int i) {
        if (trades == null || i < 1 || i >= trades.size()) {
            return false;
        }
        return isMa5GeMa10(trades.get(i)) && !isMa5GeMa10(trades.get(i - 1));
    }

    /** 死叉：i 满足 MA5&lt;MA10，且 i-1 满足 MA5≥MA10 */
    public static boolean isDeathCrossAt(List<Trade> trades, int i) {
        if (trades == null || i < 1 || i >= trades.size()) {
            return false;
        }
        return !isMa5GeMa10(trades.get(i)) && isMa5GeMa10(trades.get(i - 1));
    }

    public static boolean isMa5GeMa10(Trade bar) {
        if (bar == null || bar.getMa5() == null || bar.getMa10() == null) {
            return false;
        }
        return bar.getMa5() + EPS >= bar.getMa10();
    }

    /**
     * 从 lastIdx 往前找最近交叉 K（含 lastIdx：末 K 本身可以是最后一次金叉/死叉）。
     */
    public static int findLatestCrossIndex(List<Trade> trades, int lastIdx, CrossKind kind) {
        if (trades == null || lastIdx < 1 || kind == null) {
            return -1;
        }
        for (int i = lastIdx; i >= 1; i--) {
            if (kind == CrossKind.GOLDEN && isGoldenCrossAt(trades, i)) {
                return i;
            }
            if (kind == CrossKind.DEATH && isDeathCrossAt(trades, i)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 交叉点位：在交叉 K 与其前一根之间，对 MA5/MA10 线性插值求交点价格。
     */
    public static Double crossPriceAt(List<Trade> trades, int crossIdx) {
        if (trades == null || crossIdx < 1 || crossIdx >= trades.size()) {
            return null;
        }
        Trade prev = trades.get(crossIdx - 1);
        Trade cur = trades.get(crossIdx);
        if (prev == null || cur == null
                || prev.getMa5() == null || prev.getMa10() == null
                || cur.getMa5() == null || cur.getMa10() == null) {
            return null;
        }
        double a0 = prev.getMa5();
        double a1 = cur.getMa5();
        double b0 = prev.getMa10();
        double b1 = cur.getMa10();
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

    /**
     * 边沿突破：（prev.close ≤ 基准价 且 signal.close &gt; 基准价）
     * 或（signal.open ≤ 基准价 且 signal.close &gt; 基准价）。
     */
    public static boolean passesEdgeBreak(Trade prevBar, Trade signalBar, double breakLine) {
        if (signalBar == null || signalBar.getClose() == null) {
            return false;
        }
        if (!(signalBar.getClose() > breakLine + EPS)) {
            return false;
        }
        boolean prevCloseCross = prevBar != null && prevBar.getClose() != null
                && prevBar.getClose() <= breakLine + EPS;
        boolean openCross = signalBar.getOpen() != null
                && signalBar.getOpen() <= breakLine + EPS;
        return prevCloseCross || openCross;
    }

    /** 本档：MA10 ≥ MA60（优先用 K 线收盘价现算，避免 dayk.ma60 未落库） */
    public static boolean passesMa10GeMa60(Trade bar) {
        Double ma10 = bar == null ? null : bar.getMa10();
        Double ma60 = bar == null ? null : bar.getMa60();
        if (ma10 == null || ma60 == null) {
            return false;
        }
        return ma10 + EPS >= ma60;
    }

    public static boolean passesMa10GeMa60(List<Trade> trades) {
        Double ma10 = smaClose(trades, 10);
        Double ma60 = smaClose(trades, 60);
        if (ma10 == null || ma60 == null) {
            return false;
        }
        return ma10 + EPS >= ma60;
    }

    /** 本档：MA10 &lt; MA60 */
    public static boolean passesMa10LtMa60(Trade bar) {
        Double ma10 = bar == null ? null : bar.getMa10();
        Double ma60 = bar == null ? null : bar.getMa60();
        if (ma10 == null || ma60 == null) {
            return false;
        }
        return ma10 < ma60 - EPS;
    }

    public static boolean passesMa10LtMa60(List<Trade> trades) {
        Double ma10 = smaClose(trades, 10);
        Double ma60 = smaClose(trades, 60);
        if (ma10 == null || ma60 == null) {
            return false;
        }
        return ma10 < ma60 - EPS;
    }

    /** 本档：MA5 &gt; MA10（严格大于） */
    public static boolean passesMa5AboveMa10(Trade bar) {
        if (bar == null || bar.getMa5() == null || bar.getMa10() == null) {
            return false;
        }
        return bar.getMa5() > bar.getMa10() + EPS;
    }

    /** 均线多头：MA5 &gt; MA60（60 周期支撑均线） */
    public static boolean passesMaBull(Trade bar) {
        if (bar == null || bar.getMa5() == null || bar.getMa60() == null) {
            return false;
        }
        return bar.getMa5() > bar.getMa60() + EPS;
    }

    public static boolean passesMaBull(List<Trade> trades) {
        Double ma5 = smaClose(trades, 5);
        Double ma60 = smaClose(trades, 60);
        if (ma5 == null || ma60 == null) {
            return false;
        }
        return ma5 > ma60 + EPS;
    }

    /**
     * 末 K 的 N 日收盘简单均线；K 线不足 N 根时返回 null（不拿短窗口冒充 MA60）。
     */
    public static Double smaClose(List<Trade> trades, int period) {
        if (trades == null || period <= 0 || trades.size() < period) {
            return null;
        }
        int end = trades.size() - 1;
        double sum = 0;
        for (int i = end - period + 1; i <= end; i++) {
            Trade bar = trades.get(i);
            if (bar == null || bar.getClose() == null) {
                return null;
            }
            sum += bar.getClose();
        }
        return sum / period;
    }

    /** @deprecated 使用 {@link #passesMaBull(Trade)} */
    public static boolean passesRightTrend(Trade bar) {
        return passesMaBull(bar);
    }

    /** 均价之上：close &gt; max(MA5, MA10) */
    public static boolean passesAboveMa(Trade bar) {
        if (bar == null || bar.getClose() == null || bar.getMa5() == null || bar.getMa10() == null) {
            return false;
        }
        double maxMa = Math.max(bar.getMa5(), bar.getMa10());
        return bar.getClose() > maxMa + EPS;
    }
}
