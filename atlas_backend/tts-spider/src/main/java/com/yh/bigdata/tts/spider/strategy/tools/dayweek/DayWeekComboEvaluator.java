package com.yh.bigdata.tts.spider.strategy.tools.dayweek;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.DayWeekComboStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;

public final class DayWeekComboEvaluator {

    private DayWeekComboEvaluator() {
    }

    public static DayWeekComboEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                  DayWeekComboStrategyParams params) {
        DayWeekComboStrategyParams p = params != null ? params : DayWeekComboStrategyParams.defaults();
        DayWeekComboTools.Hit hit = DayWeekComboTools.findHit(stock, p);
        if (hit == null) {
            return DayWeekComboEvaluation.miss();
        }
        if (!DayWeekComboTools.passesOptionalGates(
                stock, checkResult, p, hit.getSignalBar(), hit.getPrevBar())) {
            return DayWeekComboEvaluation.miss();
        }
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.WEEK, DayWeekComboTools.buildTrendMessage(hit));
            checkResult.addSignal(PeriodTypeEnum.DAY, DayWeekComboTools.buildSignalMessage(hit));
        }
        return DayWeekComboEvaluation.hit();
    }

    public static final class DayWeekComboEvaluation {
        private final boolean hit;

        DayWeekComboEvaluation(boolean hit) {
            this.hit = hit;
        }

        static DayWeekComboEvaluation miss() {
            return new DayWeekComboEvaluation(false);
        }

        static DayWeekComboEvaluation hit() {
            return new DayWeekComboEvaluation(true);
        }

        public boolean isHit() {
            return hit;
        }
    }
}
