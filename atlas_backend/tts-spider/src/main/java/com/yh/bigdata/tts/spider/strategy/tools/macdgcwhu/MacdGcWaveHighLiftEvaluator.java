package com.yh.bigdata.tts.spider.strategy.tools.macdgcwhu;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MacdGcWaveHighLiftStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class MacdGcWaveHighLiftEvaluator {

    private MacdGcWaveHighLiftEvaluator() {
    }

    public static MacdGcWaveHighLiftEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                        MacdGcWaveHighLiftStrategyParams params) {
        MacdGcWaveHighLiftStrategyParams p = params != null ? params : MacdGcWaveHighLiftStrategyParams.defaults();
        MacdGcWaveHighLiftTools.TierHit hit = MacdGcWaveHighLiftTools.resolveHit(stock, p);
        if (hit == null) {
            return MacdGcWaveHighLiftEvaluation.miss();
        }
        PeriodTypeEnum period = MacdGcWaveHighLiftTools.resolvePeriod(p.getTier());
        if (!MacdGcWaveHighLiftTools.passesOptionalGates(
                stock, checkResult, period, p, hit.getSignalBar(), hit.getPrevBar())) {
            return MacdGcWaveHighLiftEvaluation.miss();
        }
        if (checkResult != null && period != null) {
            String tierLabel = MacdGcWaveHighLiftTools.buildTierLabel(p);
            checkResult.addTrendPeriod(period, "[MGCWHU]" + tierLabel + "MACD金叉波段High上移");
            checkResult.addSignal(period, MacdGcWaveHighLiftTools.buildSignalMessage(period, hit));
        }
        return MacdGcWaveHighLiftEvaluation.hit(period);
    }

    @Getter
    public static final class MacdGcWaveHighLiftEvaluation {
        private final PeriodTypeEnum period;
        private final boolean hit;

        MacdGcWaveHighLiftEvaluation(PeriodTypeEnum period, boolean hit) {
            this.period = period;
            this.hit = hit;
        }

        static MacdGcWaveHighLiftEvaluation miss() {
            return new MacdGcWaveHighLiftEvaluation(null, false);
        }

        static MacdGcWaveHighLiftEvaluation hit(PeriodTypeEnum period) {
            return new MacdGcWaveHighLiftEvaluation(period, true);
        }
    }
}
