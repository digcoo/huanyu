package com.yh.bigdata.tts.spider.strategy.tools.mabreakma;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MaBreakMaStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class MaBreakMaEvaluator {

    private MaBreakMaEvaluator() {
    }

    public static MaBreakMaEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                               MaBreakMaStrategyParams params) {
        MaBreakMaStrategyParams p = params != null ? params : MaBreakMaStrategyParams.defaults();
        PeriodTypeEnum period = MaBreakMaTools.resolvePeriod(p.getTier());

        MaBreakMaTools.Hit hit = MaBreakMaTools.findHit(stock, p);
        if (hit == null) {
            if (checkResult != null) {
                checkResult.addTrendPeriod(period, "[MBM]未满足3M1/3M2多头+边沿/开盘破MAX");
            }
            return MaBreakMaEvaluation.miss();
        }

        if (!MaBreakMaTools.passesOptionalGates(stock, checkResult, p)) {
            return MaBreakMaEvaluation.miss();
        }

        if (checkResult != null) {
            checkResult.addTrendPeriod(period, MaBreakMaTools.buildTrendMessage(hit));
            checkResult.addSignal(period, MaBreakMaTools.buildSignalMessage(hit));
        }
        return MaBreakMaEvaluation.hit(period);
    }

    @Getter
    public static final class MaBreakMaEvaluation {
        private final boolean hit;
        private final PeriodTypeEnum period;

        MaBreakMaEvaluation(boolean hit, PeriodTypeEnum period) {
            this.hit = hit;
            this.period = period;
        }

        static MaBreakMaEvaluation miss() {
            return new MaBreakMaEvaluation(false, null);
        }

        static MaBreakMaEvaluation hit(PeriodTypeEnum period) {
            return new MaBreakMaEvaluation(true, period);
        }
    }
}
