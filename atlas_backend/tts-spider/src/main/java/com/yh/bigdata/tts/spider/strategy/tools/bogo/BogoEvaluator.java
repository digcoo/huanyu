package com.yh.bigdata.tts.spider.strategy.tools.bogo;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.BogoStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.StrategyGlobalGateTools;
import lombok.Getter;

/**
 * 底部机会 · 无阻力三门 + 金叉/死叉基准突破
 */
public final class BogoEvaluator {

    private BogoEvaluator() {
    }

    public static BogoEvaluation evaluate(StockBase stock, CheckResult checkResult, BogoStrategyParams params) {
        BogoStrategyParams p = params != null ? params : BogoStrategyParams.defaults();

        if (!StrategyGlobalGateTools.passGate(stock, checkResult)) {
            return BogoEvaluation.miss();
        }

        BogoBreakoutTools.PeriodHit dayHit = p.isEnableDay()
                ? BogoBreakoutTools.findHit(stock, PeriodTypeEnum.DAY, p.getLookbackDay()) : null;
        BogoBreakoutTools.PeriodHit weekHit = p.isEnableWeek()
                ? BogoBreakoutTools.findHit(stock, PeriodTypeEnum.WEEK, p.getLookbackWeek()) : null;
        BogoBreakoutTools.PeriodHit monthHit = p.isEnableMonth()
                ? BogoBreakoutTools.findHit(stock, PeriodTypeEnum.MONTH, p.getLookbackMonth()) : null;

        boolean hit = dayHit != null || weekHit != null || monthHit != null;
        BogoEvaluation eval = new BogoEvaluation(dayHit, weekHit, monthHit, hit);

        if (hit && checkResult != null) {
            PeriodTypeEnum signalPeriod = BogoScoreCalculator.primaryPeriod(eval);
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH, BogoScoreCalculator.buildTrendMessage(eval));
            checkResult.addSignal(signalPeriod, BogoScoreCalculator.buildSignalMessage(eval));
            eval.setScore(BogoScoreCalculator.computeScore(eval));
        }
        return eval;
    }

    @Getter
    public static final class BogoEvaluation {
        private final BogoBreakoutTools.PeriodHit dayHit;
        private final BogoBreakoutTools.PeriodHit weekHit;
        private final BogoBreakoutTools.PeriodHit monthHit;
        private final boolean hit;
        private int score;

        BogoEvaluation(BogoBreakoutTools.PeriodHit dayHit, BogoBreakoutTools.PeriodHit weekHit,
                       BogoBreakoutTools.PeriodHit monthHit, boolean hit) {
            this.dayHit = dayHit;
            this.weekHit = weekHit;
            this.monthHit = monthHit;
            this.hit = hit;
        }

        static BogoEvaluation miss() {
            return new BogoEvaluation(null, null, null, false);
        }

        void setScore(int score) {
            this.score = score;
        }

        public boolean isHit() {
            return hit;
        }
    }
}
