package com.yh.bigdata.tts.spider.strategy.tools.gc2;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.Gc2StrategyParams;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;

/**
 * 金叉二次突破 v2.0 · 短/长两档扫描（含大周期 MACD 门槛）
 */
public final class Gc2TierTools {

    private Gc2TierTools() {
    }

    public static Gc2BreakoutTools.TierHit findShortHit(StockBase stock, Gc2StrategyParams p) {
        if (p == null || !p.isEnableShort()) {
            return null;
        }
        boolean weekMacdPositive = UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.WEEK);
        boolean monthMacdPositive = UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.MONTH);
        if (!weekMacdPositive && !monthMacdPositive) {
            return null;
        }
        return Gc2BreakoutTools.findShortHit(stock, p);
    }

    public static Gc2BreakoutTools.TierHit findLongHit(StockBase stock, Gc2StrategyParams p) {
        if (p == null || !p.isEnableLong()) {
            return null;
        }
        boolean monthMacdPositive = UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.MONTH);
        boolean yearMacdPositive = UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.YEAR);
        if (!monthMacdPositive && !yearMacdPositive) {
            return null;
        }
        return Gc2BreakoutTools.findLongHit(stock, p);
    }
}
