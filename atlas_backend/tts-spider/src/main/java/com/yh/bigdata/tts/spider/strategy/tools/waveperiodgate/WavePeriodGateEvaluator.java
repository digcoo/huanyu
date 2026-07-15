package com.yh.bigdata.tts.spider.strategy.tools.waveperiodgate;

import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.WavePeriodGateStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class WavePeriodGateEvaluator {

    private WavePeriodGateEvaluator() {
    }

    public static WavePeriodGateEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                    WavePeriodGateStrategyParams params) {
        WavePeriodGateStrategyParams p = params != null ? params : WavePeriodGateStrategyParams.defaults();
        if (!passesAllConditions(stock, checkResult, p)) {
            return WavePeriodGateEvaluation.miss();
        }
        return WavePeriodGateEvaluation.hit();
    }

    static boolean passesAllConditions(StockBase stock, CheckResult checkResult,
                                       WavePeriodGateStrategyParams params) {
        if (stock == null) {
            return false;
        }
        return WavePeriodGateTools.passesBreakoutGate(stock, checkResult, params)
                && WavePeriodGateTools.passesTierMacdPositiveGate(stock, checkResult, params)
                && WavePeriodGateBandLowGateTools.passesWeekMonthBandLowGate(stock, checkResult, params)
                && WavePeriodGateBandShapeGateTools.passesWeekMonthShapeGate(stock, checkResult, params)
                && WavePeriodGateYearWeekMonthYangGateTools.passesYearWeekMonthYangGate(stock, checkResult, params);
    }

    @Getter
    public static final class WavePeriodGateEvaluation {
        private final boolean hit;

        WavePeriodGateEvaluation(boolean hit) {
            this.hit = hit;
        }

        static WavePeriodGateEvaluation miss() {
            return new WavePeriodGateEvaluation(false);
        }

        static WavePeriodGateEvaluation hit() {
            return new WavePeriodGateEvaluation(true);
        }
    }
}
