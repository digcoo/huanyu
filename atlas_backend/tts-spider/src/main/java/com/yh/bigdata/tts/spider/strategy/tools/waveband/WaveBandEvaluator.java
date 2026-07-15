package com.yh.bigdata.tts.spider.strategy.tools.waveband;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.WaveBandStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeFilterTools;
import lombok.Getter;

public final class WaveBandEvaluator {

    private WaveBandEvaluator() {
    }

    public static WaveBandEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                              WaveBandStrategyParams params, WaveBandTier tier) {
        WaveBandStrategyParams p = params != null ? params : WaveBandStrategyParams.defaults();
        if (!WaveShapeFilterTools.passFilters(stock, checkResult, p.getMinAvgAmount())) {
            return WaveBandEvaluation.miss();
        }
        if (tier == WaveBandTier.MEDIUM) {
            if (!WaveBandReshapeGateTools.passesPeriodGate(
                    stock, checkResult, PeriodTypeEnum.MONTH, p.getLookbackMonth())) {
                return WaveBandEvaluation.miss();
            }
        } else {
            if (!WaveBandReshapeGateTools.passesPeriodGate(
                    stock, checkResult, PeriodTypeEnum.WEEK, p.getLookbackWeek())) {
                return WaveBandEvaluation.miss();
            }
        }
        if (!WaveBandReshapeGateTools.passesDayYangSignal(stock, checkResult)) {
            return WaveBandEvaluation.miss();
        }
        return new WaveBandEvaluation(true);
    }

    public static WaveBandEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                              WaveBandStrategyParams params) {
        return evaluate(stock, checkResult, params, WaveBandTier.SHORT);
    }

    @Getter
    public static final class WaveBandEvaluation {
        private final boolean hit;

        WaveBandEvaluation(boolean hit) {
            this.hit = hit;
        }

        static WaveBandEvaluation miss() {
            return new WaveBandEvaluation(false);
        }

        public boolean isHit() {
            return hit;
        }
    }
}
