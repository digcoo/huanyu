package com.yh.bigdata.tts.spider.strategy.tools.ma3m;

import com.yh.bigdata.tts.common.model.Trade;

/**
 * 3M 多/空排列与均线 MAX 突破公共逻辑（策略2/3 复用）。
 */
public final class Ma3mCore {

    private static final double EPS = 1e-6;

    private Ma3mCore() {
    }

    public enum AlignKind {
        /** 3M1：相对 MA20 */
        MA20,
        /** 3M2：相对 MA30 */
        MA30
    }

    public enum BreakKind {
        EDGE,
        OPEN
    }

    public static boolean passesBullAlign(Trade bar, AlignKind align) {
        if (bar == null || bar.getMa5() == null || bar.getMa10() == null || align == null) {
            return false;
        }
        if (align == AlignKind.MA30) {
            if (bar.getMa30() == null) {
                return false;
            }
            return bar.getMa5() + EPS >= bar.getMa30() && bar.getMa10() + EPS >= bar.getMa30();
        }
        if (bar.getMa20() == null) {
            return false;
        }
        return bar.getMa5() + EPS >= bar.getMa20() && bar.getMa10() + EPS >= bar.getMa20();
    }

    public static boolean passesAnyBullAlign(Trade bar) {
        return passesBullAlign(bar, AlignKind.MA20) || passesBullAlign(bar, AlignKind.MA30);
    }

    public static boolean passesBearAlign(Trade bar, AlignKind align) {
        if (bar == null || bar.getMa5() == null || bar.getMa10() == null || align == null) {
            return false;
        }
        if (align == AlignKind.MA30) {
            if (bar.getMa30() == null) {
                return false;
            }
            return bar.getMa5() <= bar.getMa30() + EPS && bar.getMa10() <= bar.getMa30() + EPS;
        }
        if (bar.getMa20() == null) {
            return false;
        }
        return bar.getMa5() <= bar.getMa20() + EPS && bar.getMa10() <= bar.getMa20() + EPS;
    }

    public static boolean passesAnyBearAlign(Trade bar) {
        return passesBearAlign(bar, AlignKind.MA20) || passesBearAlign(bar, AlignKind.MA30);
    }

    /** 日线空头附加：close &gt; max(MA5, MA10) */
    public static boolean passesBearCloseAboveMa5Ma10(Trade bar) {
        if (bar == null || bar.getClose() == null || bar.getMa5() == null || bar.getMa10() == null) {
            return false;
        }
        double maxShort = Math.max(bar.getMa5(), bar.getMa10());
        return bar.getClose() > maxShort + EPS;
    }

    /** 均线 MAX = max(MA5, MA10, MA20, MA30)，缺任一则无法计算 */
    public static Double maMax(Trade bar) {
        if (bar == null || bar.getMa5() == null || bar.getMa10() == null
                || bar.getMa20() == null || bar.getMa30() == null) {
            return null;
        }
        return Math.max(Math.max(bar.getMa5(), bar.getMa10()),
                Math.max(bar.getMa20(), bar.getMa30()));
    }

    /**
     * 边沿突破或开盘突破均线 MAX：
     * close &gt; MAX，且（prev.close ≤ MAX 或 open ≤ MAX）。
     */
    public static BreakKind resolveMaMaxBreak(Trade prevBar, Trade signalBar) {
        Double max = maMax(signalBar);
        if (max == null || signalBar == null || signalBar.getClose() == null) {
            return null;
        }
        if (signalBar.getClose() <= max + EPS) {
            return null;
        }
        boolean edge = prevBar != null && prevBar.getClose() != null
                && prevBar.getClose() <= max + EPS;
        if (edge) {
            return BreakKind.EDGE;
        }
        boolean open = signalBar.getOpen() != null && signalBar.getOpen() <= max + EPS;
        return open ? BreakKind.OPEN : null;
    }

    public static boolean isYang(Trade bar) {
        if (bar == null || bar.getOpen() == null || bar.getClose() == null) {
            return false;
        }
        return bar.getClose() >= bar.getOpen() - EPS;
    }

    /** MA5 ≥ MA10 ≥ MA20 ≥ MA30 */
    public static boolean passes4mBullAlign(Trade bar) {
        if (bar == null || bar.getMa5() == null || bar.getMa10() == null
                || bar.getMa20() == null || bar.getMa30() == null) {
            return false;
        }
        return bar.getMa5() + EPS >= bar.getMa10()
                && bar.getMa10() + EPS >= bar.getMa20()
                && bar.getMa20() + EPS >= bar.getMa30();
    }
}
