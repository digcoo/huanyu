package com.yh.bigdata.tts.spider.strategy.tools.ultragc;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.UltraGcBreakoutStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class UltraGcBreakoutEvaluator {

    private UltraGcBreakoutEvaluator() {
    }

    public static UltraGcBreakoutEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                     UltraGcBreakoutStrategyParams params) {
        UltraGcBreakoutStrategyParams p = params != null ? params : UltraGcBreakoutStrategyParams.defaults();
        UltraGcBreakoutTools.Hit hit = UltraGcBreakoutTools.findHit(stock, p);
        if (hit == null) {
            return UltraGcBreakoutEvaluation.miss();
        }
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.MIN30, UltraGcBreakoutTools.buildTrendMessage(hit));
            checkResult.addSignal(PeriodTypeEnum.MIN30, UltraGcBreakoutTools.buildSignalMessage(hit));
        }
        return UltraGcBreakoutEvaluation.hit(hit);
    }

    @Getter
    public static final class UltraGcBreakoutEvaluation {
        private final UltraGcBreakoutTools.Hit hit;
        private final boolean hitFlag;

        UltraGcBreakoutEvaluation(UltraGcBreakoutTools.Hit hit, boolean hitFlag) {
            this.hit = hit;
            this.hitFlag = hitFlag;
        }

        public boolean isHit() {
            return hitFlag;
        }

        static UltraGcBreakoutEvaluation miss() {
            return new UltraGcBreakoutEvaluation(null, false);
        }

        static UltraGcBreakoutEvaluation hit(UltraGcBreakoutTools.Hit hit) {
            return new UltraGcBreakoutEvaluation(hit, true);
        }
    }
}
