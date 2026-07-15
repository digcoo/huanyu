package com.yh.bigdata.tts.spider.strategy.tools.macdgcwh;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MacdGcWaveHighStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

/**
 * MACD金叉波段High突破评估。
 */
public final class MacdGcWaveHighEvaluator {

    private MacdGcWaveHighEvaluator() {
    }

    public static MacdGcWaveHighEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                    MacdGcWaveHighStrategyParams params) {
        MacdGcWaveHighStrategyParams p = params != null ? params : MacdGcWaveHighStrategyParams.defaults();
        MacdGcWaveHighTools.TierHit hit = MacdGcWaveHighTools.resolveHit(stock, p);
        if (hit == null) {
            return MacdGcWaveHighEvaluation.miss();
        }
        PeriodTypeEnum period = MacdGcWaveHighTools.resolvePeriod(p.getTier());
        if (!MacdGcWaveHighTools.passesOptionalGates(
                stock, checkResult, period, p, hit.getSignalBar(), hit.getPrevBar())) {
            return MacdGcWaveHighEvaluation.miss();
        }
        if (checkResult != null && period != null) {
            String tierLabel = MacdGcWaveHighTools.buildTierLabel(p);
            checkResult.addTrendPeriod(period, "[MGCWH]" + tierLabel + "MACD金叉波段High突破");
            checkResult.addSignal(period, MacdGcWaveHighTools.buildSignalMessage(period, hit));
        }
        return MacdGcWaveHighEvaluation.hit(period);
    }

    @Getter
    public static final class MacdGcWaveHighEvaluation {
        private final PeriodTypeEnum period;
        private final boolean hit;

        MacdGcWaveHighEvaluation(PeriodTypeEnum period, boolean hit) {
            this.period = period;
            this.hit = hit;
        }

        static MacdGcWaveHighEvaluation miss() {
            return new MacdGcWaveHighEvaluation(null, false);
        }

        static MacdGcWaveHighEvaluation hit(PeriodTypeEnum period) {
            return new MacdGcWaveHighEvaluation(period, true);
        }
    }
}
