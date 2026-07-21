package com.yh.bigdata.tts.spider.strategy.tools.weekwavecc;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.WeekWaveCcBreakoutStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class WeekWaveCcBreakoutEvaluator {

    private WeekWaveCcBreakoutEvaluator() {
    }

    public static WeekWaveCcBreakoutEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                       WeekWaveCcBreakoutStrategyParams params) {
        WeekWaveCcBreakoutStrategyParams p = params != null ? params : WeekWaveCcBreakoutStrategyParams.defaults();
        WeekWaveCcBreakoutTools.Hit hit = WeekWaveCcBreakoutTools.findHit(stock, p);
        if (hit == null) {
            return WeekWaveCcBreakoutEvaluation.miss();
        }
        if (!WeekWaveCcBreakoutTools.passesOptionalGates(stock, checkResult, p, hit)) {
            return WeekWaveCcBreakoutEvaluation.miss();
        }
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.WEEK, WeekWaveCcBreakoutTools.buildTrendMessage(hit));
            checkResult.addSignal(PeriodTypeEnum.WEEK, WeekWaveCcBreakoutTools.buildSignalMessage(hit));
        }
        return WeekWaveCcBreakoutEvaluation.hit();
    }

    @Getter
    public static final class WeekWaveCcBreakoutEvaluation {
        private final boolean hit;

        WeekWaveCcBreakoutEvaluation(boolean hit) {
            this.hit = hit;
        }

        static WeekWaveCcBreakoutEvaluation miss() {
            return new WeekWaveCcBreakoutEvaluation(false);
        }

        static WeekWaveCcBreakoutEvaluation hit() {
            return new WeekWaveCcBreakoutEvaluation(true);
        }
    }
}
