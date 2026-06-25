package com.yh.bigdata.tts.spider.strategy.tools.frictionless;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.FrictionlessLadderStrategyParams;
import com.yh.bigdata.tts.common.param.LongStrategyParams;
import com.yh.bigdata.tts.common.param.MediumStrategyParams;
import com.yh.bigdata.tts.common.param.TrendV2StrategyParams;
import com.yh.bigdata.tts.common.param.UltraShortStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.longterm.LongEvaluator;
import com.yh.bigdata.tts.spider.strategy.tools.longterm.LongFilterTools;
import com.yh.bigdata.tts.spider.strategy.tools.medium.MediumEvaluator;
import com.yh.bigdata.tts.spider.strategy.tools.medium.MediumFilterTools;
import com.yh.bigdata.tts.spider.strategy.tools.trend.TrendV2Evaluator;
import com.yh.bigdata.tts.spider.strategy.tools.trend.TrendV2FilterTools;
import lombok.Getter;

/**
 * 无阻力梯子 = 梯子策略（短/中/长任一档）+ 固定日/周/月 MACD 门
 */
public final class FrictionlessLadderEvaluator {

    private FrictionlessLadderEvaluator() {
    }

    public static Evaluation evaluate(StockBase stock, CheckResult checkResult,
                                      FrictionlessLadderStrategyParams nrfParams,
                                      UltraShortStrategyParams ultraParams,
                                      TrendV2StrategyParams trendParams,
                                      MediumStrategyParams mediumParams,
                                      LongStrategyParams longParams) {
        FrictionlessLadderStrategyParams.ActiveTier tier = nrfParams != null
                ? nrfParams.getActiveTier() : FrictionlessLadderStrategyParams.ActiveTier.SHORT;

        if (!StrategyGlobalGateTools.passGate(stock, checkResult)) {
            return Evaluation.miss(tier);
        }

        switch (tier) {
            case SHORT: {
                TrendV2StrategyParams p = stripMacd(trendParams);
                if (!TrendV2FilterTools.passFilters(stock, checkResult, p)) {
                    return Evaluation.miss(tier);
                }
                TrendV2Evaluator.TrendV2Evaluation eval =
                        TrendV2Evaluator.evaluate(stock, null, p, ultraParams);
                if (!eval.isHit()) {
                    return Evaluation.miss(tier);
                }
                return Evaluation.fromTrend(tier, eval);
            }
            case MEDIUM: {
                MediumStrategyParams p = stripMacd(mediumParams);
                if (!MediumFilterTools.passFilters(stock, checkResult, p)) {
                    return Evaluation.miss(tier);
                }
                MediumEvaluator.MediumEvaluation eval =
                        MediumEvaluator.evaluate(stock, null, p, ultraParams);
                if (!eval.isHit()) {
                    return Evaluation.miss(tier);
                }
                return Evaluation.fromMedium(tier, eval);
            }
            case LONG: {
                LongStrategyParams p = stripMacd(longParams);
                if (!LongFilterTools.passFilters(stock, checkResult, p)) {
                    return Evaluation.miss(tier);
                }
                LongEvaluator.LongEvaluation eval =
                        LongEvaluator.evaluate(stock, null, p, ultraParams);
                if (!eval.isHit()) {
                    return Evaluation.miss(tier);
                }
                return Evaluation.fromLong(tier, eval);
            }
            default:
                return Evaluation.miss(tier);
        }
    }

    private static TrendV2StrategyParams stripMacd(TrendV2StrategyParams params) {
        TrendV2StrategyParams p = TrendV2StrategyParams.merge(params);
        p.setRequireMonthMacd(false);
        p.setRequireWeekMacd(false);
        p.setRequireDayMacd(false);
        p.setRequireWeekGoldenCross(false);
        p.setRequireUltra(true);
        return p;
    }

    private static MediumStrategyParams stripMacd(MediumStrategyParams params) {
        MediumStrategyParams p = MediumStrategyParams.merge(params);
        p.setRequireMonthMacd(false);
        p.setRequireYearMacd(false);
        p.setRequireMonthGoldenCross(false);
        p.setRequireUltra(true);
        return p;
    }

    private static LongStrategyParams stripMacd(LongStrategyParams params) {
        LongStrategyParams p = LongStrategyParams.merge(params);
        p.setRequireYearMacd(false);
        p.setRequireMonthMacd(false);
        p.setRequireYearGoldenCross(false);
        p.setRequireUltra(true);
        return p;
    }

    @Getter
    public static final class Evaluation {
        private final FrictionlessLadderStrategyParams.ActiveTier activeTier;
        private final boolean hit;
        private final int score;
        private final TrendV2Evaluator.TrendV2Evaluation trendEval;
        private final MediumEvaluator.MediumEvaluation mediumEval;
        private final LongEvaluator.LongEvaluation longEval;

        private Evaluation(FrictionlessLadderStrategyParams.ActiveTier activeTier, boolean hit, int score,
                           TrendV2Evaluator.TrendV2Evaluation trendEval,
                           MediumEvaluator.MediumEvaluation mediumEval,
                           LongEvaluator.LongEvaluation longEval) {
            this.activeTier = activeTier;
            this.hit = hit;
            this.score = score;
            this.trendEval = trendEval;
            this.mediumEval = mediumEval;
            this.longEval = longEval;
        }

        static Evaluation miss(FrictionlessLadderStrategyParams.ActiveTier tier) {
            return new Evaluation(tier, false, 0, null, null, null);
        }

        static Evaluation fromTrend(FrictionlessLadderStrategyParams.ActiveTier tier,
                                  TrendV2Evaluator.TrendV2Evaluation eval) {
            return new Evaluation(tier, true, eval.getScore(), eval, null, null);
        }

        static Evaluation fromMedium(FrictionlessLadderStrategyParams.ActiveTier tier,
                                     MediumEvaluator.MediumEvaluation eval) {
            return new Evaluation(tier, true, eval.getScore(), null, eval, null);
        }

        static Evaluation fromLong(FrictionlessLadderStrategyParams.ActiveTier tier,
                                   LongEvaluator.LongEvaluation eval) {
            return new Evaluation(tier, true, eval.getScore(), null, null, eval);
        }
    }
}
