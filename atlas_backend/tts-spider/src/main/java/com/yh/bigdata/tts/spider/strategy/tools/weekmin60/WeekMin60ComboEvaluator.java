package com.yh.bigdata.tts.spider.strategy.tools.weekmin60;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.WeekMin60ComboStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;

public final class WeekMin60ComboEvaluator {

    private WeekMin60ComboEvaluator() {
    }

    public static WeekMin60ComboEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                   WeekMin60ComboStrategyParams params) {
        WeekMin60ComboStrategyParams p = params != null ? params : WeekMin60ComboStrategyParams.defaults();
        WeekMin60ComboTools.Hit hit = WeekMin60ComboTools.findHit(stock, p);
        if (hit == null) {
            return WeekMin60ComboEvaluation.miss();
        }
        if (!WeekMin60ComboTools.passesOptionalGates(
                stock, checkResult, p, hit.getSignalBar(), hit.getPrevBar())) {
            return WeekMin60ComboEvaluation.miss();
        }
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.WEEK, WeekMin60ComboTools.buildTrendMessage(hit));
            checkResult.addSignal(PeriodTypeEnum.MIN60, WeekMin60ComboTools.buildSignalMessage(hit));
        }
        return WeekMin60ComboEvaluation.hit();
    }

    public static final class WeekMin60ComboEvaluation {
        private final boolean hit;

        WeekMin60ComboEvaluation(boolean hit) {
            this.hit = hit;
        }

        static WeekMin60ComboEvaluation miss() {
            return new WeekMin60ComboEvaluation(false);
        }

        static WeekMin60ComboEvaluation hit() {
            return new WeekMin60ComboEvaluation(true);
        }

        public boolean isHit() {
            return hit;
        }
    }
}
