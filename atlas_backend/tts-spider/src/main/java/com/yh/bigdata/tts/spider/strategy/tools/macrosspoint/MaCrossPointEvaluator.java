package com.yh.bigdata.tts.spider.strategy.tools.macrosspoint;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MaCrossPointStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class MaCrossPointEvaluator {

    private MaCrossPointEvaluator() {
    }

    public static MaCrossPointEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                  MaCrossPointStrategyParams params,
                                                  MaCrossPointCore.CrossKind kind) {
        MaCrossPointStrategyParams p = params != null ? params : MaCrossPointStrategyParams.defaults();
        PeriodTypeEnum period = MaCrossPointTools.resolvePeriod(p.getTier());
        boolean death = kind == MaCrossPointCore.CrossKind.DEATH;
        String tag = death ? "MDB" : "MGB";
        String miss = death ? "未满足边沿破死叉交叉点" : "未满足边沿破金叉波段顶或MA5>MA10";

        MaCrossPointTools.Hit hit = MaCrossPointTools.findHit(stock, p, kind);
        if (hit == null) {
            if (checkResult != null) {
                checkResult.addTrendPeriod(period, "[" + tag + "]" + miss);
            }
            return MaCrossPointEvaluation.miss();
        }

        if (!MaCrossPointTools.passesOptionalGates(stock, checkResult, p, hit)) {
            return MaCrossPointEvaluation.miss();
        }

        if (checkResult != null) {
            checkResult.addTrendPeriod(period, MaCrossPointTools.buildTrendMessage(hit));
            checkResult.addSignal(period, MaCrossPointTools.buildSignalMessage(hit));
        }
        return MaCrossPointEvaluation.hit(period);
    }

    @Getter
    public static final class MaCrossPointEvaluation {
        private final boolean hit;
        private final PeriodTypeEnum period;

        MaCrossPointEvaluation(boolean hit, PeriodTypeEnum period) {
            this.hit = hit;
            this.period = period;
        }

        static MaCrossPointEvaluation miss() {
            return new MaCrossPointEvaluation(false, null);
        }

        static MaCrossPointEvaluation hit(PeriodTypeEnum period) {
            return new MaCrossPointEvaluation(true, period);
        }
    }
}
