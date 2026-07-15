package com.yh.bigdata.tts.spider.strategy.tools.macedge;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MacdEdgeStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class MacdEdgeEvaluator {

    private MacdEdgeEvaluator() {
    }

    public static MacdEdgeEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                              MacdEdgeStrategyParams params) {
        MacdEdgeStrategyParams p = params != null ? params : MacdEdgeStrategyParams.defaults();

        if (!MacdEdgeOptionalGateTools.passGate(stock, checkResult, p)) {
            return MacdEdgeEvaluation.miss();
        }
        if (!MacdEdgeFilterTools.passFilters(stock, checkResult, p)) {
            return MacdEdgeEvaluation.miss();
        }

        MacdEdgeBreakoutTools.TierHit min30Hit = null;
        MacdEdgeBreakoutTools.TierHit dayHit = null;
        MacdEdgeBreakoutTools.TierHit weekHit = null;
        MacdEdgeBreakoutTools.TierHit monthHit = null;
        MacdEdgeBreakoutTools.TierHit yearHit = null;

        if (p.isEnableMin30()) {
            min30Hit = MacdEdgeBreakoutTools.findTierHit(
                    stock, PeriodTypeEnum.MIN30, p.getLookbackMin30());
        }
        if (p.isEnableDay()) {
            dayHit = MacdEdgeBreakoutTools.findTierHit(
                    stock, PeriodTypeEnum.DAY, p.getLookbackDay());
        }
        if (p.isEnableWeek()) {
            weekHit = MacdEdgeBreakoutTools.findTierHit(
                    stock, PeriodTypeEnum.WEEK, p.getLookbackWeek());
        }
        if (p.isEnableMonth()) {
            monthHit = MacdEdgeBreakoutTools.findTierHit(
                    stock, PeriodTypeEnum.MONTH, p.getLookbackMonth());
        }
        if (p.isEnableYear()) {
            yearHit = MacdEdgeBreakoutTools.findTierHit(
                    stock, PeriodTypeEnum.YEAR, p.getLookbackYear());
        }
        if (min30Hit == null && dayHit == null && weekHit == null && monthHit == null && yearHit == null) {
            return MacdEdgeEvaluation.miss();
        }

        MacdEdgeEvaluation eval = new MacdEdgeEvaluation(min30Hit, dayHit, weekHit, monthHit, yearHit, true);
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH,
                    MacdEdgeScoreCalculator.buildTrendMessage(eval, p));
            appendTierSignals(checkResult, eval);
            eval.setScore(MacdEdgeScoreCalculator.computeScore(eval));
        }
        return eval;
    }

    private static void appendTierSignals(CheckResult checkResult, MacdEdgeEvaluation eval) {
        appendIfHit(checkResult, PeriodTypeEnum.YEAR, eval.getYearHit());
        appendIfHit(checkResult, PeriodTypeEnum.MONTH, eval.getMonthHit());
        appendIfHit(checkResult, PeriodTypeEnum.WEEK, eval.getWeekHit());
        appendIfHit(checkResult, PeriodTypeEnum.DAY, eval.getDayHit());
        appendIfHit(checkResult, PeriodTypeEnum.MIN30, eval.getMin30Hit());
    }

    private static void appendIfHit(CheckResult checkResult, PeriodTypeEnum period,
                                  MacdEdgeBreakoutTools.TierHit hit) {
        if (hit != null) {
            checkResult.addSignal(period, MacdEdgeScoreCalculator.buildSignalMessage(hit));
        }
    }

    @Getter
    public static final class MacdEdgeEvaluation {
        private final MacdEdgeBreakoutTools.TierHit min30Hit;
        private final MacdEdgeBreakoutTools.TierHit dayHit;
        private final MacdEdgeBreakoutTools.TierHit weekHit;
        private final MacdEdgeBreakoutTools.TierHit monthHit;
        private final MacdEdgeBreakoutTools.TierHit yearHit;
        private final boolean hit;
        private int score;

        MacdEdgeEvaluation(MacdEdgeBreakoutTools.TierHit min30Hit,
                           MacdEdgeBreakoutTools.TierHit dayHit,
                           MacdEdgeBreakoutTools.TierHit weekHit,
                           MacdEdgeBreakoutTools.TierHit monthHit,
                           MacdEdgeBreakoutTools.TierHit yearHit,
                           boolean hit) {
            this.min30Hit = min30Hit;
            this.dayHit = dayHit;
            this.weekHit = weekHit;
            this.monthHit = monthHit;
            this.yearHit = yearHit;
            this.hit = hit;
        }

        static MacdEdgeEvaluation miss() {
            return new MacdEdgeEvaluation(null, null, null, null, null, false);
        }

        void setScore(int score) {
            this.score = score;
        }

        public boolean isHit() {
            return hit;
        }
    }
}
