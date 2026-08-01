package com.yh.bigdata.tts.spider.strategy.tools.maalignlift;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MaAlignLiftStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class MaAlignLiftEvaluator {

    private MaAlignLiftEvaluator() {
    }

    public static MaAlignLiftEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                 MaAlignLiftStrategyParams params) {
        MaAlignLiftStrategyParams p = params != null ? params : MaAlignLiftStrategyParams.defaults();
        PeriodTypeEnum period = MaAlignLiftTools.resolvePeriod(p.getTier());

        MaAlignLiftTools.Hit hit = MaAlignLiftTools.findHit(stock, p);
        if (hit == null) {
            if (checkResult != null) {
                checkResult.addTrendPeriod(period,
                        "[MAL]未满足MA10>MA20>MA30/末K>均线MAX/边沿或开盘突破均线MAX");
            }
            return MaAlignLiftEvaluation.miss();
        }

        if (!MaAlignLiftTools.passesOptionalGates(stock, checkResult, p)) {
            return MaAlignLiftEvaluation.miss();
        }

        if (checkResult != null) {
            checkResult.addTrendPeriod(period, MaAlignLiftTools.buildTrendMessage(hit));
            checkResult.addSignal(period, MaAlignLiftTools.buildSignalMessage(hit));
        }
        return MaAlignLiftEvaluation.hit(period);
    }

    @Getter
    public static final class MaAlignLiftEvaluation {
        private final boolean hit;
        private final PeriodTypeEnum period;

        MaAlignLiftEvaluation(boolean hit, PeriodTypeEnum period) {
            this.hit = hit;
            this.period = period;
        }

        static MaAlignLiftEvaluation miss() {
            return new MaAlignLiftEvaluation(false, null);
        }

        static MaAlignLiftEvaluation hit(PeriodTypeEnum period) {
            return new MaAlignLiftEvaluation(true, period);
        }
    }
}
