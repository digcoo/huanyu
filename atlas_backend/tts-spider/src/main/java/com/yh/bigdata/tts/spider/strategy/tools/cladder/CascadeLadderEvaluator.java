package com.yh.bigdata.tts.spider.strategy.tools.cladder;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.CascadeLadderStrategyParams;
import com.yh.bigdata.tts.common.param.UltraShortStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.UltraShortGateTools;
import lombok.Getter;

public final class CascadeLadderEvaluator {

    private CascadeLadderEvaluator() {
    }

    public static CascadeLadderEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                   CascadeLadderStrategyParams params,
                                                   UltraShortStrategyParams ultraParams) {
        CascadeLadderStrategyParams p = params != null ? params : CascadeLadderStrategyParams.defaults();

        if (!CascadeLadderOptionalGateTools.passGate(stock, checkResult, p)) {
            return CascadeLadderEvaluation.miss();
        }
        if (!CascadeLadderFilterTools.passFilters(stock, checkResult, p)) {
            return CascadeLadderEvaluation.miss();
        }

        CascadeLadderBreakoutTools.PeriodHit dayHit =
                CascadeLadderBreakoutTools.findDayHit(stock, p.getLookbackDay());
        CascadeLadderBreakoutTools.PeriodHit weekHit =
                CascadeLadderBreakoutTools.findWeekHit(stock, p.getLookbackWeek());
        CascadeLadderBreakoutTools.PeriodHit monthHit =
                CascadeLadderBreakoutTools.findMonthHit(stock, p.getLookbackMonth());

        if (dayHit == null || weekHit == null || monthHit == null) {
            return CascadeLadderEvaluation.miss();
        }

        UltraShortGateTools.GateResult ultraGate =
                UltraShortGateTools.evaluate(stock, p.isRequireUltra(), ultraParams);
        if (!ultraGate.isPassed()) {
            return CascadeLadderEvaluation.miss();
        }

        CascadeLadderEvaluation eval = new CascadeLadderEvaluation(dayHit, weekHit, monthHit, true);
        if (checkResult != null) {
            if (ultraGate.isRequired()) {
                UltraShortGateTools.appendMessages(checkResult, ultraGate);
            }
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH,
                    CascadeLadderScoreCalculator.buildTrendMessage(p));
            checkResult.addSignal(PeriodTypeEnum.DAY,
                    CascadeLadderScoreCalculator.buildSignalMessage(dayHit));
            checkResult.addSignal(PeriodTypeEnum.WEEK,
                    CascadeLadderScoreCalculator.buildSignalMessage(weekHit));
            checkResult.addSignal(PeriodTypeEnum.MONTH,
                    CascadeLadderScoreCalculator.buildSignalMessage(monthHit));
            eval.setScore(CascadeLadderScoreCalculator.computeScore());
        }
        return eval;
    }

    @Getter
    public static final class CascadeLadderEvaluation {
        private final CascadeLadderBreakoutTools.PeriodHit dayHit;
        private final CascadeLadderBreakoutTools.PeriodHit weekHit;
        private final CascadeLadderBreakoutTools.PeriodHit monthHit;
        private final boolean hit;
        private int score;

        CascadeLadderEvaluation(CascadeLadderBreakoutTools.PeriodHit dayHit,
                                CascadeLadderBreakoutTools.PeriodHit weekHit,
                                CascadeLadderBreakoutTools.PeriodHit monthHit,
                                boolean hit) {
            this.dayHit = dayHit;
            this.weekHit = weekHit;
            this.monthHit = monthHit;
            this.hit = hit;
        }

        static CascadeLadderEvaluation miss() {
            return new CascadeLadderEvaluation(null, null, null, false);
        }

        void setScore(int score) {
            this.score = score;
        }

        public boolean isHit() {
            return hit;
        }
    }
}
