package com.yh.bigdata.tts.spider.strategy.tools.mabearbreakma;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MaBearBreakMaStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class MaBearBreakMaEvaluator {

    private MaBearBreakMaEvaluator() {
    }

    public static MaBearBreakMaEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                   MaBearBreakMaStrategyParams params) {
        MaBearBreakMaStrategyParams p = params != null ? params : MaBearBreakMaStrategyParams.defaults();

        MaBearBreakMaTools.Hit hit = MaBearBreakMaTools.findHit(stock, p);
        if (hit == null) {
            if (checkResult != null) {
                checkResult.addTrendPeriod(PeriodTypeEnum.DAY, "[MBBM]未满足日线3M空头+30分3M多头破MAX");
            }
            return MaBearBreakMaEvaluation.miss();
        }

        if (!MaBearBreakMaTools.passesOptionalGates(stock, checkResult, p)) {
            return MaBearBreakMaEvaluation.miss();
        }

        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.MIN30, MaBearBreakMaTools.buildTrendMessage(hit));
            checkResult.addSignal(PeriodTypeEnum.MIN30, MaBearBreakMaTools.buildSignalMessage(hit));
        }
        return MaBearBreakMaEvaluation.hit(PeriodTypeEnum.MIN30);
    }

    @Getter
    public static final class MaBearBreakMaEvaluation {
        private final boolean hit;
        private final PeriodTypeEnum period;

        MaBearBreakMaEvaluation(boolean hit, PeriodTypeEnum period) {
            this.hit = hit;
            this.period = period;
        }

        static MaBearBreakMaEvaluation miss() {
            return new MaBearBreakMaEvaluation(false, null);
        }

        static MaBearBreakMaEvaluation hit(PeriodTypeEnum period) {
            return new MaBearBreakMaEvaluation(true, period);
        }
    }
}
