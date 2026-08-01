package com.yh.bigdata.tts.spider.strategy.tools.trendlift;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.TrendLiftStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.macd.MacdPositiveGateTools;
import lombok.Getter;

public final class TrendLiftEvaluator {

    private TrendLiftEvaluator() {
    }

    public static TrendLiftEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                               TrendLiftStrategyParams params) {
        TrendLiftStrategyParams p = params != null ? params : TrendLiftStrategyParams.defaults();
        if (!MacdPositiveGateTools.passGate(stock, checkResult, p.toMacdPositiveGateParams())) {
            return TrendLiftEvaluation.miss();
        }
        TrendLiftTools.Hit hit = TrendLiftTools.findHit(stock);
        if (hit == null) {
            if (checkResult != null) {
                checkResult.addTrendPeriod(PeriodTypeEnum.DAY, "[TLIFT]日close未高于前日low");
            }
            return TrendLiftEvaluation.miss();
        }
        if (!TrendLiftTools.passesOptionalGates(stock, checkResult, p)) {
            return TrendLiftEvaluation.miss();
        }
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH, TrendLiftTools.buildTrendMessage(hit));
            checkResult.addSignal(PeriodTypeEnum.DAY, TrendLiftTools.buildSignalMessage(hit));
        }
        return TrendLiftEvaluation.hit();
    }

    @Getter
    public static final class TrendLiftEvaluation {
        private final boolean hit;

        TrendLiftEvaluation(boolean hit) {
            this.hit = hit;
        }

        static TrendLiftEvaluation miss() {
            return new TrendLiftEvaluation(false);
        }

        static TrendLiftEvaluation hit() {
            return new TrendLiftEvaluation(true);
        }
    }
}
