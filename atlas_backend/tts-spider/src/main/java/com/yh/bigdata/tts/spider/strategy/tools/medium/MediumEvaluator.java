package com.yh.bigdata.tts.spider.strategy.tools.medium;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MediumStrategyParams;
import com.yh.bigdata.tts.common.param.UltraShortStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.UltraShortGateTools;
import lombok.Getter;

public final class MediumEvaluator {

    private MediumEvaluator() {
    }

    public static MediumEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                            MediumStrategyParams params,
                                            UltraShortStrategyParams ultraParams) {
        MediumStrategyParams p = params != null ? params : MediumStrategyParams.defaults();
        MediumBreakoutTools.Hit hit = MediumBreakoutTools.findHit(stock, p);
        if (hit == null) {
            return new MediumEvaluation(null, false);
        }
        if (!UltraShortGateTools.passes(stock, p.isRequireUltra(), ultraParams)) {
            return new MediumEvaluation(hit, false);
        }
        MediumEvaluation eval = new MediumEvaluation(hit, true);
        if (checkResult != null) {
            if (p.isRequireUltra()) {
                UltraShortGateTools.appendMessages(checkResult, stock, ultraParams);
            }
            checkResult.addTrendPeriod(PeriodTypeEnum.WEEK, MediumScoreCalculator.buildTrendMessage(eval));
            checkResult.addSignal(PeriodTypeEnum.WEEK, MediumScoreCalculator.buildSignalMessage(eval));
            eval.setScore(MediumScoreCalculator.computeScore(eval));
        }
        return eval;
    }

    @Getter
    public static final class MediumEvaluation {
        private final MediumBreakoutTools.Hit hit;
        private final boolean hitFlag;
        private int score;

        MediumEvaluation(MediumBreakoutTools.Hit hit, boolean hitFlag) {
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
