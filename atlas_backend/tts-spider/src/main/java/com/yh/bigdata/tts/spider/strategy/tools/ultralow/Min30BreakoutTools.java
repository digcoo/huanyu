package com.yh.bigdata.tts.spider.strategy.tools.ultralow;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.UltraLowReboundStrategyParams;
import lombok.Getter;

/**
 * 30m 突破（兼容 markers API，委托 {@link BreakoutLadderTools}）
 */
public final class Min30BreakoutTools {

    private Min30BreakoutTools() {
    }

    public static BreakoutHit findBreakout(StockBase stock, UltraLowReboundStrategyParams params) {
        UltraLowReboundStrategyParams p = params != null ? params : UltraLowReboundStrategyParams.defaults();
        BreakoutLadderTools.TierHit hit = BreakoutLadderTools.findUltraHit(stock, p);
        if (hit == null) {
            return null;
        }
        return new BreakoutHit(
                hit.getReferenceBar(),
                hit.getSignalBar(),
                priorPeakHigh(hit.getReferenceBar()),
                hit.getScanWindowSize(),
                hit.getSignalBarCount());
    }

    private static double priorPeakHigh(Trade refBar) {
        return refBar != null && refBar.getHigh() != null ? refBar.getHigh() : 0;
    }

    @Getter
    public static final class BreakoutHit {
        private final Trade referenceBar;
        private final Trade signalBar;
        private final double priorPeakHigh;
        private final int scanWindowSize;
        private final int todayBarCount;

        public BreakoutHit(Trade referenceBar, Trade signalBar, double priorPeakHigh,
                           int scanWindowSize, int todayBarCount) {
            this.referenceBar = referenceBar;
            this.signalBar = signalBar;
            this.priorPeakHigh = priorPeakHigh;
            this.scanWindowSize = scanWindowSize;
            this.todayBarCount = todayBarCount;
        }
    }
}
