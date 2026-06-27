package com.yh.bigdata.tts.spider.strategy.tools.cascade;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.CascadeStrategyParams;
import com.yh.bigdata.tts.common.param.UltraShortStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.UltraShortGateTools;
import lombok.Getter;

/**
 * 级联交叉突破 · 可选三门 + 日 K 边沿 + 级联确认 + 可选 min30 / 成交额
 */
public final class CascadeEvaluator {

    private CascadeEvaluator() {
    }

    public static CascadeEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                             CascadeStrategyParams params,
                                             UltraShortStrategyParams ultraParams) {
        CascadeStrategyParams p = params != null ? params : CascadeStrategyParams.defaults();

        if (!CascadeOptionalGateTools.passGate(stock, checkResult, p)) {
            return CascadeEvaluation.miss();
        }
        if (!CascadeFilterTools.passFilters(stock, checkResult, p)) {
            return CascadeEvaluation.miss();
        }

        CascadeBreakoutTools.TierHit dayHit = null;
        CascadeBreakoutTools.TierHit weekHit = null;
        CascadeBreakoutTools.TierHit monthHit = null;

        if (p.isEnableDay()) {
            dayHit = CascadeBreakoutTools.findDayTierHit(stock, p.getLookbackDay(), p.getLookbackWeek());
        }
        if (p.isEnableWeek()) {
            weekHit = CascadeBreakoutTools.findWeekTierHit(
                    stock, p.getLookbackWeek(), p.getLookbackMonth(), p.getLookbackDay());
        }
        if (p.isEnableMonth()) {
            monthHit = CascadeBreakoutTools.findMonthTierHit(stock, p.getLookbackMonth(), p.getLookbackDay());
        }
        if (dayHit == null && weekHit == null && monthHit == null) {
            return CascadeEvaluation.miss();
        }
        return finish(stock, checkResult, p, ultraParams, dayHit, weekHit, monthHit);
    }

    private static CascadeEvaluation finish(StockBase stock, CheckResult checkResult, CascadeStrategyParams p,
                                            UltraShortStrategyParams ultraParams,
                                            CascadeBreakoutTools.TierHit dayHit,
                                            CascadeBreakoutTools.TierHit weekHit,
                                            CascadeBreakoutTools.TierHit monthHit) {
        UltraShortGateTools.GateResult ultraGate =
                UltraShortGateTools.evaluate(stock, p.isRequireUltra(), ultraParams);
        if (!ultraGate.isPassed()) {
            return CascadeEvaluation.miss();
        }

        CascadeEvaluation eval = new CascadeEvaluation(dayHit, weekHit, monthHit, true);
        if (checkResult != null) {
            if (ultraGate.isRequired()) {
                UltraShortGateTools.appendMessages(checkResult, ultraGate);
            }
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH, CascadeScoreCalculator.buildTrendMessage(eval, p));
            appendTierSignals(checkResult, eval);
            eval.setScore(CascadeScoreCalculator.computeScore(eval));
        }
        return eval;
    }

    private static void appendTierSignals(CheckResult checkResult, CascadeEvaluation eval) {
        if (eval.getDayHit() != null) {
            checkResult.addSignal(PeriodTypeEnum.DAY,
                    CascadeScoreCalculator.buildSignalMessage(eval.getDayHit()));
        }
        if (eval.getWeekHit() != null) {
            checkResult.addSignal(PeriodTypeEnum.WEEK,
                    CascadeScoreCalculator.buildSignalMessage(eval.getWeekHit()));
        }
        if (eval.getMonthHit() != null) {
            checkResult.addSignal(PeriodTypeEnum.MONTH,
                    CascadeScoreCalculator.buildSignalMessage(eval.getMonthHit()));
        }
    }

    @Getter
    public static final class CascadeEvaluation {
        private final CascadeBreakoutTools.TierHit dayHit;
        private final CascadeBreakoutTools.TierHit weekHit;
        private final CascadeBreakoutTools.TierHit monthHit;
        private final boolean hit;
        private int score;

        CascadeEvaluation(CascadeBreakoutTools.TierHit dayHit, CascadeBreakoutTools.TierHit weekHit,
                          CascadeBreakoutTools.TierHit monthHit, boolean hit) {
            this.dayHit = dayHit;
            this.weekHit = weekHit;
            this.monthHit = monthHit;
            this.hit = hit;
        }

        static CascadeEvaluation miss() {
            return new CascadeEvaluation(null, null, null, false);
        }

        void setScore(int score) {
            this.score = score;
        }

        public boolean isHit() {
            return hit;
        }
    }
}
