package com.yh.bigdata.tts.spider.strategy.tools.mabull4m;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MaBull4mStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class MaBull4mEvaluator {

    private MaBull4mEvaluator() {
    }

    public static MaBull4mEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                              MaBull4mStrategyParams params) {
        MaBull4mStrategyParams p = params != null ? params : MaBull4mStrategyParams.defaults();
        PeriodTypeEnum period = MaBull4mTools.resolvePeriod(p.getTier());

        MaBull4mTools.Hit hit = MaBull4mTools.findHit(stock, p);
        if (hit == null) {
            if (checkResult != null) {
                checkResult.addTrendPeriod(period, "[M4M]未满足4M多头+收阳+close>前Low");
            }
            return MaBull4mEvaluation.miss();
        }

        if (!MaBull4mTools.passesOptionalGates(stock, checkResult, p)) {
            return MaBull4mEvaluation.miss();
        }

        if (checkResult != null) {
            checkResult.addTrendPeriod(period, MaBull4mTools.buildTrendMessage(hit));
            checkResult.addSignal(period, MaBull4mTools.buildSignalMessage(hit));
        }
        return MaBull4mEvaluation.hit(period);
    }

    @Getter
    public static final class MaBull4mEvaluation {
        private final boolean hit;
        private final PeriodTypeEnum period;

        MaBull4mEvaluation(boolean hit, PeriodTypeEnum period) {
            this.hit = hit;
            this.period = period;
        }

        static MaBull4mEvaluation miss() {
            return new MaBull4mEvaluation(false, null);
        }

        static MaBull4mEvaluation hit(PeriodTypeEnum period) {
            return new MaBull4mEvaluation(true, period);
        }
    }
}
