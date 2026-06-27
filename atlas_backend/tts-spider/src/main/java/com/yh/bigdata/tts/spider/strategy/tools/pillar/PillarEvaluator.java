package com.yh.bigdata.tts.spider.strategy.tools.pillar;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.PillarStrategyParams;
import com.yh.bigdata.tts.common.param.UltraShortStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.StrategyGlobalGateTools;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.UltraShortGateTools;
import lombok.Getter;

/**
 * 柱子内上移 · 全局门控 + 强柱基准突破 + 可选 min30 / 成交额
 */
public final class PillarEvaluator {

    private PillarEvaluator() {
    }

    public static PillarEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                              PillarStrategyParams params,
                                              UltraShortStrategyParams ultraParams) {
        PillarStrategyParams p = params != null ? params : PillarStrategyParams.defaults();

        if (!StrategyGlobalGateTools.passGate(stock, checkResult)) {
            return PillarEvaluation.miss();
        }
        if (!PillarFilterTools.passFilters(stock, checkResult, p)) {
            return PillarEvaluation.miss();
        }

        PillarBreakoutTools.PeriodHit dayHit = p.isEnableDay()
                ? PillarBreakoutTools.findHit(stock, PeriodTypeEnum.DAY, p.getLookbackDay()) : null;
        PillarBreakoutTools.PeriodHit weekHit = p.isEnableWeek()
                ? PillarBreakoutTools.findHit(stock, PeriodTypeEnum.WEEK, p.getLookbackWeek()) : null;
        PillarBreakoutTools.PeriodHit monthHit = p.isEnableMonth()
                ? PillarBreakoutTools.findHit(stock, PeriodTypeEnum.MONTH, p.getLookbackMonth()) : null;

        boolean periodHit = dayHit != null || weekHit != null || monthHit != null;
        if (!periodHit) {
            return PillarEvaluation.miss();
        }
        UltraShortGateTools.GateResult ultraGate =
                UltraShortGateTools.evaluate(stock, p.isRequireUltra(), ultraParams);
        if (!ultraGate.isPassed()) {
            return PillarEvaluation.miss();
        }

        PillarEvaluation eval = new PillarEvaluation(dayHit, weekHit, monthHit, true);
        if (checkResult != null) {
            if (ultraGate.isRequired()) {
                UltraShortGateTools.appendMessages(checkResult, ultraGate);
            }
            PeriodTypeEnum signalPeriod = PillarScoreCalculator.primaryPeriod(eval);
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH, PillarScoreCalculator.buildTrendMessage(eval));
            checkResult.addSignal(signalPeriod, PillarScoreCalculator.buildSignalMessage(eval));
            eval.setScore(PillarScoreCalculator.computeScore(eval));
        }
        return eval;
    }

    @Getter
    public static final class PillarEvaluation {
        private final PillarBreakoutTools.PeriodHit dayHit;
        private final PillarBreakoutTools.PeriodHit weekHit;
        private final PillarBreakoutTools.PeriodHit monthHit;
        private final boolean hit;
        private int score;

        PillarEvaluation(PillarBreakoutTools.PeriodHit dayHit, PillarBreakoutTools.PeriodHit weekHit,
                         PillarBreakoutTools.PeriodHit monthHit, boolean hit) {
            this.dayHit = dayHit;
            this.weekHit = weekHit;
            this.monthHit = monthHit;
            this.hit = hit;
        }

        static PillarEvaluation miss() {
            return new PillarEvaluation(null, null, null, false);
        }

        void setScore(int score) {
            this.score = score;
        }

        public boolean isHit() {
            return hit;
        }
    }
}
