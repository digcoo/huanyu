package com.yh.bigdata.tts.spider.strategy.tools.bodybar;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.BodyBarTierStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class BodyBarTierEvaluator {

    private BodyBarTierEvaluator() {
    }

    public static BodyBarTierEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                 BodyBarTierStrategyParams params) {
        BodyBarTierStrategyParams p = params != null ? params : BodyBarTierStrategyParams.defaults();
        if (!BodyBarTierTools.passesTierGate(stock, checkResult, p)) {
            return BodyBarTierEvaluation.miss();
        }
        return BodyBarTierEvaluation.hit(BodyBarTierTools.resolvePeriod(p.getTier()));
    }

    @Getter
    public static final class BodyBarTierEvaluation {
        private final com.yh.bigdata.tts.common.constants.PeriodTypeEnum period;
        private final boolean hit;

        BodyBarTierEvaluation(com.yh.bigdata.tts.common.constants.PeriodTypeEnum period, boolean hit) {
            this.period = period;
            this.hit = hit;
        }

        static BodyBarTierEvaluation miss() {
            return new BodyBarTierEvaluation(null, false);
        }

        static BodyBarTierEvaluation hit(com.yh.bigdata.tts.common.constants.PeriodTypeEnum period) {
            return new BodyBarTierEvaluation(period, true);
        }
    }
}
