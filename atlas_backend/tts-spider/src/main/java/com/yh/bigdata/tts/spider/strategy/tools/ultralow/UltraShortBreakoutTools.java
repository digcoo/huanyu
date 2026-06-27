package com.yh.bigdata.tts.spider.strategy.tools.ultralow;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.UltraShortStrategyParams;
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.CrossPeriodInBarBreakoutTools;
import lombok.Getter;

/**
 * 超短 Min30 · 跨日桶强K基准 + 柱内 30m 突破（与 nrf 同引擎，背景桶=交易日、操作K=30m）
 */
public final class UltraShortBreakoutTools {

    private UltraShortBreakoutTools() {
    }

    @Getter
    public static final class Hit {
        private final Trade referenceBar;
        private final Trade signalBar;
        private final int scanWindowSize;
        private final int signalBarCount;

        Hit(Trade referenceBar, Trade signalBar, int scanWindowSize, int signalBarCount) {
            this.referenceBar = referenceBar;
            this.signalBar = signalBar;
            this.scanWindowSize = scanWindowSize;
            this.signalBarCount = signalBarCount;
        }
    }

    public static Hit findHit(StockBase stock, UltraShortStrategyParams params) {
        UltraShortStrategyParams p = params != null ? params : UltraShortStrategyParams.defaults();
        CrossPeriodInBarBreakoutTools.TierSpec spec = CrossPeriodInBarBreakoutTools.min30DayBucketTier(
                p.getPrevDays(),
                p.getMaxBarsPerDay(),
                p.getMinStrongPct(),
                p.isRequireCurrentBreakout());
        CrossPeriodInBarBreakoutTools.Hit cpHit = CrossPeriodInBarBreakoutTools.findHit(stock, spec);
        if (cpHit == null) {
            return null;
        }
        return new Hit(
                cpHit.getReferenceBar(),
                cpHit.getSignalBar(),
                cpHit.getScanWindowSize(),
                cpHit.getSignalBarCount());
    }
}
