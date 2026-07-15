package com.yh.bigdata.tts.spider.strategy.tools.macdgcwhr;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MacdGcWaveHighRetestStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class MacdGcWaveHighRetestEvaluator {

    private MacdGcWaveHighRetestEvaluator() {
    }

    public static MacdGcWaveHighRetestEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                          MacdGcWaveHighRetestStrategyParams params) {
        MacdGcWaveHighRetestStrategyParams p = params != null ? params : MacdGcWaveHighRetestStrategyParams.defaults();
        MacdGcWaveHighRetestTools.TierHit hit = MacdGcWaveHighRetestTools.resolveHit(stock, p);
        if (hit == null) {
            return MacdGcWaveHighRetestEvaluation.miss();
        }
        PeriodTypeEnum period = MacdGcWaveHighRetestTools.resolvePeriod(p.getTier());
        if (!MacdGcWaveHighRetestTools.passesOptionalGates(stock, checkResult, period, p)) {
            return MacdGcWaveHighRetestEvaluation.miss();
        }
        if (checkResult != null && period != null) {
            String tierLabel = MacdGcWaveHighRetestTools.buildTierLabel(p);
            checkResult.addTrendPeriod(period, "[MGCWHR]" + tierLabel + "MACD金叉波段High回踩");
            checkResult.addSignal(period, MacdGcWaveHighRetestTools.buildSignalMessage(period, hit));
        }
        return MacdGcWaveHighRetestEvaluation.hit(period);
    }

    @Getter
    public static final class MacdGcWaveHighRetestEvaluation {
        private final PeriodTypeEnum period;
        private final boolean hit;

        MacdGcWaveHighRetestEvaluation(PeriodTypeEnum period, boolean hit) {
            this.period = period;
            this.hit = hit;
        }

        static MacdGcWaveHighRetestEvaluation miss() {
            return new MacdGcWaveHighRetestEvaluation(null, false);
        }

        static MacdGcWaveHighRetestEvaluation hit(PeriodTypeEnum period) {
            return new MacdGcWaveHighRetestEvaluation(period, true);
        }
    }
}
