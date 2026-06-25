package com.yh.bigdata.tts.spider.strategy.tools.trendm;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.TrendmStrategyParams;
import com.yh.bigdata.tts.common.param.UltraShortStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.bogo.BogoBreakoutTools;
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.StrategyGlobalGateTools;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.UltraShortGateTools;
import lombok.Getter;

/**
 * 趋势策略 · 无阻力三门 + 日/周/月同时基准突破 + min30 梯子
 */
public final class TrendmEvaluator {

    private TrendmEvaluator() {
    }

    public static TrendmEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                            TrendmStrategyParams params,
                                            UltraShortStrategyParams ultraParams) {
        TrendmStrategyParams p = params != null ? params : TrendmStrategyParams.defaults();
        UltraShortStrategyParams ultra = ultraParams != null ? ultraParams : UltraShortStrategyParams.defaults();

        if (!StrategyGlobalGateTools.passGate(stock, checkResult)) {
            return TrendmEvaluation.miss();
        }

        BogoBreakoutTools.PeriodHit dayHit =
                BogoBreakoutTools.findHit(stock, PeriodTypeEnum.DAY, p.getLookbackDay());
        BogoBreakoutTools.PeriodHit weekHit =
                BogoBreakoutTools.findHit(stock, PeriodTypeEnum.WEEK, p.getLookbackWeek());
        BogoBreakoutTools.PeriodHit monthHit =
                BogoBreakoutTools.findHit(stock, PeriodTypeEnum.MONTH, p.getLookbackMonth());

        if (dayHit == null || weekHit == null || monthHit == null) {
            annotateMiss(checkResult, dayHit, weekHit, monthHit);
            return TrendmEvaluation.miss();
        }

        if (!UltraShortGateTools.passes(stock, true, ultra)) {
            if (checkResult != null) {
                checkResult.addTrendPeriod(PeriodTypeEnum.MIN30, "min30梯子未过");
            }
            return TrendmEvaluation.miss();
        }

        TrendmEvaluation eval = new TrendmEvaluation(dayHit, weekHit, monthHit, true);
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH, TrendmScoreCalculator.buildTrendMessage(eval));
            checkResult.addSignal(PeriodTypeEnum.DAY, TrendmScoreCalculator.buildDaySignal(dayHit));
            checkResult.addSignal(PeriodTypeEnum.WEEK, TrendmScoreCalculator.buildWeekSignal(weekHit));
            checkResult.addSignal(PeriodTypeEnum.MONTH, TrendmScoreCalculator.buildMonthSignal(monthHit));
            eval.setScore(TrendmScoreCalculator.computeScore(eval));
        }
        return eval;
    }

    private static void annotateMiss(CheckResult checkResult,
                                     BogoBreakoutTools.PeriodHit dayHit,
                                     BogoBreakoutTools.PeriodHit weekHit,
                                     BogoBreakoutTools.PeriodHit monthHit) {
        if (checkResult == null) {
            return;
        }
        if (dayHit == null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.DAY, "日K基准突破未过");
        }
        if (weekHit == null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.WEEK, "周K基准突破未过");
        }
        if (monthHit == null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH, "月K基准突破未过");
        }
    }

    @Getter
    public static final class TrendmEvaluation {
        private final BogoBreakoutTools.PeriodHit dayHit;
        private final BogoBreakoutTools.PeriodHit weekHit;
        private final BogoBreakoutTools.PeriodHit monthHit;
        private final boolean hit;
        private int score;

        TrendmEvaluation(BogoBreakoutTools.PeriodHit dayHit, BogoBreakoutTools.PeriodHit weekHit,
                         BogoBreakoutTools.PeriodHit monthHit, boolean hit) {
            this.dayHit = dayHit;
            this.weekHit = weekHit;
            this.monthHit = monthHit;
            this.hit = hit;
        }

        static TrendmEvaluation miss() {
            return new TrendmEvaluation(null, null, null, false);
        }

        void setScore(int score) {
            this.score = score;
        }

        public boolean isHit() {
            return hit;
        }
    }
}
