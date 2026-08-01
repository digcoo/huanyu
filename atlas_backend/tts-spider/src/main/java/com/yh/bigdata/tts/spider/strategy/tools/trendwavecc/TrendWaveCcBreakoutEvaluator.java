package com.yh.bigdata.tts.spider.strategy.tools.trendwavecc;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.TrendWaveCcBreakoutStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class TrendWaveCcBreakoutEvaluator {

    private TrendWaveCcBreakoutEvaluator() {
    }

    public static TrendWaveCcBreakoutEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                       TrendWaveCcBreakoutStrategyParams params) {
        TrendWaveCcBreakoutStrategyParams p = params != null ? params : TrendWaveCcBreakoutStrategyParams.defaults();
        if (!TrendWaveCcBreakoutTools.passesTrendMacdGate(stock, checkResult, p)) {
            return TrendWaveCcBreakoutEvaluation.miss();
        }
        TrendWaveCcBreakoutTools.Hit hit = TrendWaveCcBreakoutTools.findHit(stock, p);
        if (hit == null) {
            return TrendWaveCcBreakoutEvaluation.miss();
        }
        if (!TrendWaveCcBreakoutTools.passesOptionalGates(stock, checkResult, p, hit)) {
            return TrendWaveCcBreakoutEvaluation.miss();
        }
        if (checkResult != null) {
            checkResult.addTrendPeriod(TrendWaveCcBreakoutTools.resolvePrimaryTrendPeriod(p),
                    TrendWaveCcBreakoutTools.buildTrendMessage(hit, p));
            checkResult.addSignal(hit.getSignalPeriod(), TrendWaveCcBreakoutTools.buildSignalMessage(hit));
        }
        return TrendWaveCcBreakoutEvaluation.hit();
    }

    @Getter
    public static final class TrendWaveCcBreakoutEvaluation {
        private final boolean hit;

        TrendWaveCcBreakoutEvaluation(boolean hit) {
            this.hit = hit;
        }

        static TrendWaveCcBreakoutEvaluation miss() {
            return new TrendWaveCcBreakoutEvaluation(false);
        }

        static TrendWaveCcBreakoutEvaluation hit() {
            return new TrendWaveCcBreakoutEvaluation(true);
        }
    }
}
