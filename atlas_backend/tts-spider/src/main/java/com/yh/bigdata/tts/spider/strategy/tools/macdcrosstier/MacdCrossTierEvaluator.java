package com.yh.bigdata.tts.spider.strategy.tools.macdcrosstier;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MacdCrossTierStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.macedge.MacdEdgeScoreCalculator;
import lombok.Getter;

public final class MacdCrossTierEvaluator {

    private MacdCrossTierEvaluator() {
    }

    public static MacdCrossTierEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                   MacdCrossTierStrategyParams params) {
        MacdCrossTierStrategyParams p = params != null ? params : MacdCrossTierStrategyParams.defaults();
        MacdCrossTierHitPath path = MacdCrossTierTools.resolveHitPath(stock, p);
        if (path == null) {
            return MacdCrossTierEvaluation.miss();
        }
        PeriodTypeEnum period = MacdCrossTierTools.resolvePeriod(p.getTier());
        if (!passesPrevHighGate(stock, checkResult, path)) {
            return MacdCrossTierEvaluation.miss();
        }
        if (!MacdCrossTierBandLowGateTools.passesDayTierWeekMonthGate(stock, checkResult, p)) {
            return MacdCrossTierEvaluation.miss();
        }
        if (!MacdCrossTierBandShapeGateTools.passesWeekMonthShapeGate(stock, checkResult, p)) {
            return MacdCrossTierEvaluation.miss();
        }
        if (checkResult != null) {
            String tierLabel = MacdCrossTierTools.buildTierLabel(p);
            String pathLabel = MacdCrossTierTools.buildPathLabel(path);
            checkResult.addTrendPeriod(period,
                    "[MCT]同档MACD交叉突破|" + tierLabel + "档|" + pathLabel);
            if (path.getTierHit() != null) {
                checkResult.addSignal(period, MacdEdgeScoreCalculator.buildSignalMessage(path.getTierHit()));
            } else {
                checkResult.addSignal(period, MacdCrossTierGoldenCrossRiseTools.buildSignalMessage(
                        path.getPeriod(), path.getSignalBar(), path.getPrevBar()));
            }
        }
        return MacdCrossTierEvaluation.hit(path, period);
    }

    private static boolean passesPrevHighGate(StockBase stock, CheckResult checkResult,
                                              MacdCrossTierHitPath path) {
        if (path.getTierHit() != null) {
            return MacdCrossTierPrevHighGateTools.passesTierCloseAbovePrevHigh(
                    stock, checkResult, path.getTierHit());
        }
        return MacdCrossTierPrevHighGateTools.passesCloseAbovePrevHigh(
                stock, checkResult, path.getPeriod(), path.getPrevBar(), path.getSignalBar());
    }

    @Getter
    public static final class MacdCrossTierEvaluation {
        private final MacdCrossTierHitPath path;
        private final PeriodTypeEnum period;
        private final boolean success;

        MacdCrossTierEvaluation(MacdCrossTierHitPath path, PeriodTypeEnum period, boolean success) {
            this.path = path;
            this.period = period;
            this.success = success;
        }

        static MacdCrossTierEvaluation miss() {
            return new MacdCrossTierEvaluation(null, null, false);
        }

        static MacdCrossTierEvaluation hit(MacdCrossTierHitPath path, PeriodTypeEnum period) {
            return new MacdCrossTierEvaluation(path, period, true);
        }

        public boolean isHit() {
            return success;
        }
    }
}
