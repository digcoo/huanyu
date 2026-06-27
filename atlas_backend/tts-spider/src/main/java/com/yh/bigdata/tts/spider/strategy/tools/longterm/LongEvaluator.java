package com.yh.bigdata.tts.spider.strategy.tools.longterm;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.LongStrategyParams;
import com.yh.bigdata.tts.common.param.UltraShortStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.UltraShortGateTools;
import lombok.Getter;

public final class LongEvaluator {

    private LongEvaluator() {
    }

    public static LongEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                          LongStrategyParams params,
                                          UltraShortStrategyParams ultraParams) {
        LongStrategyParams p = params != null ? params : LongStrategyParams.defaults();
        LongBreakoutTools.Hit hit = LongBreakoutTools.findHit(stock, p);
        if (hit == null) {
            return new LongEvaluation(null, false);
        }
        UltraShortGateTools.GateResult ultraGate =
                UltraShortGateTools.evaluate(stock, p.isRequireUltra(), ultraParams);
        if (!ultraGate.isPassed()) {
            return new LongEvaluation(hit, false);
        }
        LongEvaluation eval = new LongEvaluation(hit, true);
        if (checkResult != null) {
            if (ultraGate.isRequired()) {
                UltraShortGateTools.appendMessages(checkResult, ultraGate);
            }
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH, LongScoreCalculator.buildTrendMessage(eval));
            checkResult.addSignal(PeriodTypeEnum.MONTH, LongScoreCalculator.buildSignalMessage(eval));
            eval.setScore(LongScoreCalculator.computeScore(eval));
        }
        return eval;
    }

    @Getter
    public static final class LongEvaluation {
        private final LongBreakoutTools.Hit hit;
        private final boolean hitFlag;
        private int score;

        LongEvaluation(LongBreakoutTools.Hit hit, boolean hitFlag) {
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
