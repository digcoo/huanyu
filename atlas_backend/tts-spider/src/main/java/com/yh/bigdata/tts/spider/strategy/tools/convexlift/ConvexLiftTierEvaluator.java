package com.yh.bigdata.tts.spider.strategy.tools.convexlift;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.ConvexLiftTierStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class ConvexLiftTierEvaluator {

    private ConvexLiftTierEvaluator() {
    }

    public static ConvexLiftTierEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                    ConvexLiftTierStrategyParams params) {
        ConvexLiftTierStrategyParams p = params != null ? params : ConvexLiftTierStrategyParams.defaults();
        if (!ConvexLiftTierTools.passesDayConvexLift(stock, checkResult, p.getLookbackDay())) {
            return ConvexLiftTierEvaluation.miss();
        }
        if (!ConvexLiftTierTools.passesUpperPeriodLift(stock, checkResult, p)) {
            return ConvexLiftTierEvaluation.miss();
        }
        PeriodTypeEnum upperPeriod = ConvexLiftTierTools.resolvePeriod(p.getTier());
        if (checkResult != null) {
            String tierLabel = ConvexLiftTierTools.buildTierLabel(p);
            checkResult.addTrendPeriod(PeriodTypeEnum.DAY,
                    "[CLT]凸波段上移|" + tierLabel + "档");
            checkResult.addSignal(upperPeriod,
                    "凸波段上移,tier=" + tierLabel + ",upperPeriod=" + upperPeriod.getCode());
        }
        return ConvexLiftTierEvaluation.hit(upperPeriod);
    }

    @Getter
    public static final class ConvexLiftTierEvaluation {
        private final PeriodTypeEnum upperPeriod;
        private final boolean hit;

        ConvexLiftTierEvaluation(PeriodTypeEnum upperPeriod, boolean hit) {
            this.upperPeriod = upperPeriod;
            this.hit = hit;
        }

        static ConvexLiftTierEvaluation miss() {
            return new ConvexLiftTierEvaluation(null, false);
        }

        static ConvexLiftTierEvaluation hit(PeriodTypeEnum upperPeriod) {
            return new ConvexLiftTierEvaluation(upperPeriod, true);
        }

        public boolean isHit() {
            return hit;
        }
    }
}
