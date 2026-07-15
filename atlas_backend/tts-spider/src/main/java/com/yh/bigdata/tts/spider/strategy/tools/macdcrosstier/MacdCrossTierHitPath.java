package com.yh.bigdata.tts.spider.strategy.tools.macdcrosstier;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.strategy.tools.macedge.MacdEdgeBreakoutTools;
import lombok.Getter;

/**
 * MACD 交叉突破命中路径（金叉K突破 / 死叉K突破 / 当前金叉+上涨率）。
 */
@Getter
public final class MacdCrossTierHitPath {

    public enum Kind {
        GOLDEN_CROSS_BREAKOUT,
        DEATH_CROSS_BREAKOUT,
        GOLDEN_CROSS_RISE
    }

    private final Kind kind;
    private final MacdEdgeBreakoutTools.TierHit tierHit;
    private final PeriodTypeEnum period;
    private final Trade signalBar;
    private final Trade prevBar;

    MacdCrossTierHitPath(Kind kind, MacdEdgeBreakoutTools.TierHit tierHit,
                         PeriodTypeEnum period, Trade signalBar, Trade prevBar) {
        this.kind = kind;
        this.tierHit = tierHit;
        this.period = period;
        this.signalBar = signalBar;
        this.prevBar = prevBar;
    }

    static MacdCrossTierHitPath fromBreakout(MacdEdgeBreakoutTools.TierHit hit, Kind kind) {
        return new MacdCrossTierHitPath(kind, hit, hit.getSignalTier(), hit.getSignalBar(), hit.getPrevBar());
    }

    static MacdCrossTierHitPath fromRise(PeriodTypeEnum period, Trade signalBar, Trade prevBar) {
        return new MacdCrossTierHitPath(Kind.GOLDEN_CROSS_RISE, null, period, signalBar, prevBar);
    }
}
