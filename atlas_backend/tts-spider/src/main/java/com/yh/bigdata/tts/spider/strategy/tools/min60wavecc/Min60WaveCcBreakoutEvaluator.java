package com.yh.bigdata.tts.spider.strategy.tools.min60wavecc;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.Min60WaveCcBreakoutStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class Min60WaveCcBreakoutEvaluator {

    private Min60WaveCcBreakoutEvaluator() {
    }

    public static Min60WaveCcBreakoutEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                         Min60WaveCcBreakoutStrategyParams params) {
        Min60WaveCcBreakoutStrategyParams p = params != null ? params : Min60WaveCcBreakoutStrategyParams.defaults();
        Min60WaveCcBreakoutTools.Hit hit = Min60WaveCcBreakoutTools.findHit(stock, p);
        if (hit == null) {
            return Min60WaveCcBreakoutEvaluation.miss();
        }
        if (!Min60WaveCcBreakoutTools.passesOptionalGates(
                stock, checkResult, p, hit.getSignalBar(), hit.getPrevBar())) {
            return Min60WaveCcBreakoutEvaluation.miss();
        }
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.WEEK, Min60WaveCcBreakoutTools.buildTrendMessage(hit));
            checkResult.addSignal(PeriodTypeEnum.MIN60, Min60WaveCcBreakoutTools.buildSignalMessage(hit));
        }
        return Min60WaveCcBreakoutEvaluation.hit();
    }

    @Getter
    public static final class Min60WaveCcBreakoutEvaluation {
        private final boolean hit;

        Min60WaveCcBreakoutEvaluation(boolean hit) {
            this.hit = hit;
        }

        static Min60WaveCcBreakoutEvaluation miss() {
            return new Min60WaveCcBreakoutEvaluation(false);
        }

        static Min60WaveCcBreakoutEvaluation hit() {
            return new Min60WaveCcBreakoutEvaluation(true);
        }
    }
}
