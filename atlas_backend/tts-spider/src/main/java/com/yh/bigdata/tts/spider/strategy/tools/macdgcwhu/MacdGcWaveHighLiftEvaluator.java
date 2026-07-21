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
        if (!MacdGcWaveHighLiftTools.passesMultiPeriodMacdGate(stock, checkResult, p)) {
            return MacdGcWaveHighLiftEvaluation.miss();
        }
        MacdGcWaveHighLiftTools.Hit hit = MacdGcWaveHighLiftTools.resolveHit(stock, p);
        if (hit == null) {
            return MacdGcWaveHighLiftEvaluation.miss();
        }
        if (!MacdGcWaveHighLiftTools.passesOptionalGates(
                stock, checkResult, p, hit.getSignalBar(), hit.getPrevBar())) {
            return MacdGcWaveHighLiftEvaluation.miss();
        }
        if (checkResult != null) {
            String trendMessage = MacdGcWaveHighLiftTools.buildTrendMessage(p);
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH, trendMessage);
            checkResult.addTrendPeriod(PeriodTypeEnum.WEEK, trendMessage);
            checkResult.addTrendPeriod(PeriodTypeEnum.DAY, trendMessage);
            checkResult.addSignal(PeriodTypeEnum.DAY, MacdGcWaveHighLiftTools.buildSignalMessage(hit));
        }
        return MacdGcWaveHighLiftEvaluation.hit();
    }

    @Getter
    public static final class MacdGcWaveHighLiftEvaluation {
        private final boolean hit;

        MacdGcWaveHighLiftEvaluation(boolean hit) {
            this.hit = hit;
        }

        static MacdGcWaveHighLiftEvaluation miss() {
            return new MacdGcWaveHighLiftEvaluation(false);
        }

        static MacdGcWaveHighLiftEvaluation hit() {
            return new MacdGcWaveHighLiftEvaluation(true);
        }
    }
}
