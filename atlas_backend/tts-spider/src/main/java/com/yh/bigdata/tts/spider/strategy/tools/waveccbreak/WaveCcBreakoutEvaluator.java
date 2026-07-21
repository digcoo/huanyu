package com.yh.bigdata.tts.spider.strategy.tools.waveccbreak;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.WaveCcBreakoutStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class WaveCcBreakoutEvaluator {

    private WaveCcBreakoutEvaluator() {
    }

    public static WaveCcBreakoutEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                    WaveCcBreakoutStrategyParams params) {
        WaveCcBreakoutStrategyParams p = params != null ? params : WaveCcBreakoutStrategyParams.defaults();
        WaveCcBreakoutTools.TierHit hit = WaveCcBreakoutTools.resolveHit(stock, p);
        if (hit == null) {
            return WaveCcBreakoutEvaluation.miss();
        }
        PeriodTypeEnum period = WaveCcBreakoutTools.resolvePeriod(p.getTier());
        if (!WaveCcBreakoutTools.passesOptionalGates(
                stock, checkResult, period, p, hit.getSignalBar(), hit.getPrevBar())) {
            return WaveCcBreakoutEvaluation.miss();
        }
        if (checkResult != null && period != null) {
            String tierLabel = WaveCcBreakoutTools.buildTierLabel(p);
            String shapeLabel = hit.getShape() == WaveCcBreakoutTools.BandShape.CONVEX ? "凸" : "凹";
            checkResult.addTrendPeriod(period, "[WCCB]" + tierLabel + shapeLabel + "波段突破");
            checkResult.addSignal(period, WaveCcBreakoutTools.buildSignalMessage(period, hit));
        }
        return WaveCcBreakoutEvaluation.hit(period);
    }

    @Getter
    public static final class WaveCcBreakoutEvaluation {
        private final PeriodTypeEnum period;
        private final boolean hit;

        WaveCcBreakoutEvaluation(PeriodTypeEnum period, boolean hit) {
            this.period = period;
            this.hit = hit;
        }

        static WaveCcBreakoutEvaluation miss() {
            return new WaveCcBreakoutEvaluation(null, false);
        }

        static WaveCcBreakoutEvaluation hit(PeriodTypeEnum period) {
            return new WaveCcBreakoutEvaluation(period, true);
        }
    }
}
