package com.yh.bigdata.tts.spider.strategy.tools.ultralow;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.UltraLowReboundStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

/**
 * 超短线：前1~2日 30m 新高强K + 当日 30m 突破
 */
public final class UltraLowReboundEvaluator {

    private UltraLowReboundEvaluator() {
    }

    public static UltraLowEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                              UltraLowReboundStrategyParams params) {
        UltraLowReboundStrategyParams p = params != null ? params : UltraLowReboundStrategyParams.defaults();

        Min30BreakoutTools.BreakoutHit breakout = Min30BreakoutTools.findBreakout(stock, p);
        boolean hit = breakout != null;
        UltraLowEvaluation eval = new UltraLowEvaluation(breakout, hit);

        if (hit && checkResult != null) {
            fillMessages(checkResult, eval, p);
        }
        return eval;
    }

    private static void fillMessages(CheckResult checkResult, UltraLowEvaluation eval,
                                     UltraLowReboundStrategyParams params) {
        char tier = UltraLowReboundScoreCalculator.computeTier(eval);
        if (!params.passTierFilter(tier)) {
            eval.hit = false;
            return;
        }

        String trendLabel = UltraLowReboundScoreCalculator.buildTrendLabel(tier);
        String trendDetail = UltraLowReboundScoreCalculator.buildTrendDetail(eval);
        checkResult.addTrendPeriod(
                UltraLowReboundScoreCalculator.trendPeriodForTier(tier),
                "[" + tier + "]" + trendLabel + "|" + trendDetail);
        checkResult.addSignal(
                UltraLowReboundScoreCalculator.signalPeriodForTier(tier),
                UltraLowReboundScoreCalculator.buildSignalDetail(eval));
        eval.setTier(tier);
        eval.setScore(UltraLowReboundScoreCalculator.computeScore(eval));
    }

    @Getter
    public static final class UltraLowEvaluation {
        private final Min30BreakoutTools.BreakoutHit breakout;
        private boolean hit;
        private int score;
        private char tier;

        public UltraLowEvaluation(Min30BreakoutTools.BreakoutHit breakout, boolean hit) {
            this.breakout = breakout;
            this.hit = hit;
        }

        void setScore(int score) {
            this.score = score;
        }

        void setTier(char tier) {
            this.tier = tier;
        }

        public boolean isHit() {
            return hit;
        }
    }
}
