package com.yh.bigdata.tts.spider.strategy.tools.retest;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.RetestStrategyParams;
import lombok.Getter;

/**
 * 四档合判 + bear/bull 标签
 */
public final class RetestTierTools {

    private RetestTierTools() {
    }

    @Getter
    public static final class TierHit {
        private final RetestStructureTools.StructureHit structure;
        private final boolean bear;
        private final boolean bull;

        TierHit(RetestStructureTools.StructureHit structure, boolean bear, boolean bull) {
            this.structure = structure;
            this.bear = bear;
            this.bull = bull;
        }
    }

    public static TierHit evaluateTier(StockBase stock, RetestStrategyParams p,
                                       RetestStructureTools.StructureHit structure) {
        if (structure == null) {
            return null;
        }
        PeriodTypeEnum macro = RetestStructureTools.macroPeriodFor(structure.getKind());
        boolean bear = p.isEnableBear() && RetestMacroTools.passBear(stock, macro, structure);
        boolean bull = p.isEnableBull() && RetestMacroTools.passBull(stock, macro, structure);
        if (!bear && !bull) {
            return null;
        }
        return new TierHit(structure, bear, bull);
    }

    public static TierHit findUltraHit(StockBase stock, RetestStrategyParams p) {
        if (!p.isEnableUltra()) {
            return null;
        }
        return evaluateTier(stock, p, RetestStructureTools.findUltraHit(stock, p));
    }

    public static TierHit findShortHit(StockBase stock, RetestStrategyParams p) {
        if (!p.isEnableShort()) {
            return null;
        }
        return evaluateTier(stock, p, RetestStructureTools.findShortHit(stock, p));
    }

    public static TierHit findMediumHit(StockBase stock, RetestStrategyParams p) {
        if (!p.isEnableMedium()) {
            return null;
        }
        return evaluateTier(stock, p, RetestStructureTools.findMediumHit(stock, p));
    }

    public static TierHit findLongHit(StockBase stock, RetestStrategyParams p) {
        if (!p.isEnableLong()) {
            return null;
        }
        return evaluateTier(stock, p, RetestStructureTools.findLongHit(stock, p));
    }
}
