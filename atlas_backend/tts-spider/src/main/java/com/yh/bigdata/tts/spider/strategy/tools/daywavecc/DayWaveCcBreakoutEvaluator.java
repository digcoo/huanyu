package com.yh.bigdata.tts.spider.strategy.tools.daywavecc;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.DayWaveCcBreakoutStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class DayWaveCcBreakoutEvaluator {

    private DayWaveCcBreakoutEvaluator() {
    }

    public static DayWaveCcBreakoutEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                       DayWaveCcBreakoutStrategyParams params) {
        DayWaveCcBreakoutStrategyParams p = params != null ? params : DayWaveCcBreakoutStrategyParams.defaults();
        DayWaveCcBreakoutTools.Hit hit = DayWaveCcBreakoutTools.findHit(stock, p);
        if (hit == null) {
            return DayWaveCcBreakoutEvaluation.miss();
        }
        if (!DayWaveCcBreakoutTools.passesOptionalGates(
                stock, checkResult, p, hit.getSignalBar(), hit.getPrevBar())) {
            return DayWaveCcBreakoutEvaluation.miss();
        }
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH, DayWaveCcBreakoutTools.buildTrendMessage(hit));
            checkResult.addSignal(PeriodTypeEnum.DAY, DayWaveCcBreakoutTools.buildSignalMessage(hit));
        }
        return DayWaveCcBreakoutEvaluation.hit();
    }

    @Getter
    public static final class DayWaveCcBreakoutEvaluation {
        private final boolean hit;

        DayWaveCcBreakoutEvaluation(boolean hit) {
            this.hit = hit;
        }

        static DayWaveCcBreakoutEvaluation miss() {
            return new DayWaveCcBreakoutEvaluation(false);
        }

        static DayWaveCcBreakoutEvaluation hit() {
            return new DayWaveCcBreakoutEvaluation(true);
        }
    }
}
