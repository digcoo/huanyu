package com.yh.bigdata.tts.spider.strategy.tools.trendma;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.TrendMaStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class TrendMaEvaluator {

    private TrendMaEvaluator() {
    }

    public static TrendMaEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                             TrendMaStrategyParams params) {
        TrendMaStrategyParams p = params != null ? params : TrendMaStrategyParams.defaults();
        TrendMaTools.Hit hit = TrendMaTools.findHit(stock);
        if (hit == null) {
            if (checkResult != null) {
                checkResult.addTrendPeriod(PeriodTypeEnum.DAY, "[TMA]日/周/月趋势MA不足2档");
            }
            return TrendMaEvaluation.miss();
        }
        if (!TrendMaTools.passesOptionalGates(stock, checkResult, p)) {
            return TrendMaEvaluation.miss();
        }
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH, TrendMaTools.buildTrendMessage(hit));
            checkResult.addSignal(PeriodTypeEnum.DAY, TrendMaTools.buildSignalMessage(hit));
        }
        return TrendMaEvaluation.hit();
    }

    @Getter
    public static final class TrendMaEvaluation {
        private final boolean hit;

        TrendMaEvaluation(boolean hit) {
            this.hit = hit;
        }

        static TrendMaEvaluation miss() {
            return new TrendMaEvaluation(false);
        }

        static TrendMaEvaluation hit() {
            return new TrendMaEvaluation(true);
        }
    }
}
