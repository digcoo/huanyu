package com.yh.bigdata.tts.spider.strategy.tools.prevbandhigh;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MaCrossPointStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class PrevBandHighEvaluator {

    private PrevBandHighEvaluator() {
    }

    public static PrevBandHighEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                  MaCrossPointStrategyParams params) {
        MaCrossPointStrategyParams p = params != null ? params : MaCrossPointStrategyParams.defaults();
        PeriodTypeEnum period = PrevBandHighTools.resolvePeriod(p.getTier());
        PrevBandHighTools.Hit hit = PrevBandHighTools.findHit(stock, p);
        if (hit == null) {
            if (checkResult != null && period != null) {
                checkResult.addTrendPeriod(period, "[PBH]未满足边沿突破前波段High");
            }
            return PrevBandHighEvaluation.miss();
        }
        if (!PrevBandHighTools.passesOptionalGates(stock, checkResult, p, hit)) {
            return PrevBandHighEvaluation.miss();
        }
        if (checkResult != null) {
            checkResult.addTrendPeriod(period, PrevBandHighTools.buildTrendMessage(hit));
            checkResult.addSignal(period, PrevBandHighTools.buildSignalMessage(hit));
        }
        return PrevBandHighEvaluation.hit(period);
    }

    @Getter
    public static final class PrevBandHighEvaluation {
        private final boolean hit;
        private final PeriodTypeEnum period;

        PrevBandHighEvaluation(boolean hit, PeriodTypeEnum period) {
            this.hit = hit;
            this.period = period;
        }

        static PrevBandHighEvaluation miss() {
            return new PrevBandHighEvaluation(false, null);
        }

        static PrevBandHighEvaluation hit(PeriodTypeEnum period) {
            return new PrevBandHighEvaluation(true, period);
        }
    }
}
