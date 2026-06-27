package com.yh.bigdata.tts.spider.strategy.tools.trend;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.TrendV2StrategyParams;
import com.yh.bigdata.tts.common.param.UltraShortStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.UltraShortGateTools;
import lombok.Getter;

public final class TrendV2Evaluator {

    private TrendV2Evaluator() {
    }

    public static TrendV2Evaluation evaluate(StockBase stock, CheckResult checkResult,
                                           TrendV2StrategyParams params,
                                           UltraShortStrategyParams ultraParams) {
        TrendV2StrategyParams p = params != null ? params : TrendV2StrategyParams.defaults();
        TrendBreakoutTools.Hit hit = TrendBreakoutTools.findHit(stock, p);
        if (hit == null) {
            return new TrendV2Evaluation(null, false);
        }
        UltraShortGateTools.GateResult ultraGate =
                UltraShortGateTools.evaluate(stock, p.isRequireUltra(), ultraParams);
        if (!ultraGate.isPassed()) {
            return new TrendV2Evaluation(hit, false);
        }
        TrendV2Evaluation eval = new TrendV2Evaluation(hit, true);
        if (checkResult != null) {
            if (ultraGate.isRequired()) {
                UltraShortGateTools.appendMessages(checkResult, ultraGate);
            }
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
