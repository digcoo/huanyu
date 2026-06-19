package com.yh.bigdata.tts.spider.strategy.tools.dc2;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.Dc2StrategyParams;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;

/**
 * 死叉突破 · 短/长两档（大周期 MACD&gt;0 门槛，与 gc2 相同）
 */
public final class Dc2TierTools {

    private Dc2TierTools() {
    }

    public static Dc2BreakoutTools.TierHit findShortHit(StockBase stock, Dc2StrategyParams p) {
        if (p == null || !p.isEnableShort()) {
            return null;
        }
        boolean weekMacdPositive = UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.WEEK);
        boolean monthMacdPositive = UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.MONTH);
        if (!weekMacdPositive && !monthMacdPositive) {
            return null;
        }
        return Dc2BreakoutTools.findShortHit(stock, p);
    }

    public static Dc2BreakoutTools.TierHit findLongHit(StockBase stock, Dc2StrategyParams p) {
        if (p == null || !p.isEnableLong()) {
            return null;
        }
        boolean monthMacdPositive = UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.MONTH);
        boolean yearMacdPositive = UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.YEAR);
        if (!monthMacdPositive && !yearMacdPositive) {
            return null;
        }
        return Dc2BreakoutTools.findLongHit(stock, p);
    }
}
