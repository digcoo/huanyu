package com.yh.bigdata.tts.spider.strategy.tools.gc2;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.Gc2StrategyParams;

/**
 * 金叉二次突破 · 三档扫描
 */
public final class Gc2TierTools {

    private Gc2TierTools() {
    }

    public static Gc2BreakoutTools.TierHit findShortHit(StockBase stock, Gc2StrategyParams p) {
        if (p == null || !p.isEnableShort()) {
            return null;
        }
        return Gc2BreakoutTools.findShortHit(stock, p);
    }

    public static Gc2BreakoutTools.TierHit findMediumHit(StockBase stock, Gc2StrategyParams p) {
        if (p == null || !p.isEnableMedium()) {
            return null;
        }
        return Gc2BreakoutTools.findMediumHit(stock, p);
    }

    public static Gc2BreakoutTools.TierHit findLongHit(StockBase stock, Gc2StrategyParams p) {
        if (p == null || !p.isEnableLong()) {
            return null;
        }
        return Gc2BreakoutTools.findLongHit(stock, p);
    }
}
