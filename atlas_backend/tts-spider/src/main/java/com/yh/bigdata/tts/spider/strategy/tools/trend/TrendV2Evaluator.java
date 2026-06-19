package com.yh.bigdata.tts.spider.strategy.tools.trend;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.TrendV2StrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class TrendV2Evaluator {

    private TrendV2Evaluator() {
    }

    public static TrendV2Evaluation evaluate(StockBase stock, CheckResult checkResult,
                                           TrendV2StrategyParams params) {
        TrendV2StrategyParams p = params != null ? params : TrendV2StrategyParams.defaults();
        TrendBreakoutTools.Hit hit = TrendBreakoutTools.findHit(stock, p);
        boolean success = hit != null;
        TrendV2Evaluation eval = new TrendV2Evaluation(hit, success);
        if (success && checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.DAY, TrendV2ScoreCalculator.buildTrendMessage(eval));
            checkResult.addSignal(PeriodTypeEnum.DAY, TrendV2ScoreCalculator.buildSignalMessage(eval));
            eval.setScore(TrendV2ScoreCalculator.computeScore(eval));
        }
        return eval;
    }

    @Getter
    public static final class TrendV2Evaluation {
        private final TrendBreakoutTools.Hit hit;
        private final boolean hitFlag;
        private int score;

        TrendV2Evaluation(TrendBreakoutTools.Hit hit, boolean hitFlag) {
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
