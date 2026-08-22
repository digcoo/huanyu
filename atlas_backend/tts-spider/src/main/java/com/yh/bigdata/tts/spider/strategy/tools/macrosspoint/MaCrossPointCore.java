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

    /** 从 lastIdx 往前找最近交叉 K（不含 lastIdx 本身，交叉须在末K之前） */
    public static int findLatestCrossIndex(List<Trade> trades, int lastIdx, CrossKind kind) {
        if (trades == null || lastIdx < 2 || kind == null) {
            return -1;
        }
        for (int i = lastIdx - 1; i >= 1; i--) {
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

    /** 边沿突破：prev.close ≤ 基准价，signal.close &gt; 基准价 */
    public static boolean passesEdgeBreak(Trade prevBar, Trade signalBar, double breakLine) {
        if (prevBar == null || signalBar == null) {
            return false;
        }
        Double prevClose = prevBar.getClose();
        Double signalClose = signalBar.getClose();
        if (prevClose == null || signalClose == null) {
            return false;
        }
        return prevClose <= breakLine + EPS && signalClose > breakLine + EPS;
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
