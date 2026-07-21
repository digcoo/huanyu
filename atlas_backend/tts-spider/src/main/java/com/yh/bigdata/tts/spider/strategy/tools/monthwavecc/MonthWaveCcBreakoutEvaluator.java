package com.yh.bigdata.tts.spider.strategy.tools.monthwavecc;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MonthWaveCcBreakoutStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class MonthWaveCcBreakoutEvaluator {

    private MonthWaveCcBreakoutEvaluator() {
    }

    public static MonthWaveCcBreakoutEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                         MonthWaveCcBreakoutStrategyParams params) {
        MonthWaveCcBreakoutStrategyParams p = params != null ? params : MonthWaveCcBreakoutStrategyParams.defaults();
        MonthWaveCcBreakoutTools.Hit hit = MonthWaveCcBreakoutTools.findHit(stock, p);
        if (hit == null) {
            return MonthWaveCcBreakoutEvaluation.miss();
        }
        if (!MonthWaveCcBreakoutTools.passesOptionalGates(stock, checkResult, p, hit)) {
            return MonthWaveCcBreakoutEvaluation.miss();
        }
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH, MonthWaveCcBreakoutTools.buildTrendMessage(hit));
            checkResult.addSignal(PeriodTypeEnum.MONTH, MonthWaveCcBreakoutTools.buildSignalMessage(hit));
        }
        return MonthWaveCcBreakoutEvaluation.hit();
    }

    @Getter
    public static final class MonthWaveCcBreakoutEvaluation {
        private final boolean hit;

        MonthWaveCcBreakoutEvaluation(boolean hit) {
            this.hit = hit;
        }

        static MonthWaveCcBreakoutEvaluation miss() {
            return new MonthWaveCcBreakoutEvaluation(false);
        }

        static MonthWaveCcBreakoutEvaluation hit() {
            return new MonthWaveCcBreakoutEvaluation(true);
        }
    }
}
