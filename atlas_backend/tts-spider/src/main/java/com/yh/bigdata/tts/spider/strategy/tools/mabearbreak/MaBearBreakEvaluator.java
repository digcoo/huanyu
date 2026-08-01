package com.yh.bigdata.tts.spider.strategy.tools.mabearbreak;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MaBearBreakStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class MaBearBreakEvaluator {

    private MaBearBreakEvaluator() {
    }

    public static MaBearBreakEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                 MaBearBreakStrategyParams params) {
        MaBearBreakStrategyParams p = params != null ? params : MaBearBreakStrategyParams.defaults();
        PeriodTypeEnum period = MaBearBreakTools.resolvePeriod(p.getTier());

        MaBearBreakTools.Hit hit = MaBearBreakTools.findHit(stock, p);
        if (hit == null) {
            if (checkResult != null) {
                checkResult.addTrendPeriod(period,
                        "[MBR]未满足MA10<MA20<MA30/末K>均线MAX/边沿或开盘突破均线MAX");
            }
            return MaBearBreakEvaluation.miss();
        }

        if (!MaBearBreakTools.passesOptionalGates(stock, checkResult, p)) {
            return MaBearBreakEvaluation.miss();
        }

        if (checkResult != null) {
            checkResult.addTrendPeriod(period, MaBearBreakTools.buildTrendMessage(hit));
            checkResult.addSignal(period, MaBearBreakTools.buildSignalMessage(hit));
        }
        return MaBearBreakEvaluation.hit(period);
    }

    @Getter
    public static final class MaBearBreakEvaluation {
        private final boolean hit;
        private final PeriodTypeEnum period;

        MaBearBreakEvaluation(boolean hit, PeriodTypeEnum period) {
            this.hit = hit;
            this.period = period;
        }

        static MaBearBreakEvaluation miss() {
            return new MaBearBreakEvaluation(false, null);
        }

        static MaBearBreakEvaluation hit(PeriodTypeEnum period) {
            return new MaBearBreakEvaluation(true, period);
        }
    }
}
