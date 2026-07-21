package com.yh.bigdata.tts.spider.strategy.tools.daymonth;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.DayMonthComboStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;

public final class DayMonthComboEvaluator {

    private DayMonthComboEvaluator() {
    }

    public static DayMonthComboEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                   DayMonthComboStrategyParams params) {
        DayMonthComboStrategyParams p = params != null ? params : DayMonthComboStrategyParams.defaults();
        DayMonthComboTools.Hit hit = DayMonthComboTools.findHit(stock, p);
        if (hit == null) {
            return DayMonthComboEvaluation.miss();
        }
        if (!DayMonthComboTools.passesOptionalGates(
                stock, checkResult, p, hit.getSignalBar(), hit.getPrevBar())) {
            return DayMonthComboEvaluation.miss();
        }
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH, DayMonthComboTools.buildTrendMessage(hit));
            checkResult.addSignal(PeriodTypeEnum.DAY, DayMonthComboTools.buildSignalMessage(hit));
        }
        return DayMonthComboEvaluation.hit();
    }

    public static final class DayMonthComboEvaluation {
        private final boolean hit;

        DayMonthComboEvaluation(boolean hit) {
            this.hit = hit;
        }

        static DayMonthComboEvaluation miss() {
            return new DayMonthComboEvaluation(false);
        }

        static DayMonthComboEvaluation hit() {
            return new DayMonthComboEvaluation(true);
        }

        public boolean isHit() {
            return hit;
        }
    }
}
