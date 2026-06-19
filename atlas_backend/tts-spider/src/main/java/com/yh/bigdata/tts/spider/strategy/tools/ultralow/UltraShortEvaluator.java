package com.yh.bigdata.tts.spider.strategy.tools.ultralow;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.UltraShortStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class UltraShortEvaluator {

    private UltraShortEvaluator() {
    }

    public static UltraShortEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                UltraShortStrategyParams params) {
        UltraShortStrategyParams p = params != null ? params : UltraShortStrategyParams.defaults();
        UltraShortBreakoutTools.Hit hit = UltraShortBreakoutTools.findHit(stock, p);
        boolean success = hit != null;
        UltraShortEvaluation eval = new UltraShortEvaluation(hit, success);
        if (success && checkResult != null) {
            fillMessages(checkResult, eval);
        }
        return eval;
    }

    private static void fillMessages(CheckResult checkResult, UltraShortEvaluation eval) {
        checkResult.addTrendPeriod(PeriodTypeEnum.MIN30, UltraShortScoreCalculator.buildTrendMessage(eval));
        checkResult.addSignal(PeriodTypeEnum.MIN30, UltraShortScoreCalculator.buildSignalMessage(eval));
        eval.setScore(UltraShortScoreCalculator.computeScore(eval));
    }

    @Getter
    public static final class UltraShortEvaluation {
        private final UltraShortBreakoutTools.Hit hit;
        private final boolean hitFlag;
        private int score;

        UltraShortEvaluation(UltraShortBreakoutTools.Hit hit, boolean hitFlag) {
            this.hit = hit;
            this.hitFlag = hitFlag;
        }

        public boolean isHit() {
            return hitFlag;
        }

        void setScore(int score) {
            this.score = score;
        }
    }
}
