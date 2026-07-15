package com.yh.bigdata.tts.spider.strategy.tools.macddcb;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MacdDcBreakoutStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class MacdDcBreakoutEvaluator {

    private MacdDcBreakoutEvaluator() {
    }

    public static MacdDcBreakoutEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                    MacdDcBreakoutStrategyParams params) {
        MacdDcBreakoutStrategyParams p = params != null ? params : MacdDcBreakoutStrategyParams.defaults();
        MacdDcBreakoutTools.TierHit hit = MacdDcBreakoutTools.resolveHit(stock, p);
        if (hit == null) {
            return MacdDcBreakoutEvaluation.miss();
        }
        PeriodTypeEnum period = MacdDcBreakoutTools.resolvePeriod(p.getTier());
        if (!MacdDcBreakoutTools.passesOptionalGates(
                stock, checkResult, period, p, hit.getSignalBar(), hit.getPrevBar())) {
            return MacdDcBreakoutEvaluation.miss();
        }
        if (checkResult != null && period != null) {
            String tierLabel = MacdDcBreakoutTools.buildTierLabel(p);
            checkResult.addTrendPeriod(period, "[MDCB]" + tierLabel + "MACD死叉突破");
            checkResult.addSignal(period, MacdDcBreakoutTools.buildSignalMessage(period, hit));
        }
        return MacdDcBreakoutEvaluation.hit(period);
    }

    @Getter
    public static final class MacdDcBreakoutEvaluation {
        private final PeriodTypeEnum period;
        private final boolean hit;

        MacdDcBreakoutEvaluation(PeriodTypeEnum period, boolean hit) {
            this.period = period;
            this.hit = hit;
        }

        static MacdDcBreakoutEvaluation miss() {
            return new MacdDcBreakoutEvaluation(null, false);
        }

        static MacdDcBreakoutEvaluation hit(PeriodTypeEnum period) {
            return new MacdDcBreakoutEvaluation(period, true);
        }
    }
}
