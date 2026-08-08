package com.yh.bigdata.tts.spider.strategy.tools.mabull3m;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MaBull3mStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class MaBull3mEvaluator {

    private MaBull3mEvaluator() {
    }

    public static MaBull3mEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                              MaBull3mStrategyParams params) {
        MaBull3mStrategyParams p = params != null ? params : MaBull3mStrategyParams.defaults();
        PeriodTypeEnum period = MaBull3mTools.resolvePeriod(p.getTier());

        MaBull3mTools.Hit hit = MaBull3mTools.findHit(stock, p);
        if (hit == null) {
            if (checkResult != null) {
                checkResult.addTrendPeriod(period, "[M3M]未满足3M1/3M2多头+边沿破金叉/死叉/关键K High");
            }
            return MaBull3mEvaluation.miss();
        }

        if (!MaBull3mTools.passesOptionalGates(stock, checkResult, p)) {
            return MaBull3mEvaluation.miss();
        }

        if (checkResult != null) {
            checkResult.addTrendPeriod(period, MaBull3mTools.buildTrendMessage(hit));
            checkResult.addSignal(period, MaBull3mTools.buildSignalMessage(hit));
        }
        return MaBull3mEvaluation.hit(period);
    }

    @Getter
    public static final class MaBull3mEvaluation {
        private final boolean hit;
        private final PeriodTypeEnum period;

        MaBull3mEvaluation(boolean hit, PeriodTypeEnum period) {
            this.hit = hit;
            this.period = period;
        }

        static MaBull3mEvaluation miss() {
            return new MaBull3mEvaluation(false, null);
        }

        static MaBull3mEvaluation hit(PeriodTypeEnum period) {
            return new MaBull3mEvaluation(true, period);
        }
    }
}
