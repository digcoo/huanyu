package com.yh.bigdata.tts.spider.strategy.tools.daymin60;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.DayMin60ComboStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class DayMin60ComboEvaluator {

    private DayMin60ComboEvaluator() {
    }

    public static DayMin60ComboEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                   DayMin60ComboStrategyParams params) {
        DayMin60ComboStrategyParams p = params != null ? params : DayMin60ComboStrategyParams.defaults();
        DayMin60ComboTools.Hit hit = DayMin60ComboTools.findHit(stock, p);
        if (hit == null) {
            return DayMin60ComboEvaluation.miss();
        }
        if (!DayMin60ComboTools.passesOptionalGates(
                stock, checkResult, p, hit.getSignalBar(), hit.getPrevBar())) {
            return DayMin60ComboEvaluation.miss();
        }
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.DAY, DayMin60ComboTools.buildTrendMessage(hit));
            checkResult.addSignal(PeriodTypeEnum.MIN60, DayMin60ComboTools.buildSignalMessage(hit));
        }
        return DayMin60ComboEvaluation.hit();
    }

    @Getter
    public static final class DayMin60ComboEvaluation {
        private final boolean hit;

        DayMin60ComboEvaluation(boolean hit) {
            this.hit = hit;
        }

        static DayMin60ComboEvaluation miss() {
            return new DayMin60ComboEvaluation(false);
        }

        static DayMin60ComboEvaluation hit() {
            return new DayMin60ComboEvaluation(true);
        }
    }
}
