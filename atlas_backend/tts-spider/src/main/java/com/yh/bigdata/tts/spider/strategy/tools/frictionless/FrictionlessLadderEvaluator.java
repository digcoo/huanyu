package com.yh.bigdata.tts.spider.strategy.tools.frictionless;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.FrictionlessLadderStrategyParams;
import com.yh.bigdata.tts.common.param.LongStrategyParams;
import com.yh.bigdata.tts.common.param.MediumStrategyParams;
import com.yh.bigdata.tts.common.param.TrendV2StrategyParams;
import com.yh.bigdata.tts.common.param.UltraShortStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.UltraShortGateTools;
import lombok.Getter;

/**
 * 跨周期内梯子上移（nrf）· 全局三门 + 单档跨周期柱内突破
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

        TierContext ctx = resolveTierContext(tier, trendParams, mediumParams, longParams);
        if (!MinAvgAmountFilterTools.passWithMessage(stock, checkResult, ctx.getMinAvgAmount())) {
            return Evaluation.miss(tier);
        }

        CrossPeriodInBarBreakoutTools.Hit hit = CrossPeriodInBarBreakoutTools.findHit(stock, ctx.getSpec());
        if (hit == null) {
            return Evaluation.miss(tier);
        }

        UltraShortGateTools.GateResult ultraGate =
                UltraShortGateTools.evaluate(stock, ctx.isRequireUltra(), ultraParams);
        if (!ultraGate.isPassed()) {
            return Evaluation.miss(tier);
        }

        return Evaluation.hit(tier, hit, ultraGate);
    }

    /** 标记 API · 仅计算基准/突破 K，不重复写门控结果 */
    public static CrossPeriodInBarBreakoutTools.Hit findBreakoutHit(StockBase stock,
                                                                    FrictionlessLadderStrategyParams nrfParams,
                                                                    TrendV2StrategyParams trendParams,
                                                                    MediumStrategyParams mediumParams,
                                                                    LongStrategyParams longParams) {
        FrictionlessLadderStrategyParams.ActiveTier tier = nrfParams != null
                ? nrfParams.getActiveTier() : FrictionlessLadderStrategyParams.ActiveTier.SHORT;
        TierContext ctx = resolveTierContext(tier, trendParams, mediumParams, longParams);
        return CrossPeriodInBarBreakoutTools.findHit(stock, ctx.getSpec());
    }

    private static TierContext resolveTierContext(FrictionlessLadderStrategyParams.ActiveTier tier,
                                                  TrendV2StrategyParams trendParams,
                                                  MediumStrategyParams mediumParams,
                                                  LongStrategyParams longParams) {
        switch (tier) {
            case MEDIUM: {
                MediumStrategyParams p = MediumStrategyParams.merge(mediumParams);
                return new TierContext(
                        CrossPeriodInBarBreakoutTools.weekTier(
                                p.getPrevMonths(), p.getMaxWeeksPerMonth(),
                                p.getMinStrongPct(), p.isRequireCurrentBreakout()),
                        p.getMinAvgAmount(),
                        p.isRequireUltra());
            }
            case LONG: {
                LongStrategyParams p = LongStrategyParams.merge(longParams);
                return new TierContext(
                        CrossPeriodInBarBreakoutTools.monthTier(
                                p.getPrevYears(), p.getMaxMonthsPerYear(),
                                p.getMinStrongPct(), p.isRequireCurrentBreakout()),
                        p.getMinAvgAmount(),
                        p.isRequireUltra());
            }
            case SHORT:
            default: {
                TrendV2StrategyParams p = TrendV2StrategyParams.merge(trendParams);
                return new TierContext(
                        CrossPeriodInBarBreakoutTools.dayTier(
                                p.getPrevWeeks(), p.getMaxDaysPerWeek(),
                                p.getMinStrongPct(), p.isRequireCurrentBreakout()),
                        p.getMinAvgAmount(),
                        p.isRequireUltra());
            }
        }
    }

    @Getter
    private static final class TierContext {
        private final CrossPeriodInBarBreakoutTools.TierSpec spec;
        private final double minAvgAmount;
        private final boolean requireUltra;

        TierContext(CrossPeriodInBarBreakoutTools.TierSpec spec, double minAvgAmount, boolean requireUltra) {
            this.spec = spec;
            this.minAvgAmount = minAvgAmount;
            this.requireUltra = requireUltra;
        }
    }

    @Getter
    public static final class Evaluation {
        private final FrictionlessLadderStrategyParams.ActiveTier activeTier;
        private final boolean hit;
        private final CrossPeriodInBarBreakoutTools.Hit periodHit;
        private final UltraShortGateTools.GateResult ultraGate;

        private Evaluation(FrictionlessLadderStrategyParams.ActiveTier activeTier, boolean hit,
                           CrossPeriodInBarBreakoutTools.Hit periodHit,
                           UltraShortGateTools.GateResult ultraGate) {
            this.activeTier = activeTier;
            this.hit = hit;
            this.periodHit = periodHit;
            this.ultraGate = ultraGate;
        }

        static Evaluation miss(FrictionlessLadderStrategyParams.ActiveTier tier) {
            return new Evaluation(tier, false, null, null);
        }

        static Evaluation hit(FrictionlessLadderStrategyParams.ActiveTier tier,
                              CrossPeriodInBarBreakoutTools.Hit periodHit,
                              UltraShortGateTools.GateResult ultraGate) {
            return new Evaluation(tier, true, periodHit, ultraGate);
        }

        public boolean isRequireUltra() {
            return ultraGate != null && ultraGate.isRequired();
        }

        public int getScore() {
            if (!hit || periodHit == null) {
                return 0;
            }
            int score = 35;
            if (activeTier == FrictionlessLadderStrategyParams.ActiveTier.MEDIUM) {
                score += 10;
            } else if (activeTier == FrictionlessLadderStrategyParams.ActiveTier.LONG) {
                score += 20;
            }
            return score;
        }
    }
}
