package com.yh.bigdata.tts.spider.strategy.tools.ultralow;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;

import java.util.List;

/**
 * 梯子内上移 · 突破K 统一判定（含宏观底部强突破 ref.high）
 */
public final class BreakoutSignalTools {

    private static final double HIGH_EPS = 1e-6;

    private BreakoutSignalTools() {
    }

    public static boolean isSignalCandidate(StockBase stock, Trade bar, List<Trade> allBars,
                                            double refLow, double refHigh, double minStrongPct,
                                            BreakoutPositionContextTools.MacroTier tier) {
        return isSignalCandidate(stock, bar, allBars, refLow, refHigh, minStrongPct, tier, true);
    }

    public static boolean isSignalCandidate(StockBase stock, Trade bar, List<Trade> allBars,
                                            double refLow, double refHigh, double minStrongPct,
                                            BreakoutPositionContextTools.MacroTier tier,
                                            boolean requireRefHighAtBottom) {
        if (!BreakoutBarTools.isStrongBar(bar, minStrongPct)) {
            return false;
        }
        if (bar.getClose() == null || bar.getClose() <= refLow + HIGH_EPS) {
            return false;
        }
        Trade prev = previousBar(allBars, bar);
        if (prev == null || prev.getHigh() == null) {
            return false;
        }
        if (bar.getClose() <= prev.getHigh() + HIGH_EPS) {
            return false;
        }
        if (!belowRefHigh(bar, prev, refHigh)) {
            return false;
        }
        if (requireRefHighAtBottom && tier != null && stock != null) {
            BreakoutPositionContextTools.ContextSnapshot ctx =
                    BreakoutPositionContextTools.evaluate(stock, tier);
            if (ctx.isReliable() && ctx.isAtBottom()
                    && bar.getClose() <= refHigh + HIGH_EPS) {
                return false;
            }
        }
        return true;
    }

    /** 突破K.low 或 前K.close 至少其一低于 ref.high */
    static boolean belowRefHigh(Trade signalBar, Trade prevBar, double refHigh) {
        boolean signalLowBelow = signalBar.getLow() != null && signalBar.getLow() < refHigh - HIGH_EPS;
        boolean prevCloseBelow = prevBar.getClose() != null && prevBar.getClose() < refHigh - HIGH_EPS;
        return signalLowBelow || prevCloseBelow;
    }

    static Trade previousBar(List<Trade> allBars, Trade bar) {
        for (int i = 1; i < allBars.size(); i++) {
            if (sameBar(allBars.get(i), bar)) {
                return allBars.get(i - 1);
            }
        }
        return null;
    }

    static boolean sameBar(Trade a, Trade b) {
        return a != null && b != null && a.getDay() != null && a.getDay().equals(b.getDay());
    }
}
